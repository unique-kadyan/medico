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
  FormControlLabel,
  Checkbox,
  Alert,
} from "@mui/material";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from "dayjs";
import { useSelector } from "react-redux";
import otRequestService from "../../services/otRequestService";
import patientService from "../../services/patientService";

const surgeryTypes = [
  "GENERAL_SURGERY",
  "CARDIOVASCULAR",
  "NEUROSURGERY",
  "ORTHOPEDIC",
  "PLASTIC_SURGERY",
  "UROLOGICAL",
  "GYNECOLOGICAL",
  "OPHTHALMIC",
  "ENT",
  "DENTAL",
  "EMERGENCY",
  "OTHER",
];

function OTRequestForm() {
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [formData, setFormData] = useState({
    patientId: "",
    surgeonId: user?.id || "",
    surgeryType: "",
    surgeryPurpose: "",
    scheduledStartTime: dayjs().add(1, "day"),
    estimatedDuration: "",
    otRoomNumber: "",
    requiredInstruments: "",
    requiredMedications: "",
    anesthesiaType: "",
    surgeryNotes: "",
    isEmergency: false,
  });

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      const data = await patientService.getAllPatients();
      setPatients(data);
    } catch (err) {
      console.error("Failed to fetch patients:", err);
    }
  };

  const handleChange = (e) => {
    const { name, value, checked, type } = e.target;
    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleDateChange = (newValue) => {
    setFormData({ ...formData, scheduledStartTime: newValue });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const submitData = {
        ...formData,
        scheduledStartTime: formData.scheduledStartTime.toISOString(),
        estimatedDuration: parseInt(formData.estimatedDuration),
      };

      await otRequestService.createOTRequest(submitData);
      setSuccess("OT Request created successfully");
      setTimeout(() => {
        navigate("/ot-requests");
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create OT request");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        New OT Request
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
                label="Surgery Type"
                name="surgeryType"
                value={formData.surgeryType}
                onChange={handleChange}
              >
                <MenuItem value="">Select Surgery Type</MenuItem>
                {surgeryTypes.map((type) => (
                  <MenuItem key={type} value={type}>
                    {type.replace(/_/g, " ")}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                multiline
                rows={3}
                label="Surgery Purpose"
                name="surgeryPurpose"
                value={formData.surgeryPurpose}
                onChange={handleChange}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DateTimePicker
                  label="Scheduled Start Time"
                  value={formData.scheduledStartTime}
                  onChange={handleDateChange}
                  slotProps={{
                    textField: {
                      fullWidth: true,
                      required: true,
                    },
                  }}
                  minDateTime={dayjs()}
                />
              </LocalizationProvider>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                required
                fullWidth
                type="number"
                label="Estimated Duration (minutes)"
                name="estimatedDuration"
                value={formData.estimatedDuration}
                onChange={handleChange}
                inputProps={{ min: 15, step: 15 }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="OT Room Number (Optional)"
                name="otRoomNumber"
                value={formData.otRoomNumber}
                onChange={handleChange}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Anesthesia Type"
                name="anesthesiaType"
                value={formData.anesthesiaType}
                onChange={handleChange}
                placeholder="e.g., General, Local, Regional"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Required Instruments"
                name="requiredInstruments"
                value={formData.requiredInstruments}
                onChange={handleChange}
                placeholder="List required surgical instruments"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Required Medications"
                name="requiredMedications"
                value={formData.requiredMedications}
                onChange={handleChange}
                placeholder="List required medications and supplies"
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Additional Notes"
                name="surgeryNotes"
                value={formData.surgeryNotes}
                onChange={handleChange}
              />
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.isEmergency}
                    onChange={handleChange}
                    name="isEmergency"
                    color="error"
                  />
                }
                label="Emergency Surgery"
              />
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate("/ot-requests")}
                  disabled={loading}
                >
                  Cancel
                </Button>
                <Button type="submit" variant="contained" disabled={loading}>
                  {loading ? "Creating..." : "Create OT Request"}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
}

export default OTRequestForm;
