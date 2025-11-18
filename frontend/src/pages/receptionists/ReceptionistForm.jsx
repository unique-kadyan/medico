import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  CircularProgress,
} from "@mui/material";
import { ArrowBack as BackIcon, Save as SaveIcon } from "@mui/icons-material";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import api from "../../services/api";

function ReceptionistForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    phone: "",
    role: "RECEPTIONIST",
  });

  const fetchReceptionist = useCallback(async () => {
    try {
      const response = await api.get(`/users/${id}`);
      const receptionist = response.data;
      setFormData({
        username: receptionist.username,
        email: receptionist.email,
        password: "",
        firstName: receptionist.firstName,
        lastName: receptionist.lastName,
        phone: receptionist.phone || "",
        role: "RECEPTIONIST",
      });
    } catch {
      toast.error("Failed to fetch receptionist details");
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      fetchReceptionist();
    }
  }, [id, fetchReceptionist]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (id) {
        await api.put(`/users/${id}`, formData);
        toast.success("Receptionist updated successfully");
      } else {
        await api.post("/auth/admin/create-user", formData);
        toast.success("Receptionist created successfully");
      }
      navigate("/receptionists");
    } catch (error) {
      const errorMessage =
        error.response?.data?.message ||
        (id
          ? "Failed to update receptionist"
          : "Failed to create receptionist");
      toast.error(errorMessage);
      console.error("Error:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/receptionists")}
        >
          Back to Receptionists
        </Button>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight={700} gutterBottom>
            {id ? "Edit Receptionist" : "Add New Receptionist"}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {id
              ? "Update receptionist information"
              : "Enter receptionist details"}
          </Typography>

          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  disabled={id}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </Grid>

              {!id && (
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Password"
                    name="password"
                    type="password"
                    value={formData.password}
                    onChange={handleChange}
                    required={!id}
                    helperText="Minimum 6 characters"
                  />
                </Grid>
              )}

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                />
              </Grid>

              <Grid item xs={12}>
                <Box
                  sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}
                >
                  <Button
                    variant="outlined"
                    onClick={() => navigate("/receptionists")}
                    disabled={loading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={
                      loading ? <CircularProgress size={20} /> : <SaveIcon />
                    }
                    disabled={loading}
                  >
                    {id ? "Update Receptionist" : "Create Receptionist"}
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

export default ReceptionistForm;
