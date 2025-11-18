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

function ReceptionistList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [receptionists, setReceptionists] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReceptionists();
  }, []);

  const fetchReceptionists = async () => {
    try {
      setLoading(true);
      const response = await api.get("/users");
      const receptionistUsers = response.data.filter(
        (user) => user.role === "RECEPTIONIST"
      );
      setReceptionists(receptionistUsers);
    } catch (error) {
      toast.error("Failed to load receptionists");
      console.error("Error fetching receptionists:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this receptionist?")) {
      return;
    }

    try {
      await api.delete(`/users/${id}`);
      toast.success("Receptionist deleted successfully");
      fetchReceptionists();
    } catch (error) {
      toast.error("Failed to delete receptionist");
      console.error("Error deleting receptionist:", error);
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
          Receptionists
        </Typography>
        {can(PERMISSIONS.ADD_RECEPTIONIST) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/receptionists/new")}
          >
            Add Receptionist
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
                  {(can(PERMISSIONS.EDIT_RECEPTIONIST) ||
                    can(PERMISSIONS.DELETE_RECEPTIONIST)) && (
                    <TableCell align="center">
                      <strong>Actions</strong>
                    </TableCell>
                  )}
                </TableRow>
              </TableHead>
              <TableBody>
                {receptionists.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No receptionists found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  receptionists.map((receptionist) => (
                    <TableRow key={receptionist.userId}>
                      <TableCell>#{receptionist.userId}</TableCell>
                      <TableCell>
                        {receptionist.firstName} {receptionist.lastName}
                      </TableCell>
                      <TableCell>{receptionist.email}</TableCell>
                      <TableCell>{receptionist.phone || "-"}</TableCell>
                      <TableCell>
                        <Chip
                          label={receptionist.enabled ? "Active" : "Inactive"}
                          size="small"
                          color={receptionist.enabled ? "success" : "default"}
                        />
                      </TableCell>
                      {(can(PERMISSIONS.EDIT_RECEPTIONIST) ||
                        can(PERMISSIONS.DELETE_RECEPTIONIST)) && (
                        <TableCell align="center">
                          <Box display="flex" gap={0.5} justifyContent="center">
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() =>
                                navigate(
                                  `/receptionists/${receptionist.userId}`
                                )
                              }
                            >
                              <ViewIcon />
                            </IconButton>
                            {can(PERMISSIONS.EDIT_RECEPTIONIST) && (
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() =>
                                  navigate(
                                    `/receptionists/edit/${receptionist.userId}`
                                  )
                                }
                              >
                                <EditIcon />
                              </IconButton>
                            )}
                            {can(PERMISSIONS.DELETE_RECEPTIONIST) && (
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() =>
                                  handleDelete(receptionist.userId)
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

export default ReceptionistList;
