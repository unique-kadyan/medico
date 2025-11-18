import { useState, useEffect } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Chip,
  InputAdornment,
  CircularProgress,
} from "@mui/material";
import {
  Add as AddIcon,
  Search as SearchIcon,
  Edit as EditIcon,
  Visibility as ViewIcon,
  Delete as DeleteIcon,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import patientService from "../../services/patientService";
import BloodGroupIcon from "../../components/BloodGroupIcon";
import { usePermissions } from "../../hooks/usePermissions";
import { PERMISSIONS } from "../../utils/permissions";

function PatientList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      const data = await patientService.getAllPatients();
      setPatients(data);
    } catch (error) {
      toast.error("Failed to fetch patients");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to delete this patient?")) {
      try {
        await patientService.deletePatient(id);
        toast.success("Patient deleted successfully");
        fetchPatients();
      } catch {
        toast.error("Failed to delete patient");
      }
    }
  };

  const filteredPatients = patients.filter(
    (patient) =>
      patient.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      patient.phone?.includes(searchQuery) ||
      patient.email?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
        <CircularProgress />
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
        <div>
          <Typography variant="h4" fontWeight={700}>
            Patients
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage patient records
          </Typography>
        </div>
        {can(PERMISSIONS.ADD_PATIENT) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/patients/new")}
          >
            Add Patient
          </Button>
        )}
      </Box>

      <Card>
        <CardContent>
          <TextField
            fullWidth
            placeholder="Search patients by name, phone, or email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ mb: 3 }}
          />

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Age</TableCell>
                  <TableCell>Gender</TableCell>
                  <TableCell>Phone</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Blood Group</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredPatients.length > 0 ? (
                  filteredPatients.map((patient) => (
                    <TableRow key={patient.id} hover>
                      <TableCell>{patient.id}</TableCell>
                      <TableCell>
                        <Typography fontWeight={600}>{patient.name}</Typography>
                      </TableCell>
                      <TableCell>{patient.age}</TableCell>
                      <TableCell>
                        <Chip
                          label={patient.gender}
                          size="small"
                          color={
                            patient.gender === "MALE" ? "primary" : "secondary"
                          }
                        />
                      </TableCell>
                      <TableCell>{patient.phone}</TableCell>
                      <TableCell>{patient.email}</TableCell>
                      <TableCell>
                        <BloodGroupIcon
                          bloodGroup={patient.bloodGroup}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        {can(PERMISSIONS.VIEW_PATIENT_DETAILS) && (
                          <IconButton
                            size="small"
                            onClick={() => navigate(`/patients/${patient.id}`)}
                            color="primary"
                          >
                            <ViewIcon />
                          </IconButton>
                        )}
                        {can(PERMISSIONS.EDIT_PATIENT) && (
                          <IconButton
                            size="small"
                            onClick={() =>
                              navigate(`/patients/${patient.id}/edit`)
                            }
                            color="info"
                          >
                            <EditIcon />
                          </IconButton>
                        )}
                        {can(PERMISSIONS.DELETE_PATIENT) && (
                          <IconButton
                            size="small"
                            onClick={() => handleDelete(patient.id)}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={8} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No patients found
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  );
}

export default PatientList;
