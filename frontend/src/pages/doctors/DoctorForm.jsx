import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  FormControlLabel,
  Switch,
  CircularProgress,
  MenuItem,
} from '@mui/material';
import { Save as SaveIcon, ArrowBack as BackIcon } from '@mui/icons-material';
import { toast } from 'sonner';
import doctorService from '../../services/doctorService';

const SPECIALIZATIONS = [
  'CARDIOLOGY',
  'NEUROLOGY',
  'PEDIATRICS',
  'ORTHOPEDICS',
  'DERMATOLOGY',
  'GENERAL_MEDICINE',
  'PSYCHIATRY',
  'SURGERY',
  'OPHTHALMOLOGY',
  'ENT',
  'GYNECOLOGY',
  'UROLOGY',
  'ONCOLOGY',
  'RADIOLOGY',
  'ANESTHESIOLOGY',
];

function DoctorForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    doctorId: '',
    firstName: '',
    lastName: '',
    specialization: '',
    licenseNumber: '',
    phone: '',
    email: '',
    department: '',
    yearsOfExperience: 0,
    qualification: '',
    about: '',
    availableForConsultation: true,
    active: true,
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isEditMode) {
      fetchDoctor();
    }
  }, [id]);

  const fetchDoctor = async () => {
    try {
      setLoading(true);
      const data = await doctorService.getDoctorById(id);
      setFormData({
        doctorId: data.doctorId || '',
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        specialization: data.specialization || '',
        licenseNumber: data.licenseNumber || '',
        phone: data.phone || '',
        email: data.email || '',
        department: data.department || '',
        yearsOfExperience: data.yearsOfExperience || 0,
        qualification: data.qualification || '',
        about: data.about || '',
        availableForConsultation: data.availableForConsultation ?? true,
        active: data.active ?? true,
      });
    } catch (error) {
      toast.error('Failed to fetch doctor details');
      console.error('Error fetching doctor:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, checked, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));

    // Clear error for this field
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.doctorId.trim()) {
      newErrors.doctorId = 'Doctor ID is required';
    }
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    if (!formData.specialization) {
      newErrors.specialization = 'Specialization is required';
    }
    if (formData.yearsOfExperience < 0) {
      newErrors.yearsOfExperience = 'Years of experience cannot be negative';
    }
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Please fix the errors in the form');
      return;
    }

    try {
      setSaving(true);

      if (isEditMode) {
        await doctorService.updateDoctor(id, formData);
        toast.success('Doctor updated successfully');
      } else {
        await doctorService.createDoctor(formData);
        toast.success('Doctor created successfully');
      }

      navigate('/doctors');
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to save doctor';
      toast.error(errorMessage);
      console.error('Error saving doctor:', error);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('/doctors');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          {isEditMode ? 'Edit Doctor' : 'Add New Doctor'}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {isEditMode ? 'Update doctor information' : 'Enter doctor details to add to the system'}
        </Typography>
      </Box>

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              {/* Basic Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Basic Information
                </Typography>
              </Grid>

              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  required
                  label="Doctor ID"
                  name="doctorId"
                  value={formData.doctorId}
                  onChange={handleChange}
                  error={Boolean(errors.doctorId)}
                  helperText={errors.doctorId}
                  disabled={isEditMode}
                />
              </Grid>

              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  required
                  label="First Name"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  error={Boolean(errors.firstName)}
                  helperText={errors.firstName}
                />
              </Grid>

              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  required
                  label="Last Name"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  error={Boolean(errors.lastName)}
                  helperText={errors.lastName}
                />
              </Grid>

              {/* Professional Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Professional Information
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  required
                  select
                  label="Specialization"
                  name="specialization"
                  value={formData.specialization}
                  onChange={handleChange}
                  error={Boolean(errors.specialization)}
                  helperText={errors.specialization}
                >
                  {SPECIALIZATIONS.map((spec) => (
                    <MenuItem key={spec} value={spec}>
                      {spec.replace(/_/g, ' ')}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="License Number"
                  name="licenseNumber"
                  value={formData.licenseNumber}
                  onChange={handleChange}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Qualification"
                  name="qualification"
                  value={formData.qualification}
                  onChange={handleChange}
                  placeholder="e.g., MBBS, MD"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  required
                  type="number"
                  label="Years of Experience"
                  name="yearsOfExperience"
                  value={formData.yearsOfExperience}
                  onChange={handleChange}
                  error={Boolean(errors.yearsOfExperience)}
                  helperText={errors.yearsOfExperience}
                  inputProps={{ min: 0 }}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Department"
                  name="department"
                  value={formData.department}
                  onChange={handleChange}
                  placeholder="e.g., Emergency, OPD"
                />
              </Grid>

              {/* Contact Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Contact Information
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="555-0123"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  type="email"
                  label="Email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  error={Boolean(errors.email)}
                  helperText={errors.email}
                  placeholder="doctor@example.com"
                />
              </Grid>

              {/* Additional Information */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Additional Information
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={4}
                  label="About"
                  name="about"
                  value={formData.about}
                  onChange={handleChange}
                  placeholder="Brief description about the doctor..."
                />
              </Grid>

              {/* Status */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Status
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.availableForConsultation}
                      onChange={handleChange}
                      name="availableForConsultation"
                      color="primary"
                    />
                  }
                  label="Available for Consultation"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.active}
                      onChange={handleChange}
                      name="active"
                      color="primary"
                    />
                  }
                  label="Active"
                />
              </Grid>

              {/* Actions */}
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                    disabled={saving}
                  >
                    {saving ? 'Saving...' : isEditMode ? 'Update Doctor' : 'Add Doctor'}
                  </Button>
                  <Button variant="outlined" startIcon={<BackIcon />} onClick={handleCancel}>
                    Cancel
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

export default DoctorForm;
