import { useState } from "react";
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Link,
  InputAdornment,
  IconButton,
  Alert,
  CircularProgress,
  Grid,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  FormHelperText,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
} from "@mui/material";
import {
  Visibility,
  VisibilityOff,
  LocalHospital,
  CloudUpload,
  Description as DocumentIcon,
  Delete as DeleteIcon,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import pendingUserService from "../../services/pendingUserService";
import { ROLES } from "../../utils/permissions";

function Register() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [documents, setDocuments] = useState([]);
  const [formData, setFormData] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    phone: "",
    role: "",
  });

  const availableRoles = [
    { value: ROLES.DOCTOR, label: "Doctor" },
    { value: ROLES.DOCTOR_SUPERVISOR, label: "Doctor Supervisor" },
    { value: ROLES.NURSE, label: "Nurse" },
    { value: ROLES.NURSE_MANAGER, label: "Nurse Manager" },
    { value: ROLES.NURSE_SUPERVISOR, label: "Nurse Supervisor" },
    { value: ROLES.RECEPTIONIST, label: "Receptionist" },
  ];

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
  };

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    setDocuments([...documents, ...files]);
    setError("");
  };

  const handleRemoveDocument = (index) => {
    setDocuments(documents.filter((_, i) => i !== index));
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + " KB";
    return (bytes / (1024 * 1024)).toFixed(2) + " MB";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!formData.role) {
      setError("Please select a role");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters");
      return;
    }

    if (documents.length < 2) {
      setError("Please upload at least 2 government ID documents");
      return;
    }

    setLoading(true);

    try {
      const uploadData = new FormData();
      uploadData.append("username", formData.username);
      uploadData.append("password", formData.password);
      uploadData.append("email", formData.email);
      uploadData.append("firstName", formData.firstName);
      uploadData.append("lastName", formData.lastName);
      uploadData.append("phone", formData.phone);
      uploadData.append("role", formData.role);

      documents.forEach((doc) => {
        uploadData.append("documents", doc);
      });

      const result = await pendingUserService.registerWithDocuments(uploadData);

      setSuccess(
        result.message ||
          "Registration submitted successfully! Your account is pending approval. You will be notified once approved."
      );
      toast.success("Registration submitted for approval!");

      setFormData({
        username: "",
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
        phone: "",
        role: "",
      });
      setDocuments([]);

      // Navigate to login after 3 seconds
      setTimeout(() => {
        navigate("/login");
      }, 3000);
    } catch (err) {
      const message =
        err.response?.data?.message || "Registration failed. Please try again.";
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        p: 2,
      }}
    >
      <Card
        sx={{
          maxWidth: 700,
          width: "100%",
          boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
        }}
      >
        <CardContent sx={{ p: 4 }}>
          {/* Logo and Title */}
          <Box sx={{ textAlign: "center", mb: 3 }}>
            <Box
              sx={{
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                width: 64,
                height: 64,
                borderRadius: "50%",
                bgcolor: "primary.main",
                mb: 2,
              }}
            >
              <LocalHospital sx={{ fontSize: 36, color: "white" }} />
            </Box>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Staff Registration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Join Medico Hospital Management System
            </Typography>
          </Box>

          {success && (
            <Alert severity="success" sx={{ mb: 3 }}>
              {success}
            </Alert>
          )}

          {/* Error Alert */}
          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {/* Registration Form */}
          <form onSubmit={handleSubmit}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  autoFocus
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth required>
                  <InputLabel>Role</InputLabel>
                  <Select
                    name="role"
                    value={formData.role}
                    onChange={handleChange}
                    label="Role"
                  >
                    {availableRoles.map((role) => (
                      <MenuItem key={role.value} value={role.value}>
                        {role.label}
                      </MenuItem>
                    ))}
                  </Select>
                  <FormHelperText>
                    Select your role (requires approval)
                  </FormHelperText>
                </FormControl>
              </Grid>

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
                  label="Email Address"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Phone Number"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  value={formData.password}
                  onChange={handleChange}
                  required
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => setShowPassword(!showPassword)}
                          edge="end"
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Confirm Password"
                  name="confirmPassword"
                  type={showPassword ? "text" : "password"}
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Upload Government ID Documents (Minimum 2 required)
                </Typography>
                <Button
                  variant="outlined"
                  component="label"
                  startIcon={<CloudUpload />}
                  fullWidth
                  sx={{ mb: 2 }}
                >
                  Upload Documents
                  <input
                    type="file"
                    hidden
                    multiple
                    accept="image/*,.pdf"
                    onChange={handleFileChange}
                  />
                </Button>

                {documents.length > 0 && (
                  <List dense>
                    {documents.map((doc, index) => (
                      <ListItem
                        key={index}
                        secondaryAction={
                          <IconButton
                            edge="end"
                            onClick={() => handleRemoveDocument(index)}
                            size="small"
                          >
                            <DeleteIcon />
                          </IconButton>
                        }
                      >
                        <ListItemIcon>
                          <DocumentIcon />
                        </ListItemIcon>
                        <ListItemText
                          primary={doc.name}
                          secondary={formatFileSize(doc.size)}
                        />
                      </ListItem>
                    ))}
                  </List>
                )}

                <FormHelperText>
                  <Chip
                    label={`${documents.length} document(s) uploaded`}
                    size="small"
                    color={documents.length >= 2 ? "success" : "default"}
                  />
                </FormHelperText>
              </Grid>
            </Grid>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading || documents.length < 2}
              sx={{
                mt: 3,
                py: 1.5,
                fontSize: "1rem",
                fontWeight: 600,
                textTransform: "none",
              }}
            >
              {loading ? <CircularProgress size={24} /> : "Submit Registration"}
            </Button>

            <Box sx={{ textAlign: "center", mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Already have an account?{" "}
                <Link
                  href="/login"
                  underline="hover"
                  sx={{ color: "primary.main", fontWeight: 600 }}
                >
                  Sign In
                </Link>
              </Typography>
            </Box>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}

export default Register;
