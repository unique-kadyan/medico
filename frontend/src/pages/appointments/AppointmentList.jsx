import { useState, useEffect } from 'react';
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
  Chip,
  CircularProgress,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import appointmentService from '../../services/appointmentService';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';

function AppointmentList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const data = await appointmentService.getAllAppointments();
      setAppointments(data);
    } catch (error) {
      toast.error('Failed to fetch appointments');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SCHEDULED': return 'primary';
      case 'COMPLETED': return 'success';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
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
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <div>
          <Typography variant="h4" fontWeight={700}>
            Appointments
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage patient appointments
          </Typography>
        </div>
        {can(PERMISSIONS.BOOK_APPOINTMENT) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/appointments/new')}
          >
            Book Appointment
          </Button>
        )}
      </Box>

      <Card>
        <CardContent>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Patient</TableCell>
                  <TableCell>Doctor</TableCell>
                  <TableCell>Date & Time</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {appointments.length > 0 ? (
                  appointments.map((appointment) => (
                    <TableRow key={appointment.id} hover>
                      <TableCell>{appointment.id}</TableCell>
                      <TableCell>{appointment.patientName || 'N/A'}</TableCell>
                      <TableCell>{appointment.doctorName || 'N/A'}</TableCell>
                      <TableCell>{new Date(appointment.appointmentDate).toLocaleString()}</TableCell>
                      <TableCell>
                        <Chip label={appointment.type} size="small" variant="outlined" />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={appointment.status}
                          size="small"
                          color={getStatusColor(appointment.status)}
                        />
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No appointments found
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  );
}

export default AppointmentList;
