import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Grid,
  MenuItem,
  Alert,
} from "@mui/material";
import patientService from "../../services/patientService";
import emergencyRoomService from "../../services/emergencyRoomService";
import emergencyPatientService from "../../services/emergencyPatientService";
import doctorService from "../../services/doctorService";

const conditionOptions = ["CRITICAL", "SERIOUS", "STABLE", "IMPROVING"];

function EmergencyAdmitForm() {
  const navigate = useNavigate();
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [availableRooms, setAvailableRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [formData, setFormData] = useState({
    patientId: "",
    emergencyRoomId: "",
    attendingDoctorId: "",
    condition: "STABLE",
    triageLevel: 3,
    chiefComplaint: "",
    vitalSigns: "",
    treatmentPlan: "",
    medicationsAdministered: "",
    requiresMonitoring: false,
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [patientsData, doctorsData, roomsData] = await Promise.all([
        patientService.getAllPatients(),
        doctorService.getAllDoctors(),
        emergencyRoomService.getAvailableRooms(),
      ]);
      setPatients(patientsData);
      setDoctors(doctorsData);
      setAvailableRooms(roomsData);
    } catch (err) {
      console.error("Failed to fetch data:", err);
      setError("Failed to load form data");
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const submitData = {
        ...formData,
        triageLevel: parseInt(formData.triageLevel),
        requiresMonitoring:
          formData.condition === "CRITICAL" || formData.triageLevel <= 2,
      };

      await emergencyPatientService.admitPatient(submitData);
      setSuccess("Patient admitted to emergency room successfully");
      setTimeout(() => {
        navigate("/emergency");
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to admit patient");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Admit Patient to Emergency
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      {availableRooms.length === 0 && !loading && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          No available emergency rooms. All rooms are currently occupied.
        </Alert>
      )}

      <Paper sx={{ p: 3, mt: 3 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <TextField
                select
                required
                fullWidth
                label="Patient"
                name="patientId"
                value={formData.patientId}
                onChange={handleChange}
              >
                <MenuItem value="">Select Patient</MenuItem>
                {patients.map((patient) => (
                  <MenuItem key={patient.id} value={patient.id}>
                    {patient.firstName} {patient.lastName} - {patient.patientId}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                select
                required
                fullWidth
                label="Emergency Room"
                name="emergencyRoomId"
                value={formData.emergencyRoomId}
                onChange={handleChange}
                disabled={availableRooms.length === 0}
              >
                <MenuItem value="">Select Room</MenuItem>
                {availableRooms.map((room) => (
                  <MenuItem key={room.id} value={room.id}>
                    {room.roomNumber} - Floor {room.floorNumber} (Capacity:{" "}
                    {room.currentOccupancy}/{room.capacity})
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                select
                fullWidth
                label="Attending Doctor (Optional)"
                name="attendingDoctorId"
                value={formData.attendingDoctorId}
                onChange={handleChange}
              >
                <MenuItem value="">No Doctor Assigned Yet</MenuItem>
                {doctors.map((doctor) => (
                  <MenuItem key={doctor.id} value={doctor.id}>
                    Dr. {doctor.firstName} {doctor.lastName} -{" "}
                    {doctor.specialization}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                select
                required
                fullWidth
                label="Patient Condition"
                name="condition"
                value={formData.condition}
                onChange={handleChange}
              >
                {conditionOptions.map((condition) => (
                  <MenuItem key={condition} value={condition}>
                    {condition}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                select
                required
                fullWidth
                label="Triage Level (1=Most Urgent, 5=Least Urgent)"
                name="triageLevel"
                value={formData.triageLevel}
                onChange={handleChange}
              >
                <MenuItem value={1}>
                  Level 1 - Immediate (Life-threatening)
                </MenuItem>
                <MenuItem value={2}>Level 2 - Emergency (Serious)</MenuItem>
                <MenuItem value={3}>Level 3 - Urgent</MenuItem>
                <MenuItem value={4}>Level 4 - Semi-urgent</MenuItem>
                <MenuItem value={5}>Level 5 - Non-urgent</MenuItem>
              </TextField>
            </Grid>

            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                multiline
                rows={3}
                label="Chief Complaint"
                name="chiefComplaint"
                value={formData.chiefComplaint}
                onChange={handleChange}
                placeholder="Primary reason for emergency visit"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Vital Signs"
                name="vitalSigns"
                value={formData.vitalSigns}
                onChange={handleChange}
                placeholder="BP, Heart Rate, Temperature, Respiratory Rate, O2 Saturation"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Initial Treatment Plan"
                name="treatmentPlan"
                value={formData.treatmentPlan}
                onChange={handleChange}
                placeholder="Immediate treatment plan and interventions"
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Medications Administered"
                name="medicationsAdministered"
                value={formData.medicationsAdministered}
                onChange={handleChange}
                placeholder="List any medications given during admission"
              />
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate("/emergency")}
                  disabled={loading}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={loading || availableRooms.length === 0}
                >
                  {loading ? "Admitting..." : "Admit Patient"}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
}

export default EmergencyAdmitForm;
