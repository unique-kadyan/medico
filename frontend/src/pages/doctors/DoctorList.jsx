import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
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
  Chip,
  IconButton,
  CircularProgress,
  TextField,
  InputAdornment,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";
import {
  Add as AddIcon,
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Check as CheckIcon,
  Close as CloseIcon,
} from "@mui/icons-material";
import { toast } from "sonner";
import doctorService from "../../services/doctorService";
import { usePermissions } from "../../hooks/usePermissions";
import { PERMISSIONS } from "../../utils/permissions";

function DoctorList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [doctors, setDoctors] = useState([]);
  const [filteredDoctors, setFilteredDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);

  const fetchDoctors = useCallback(async () => {
    try {
      setLoading(true);
      const data = await doctorService.getAllDoctors();
      setDoctors(data);
      setFilteredDoctors(data);
    } catch (error) {
      toast.error("Failed to fetch doctors");
      console.error("Error fetching doctors:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  const filterDoctors = useCallback(() => {
    if (!searchQuery.trim()) {
      setFilteredDoctors(doctors);
      return;
    }

    const query = searchQuery.toLowerCase();
    const filtered = doctors.filter(
      (doctor) =>
        doctor.firstName?.toLowerCase().includes(query) ||
        doctor.lastName?.toLowerCase().includes(query) ||
        doctor.fullName?.toLowerCase().includes(query) ||
        doctor.doctorId?.toLowerCase().includes(query) ||
        doctor.specialization?.toLowerCase().includes(query) ||
        doctor.email?.toLowerCase().includes(query) ||
        doctor.phone?.includes(query)
    );
    setFilteredDoctors(filtered);
  }, [searchQuery, doctors]);

  useEffect(() => {
    fetchDoctors();
  }, [fetchDoctors]);

  useEffect(() => {
    filterDoctors();
  }, [filterDoctors]);

  const handleAddDoctor = () => {
    navigate("/doctors/new");
  };

  const handleEditDoctor = (id) => {
    navigate(`/doctors/edit/${id}`);
  };

  const handleDeleteClick = (doctor) => {
    setSelectedDoctor(doctor);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      await doctorService.deleteDoctor(selectedDoctor.id);
      toast.success("Doctor deleted successfully");
      setDeleteDialogOpen(false);
      setSelectedDoctor(null);
      fetchDoctors();
    } catch (error) {
      toast.error("Failed to delete doctor");
      console.error("Error deleting doctor:", error);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteDialogOpen(false);
    setSelectedDoctor(null);
  };

  const toggleAvailability = async (doctor) => {
    try {
      await doctorService.updateAvailability(
        doctor.id,
        !doctor.availableForConsultation
      );
      toast.success("Availability updated successfully");
      fetchDoctors();
    } catch (error) {
      toast.error("Failed to update availability");
      console.error("Error updating availability:", error);
    }
  };

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
        <Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Doctors
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage medical staff directory
          </Typography>
        </Box>
        {can(PERMISSIONS.ADD_DOCTOR) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddDoctor}
          >
            Add Doctor
          </Button>
        )}
      </Box>

      <Card>
        <CardContent>
          <TextField
            fullWidth
            placeholder="Search by name, ID, specialization, email, or phone..."
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
                  <TableCell>Doctor ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Specialization</TableCell>
                  <TableCell>Experience</TableCell>
                  <TableCell>Phone</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Availability</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredDoctors.length > 0 ? (
                  filteredDoctors.map((doctor) => (
                    <TableRow key={doctor.id} hover>
                      <TableCell>{doctor.doctorId}</TableCell>
                      <TableCell>
                        <Typography fontWeight={600}>
                          {doctor.fullName ||
                            `Dr. ${doctor.firstName} ${doctor.lastName}`}
                        </Typography>
                        {doctor.qualification && (
                          <Typography variant="caption" color="text.secondary">
                            {doctor.qualification}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={doctor.specialization}
                          size="small"
                          color="primary"
                        />
                      </TableCell>
                      <TableCell>{doctor.yearsOfExperience} years</TableCell>
                      <TableCell>{doctor.phone || "-"}</TableCell>
                      <TableCell>{doctor.email || "-"}</TableCell>
                      <TableCell>
                        <Chip
                          label={
                            doctor.availableForConsultation
                              ? "Available"
                              : "Unavailable"
                          }
                          size="small"
                          color={
                            doctor.availableForConsultation
                              ? "success"
                              : "default"
                          }
                          onClick={() => toggleAvailability(doctor)}
                          sx={{ cursor: "pointer" }}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={doctor.active ? "Active" : "Inactive"}
                          size="small"
                          color={doctor.active ? "success" : "default"}
                        />
                      </TableCell>
                      <TableCell align="right">
                        {can(PERMISSIONS.EDIT_DOCTOR) && (
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => handleEditDoctor(doctor.id)}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        )}
                        {can(PERMISSIONS.DELETE_DOCTOR) && (
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleDeleteClick(doctor)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={9} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        {searchQuery
                          ? "No doctors found matching your search"
                          : 'No doctors found. Click "Add Doctor" to create one.'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
        <DialogTitle>Delete Doctor</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete Dr. {selectedDoctor?.firstName}{" "}
            {selectedDoctor?.lastName}? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDeleteCancel} startIcon={<CloseIcon />}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteConfirm}
            color="error"
            variant="contained"
            startIcon={<CheckIcon />}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default DoctorList;
