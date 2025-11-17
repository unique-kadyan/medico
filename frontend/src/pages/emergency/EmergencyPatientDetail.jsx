import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  Chip,
  Divider,
  Alert,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
} from "@mui/material";
import {
  ArrowBack as BackIcon,
  Edit as EditIcon,
  LocalHospital as DischargeIcon,
} from "@mui/icons-material";
import emergencyPatientService from "../../services/emergencyPatientService";

function EmergencyPatientDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [openUpdateDialog, setOpenUpdateDialog] = useState(false);
  const [openDischargeDialog, setOpenDischargeDialog] = useState(false);
  const [updating, setUpdating] = useState(false);
  const [discharging, setDischarging] = useState(false);
  const [updateData, setUpdateData] = useState({
    condition: "",
    triageLevel: "",
  });
  const [dischargeSummary, setDischargeSummary] = useState("");

  const conditionColors = {
    CRITICAL: "error",
    SERIOUS: "warning",
    STABLE: "success",
    IMPROVING: "info",
    DISCHARGED: "default",
  };

  const conditionOptions = ["CRITICAL", "SERIOUS", "STABLE", "IMPROVING"];
  const triageLevels = [1, 2, 3, 4, 5];

  const fetchPatientDetails = useCallback(async () => {
    try {
      setLoading(true);
      const data = await emergencyPatientService.getEmergencyPatientById(id);
      setPatient(data);
      setUpdateData({
        condition: data.condition,
        triageLevel: data.triageLevel,
      });
      setError("");
    } catch (err) {
      setError("Failed to fetch patient details");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPatientDetails();
  }, [fetchPatientDetails]);

  const handleUpdate = async () => {
    try {
      setUpdating(true);
      const updatedPatient = { ...patient, ...updateData };
      await emergencyPatientService.updateEmergencyPatient(id, updatedPatient);
      setOpenUpdateDialog(false);
      fetchPatientDetails();
      setError("");
    } catch (err) {
      setError("Failed to update patient");
      console.error(err);
    } finally {
      setUpdating(false);
    }
  };

  const handleDischarge = async () => {
    try {
      setDischarging(true);
      await emergencyPatientService.dischargePatient(id, dischargeSummary);
      navigate("/emergency");
    } catch (err) {
      setError("Failed to discharge patient");
      console.error(err);
    } finally {
      setDischarging(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography>Loading patient details...</Typography>
      </Box>
    );
  }

  if (!patient) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Patient not found</Alert>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/emergency")}
          sx={{ mt: 2 }}
        >
          Back to Emergency Dashboard
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 3,
        }}
      >
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Button
            startIcon={<BackIcon />}
            onClick={() => navigate("/emergency")}
          >
            Back
          </Button>
          <Typography variant="h4" fontWeight={600}>
            Emergency Patient Details
          </Typography>
        </Box>
        <Box sx={{ display: "flex", gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<EditIcon />}
            onClick={() => setOpenUpdateDialog(true)}
            disabled={patient.condition === "DISCHARGED"}
          >
            Update Status
          </Button>
          <Button
            variant="contained"
            color="success"
            startIcon={<DischargeIcon />}
            onClick={() => setOpenDischargeDialog(true)}
            disabled={patient.condition === "DISCHARGED"}
          >
            Discharge Patient
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Patient Information
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <TableContainer>
              <Table>
                <TableBody>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 600 }}>
                      Patient Name
                    </TableCell>
                    <TableCell>{patient.patientName}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 600 }}>
                      Age
                    </TableCell>
                    <TableCell>{patient.age} years</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 600 }}>
                      Gender
                    </TableCell>
                    <TableCell>{patient.gender}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 600 }}>
                      Phone Number
                    </TableCell>
                    <TableCell>{patient.phoneNumber || "N/A"}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 600 }}>
                      Emergency Contact
                    </TableCell>
                    <TableCell>
                      {patient.emergencyContactName || "N/A"}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 600 }}>
                      Emergency Contact Phone
                    </TableCell>
                    <TableCell>
                      {patient.emergencyContactPhone || "N/A"}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Medical Status
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Card
                  variant="outlined"
                  sx={{ bgcolor: "error.light", color: "error.contrastText" }}
                >
                  <CardContent>
                    <Typography variant="body2">Triage Level</Typography>
                    <Typography variant="h3" fontWeight={600}>
                      {patient.triageLevel}
                    </Typography>
                    <Typography variant="body2">
                      {patient.triageLevel <= 2
                        ? "Immediate Attention Required"
                        : patient.triageLevel === 3
                          ? "Urgent"
                          : "Non-Urgent"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Typography color="text.secondary" variant="body2">
                  Condition
                </Typography>
                <Chip
                  label={patient.condition}
                  color={conditionColors[patient.condition]}
                  sx={{ mt: 0.5, fontSize: "1rem", py: 2.5 }}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography color="text.secondary" variant="body2">
                  Assigned Room
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {patient.emergencyRoomNumber || "Not Assigned"}
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <Typography color="text.secondary" variant="body2">
                  Admission Time
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {new Date(patient.admissionTime).toLocaleString()}
                </Typography>
              </Grid>

              {patient.dischargeTime && (
                <Grid item xs={12}>
                  <Typography color="text.secondary" variant="body2">
                    Discharge Time
                  </Typography>
                  <Typography variant="body1" fontWeight={500}>
                    {new Date(patient.dischargeTime).toLocaleString()}
                  </Typography>
                </Grid>
              )}
            </Grid>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Chief Complaint
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Typography variant="body1">{patient.chiefComplaint}</Typography>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Vital Signs
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="text.secondary" variant="body2">
                      Blood Pressure
                    </Typography>
                    <Typography variant="h6" fontWeight={600}>
                      {patient.bloodPressure || "N/A"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="text.secondary" variant="body2">
                      Heart Rate
                    </Typography>
                    <Typography variant="h6" fontWeight={600}>
                      {patient.heartRate ? `${patient.heartRate} bpm` : "N/A"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="text.secondary" variant="body2">
                      Temperature
                    </Typography>
                    <Typography variant="h6" fontWeight={600}>
                      {patient.temperature ? `${patient.temperature}°F` : "N/A"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="text.secondary" variant="body2">
                      Oxygen Saturation
                    </Typography>
                    <Typography variant="h6" fontWeight={600}>
                      {patient.oxygenSaturation
                        ? `${patient.oxygenSaturation}%`
                        : "N/A"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {patient.initialAssessment && (
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Initial Assessment
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body1">
                {patient.initialAssessment}
              </Typography>
            </Paper>
          </Grid>
        )}

        {patient.treatmentPlan && (
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Treatment Plan
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body1">{patient.treatmentPlan}</Typography>
            </Paper>
          </Grid>
        )}

        {patient.dischargeSummary && (
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Discharge Summary
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body1">
                {patient.dischargeSummary}
              </Typography>
            </Paper>
          </Grid>
        )}
      </Grid>

      <Dialog
        open={openUpdateDialog}
        onClose={() => setOpenUpdateDialog(false)}
      >
        <DialogTitle>Update Patient Status</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Condition</InputLabel>
                <Select
                  value={updateData.condition}
                  label="Condition"
                  onChange={(e) =>
                    setUpdateData({ ...updateData, condition: e.target.value })
                  }
                >
                  {conditionOptions.map((condition) => (
                    <MenuItem key={condition} value={condition}>
                      {condition}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Triage Level</InputLabel>
                <Select
                  value={updateData.triageLevel}
                  label="Triage Level"
                  onChange={(e) =>
                    setUpdateData({
                      ...updateData,
                      triageLevel: e.target.value,
                    })
                  }
                >
                  {triageLevels.map((level) => (
                    <MenuItem key={level} value={level}>
                      Level {level}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenUpdateDialog(false)}>Cancel</Button>
          <Button
            onClick={handleUpdate}
            variant="contained"
            disabled={updating}
          >
            {updating ? "Updating..." : "Update"}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={openDischargeDialog}
        onClose={() => setOpenDischargeDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Discharge Patient</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            multiline
            rows={4}
            label="Discharge Summary"
            value={dischargeSummary}
            onChange={(e) => setDischargeSummary(e.target.value)}
            sx={{ mt: 2 }}
            placeholder="Enter discharge summary, instructions, and follow-up care..."
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDischargeDialog(false)}>Cancel</Button>
          <Button
            onClick={handleDischarge}
            variant="contained"
            color="success"
            disabled={discharging || !dischargeSummary.trim()}
          >
            {discharging ? "Discharging..." : "Discharge Patient"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default EmergencyPatientDetail;
