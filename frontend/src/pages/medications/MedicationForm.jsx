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
import medicationService from '../../services/medicationService';

const CATEGORIES = ['ANTIBIOTIC', 'PAINKILLER', 'VITAMIN', 'ANTIHISTAMINE', 'ANTACID', 'OTHER'];

function MedicationForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    category: 'ANTIBIOTIC',
    manufacturer: '',
    description: '',
    quantity: '',
    price: '',
    expiryDate: '',
  });

  useEffect(() => {
    if (id) {
      fetchMedication();
    }
  }, [id]);

  const fetchMedication = async () => {
    try {
      const data = await medicationService.getMedicationById(id);
      setFormData(data);
    } catch (error) {
      toast.error('Failed to fetch medication details');
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
        await medicationService.updateMedication(id, formData);
        toast.success('Medication updated successfully');
      } else {
        await medicationService.createMedication(formData);
        toast.success('Medication created successfully');
      }
      navigate('/medications');
    } catch (error) {
      toast.error(id ? 'Failed to update medication' : 'Failed to create medication');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<BackIcon />} onClick={() => navigate('/medications')}>
          Back to Medications
        </Button>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight={700} gutterBottom>
            {id ? 'Edit Medication' : 'Add New Medication'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {id ? 'Update medication information' : 'Enter medication details'}
          </Typography>

          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Medication Name"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Category"
                  name="category"
                  value={formData.category}
                  onChange={handleChange}
                  required
                >
                  {CATEGORIES.map((category) => (
                    <MenuItem key={category} value={category}>
                      {category}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Manufacturer"
                  name="manufacturer"
                  value={formData.manufacturer}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Quantity"
                  name="quantity"
                  type="number"
                  value={formData.quantity}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Price"
                  name="price"
                  type="number"
                  inputProps={{ step: '0.01' }}
                  value={formData.price}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Expiry Date"
                  name="expiryDate"
                  type="date"
                  value={formData.expiryDate}
                  onChange={handleChange}
                  InputLabelProps={{ shrink: true }}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  multiline
                  rows={3}
                />
              </Grid>

              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/medications')}
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
                    {id ? 'Update Medication' : 'Create Medication'}
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

export default MedicationForm;
