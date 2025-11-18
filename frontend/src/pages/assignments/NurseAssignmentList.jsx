import { useState, useEffect } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Chip,
  CircularProgress,
} from "@mui/material";
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  PersonAdd as AssignIcon,
} from "@mui/icons-material";
import { toast } from "sonner";
import api from "../../services/api";
import patientService from "../../services/patientService";
import { usePermissions } from "../../hooks/usePermissions";
import { PERMISSIONS } from "../../utils/permissions";

function NurseAssignmentList() {
  const { can } = usePermissions();
  const [assignments, setAssignments] = useState([]);
  const [nurses, setNurses] = useState([]);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedNurseId, setSelectedNurseId] = useState("");
  const [selectedPatient, setSelectedPatient] = useState("");
  const [assignedAs, setAssignedAs] = useState("PRIMARY");

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [nursesData, patientsData] = await Promise.all([
        api
          .get("/users")
          .then((res) => res.data.filter((u) => u.role === "NURSE")),
        patientService.getAllPatients(),
      ]);

      setNurses(nursesData || []);
      setPatients(patientsData || []);

      // Fetch all nurse assignments
      const response = await api.get("/nurse-assignments");
      setAssignments(response.data || []);
    } catch (error) {
      toast.error("Failed to load data");
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = async () => {
    if (!selectedNurseId || !selectedPatient) {
      toast.error("Please select both nurse and patient");
      return;
    }

    try {
      const assignmentData = {
        nurseId: selectedNurseId,
        patientId: selectedPatient,
        assignedAs: assignedAs,
      };
      await api.post("/nurse-assignments", assignmentData);
      toast.success("Nurse assigned successfully");
      setOpenDialog(false);
      setSelectedNurseId("");
      setSelectedPatient("");
      setAssignedAs("PRIMARY");
      fetchData();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to assign nurse");
      console.error("Error assigning nurse:", error);
    }
  };

  const handleRemoveAssignment = async (assignmentId) => {
    if (!window.confirm("Are you sure you want to remove this assignment?")) {
      return;
    }

    try {
      await api.delete(`/nurse-assignments/${assignmentId}`);
      toast.success("Assignment removed successfully");
      fetchData();
    } catch (error) {
      toast.error("Failed to remove assignment");
      console.error("Error removing assignment:", error);
    }
  };

  const getNurseName = (nurseId) => {
    const nurse = nurses.find((n) => n.userId === nurseId);
    return nurse ? `${nurse.firstName} ${nurse.lastName}` : "Unknown";
  };

  const getPatientName = (patientId) => {
    const patient = patients.find((p) => p.id === patientId);
    if (!patient) return "Unknown";
    return (
      patient.name ||
      `${patient.firstName || ""} ${patient.lastName || ""}`.trim() ||
      "Unknown"
    );
  };

  if (loading) {
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
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
        <Typography variant="h4" fontWeight={600}>
          Nurse-Patient Assignments
        </Typography>
        {can(PERMISSIONS.ASSIGN_DOCTOR_TO_PATIENT) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpenDialog(true)}
          >
            Assign Nurse
          </Button>
        )}
      </Box>

      <Card>
        <CardContent>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <strong>Patient</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Nurse</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Type</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Status</strong>
                  </TableCell>
                  {can(PERMISSIONS.REMOVE_DOCTOR_FROM_PATIENT) && (
                    <TableCell align="center">
                      <strong>Actions</strong>
                    </TableCell>
                  )}
                </TableRow>
              </TableHead>
              <TableBody>
                {assignments.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No assignments found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  assignments.map((assignment) => (
                    <TableRow key={assignment.id}>
                      <TableCell>
                        {getPatientName(assignment.patientId)}
                      </TableCell>
                      <TableCell>{getNurseName(assignment.nurseId)}</TableCell>
                      <TableCell>
                        <Chip
                          label={assignment.assignedAs || "PRIMARY"}
                          size="small"
                          color={
                            assignment.assignedAs === "PRIMARY"
                              ? "primary"
                              : "default"
                          }
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={assignment.active ? "Active" : "Inactive"}
                          size="small"
                          color={assignment.active ? "success" : "default"}
                        />
                      </TableCell>
                      {can(PERMISSIONS.REMOVE_DOCTOR_FROM_PATIENT) && (
                        <TableCell align="center">
                          <IconButton
                            color="error"
                            size="small"
                            onClick={() =>
                              handleRemoveAssignment(assignment.id)
                            }
                          >
                            <DeleteIcon />
                          </IconButton>
                        </TableCell>
                      )}
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      <Dialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <AssignIcon />
            Assign Nurse to Patient
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={2}>
            <TextField
              select
              fullWidth
              label="Select Patient"
              value={selectedPatient}
              onChange={(e) => setSelectedPatient(e.target.value)}
            >
              {patients.map((patient) => (
                <MenuItem key={patient.id} value={patient.id}>
                  {patient.name ||
                    `${patient.firstName || ""} ${patient.lastName || ""}`.trim()}{" "}
                  - {patient.email}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              select
              fullWidth
              label="Select Nurse"
              value={selectedNurseId}
              onChange={(e) => setSelectedNurseId(e.target.value)}
            >
              {nurses.map((nurse) => (
                <MenuItem key={nurse.userId} value={nurse.userId}>
                  {nurse.firstName} {nurse.lastName} - {nurse.email}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              select
              fullWidth
              label="Role"
              value={assignedAs}
              onChange={(e) => setAssignedAs(e.target.value)}
            >
              <MenuItem value="PRIMARY">Primary</MenuItem>
              <MenuItem value="CONSULTING">Consulting</MenuItem>
              <MenuItem value="SPECIALIST">Specialist</MenuItem>
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button onClick={handleAssign} variant="contained">
            Assign
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default NurseAssignmentList;
