import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  MenuItem,
  Grid,
  CircularProgress,
} from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import appointmentService from "../../services/appointmentService";
import patientService from "../../services/patientService";
import doctorService from "../../services/doctorService";

const APPOINTMENT_STATUSES = [
  { value: "SCHEDULED", label: "Scheduled" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "COMPLETED", label: "Completed" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "NO_SHOW", label: "No Show" },
];

function AppointmentForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(isEditMode);
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [formData, setFormData] = useState({
    patientId: "",
    doctorId: "",
    appointmentDateTime: new Date(),
    status: "SCHEDULED",
    reasonForVisit: "",
    symptoms: "",
    diagnosis: "",
    notes: "",
    duration: 30,
  });

  const fetchPatientsAndDoctors = useCallback(async () => {
    try {
      const [patientsData, doctorsData] = await Promise.all([
        patientService.getAllPatients(),
        doctorService.getAllDoctors(),
      ]);
      setPatients(patientsData);
      setDoctors(doctorsData);
    } catch (error) {
      toast.error("Failed to load patients and doctors");
      console.error("Error fetching data:", error);
    }
  }, []);

  const fetchAppointment = useCallback(async () => {
    try {
      setInitialLoading(true);
      const data = await appointmentService.getAppointmentById(id);
      setFormData({
        patientId: data.patientId || "",
        doctorId: data.doctorId || "",
        appointmentDateTime: data.appointmentDateTime
          ? new Date(data.appointmentDateTime)
          : new Date(),
        status: data.status || "SCHEDULED",
        reasonForVisit: data.reasonForVisit || "",
        symptoms: data.symptoms || "",
        diagnosis: data.diagnosis || "",
        notes: data.notes || "",
        duration: data.duration || 30,
      });
    } catch (error) {
      toast.error("Failed to fetch appointment details");
      console.error("Error fetching appointment:", error);
    } finally {
      setInitialLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPatientsAndDoctors();
    if (isEditMode) {
      fetchAppointment();
    }
  }, [isEditMode, fetchPatientsAndDoctors, fetchAppointment]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleDateChange = (newDate) => {
    setFormData((prev) => ({
      ...prev,
      appointmentDateTime: newDate,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validation
    if (!formData.patientId) {
      toast.error("Please select a patient");
      return;
    }
    if (!formData.doctorId) {
      toast.error("Please select a doctor");
      return;
    }
    if (!formData.appointmentDateTime) {
      toast.error("Please select appointment date and time");
      return;
    }

    try {
      setLoading(true);

      // Format the data for the backend
      const appointmentData = {
        ...formData,
        appointmentDateTime: formData.appointmentDateTime.toISOString(),
      };

      if (isEditMode) {
        await appointmentService.updateAppointment(id, appointmentData);
        toast.success("Appointment updated successfully");
      } else {
        await appointmentService.createAppointment(appointmentData);
        toast.success("Appointment created successfully");
      }
      navigate("/appointments");
    } catch (error) {
      toast.error(
        isEditMode
          ? "Failed to update appointment"
          : "Failed to create appointment"
      );
      console.error("Error saving appointment:", error);
    } finally {
      setLoading(false);
    }
  };

  if (initialLoading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="400px"
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          {isEditMode ? "Edit Appointment" : "Book New Appointment"}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {isEditMode
            ? "Update appointment details"
            : "Schedule a new appointment for a patient"}
        </Typography>
      </Box>

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  select
                  fullWidth
                  label="Patient"
                  name="patientId"
                  value={formData.patientId}
                  onChange={handleChange}
                  required
                  disabled={loading}
                >
                  <MenuItem value="">
                    <em>Select Patient</em>
                  </MenuItem>
                  {patients.map((patient) => (
                    <MenuItem key={patient.id} value={patient.id}>
                      {patient.firstName} {patient.lastName} - {patient.email}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  select
                  fullWidth
                  label="Doctor"
                  name="doctorId"
                  value={formData.doctorId}
                  onChange={handleChange}
                  required
                  disabled={loading}
                >
                  <MenuItem value="">
                    <em>Select Doctor</em>
                  </MenuItem>
                  {doctors.map((doctor) => (
                    <MenuItem key={doctor.id} value={doctor.id}>
                      Dr. {doctor.firstName} {doctor.lastName} -{" "}
                      {doctor.specialization}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} md={6}>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DateTimePicker
                    label="Appointment Date & Time"
                    value={formData.appointmentDateTime}
                    onChange={handleDateChange}
                    disabled={loading}
                    slotProps={{
                      textField: {
                        fullWidth: true,
                        required: true,
                      },
                    }}
                  />
                </LocalizationProvider>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  select
                  fullWidth
                  label="Status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  disabled={loading}
                >
                  {APPOINTMENT_STATUSES.map((status) => (
                    <MenuItem key={status.value} value={status.value}>
                      {status.label}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Duration (minutes)"
                  name="duration"
                  type="number"
                  value={formData.duration}
                  onChange={handleChange}
                  disabled={loading}
                  inputProps={{ min: 15, max: 180, step: 15 }}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Reason for Visit"
                  name="reasonForVisit"
                  value={formData.reasonForVisit}
                  onChange={handleChange}
                  disabled={loading}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  label="Symptoms"
                  name="symptoms"
                  value={formData.symptoms}
                  onChange={handleChange}
                  disabled={loading}
                  placeholder="Describe the patient's symptoms..."
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  label="Diagnosis"
                  name="diagnosis"
                  value={formData.diagnosis}
                  onChange={handleChange}
                  disabled={loading}
                  placeholder="Enter diagnosis (if available)..."
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  label="Notes"
                  name="notes"
                  value={formData.notes}
                  onChange={handleChange}
                  disabled={loading}
                  placeholder="Additional notes or comments..."
                />
              </Grid>

              <Grid item xs={12}>
                <Box display="flex" gap={2} justifyContent="flex-end">
                  <Button
                    variant="outlined"
                    onClick={() => navigate("/appointments")}
                    disabled={loading}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" variant="contained" disabled={loading}>
                    {loading ? (
                      <CircularProgress size={24} />
                    ) : isEditMode ? (
                      "Update Appointment"
                    ) : (
                      "Book Appointment"
                    )}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}

export default AppointmentForm;
