import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  MenuItem,
  CircularProgress,
} from '@mui/material';
import { ArrowBack as BackIcon, Save as SaveIcon } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import patientService from '../../services/patientService';

const BLOOD_GROUPS = ['A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE'];
const GENDERS = ['MALE', 'FEMALE', 'OTHER'];

function PatientForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    age: '',
    gender: 'MALE',
    phone: '',
    email: '',
    address: '',
    bloodGroup: 'O_POSITIVE',
  });

  useEffect(() => {
    if (id) {
      fetchPatient();
    }
  }, [id]);

  const fetchPatient = async () => {
    try {
      const data = await patientService.getPatientById(id);
      setFormData(data);
    } catch (error) {
      toast.error('Failed to fetch patient details');
    }
  };

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
        await patientService.updatePatient(id, formData);
        toast.success('Patient updated successfully');
      } else {
        await patientService.createPatient(formData);
        toast.success('Patient created successfully');
      }
      navigate('/patients');
    } catch (error) {
      toast.error(id ? 'Failed to update patient' : 'Failed to create patient');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<BackIcon />} onClick={() => navigate('/patients')}>
          Back to Patients
        </Button>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight={700} gutterBottom>
            {id ? 'Edit Patient' : 'Add New Patient'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {id ? 'Update patient information' : 'Enter patient details'}
          </Typography>

          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Full Name"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Age"
                  name="age"
                  type="number"
                  value={formData.age}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Gender"
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  required
                >
                  {GENDERS.map((gender) => (
                    <MenuItem key={gender} value={gender}>
                      {gender}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Blood Group"
                  name="bloodGroup"
                  value={formData.bloodGroup}
                  onChange={handleChange}
                  required
                >
                  {BLOOD_GROUPS.map((group) => (
                    <MenuItem key={group} value={group}>
                      {group.replace('_', ' ')}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  required
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

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Address"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  multiline
                  rows={3}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/patients')}
                    disabled={loading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={loading ? <CircularProgress size={20} /> : <SaveIcon />}
                    disabled={loading}
                  >
                    {id ? 'Update Patient' : 'Create Patient'}
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

export default PatientForm;
