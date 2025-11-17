import { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Avatar,
  Chip,
  Divider,
  CircularProgress,
} from '@mui/material';
import { ArrowBack as BackIcon, Edit as EditIcon } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import patientService from '../../services/patientService';

function PatientDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchPatient = useCallback(async () => {
    try {
      const data = await patientService.getPatientById(id);
      setPatient(data);
    } catch (error) {
      toast.error('Failed to fetch patient details');
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPatient();
  }, [fetchPatient]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!patient) {
    return (
      <Box sx={{ p: 4, textAlign: 'center' }}>
        <Typography>Patient not found</Typography>
        <Button startIcon={<BackIcon />} onClick={() => navigate('/patients')} sx={{ mt: 2 }}>
          Back to Patients
        </Button>
      </Box>
    );
  }

  const InfoRow = ({ label, value }) => (
    <Box sx={{ py: 1.5 }}>
      <Typography variant="caption" color="text.secondary" display="block">
        {label}
      </Typography>
      <Typography variant="body1" fontWeight={500}>
        {value || 'N/A'}
      </Typography>
    </Box>
  );

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<BackIcon />} onClick={() => navigate('/patients')}>
          Back to Patients
        </Button>
        <Button
          variant="contained"
          startIcon={<EditIcon />}
          onClick={() => navigate(`/patients/${id}/edit`)}
        >
          Edit Patient
        </Button>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Avatar
                sx={{
                  width: 120,
                  height: 120,
                  mx: 'auto',
                  mb: 2,
                  bgcolor: 'primary.main',
                  fontSize: 48,
                }}
              >
                {patient.name?.charAt(0)}
              </Avatar>
              <Typography variant="h5" fontWeight={700} gutterBottom>
                {patient.name}
              </Typography>
              <Chip
                label={`${patient.age} years`}
                size="small"
                sx={{ mr: 1 }}
              />
              <Chip
                label={patient.gender}
                size="small"
                color={patient.gender === 'MALE' ? 'primary' : 'secondary'}
              />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Personal Information
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <InfoRow label="Patient ID" value={patient.id} />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <InfoRow label="Blood Group" value={patient.bloodGroup} />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <InfoRow label="Phone" value={patient.phone} />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <InfoRow label="Email" value={patient.email} />
                </Grid>
                <Grid item xs={12}>
                  <InfoRow label="Address" value={patient.address} />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <InfoRow
                    label="Created Date"
                    value={new Date(patient.createdAt).toLocaleDateString()}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <InfoRow
                    label="Last Updated"
                    value={new Date(patient.updatedAt).toLocaleDateString()}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          <Card sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Medical History
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body2" color="text.secondary">
                Medical history will be displayed here...
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

export default PatientDetail;
