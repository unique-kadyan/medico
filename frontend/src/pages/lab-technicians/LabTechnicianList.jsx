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
  Chip,
  IconButton,
  CircularProgress,
} from "@mui/material";
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import api from "../../services/api";
import { usePermissions } from "../../hooks/usePermissions";
import { PERMISSIONS } from "../../utils/permissions";

function LabTechnicianList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [labTechnicians, setLabTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLabTechnicians();
  }, []);

  const fetchLabTechnicians = async () => {
    try {
      setLoading(true);
      // Fetch all users and filter by LAB_TECHNICIAN role
      const response = await api.get("/users");
      const labTechnicianUsers = response.data.filter(
        (user) => user.role === "LAB_TECHNICIAN"
      );
      setLabTechnicians(labTechnicianUsers);
    } catch (error) {
      toast.error("Failed to load lab technicians");
      console.error("Error fetching lab technicians:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (
      !window.confirm("Are you sure you want to delete this lab technician?")
    ) {
      return;
    }

    try {
      await api.delete(`/users/${id}`);
      toast.success("Lab technician deleted successfully");
      fetchLabTechnicians();
    } catch (error) {
      toast.error("Failed to delete lab technician");
      console.error("Error deleting lab technician:", error);
    }
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
          Lab Technicians
        </Typography>
        {can(PERMISSIONS.ADD_LAB_TECHNICIAN) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/lab-technicians/new")}
          >
            Add Lab Technician
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
                    <strong>ID</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Name</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Email</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Phone</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Status</strong>
                  </TableCell>
                  {(can(PERMISSIONS.EDIT_LAB_TECHNICIAN) ||
                    can(PERMISSIONS.DELETE_LAB_TECHNICIAN)) && (
                    <TableCell align="center">
                      <strong>Actions</strong>
                    </TableCell>
                  )}
                </TableRow>
              </TableHead>
              <TableBody>
                {labTechnicians.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No lab technicians found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  labTechnicians.map((labTechnician) => (
                    <TableRow key={labTechnician.userId}>
                      <TableCell>#{labTechnician.userId}</TableCell>
                      <TableCell>
                        {labTechnician.firstName} {labTechnician.lastName}
                      </TableCell>
                      <TableCell>{labTechnician.email}</TableCell>
                      <TableCell>{labTechnician.phone || "-"}</TableCell>
                      <TableCell>
                        <Chip
                          label={labTechnician.enabled ? "Active" : "Inactive"}
                          size="small"
                          color={labTechnician.enabled ? "success" : "default"}
                        />
                      </TableCell>
                      {(can(PERMISSIONS.EDIT_LAB_TECHNICIAN) ||
                        can(PERMISSIONS.DELETE_LAB_TECHNICIAN)) && (
                        <TableCell align="center">
                          <Box display="flex" gap={0.5} justifyContent="center">
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() =>
                                navigate(
                                  `/lab-technicians/${labTechnician.userId}`
                                )
                              }
                            >
                              <ViewIcon />
                            </IconButton>
                            {can(PERMISSIONS.EDIT_LAB_TECHNICIAN) && (
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() =>
                                  navigate(
                                    `/lab-technicians/edit/${labTechnician.userId}`
                                  )
                                }
                              >
                                <EditIcon />
                              </IconButton>
                            )}
                            {can(PERMISSIONS.DELETE_LAB_TECHNICIAN) && (
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() =>
                                  handleDelete(labTechnician.userId)
                                }
                              >
                                <DeleteIcon />
                              </IconButton>
                            )}
                          </Box>
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
    </Box>
  );
}

export default LabTechnicianList;
