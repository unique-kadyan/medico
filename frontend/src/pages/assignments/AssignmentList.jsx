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
import assignmentService from "../../services/assignmentService";
import doctorService from "../../services/doctorService";
import patientService from "../../services/patientService";
import { usePermissions } from "../../hooks/usePermissions";
import { PERMISSIONS } from "../../utils/permissions";

function AssignmentList() {
  const { can } = usePermissions();
  const [assignments, setAssignments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [nurses, setNurses] = useState([]);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [assignmentType, setAssignmentType] = useState("DOCTOR"); // 'DOCTOR' or 'NURSE'
  const [selectedStaffId, setSelectedStaffId] = useState("");
  const [selectedPatient, setSelectedPatient] = useState("");
  const [assignedAs, setAssignedAs] = useState("PRIMARY");

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [doctorsData, nursesData, patientsData] = await Promise.all([
        doctorService.getAllDoctors(),
        api
          .get("/users")
          .then((res) => res.data.filter((u) => u.role === "NURSE")),
        patientService.getAllPatients(),
      ]);

      console.log('Fetched data:', {
        doctors: doctorsData?.length || 0,
        nurses: nursesData?.length || 0,
        patients: patientsData?.length || 0
      });

      setDoctors(doctorsData || []);
      setNurses(nursesData || []);
      setPatients(patientsData || []);

      const allAssignments = [];
      if (patientsData && patientsData.length > 0) {
        for (const patient of patientsData) {
          try {
            const patientAssignments =
              await assignmentService.getPatientAssignments(patient.id);
            if (patientAssignments) {
              allAssignments.push(...patientAssignments);
            }
          } catch (assignError) {
            console.error(`Error fetching assignments for patient ${patient.id}:`, assignError);
          }
        }
      }
      setAssignments(allAssignments);
    } catch (error) {
      toast.error("Failed to load data");
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = async () => {
    if (!selectedStaffId || !selectedPatient) {
      toast.error(
        `Please select both ${assignmentType.toLowerCase()} and patient`
      );
      return;
    }

    try {
      if (assignmentType === "DOCTOR") {
        const assignmentData = {
          doctorId: selectedStaffId,
          patientId: selectedPatient,
          assignedAs: assignedAs,
        };
        await assignmentService.assignDoctorToPatient(assignmentData);
        toast.success("Doctor assigned successfully");
      } else {
        const assignmentData = {
          nurseId: selectedStaffId,
          patientId: selectedPatient,
          assignedAs: assignedAs,
        };
        await api.post("/nurse-assignments", assignmentData);
        toast.success("Nurse assigned successfully");
      }
      setOpenDialog(false);
      setSelectedStaffId("");
      setSelectedPatient("");
      setAssignedAs("PRIMARY");
      setAssignmentType("DOCTOR");
      fetchData();
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          `Failed to assign ${assignmentType.toLowerCase()}`
      );
      console.error(`Error assigning ${assignmentType.toLowerCase()}:`, error);
    }
  };

  const handleRemoveAssignment = async (assignmentId) => {
    if (!window.confirm("Are you sure you want to remove this assignment?")) {
      return;
    }

    try {
      await assignmentService.removeAssignment(assignmentId);
      toast.success("Assignment removed successfully");
      fetchData();
    } catch (error) {
      toast.error("Failed to remove assignment");
      console.error("Error removing assignment:", error);
    }
  };

  const getDoctorName = (doctorId) => {
    const doctor = doctors.find((d) => d.id === doctorId);
    return doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : "Unknown";
  };

  const getPatientName = (patientId) => {
    const patient = patients.find((p) => p.id === patientId);
    if (!patient) return "Unknown";
    return patient.name || `${patient.firstName || ''} ${patient.lastName || ''}`.trim() || "Unknown";
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
          Doctor-Patient Assignments
        </Typography>
        {can(PERMISSIONS.ASSIGN_DOCTOR_TO_PATIENT) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpenDialog(true)}
          >
            Assign Doctor / Nurse
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
                    <strong>Doctor</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Type</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Assigned Date</strong>
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
                      <TableCell>
                        {getDoctorName(assignment.doctorId)}
                      </TableCell>
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
                        {assignment.assignedDate
                          ? new Date(
                              assignment.assignedDate
                            ).toLocaleDateString()
                          : "-"}
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
            Assign Staff to Patient
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={2}>
            <TextField
              select
              fullWidth
              label="Assignment Type"
              value={assignmentType}
              onChange={(e) => {
                setAssignmentType(e.target.value);
                setSelectedStaffId(""); // Reset selection when type changes
              }}
            >
              <MenuItem value="DOCTOR">Doctor</MenuItem>
              <MenuItem value="NURSE">Nurse</MenuItem>
            </TextField>

            <TextField
              select
              fullWidth
              label="Select Patient"
              value={selectedPatient}
              onChange={(e) => setSelectedPatient(e.target.value)}
            >
              {patients.map((patient) => (
                <MenuItem key={patient.id} value={patient.id}>
                  {patient.name || `${patient.firstName || ''} ${patient.lastName || ''}`.trim()} - {patient.email}
                </MenuItem>
              ))}
            </TextField>

            {assignmentType === "DOCTOR" ? (
              <TextField
                select
                fullWidth
                label="Select Doctor"
                value={selectedStaffId}
                onChange={(e) => setSelectedStaffId(e.target.value)}
              >
                {doctors.map((doctor) => (
                  <MenuItem key={doctor.id} value={doctor.id}>
                    Dr. {doctor.firstName} {doctor.lastName} -{" "}
                    {doctor.specialization}
                  </MenuItem>
                ))}
              </TextField>
            ) : (
              <TextField
                select
                fullWidth
                label="Select Nurse"
                value={selectedStaffId}
                onChange={(e) => setSelectedStaffId(e.target.value)}
              >
                {nurses.map((nurse) => (
                  <MenuItem key={nurse.userId} value={nurse.userId}>
                    {nurse.firstName} {nurse.lastName} - {nurse.email}
                  </MenuItem>
                ))}
              </TextField>
            )}

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

export default AssignmentList;
