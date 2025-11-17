import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Chip,
  InputAdornment,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import medicationService from '../../services/medicationService';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';

function MedicationList() {
  const navigate = useNavigate();
  const { can } = usePermissions();
  const [medications, setMedications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [lowStockCount, setLowStockCount] = useState(0);

  useEffect(() => {
    fetchMedications();
    fetchLowStockCount();
  }, []);

  const fetchMedications = async () => {
    try {
      const data = await medicationService.getAllMedications();
      setMedications(data);
    } catch (error) {
      toast.error('Failed to fetch medications');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const fetchLowStockCount = async () => {
    try {
      const data = await medicationService.getLowStockMedications(10);
      setLowStockCount(data.length);
    } catch (error) {
      console.error(error);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this medication?')) {
      try {
        await medicationService.deleteMedication(id);
        toast.success('Medication deleted successfully');
        fetchMedications();
      } catch (error) {
        toast.error('Failed to delete medication');
      }
    }
  };

  const getStockStatus = (quantity) => {
    if (quantity === 0) return { label: 'Out of Stock', color: 'error' };
    if (quantity < 10) return { label: 'Low Stock', color: 'warning' };
    return { label: 'In Stock', color: 'success' };
  };

  const filteredMedications = medications.filter(
    (med) =>
      med.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      med.manufacturer?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      med.category?.toLowerCase().includes(searchQuery.toLowerCase())
  );

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
            Medication Inventory
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage medications and stock levels
          </Typography>
        </div>
        {can(PERMISSIONS.ADD_MEDICATION) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/medications/new')}
          >
            Add Medication
          </Button>
        )}
      </Box>

      {lowStockCount > 0 && (
        <Alert severity="warning" icon={<WarningIcon />} sx={{ mb: 3 }}>
          <strong>{lowStockCount}</strong> medication(s) are running low on stock!
        </Alert>
      )}

      <Card>
        <CardContent>
          <TextField
            fullWidth
            placeholder="Search medications by name, manufacturer, or category..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ mb: 3 }}
          />

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell>Manufacturer</TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Price</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredMedications.length > 0 ? (
                  filteredMedications.map((medication) => {
                    const stockStatus = getStockStatus(medication.quantity);
                    return (
                      <TableRow key={medication.id} hover>
                        <TableCell>{medication.id}</TableCell>
                        <TableCell>
                          <Typography fontWeight={600}>{medication.name}</Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={medication.category} size="small" variant="outlined" />
                        </TableCell>
                        <TableCell>{medication.manufacturer}</TableCell>
                        <TableCell>
                          <Typography
                            fontWeight={600}
                            color={medication.quantity < 10 ? 'error' : 'text.primary'}
                          >
                            {medication.quantity}
                          </Typography>
                        </TableCell>
                        <TableCell>${medication.price?.toFixed(2)}</TableCell>
                        <TableCell>
                          <Chip label={stockStatus.label} size="small" color={stockStatus.color} />
                        </TableCell>
                        <TableCell align="right">
                          {can(PERMISSIONS.EDIT_MEDICATION) && (
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/medications/${medication.id}/edit`)}
                              color="info"
                            >
                              <EditIcon />
                            </IconButton>
                          )}
                          {can(PERMISSIONS.DELETE_MEDICATION) && (
                            <IconButton
                              size="small"
                              onClick={() => handleDelete(medication.id)}
                              color="error"
                            >
                              <DeleteIcon />
                            </IconButton>
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })
                ) : (
                  <TableRow>
                    <TableCell colSpan={8} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No medications found
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

export default MedicationList;
