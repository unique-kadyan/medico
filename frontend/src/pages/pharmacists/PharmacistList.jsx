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

function PharmacistList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [pharmacists, setPharmacists] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPharmacists();
  }, []);

  const fetchPharmacists = async () => {
    try {
      setLoading(true);
      // Fetch all users and filter by PHARMACIST role
      const response = await api.get("/users");
      const pharmacistUsers = response.data.filter(
        (user) => user.role === "PHARMACIST"
      );
      setPharmacists(pharmacistUsers);
    } catch (error) {
      toast.error("Failed to load pharmacists");
      console.error("Error fetching pharmacists:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this pharmacist?")) {
      return;
    }

    try {
      await api.delete(`/users/${id}`);
      toast.success("Pharmacist deleted successfully");
      fetchPharmacists();
    } catch (error) {
      toast.error("Failed to delete pharmacist");
      console.error("Error deleting pharmacist:", error);
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
          Pharmacists
        </Typography>
        {can(PERMISSIONS.ADD_PHARMACIST) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/pharmacists/new")}
          >
            Add Pharmacist
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
                  {(can(PERMISSIONS.EDIT_PHARMACIST) ||
                    can(PERMISSIONS.DELETE_PHARMACIST)) && (
                    <TableCell align="center">
                      <strong>Actions</strong>
                    </TableCell>
                  )}
                </TableRow>
              </TableHead>
              <TableBody>
                {pharmacists.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No pharmacists found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  pharmacists.map((pharmacist) => (
                    <TableRow key={pharmacist.userId}>
                      <TableCell>#{pharmacist.userId}</TableCell>
                      <TableCell>
                        {pharmacist.firstName} {pharmacist.lastName}
                      </TableCell>
                      <TableCell>{pharmacist.email}</TableCell>
                      <TableCell>{pharmacist.phone || "-"}</TableCell>
                      <TableCell>
                        <Chip
                          label={pharmacist.enabled ? "Active" : "Inactive"}
                          size="small"
                          color={pharmacist.enabled ? "success" : "default"}
                        />
                      </TableCell>
                      {(can(PERMISSIONS.EDIT_PHARMACIST) ||
                        can(PERMISSIONS.DELETE_PHARMACIST)) && (
                        <TableCell align="center">
                          <Box display="flex" gap={0.5} justifyContent="center">
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() =>
                                navigate(`/pharmacists/${pharmacist.userId}`)
                              }
                            >
                              <ViewIcon />
                            </IconButton>
                            {can(PERMISSIONS.EDIT_PHARMACIST) && (
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() =>
                                  navigate(
                                    `/pharmacists/edit/${pharmacist.userId}`
                                  )
                                }
                              >
                                <EditIcon />
                              </IconButton>
                            )}
                            {can(PERMISSIONS.DELETE_PHARMACIST) && (
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() => handleDelete(pharmacist.userId)}
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

export default PharmacistList;
