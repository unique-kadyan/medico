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
  Stepper,
  Step,
  StepLabel,
  Divider,
  Chip,
  Paper,
  Avatar,
} from "@mui/material";
import {
  Visibility,
  VisibilityOff,
  LocalHospital,
  Business,
  Person,
  CheckCircle,
  Star,
  Rocket,
  Diamond,
  CloudUpload,
  Delete,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import hospitalService from "../../services/hospitalService";

const steps = ["Hospital Details", "Admin Account", "Review & Start Trial"];

const subscriptionPlans = [
  {
    name: "TRIAL",
    displayName: "10-Day Free Trial",
    price: 0,
    maxUsers: 10,
    maxPatients: 100,
    features: [
      "Basic Features",
      "10 Staff Members",
      "100 Patients",
      "Email Support",
    ],
    icon: <Rocket />,
    color: "warning",
    recommended: false,
  },
  {
    name: "BASIC",
    displayName: "Basic",
    price: 99,
    maxUsers: 25,
    maxPatients: 500,
    features: [
      "All Trial Features",
      "25 Staff Members",
      "500 Patients",
      "Priority Support",
      "Reports",
    ],
    icon: <Star />,
    color: "info",
    recommended: false,
  },
  {
    name: "PROFESSIONAL",
    displayName: "Professional",
    price: 299,
    maxUsers: 100,
    maxPatients: 5000,
    features: [
      "All Basic Features",
      "100 Staff Members",
      "5000 Patients",
      "AI Features",
      "FHIR Integration",
      "API Access",
    ],
    icon: <CheckCircle />,
    color: "success",
    recommended: true,
  },
  {
    name: "ENTERPRISE",
    displayName: "Enterprise",
    price: 999,
    maxUsers: -1,
    maxPatients: -1,
    features: [
      "All Professional Features",
      "Unlimited Staff",
      "Unlimited Patients",
      "24/7 Support",
      "Custom Integrations",
      "Dedicated Account Manager",
    ],
    icon: <Diamond />,
    color: "secondary",
    recommended: false,
  },
];

function HospitalRegistration() {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [hospitalData, setHospitalData] = useState({
    name: "",
    code: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    state: "",
    country: "",
    postalCode: "",
    registrationNumber: "",
    taxId: "",
    website: "",
  });

  const [logoFile, setLogoFile] = useState(null);
  const [logoPreview, setLogoPreview] = useState(null);

  const [adminData, setAdminData] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    phone: "",
  });

  const handleHospitalChange = (e) => {
    const { name, value } = e.target;
    setHospitalData((prev) => ({ ...prev, [name]: value }));
    setError("");

    // Auto-generate code from name
    if (name === "name" && !hospitalData.code) {
      const code = value
        .toUpperCase()
        .replace(/[^A-Z0-9]/g, "")
        .substring(0, 10);
      setHospitalData((prev) => ({ ...prev, code }));
    }
  };

  const handleAdminChange = (e) => {
    const { name, value } = e.target;
    setAdminData((prev) => ({ ...prev, [name]: value }));
    setError("");
  };

  const handleLogoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.startsWith("image/")) {
        setError("Please select an image file (PNG, JPG, etc.)");
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        setError("Logo file must be less than 2MB");
        return;
      }
      setLogoFile(file);
      setLogoPreview(URL.createObjectURL(file));
      setError("");
    }
  };

  const handleRemoveLogo = () => {
    setLogoFile(null);
    if (logoPreview) {
      URL.revokeObjectURL(logoPreview);
    }
    setLogoPreview(null);
  };

  const validateStep = (step) => {
    if (step === 0) {
      if (!hospitalData.name || !hospitalData.code || !hospitalData.email) {
        setError("Please fill in all required hospital fields");
        return false;
      }
      if (!/^[A-Z0-9]+$/.test(hospitalData.code)) {
        setError(
          "Hospital code must contain only uppercase letters and numbers"
        );
        return false;
      }
    } else if (step === 1) {
      if (
        !adminData.username ||
        !adminData.email ||
        !adminData.password ||
        !adminData.firstName ||
        !adminData.lastName
      ) {
        setError("Please fill in all required admin fields");
        return false;
      }
      if (adminData.password !== adminData.confirmPassword) {
        setError("Passwords do not match");
        return false;
      }
      if (adminData.password.length < 6) {
        setError("Password must be at least 6 characters");
        return false;
      }
    }
    return true;
  };

  const handleNext = () => {
    if (validateStep(activeStep)) {
      setActiveStep((prev) => prev + 1);
      setError("");
    }
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
    setError("");
  };

  const handleSubmit = async () => {
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const registrationData = {
        hospital: hospitalData,
        admin: {
          username: adminData.username,
          firstName: adminData.firstName,
          lastName: adminData.lastName,
          email: adminData.email,
          password: adminData.password,
          phone: adminData.phone,
        },
      };

      const response = await hospitalService.register(registrationData);

      if (logoFile && response?.id) {
        try {
          await hospitalService.uploadLogo(response.id, logoFile);
          toast.success("Logo uploaded successfully!");
        } catch (logoErr) {
          console.error("Logo upload failed:", logoErr);
          toast.warning(
            "Hospital registered but logo upload failed. You can upload it later."
          );
        }
      }

      setSuccess(
        "Hospital registered successfully! Your 10-day trial has started. You can now login with your admin credentials."
      );
      toast.success("Hospital registered successfully!");

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

  const renderHospitalDetails = () => (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography
          variant="h6"
          gutterBottom
          sx={{ display: "flex", alignItems: "center", gap: 1 }}
        >
          <Business color="primary" />
          Hospital Information
        </Typography>
      </Grid>

      <Grid item xs={12} sm={8}>
        <TextField
          fullWidth
          label="Hospital Name"
          name="name"
          value={hospitalData.name}
          onChange={handleHospitalChange}
          required
          autoFocus
          placeholder="e.g., City General Hospital"
        />
      </Grid>

      <Grid item xs={12} sm={4}>
        <TextField
          fullWidth
          label="Hospital Code"
          name="code"
          value={hospitalData.code}
          onChange={handleHospitalChange}
          required
          placeholder="e.g., CGH001"
          helperText="Unique identifier (uppercase letters & numbers)"
          inputProps={{ style: { textTransform: "uppercase" } }}
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Hospital Email"
          name="email"
          type="email"
          value={hospitalData.email}
          onChange={handleHospitalChange}
          required
          placeholder="hospital@example.com"
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Phone Number"
          name="phone"
          value={hospitalData.phone}
          onChange={handleHospitalChange}
          placeholder="+1 234 567 8900"
        />
      </Grid>

      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Address"
          name="address"
          value={hospitalData.address}
          onChange={handleHospitalChange}
          placeholder="Street address"
          multiline
          rows={2}
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="City"
          name="city"
          value={hospitalData.city}
          onChange={handleHospitalChange}
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="State/Province"
          name="state"
          value={hospitalData.state}
          onChange={handleHospitalChange}
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Country"
          name="country"
          value={hospitalData.country}
          onChange={handleHospitalChange}
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Postal Code"
          name="postalCode"
          value={hospitalData.postalCode}
          onChange={handleHospitalChange}
        />
      </Grid>

      <Grid item xs={12}>
        <Divider sx={{ my: 1 }} />
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          Optional Information
        </Typography>
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Registration Number"
          name="registrationNumber"
          value={hospitalData.registrationNumber}
          onChange={handleHospitalChange}
          placeholder="Business registration number"
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Tax ID"
          name="taxId"
          value={hospitalData.taxId}
          onChange={handleHospitalChange}
          placeholder="Tax identification number"
        />
      </Grid>

      <Grid item xs={12}>
        <TextField
          fullWidth
          label="Website"
          name="website"
          value={hospitalData.website}
          onChange={handleHospitalChange}
          placeholder="https://www.hospital.com"
        />
      </Grid>

      <Grid item xs={12}>
        <Divider sx={{ my: 1 }} />
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          Hospital Logo
        </Typography>
      </Grid>

      <Grid item xs={12}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          {logoPreview ? (
            <Avatar
              src={logoPreview}
              alt="Hospital Logo"
              sx={{
                width: 100,
                height: 100,
                border: "2px solid",
                borderColor: "primary.main",
              }}
              variant="rounded"
            />
          ) : (
            <Avatar
              sx={{ width: 100, height: 100, bgcolor: "grey.200" }}
              variant="rounded"
            >
              <LocalHospital sx={{ fontSize: 48, color: "grey.400" }} />
            </Avatar>
          )}
          <Box>
            <input
              accept="image/*"
              style={{ display: "none" }}
              id="logo-upload"
              type="file"
              onChange={handleLogoChange}
            />
            <label htmlFor="logo-upload">
              <Button
                variant="outlined"
                component="span"
                startIcon={<CloudUpload />}
                size="small"
              >
                Upload Logo
              </Button>
            </label>
            {logoFile && (
              <Button
                variant="text"
                color="error"
                onClick={handleRemoveLogo}
                startIcon={<Delete />}
                size="small"
                sx={{ ml: 1 }}
              >
                Remove
              </Button>
            )}
            <Typography
              variant="caption"
              display="block"
              color="text.secondary"
              sx={{ mt: 0.5 }}
            >
              PNG, JPG up to 2MB. Recommended: 200x200 pixels.
            </Typography>
          </Box>
        </Box>
      </Grid>
    </Grid>
  );

  const renderAdminAccount = () => (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography
          variant="h6"
          gutterBottom
          sx={{ display: "flex", alignItems: "center", gap: 1 }}
        >
          <Person color="primary" />
          Hospital Admin Account
        </Typography>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          This account will have full administrative access to manage your
          hospital
        </Typography>
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Username"
          name="username"
          value={adminData.username}
          onChange={handleAdminChange}
          required
          autoFocus
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Email"
          name="email"
          type="email"
          value={adminData.email}
          onChange={handleAdminChange}
          required
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="First Name"
          name="firstName"
          value={adminData.firstName}
          onChange={handleAdminChange}
          required
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Last Name"
          name="lastName"
          value={adminData.lastName}
          onChange={handleAdminChange}
          required
        />
      </Grid>

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Phone"
          name="phone"
          value={adminData.phone}
          onChange={handleAdminChange}
        />
      </Grid>

      <Grid item xs={12} sm={6} />

      <Grid item xs={12} sm={6}>
        <TextField
          fullWidth
          label="Password"
          name="password"
          type={showPassword ? "text" : "password"}
          value={adminData.password}
          onChange={handleAdminChange}
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
          value={adminData.confirmPassword}
          onChange={handleAdminChange}
          required
        />
      </Grid>
    </Grid>
  );

  const renderReview = () => (
    <Box>
      <Typography
        variant="h6"
        gutterBottom
        sx={{ display: "flex", alignItems: "center", gap: 1 }}
      >
        <CheckCircle color="success" />
        Review & Start Your Free Trial
      </Typography>

      <Box sx={{ mb: 4 }}>
        <Typography variant="subtitle1" fontWeight={600} gutterBottom>
          Available Plans (Upgrade Anytime)
        </Typography>
        <Grid container spacing={2}>
          {subscriptionPlans.map((plan) => (
            <Grid item xs={12} sm={6} md={3} key={plan.name}>
              <Paper
                elevation={plan.recommended ? 8 : 1}
                sx={{
                  p: 2,
                  height: "100%",
                  border: plan.recommended ? 2 : 1,
                  borderColor: plan.recommended ? "success.main" : "divider",
                  position: "relative",
                }}
              >
                {plan.recommended && (
                  <Chip
                    label="Recommended"
                    color="success"
                    size="small"
                    sx={{ position: "absolute", top: -10, right: 10 }}
                  />
                )}
                <Box
                  sx={{ display: "flex", alignItems: "center", gap: 1, mb: 1 }}
                >
                  {plan.icon}
                  <Typography variant="subtitle1" fontWeight={600}>
                    {plan.displayName}
                  </Typography>
                </Box>
                <Typography variant="h5" color="primary" fontWeight={700}>
                  ${plan.price}
                  <Typography
                    component="span"
                    variant="body2"
                    color="text.secondary"
                  >
                    /month
                  </Typography>
                </Typography>
                <Box sx={{ mt: 1 }}>
                  {plan.features.slice(0, 3).map((feature, idx) => (
                    <Typography
                      key={idx}
                      variant="caption"
                      display="block"
                      color="text.secondary"
                    >
                      {feature}
                    </Typography>
                  ))}
                </Box>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Box>

      <Divider sx={{ my: 3 }} />

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, bgcolor: "grey.50" }}>
            <Box sx={{ display: "flex", alignItems: "flex-start", gap: 2 }}>
              {logoPreview ? (
                <Avatar
                  src={logoPreview}
                  alt="Hospital Logo"
                  sx={{ width: 60, height: 60 }}
                  variant="rounded"
                />
              ) : (
                <Avatar
                  sx={{ width: 60, height: 60, bgcolor: "primary.main" }}
                  variant="rounded"
                >
                  <LocalHospital sx={{ fontSize: 32 }} />
                </Avatar>
              )}
              <Box>
                <Typography
                  variant="subtitle2"
                  color="text.secondary"
                  gutterBottom
                >
                  Hospital Details
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {hospitalData.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Code: {hospitalData.code}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {hospitalData.email}
                </Typography>
                {hospitalData.phone && (
                  <Typography variant="body2" color="text.secondary">
                    {hospitalData.phone}
                  </Typography>
                )}
                {hospitalData.city && (
                  <Typography variant="body2" color="text.secondary">
                    {hospitalData.city}, {hospitalData.state}{" "}
                    {hospitalData.country}
                  </Typography>
                )}
              </Box>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, bgcolor: "grey.50" }}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Admin Account
            </Typography>
            <Typography variant="body1" fontWeight={600}>
              {adminData.firstName} {adminData.lastName}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Username: {adminData.username}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {adminData.email}
            </Typography>
            {adminData.phone && (
              <Typography variant="body2" color="text.secondary">
                {adminData.phone}
              </Typography>
            )}
          </Paper>
        </Grid>
      </Grid>

      <Alert severity="info" sx={{ mt: 3 }}>
        <Typography variant="body2">
          <strong>10-Day Free Trial:</strong> Your trial starts immediately upon
          registration. No credit card required. You can upgrade to a paid plan
          at any time to unlock more features.
        </Typography>
      </Alert>
    </Box>
  );

  const getStepContent = (step) => {
    switch (step) {
      case 0:
        return renderHospitalDetails();
      case 1:
        return renderAdminAccount();
      case 2:
        return renderReview();
      default:
        return null;
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background:
          "linear-gradient(135deg, #1a237e 0%, #0d47a1 50%, #01579b 100%)",
        p: 2,
        py: 4,
      }}
    >
      <Card
        sx={{
          maxWidth: 900,
          width: "100%",
          boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
        }}
      >
        <CardContent sx={{ p: 4 }}>
          {/* Logo and Title */}
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
              Register Your Hospital
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Start your 10-day free trial of Medico Hospital Management System
            </Typography>
          </Box>

          <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>

          {success && (
            <Alert severity="success" sx={{ mb: 3 }}>
              {success}
            </Alert>
          )}

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Box sx={{ mb: 4 }}>{getStepContent(activeStep)}</Box>

          <Box sx={{ display: "flex", justifyContent: "space-between", mt: 3 }}>
            <Button
              disabled={activeStep === 0}
              onClick={handleBack}
              variant="outlined"
            >
              Back
            </Button>

            {activeStep === steps.length - 1 ? (
              <Button
                variant="contained"
                onClick={handleSubmit}
                disabled={loading}
                size="large"
                sx={{ minWidth: 200 }}
              >
                {loading ? <CircularProgress size={24} /> : "Start Free Trial"}
              </Button>
            ) : (
              <Button variant="contained" onClick={handleNext}>
                Next
              </Button>
            )}
          </Box>

          <Box sx={{ textAlign: "center", mt: 3 }}>
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
        </CardContent>
      </Card>
    </Box>
  );
}

export default HospitalRegistration;
