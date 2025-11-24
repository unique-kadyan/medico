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
} from "@mui/material";
import {
  Visibility,
  VisibilityOff,
  LocalHospital,
  Email,
  Phone,
  ContactSupport,
  Person,
  MedicalServices,
  Science,
  AdminPanelSettings,
  ReceiptLong,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { toast } from "sonner";
import authService from "../../services/authService";
import {
  loginStart,
  loginSuccess,
  loginFailure,
} from "../../store/slices/authSlice";

function Login() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    dispatch(loginStart());

    try {
      const response = await authService.login({
        email: formData.email,
        password: formData.password,
      });

      const { ...userData } = response;
      dispatch(loginSuccess(userData));
      toast.success("Login successful!");
      navigate("/dashboard");
    } catch (err) {
      const message =
        err.response?.data?.message ||
        "Login failed. Please check your credentials.";
      setError(message);
      dispatch(loginFailure(message));
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
          maxWidth: 450,
          width: "100%",
          boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
        }}
      >
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ textAlign: "center", mb: 4 }}>
            <Box
              sx={{
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                width: 80,
                height: 80,
                borderRadius: "50%",
                bgcolor: "primary.main",
                mb: 2,
              }}
            >
              <LocalHospital sx={{ fontSize: 48, color: "white" }} />
            </Box>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Welcome to Medico
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Hospital & Pharmacy Management System
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Email Address"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              required
              autoComplete="email"
              autoFocus
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="Password"
              name="password"
              type={showPassword ? "text" : "password"}
              value={formData.password}
              onChange={handleChange}
              required
              autoComplete="current-password"
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
              sx={{ mb: 1 }}
            />

            <Box sx={{ textAlign: "right", mb: 3 }}>
              <Link
                href="#"
                underline="hover"
                variant="body2"
                sx={{ color: "primary.main" }}
              >
                Forgot Password?
              </Link>
            </Box>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                fontSize: "1rem",
                fontWeight: 600,
                textTransform: "none",
                mb: 2,
              }}
            >
              {loading ? <CircularProgress size={24} /> : "Sign In"}
            </Button>

            <Box sx={{ textAlign: "center" }}>
              <Typography variant="body2" color="text.secondary">
                Don&apos;t have an account?{" "}
                <Link
                  href="/register"
                  underline="hover"
                  sx={{ color: "primary.main", fontWeight: 600 }}
                >
                  Sign Up
                </Link>
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Want to register your hospital?{" "}
                <Link
                  href="/hospital-registration"
                  underline="hover"
                  sx={{ color: "secondary.main", fontWeight: 600 }}
                >
                  Start Free Trial
                </Link>
              </Typography>
            </Box>
          </form>

          <Box
            sx={{
              mt: 4,
              p: 2.5,
              bgcolor: "grey.50",
              borderRadius: 2,
              border: "1px solid",
              borderColor: "grey.200",
            }}
          >
            <Box sx={{ display: "flex", alignItems: "center", mb: 1.5 }}>
              <ContactSupport
                sx={{ fontSize: 20, color: "primary.main", mr: 1 }}
              />
              <Typography
                variant="subtitle2"
                fontWeight={600}
                color="text.primary"
              >
                Support & Grievances
              </Typography>
            </Box>

            <Box sx={{ display: "flex", alignItems: "center", mb: 1 }}>
              <Email sx={{ fontSize: 18, color: "text.secondary", mr: 1.5 }} />
              <Typography variant="body2" color="text.secondary">
                admin@medico.com
              </Typography>
            </Box>

            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Phone sx={{ fontSize: 18, color: "text.secondary", mr: 1.5 }} />
              <Typography variant="body2" color="text.secondary">
                +91 81684 81271
              </Typography>
            </Box>
          </Box>

          <Box
            sx={{
              mt: 2,
              p: 2.5,
              bgcolor: "info.lighter",
              borderRadius: 2,
              border: "1px solid",
              borderColor: "info.light",
            }}
          >
            <Typography
              variant="subtitle2"
              fontWeight={600}
              color="info.dark"
              gutterBottom
              sx={{ mb: 2 }}
            >
              Test Users (Development)
            </Typography>

            <Box sx={{ display: "grid", gap: 1 }}>
              <Box sx={{ display: "flex", alignItems: "center" }}>
                <AdminPanelSettings
                  sx={{ fontSize: 16, color: "info.dark", mr: 1 }}
                />
                <Typography variant="caption" sx={{ fontFamily: "monospace" }}>
                  <strong>Admin:</strong> admin / admin123
                </Typography>
              </Box>

              <Box sx={{ display: "flex", alignItems: "center" }}>
                <MedicalServices
                  sx={{ fontSize: 16, color: "info.dark", mr: 1 }}
                />
                <Typography variant="caption" sx={{ fontFamily: "monospace" }}>
                  <strong>Doctor:</strong> doctor / doctor123
                </Typography>
              </Box>

              <Box sx={{ display: "flex", alignItems: "center" }}>
                <Person sx={{ fontSize: 16, color: "info.dark", mr: 1 }} />
                <Typography variant="caption" sx={{ fontFamily: "monospace" }}>
                  <strong>Nurse:</strong> nurse / nurse123
                </Typography>
              </Box>

              <Box sx={{ display: "flex", alignItems: "center" }}>
                <Science sx={{ fontSize: 16, color: "info.dark", mr: 1 }} />
                <Typography variant="caption" sx={{ fontFamily: "monospace" }}>
                  <strong>Lab Tech:</strong> labtech / labtech123
                </Typography>
              </Box>

              <Box sx={{ display: "flex", alignItems: "center" }}>
                <ReceiptLong sx={{ fontSize: 16, color: "info.dark", mr: 1 }} />
                <Typography variant="caption" sx={{ fontFamily: "monospace" }}>
                  <strong>Receptionist:</strong> receptionist / receptionist123
                </Typography>
              </Box>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}

export default Login;
