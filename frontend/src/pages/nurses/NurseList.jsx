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
  Paper,
  Chip,
  IconButton,
  CircularProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import api from '../../services/api';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';

function NurseList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [nurses, setNurses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNurses();
  }, []);

  const fetchNurses = async () => {
    try {
      setLoading(true);
      // Fetch all users and filter by NURSE role
      const response = await api.get('/users');
      const nurseUsers = response.data.filter(user => user.role === 'NURSE');
      setNurses(nurseUsers);
    } catch (error) {
      toast.error('Failed to load nurses');
      console.error('Error fetching nurses:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this nurse?')) {
      return;
    }

    try {
      await api.delete(`/users/${id}`);
      toast.success('Nurse deleted successfully');
      fetchNurses();
    } catch (error) {
      toast.error('Failed to delete nurse');
      console.error('Error deleting nurse:', error);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight={600}>
          Nurses
        </Typography>
        {can(PERMISSIONS.ADD_NURSE) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/nurses/new')}
          >
            Add Nurse
          </Button>
        )}
      </Box>

      <Card>
        <CardContent>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>ID</strong></TableCell>
                  <TableCell><strong>Name</strong></TableCell>
                  <TableCell><strong>Email</strong></TableCell>
                  <TableCell><strong>Phone</strong></TableCell>
                  <TableCell><strong>Status</strong></TableCell>
                  {(can(PERMISSIONS.EDIT_NURSE) || can(PERMISSIONS.DELETE_NURSE)) && (
                    <TableCell align="center"><strong>Actions</strong></TableCell>
                  )}
                </TableRow>
              </TableHead>
              <TableBody>
                {nurses.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No nurses found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  nurses.map((nurse) => (
                    <TableRow key={nurse.userId}>
                      <TableCell>#{nurse.userId}</TableCell>
                      <TableCell>
                        {nurse.firstName} {nurse.lastName}
                      </TableCell>
                      <TableCell>{nurse.email}</TableCell>
                      <TableCell>{nurse.phone || '-'}</TableCell>
                      <TableCell>
                        <Chip
                          label={nurse.enabled ? 'Active' : 'Inactive'}
                          size="small"
                          color={nurse.enabled ? 'success' : 'default'}
                        />
                      </TableCell>
                      {(can(PERMISSIONS.EDIT_NURSE) || can(PERMISSIONS.DELETE_NURSE)) && (
                        <TableCell align="center">
                          <Box display="flex" gap={0.5} justifyContent="center">
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() => navigate(`/nurses/${nurse.userId}`)}
                            >
                              <ViewIcon />
                            </IconButton>
                            {can(PERMISSIONS.EDIT_NURSE) && (
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => navigate(`/nurses/edit/${nurse.userId}`)}
                              >
                                <EditIcon />
                              </IconButton>
                            )}
                            {can(PERMISSIONS.DELETE_NURSE) && (
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() => handleDelete(nurse.userId)}
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

export default NurseList;
