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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  MenuItem,
} from '@mui/material';
import {
  Add as AddIcon,
  CloudUpload as UploadIcon,
  Visibility as ViewIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { toast } from 'sonner';
import { useSelector } from 'react-redux';
import labTestService from '../../services/labTestService';
import patientService from '../../services/patientService';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';

function LabTestList() {
  const { can } = usePermissions();
  const user = useSelector((state) => state.auth.user);
  const [labTests, setLabTests] = useState([]);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [orderDialogOpen, setOrderDialogOpen] = useState(false);
  const [selectedTest, setSelectedTest] = useState(null);
  const [uploadData, setUploadData] = useState({
    testResults: '',
    resultFilePath: '',
    remarks: '',
  });
  const [orderData, setOrderData] = useState({
    patientId: '',
    testType: '',
    testDate: new Date().toISOString().split('T')[0],
    description: '',
  });

  useEffect(() => {
    fetchLabTests();
    fetchPatients();
  }, []);

  const fetchLabTests = async () => {
    try {
      setLoading(true);
      const data = await labTestService.getAllLabTests();
      setLabTests(data);
    } catch (error) {
      toast.error('Failed to load lab tests');
      console.error('Error fetching lab tests:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchPatients = async () => {
    try {
      const data = await patientService.getAllPatients();
      setPatients(data);
    } catch (error) {
      console.error('Error fetching patients:', error);
    }
  };

  const handleOrderTest = async () => {
    if (!orderData.patientId || !orderData.testType) {
      toast.error('Please fill in all required fields');
      return;
    }

    try {
      const testData = {
        ...orderData,
        doctorId: user.userId, // Current logged-in doctor
        status: 'PENDING',
      };
      await labTestService.orderLabTest(testData);
      toast.success('Lab test ordered successfully');
      setOrderDialogOpen(false);
      setOrderData({
        patientId: '',
        testType: '',
        testDate: new Date().toISOString().split('T')[0],
        description: '',
      });
      fetchLabTests();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to order lab test');
      console.error('Error ordering lab test:', error);
    }
  };

  const handleUploadClick = (test) => {
    setSelectedTest(test);
    setUploadData({
      testResults: test.testResults || '',
      resultFilePath: test.resultFilePath || '',
      remarks: test.remarks || '',
    });
    setUploadDialogOpen(true);
  };

  const handleUploadSubmit = async () => {
    if (!uploadData.testResults.trim()) {
      toast.error('Please enter test results');
      return;
    }

    try {
      await labTestService.uploadResults(selectedTest.id, uploadData);
      toast.success('Results uploaded successfully');
      setUploadDialogOpen(false);
      setSelectedTest(null);
      setUploadData({ testResults: '', resultFilePath: '', remarks: '' });
      fetchLabTests();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to upload results');
      console.error('Error uploading results:', error);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this lab test?')) {
      return;
    }

    try {
      await labTestService.deleteLabTest(id);
      toast.success('Lab test deleted successfully');
      fetchLabTests();
    } catch (error) {
      toast.error('Failed to delete lab test');
      console.error('Error deleting lab test:', error);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'IN_PROGRESS':
        return 'info';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
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
          Laboratory Tests
        </Typography>
        {can(PERMISSIONS.ORDER_LAB_TEST) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOrderDialogOpen(true)}
          >
            Order Test
          </Button>
        )}
      </Box>

      <Card>
        <CardContent>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>Test ID</strong></TableCell>
                  <TableCell><strong>Patient</strong></TableCell>
                  <TableCell><strong>Test Type</strong></TableCell>
                  <TableCell><strong>Ordered By</strong></TableCell>
                  <TableCell><strong>Test Date</strong></TableCell>
                  <TableCell><strong>Status</strong></TableCell>
                  <TableCell align="center"><strong>Actions</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {labTests.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No lab tests found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  labTests.map((test) => (
                    <TableRow key={test.id}>
                      <TableCell>#{test.id}</TableCell>
                      <TableCell>
                        {test.patientName || `Patient ID: ${test.patientId}`}
                      </TableCell>
                      <TableCell>{test.testType}</TableCell>
                      <TableCell>
                        {test.doctorName || `Doctor ID: ${test.doctorId}`}
                      </TableCell>
                      <TableCell>
                        {test.testDate ? new Date(test.testDate).toLocaleDateString() : '-'}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={test.status || 'PENDING'}
                          size="small"
                          color={getStatusColor(test.status)}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Box display="flex" gap={0.5} justifyContent="center">
                          <IconButton size="small" color="primary">
                            <ViewIcon />
                          </IconButton>
                          {can(PERMISSIONS.UPLOAD_LAB_RESULTS) && (
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => handleUploadClick(test)}
                              title="Upload Results"
                            >
                              <UploadIcon />
                            </IconButton>
                          )}
                          {can(PERMISSIONS.DELETE_LAB_TEST) && (
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => handleDelete(test.id)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          )}
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Upload Results Dialog */}
      <Dialog
        open={uploadDialogOpen}
        onClose={() => setUploadDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <UploadIcon />
            Upload Test Results - {selectedTest?.testType}
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={2}>
            <Typography variant="body2" color="text.secondary">
              Patient: {selectedTest?.patientName || `ID: ${selectedTest?.patientId}`}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Test Date: {selectedTest?.testDate ? new Date(selectedTest.testDate).toLocaleDateString() : 'N/A'}
            </Typography>

            <TextField
              fullWidth
              multiline
              rows={4}
              label="Test Results"
              value={uploadData.testResults}
              onChange={(e) => setUploadData({ ...uploadData, testResults: e.target.value })}
              placeholder="Enter detailed test results..."
              required
            />

            <TextField
              fullWidth
              label="Result File Path/URL"
              value={uploadData.resultFilePath}
              onChange={(e) => setUploadData({ ...uploadData, resultFilePath: e.target.value })}
              placeholder="e.g., /reports/lab/test-result-123.pdf"
            />

            <TextField
              fullWidth
              multiline
              rows={2}
              label="Remarks"
              value={uploadData.remarks}
              onChange={(e) => setUploadData({ ...uploadData, remarks: e.target.value })}
              placeholder="Any additional remarks or notes..."
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUploadDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleUploadSubmit} variant="contained" startIcon={<UploadIcon />}>
            Upload Results
          </Button>
        </DialogActions>
      </Dialog>

      {/* Order Test Dialog */}
      <Dialog
        open={orderDialogOpen}
        onClose={() => setOrderDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <AddIcon />
            Order Lab Test
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={2}>
            <TextField
              select
              fullWidth
              label="Select Patient"
              value={orderData.patientId}
              onChange={(e) => setOrderData({ ...orderData, patientId: e.target.value })}
              required
            >
              {patients.map((patient) => (
                <MenuItem key={patient.id} value={patient.id}>
                  {patient.name || `${patient.firstName || ''} ${patient.lastName || ''}`.trim()} - {patient.email}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              fullWidth
              label="Test Type"
              value={orderData.testType}
              onChange={(e) => setOrderData({ ...orderData, testType: e.target.value })}
              placeholder="e.g., Blood Test, X-Ray, MRI, CT Scan..."
              required
            />

            <TextField
              fullWidth
              type="date"
              label="Test Date"
              value={orderData.testDate}
              onChange={(e) => setOrderData({ ...orderData, testDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />

            <TextField
              fullWidth
              multiline
              rows={3}
              label="Description/Notes"
              value={orderData.description}
              onChange={(e) => setOrderData({ ...orderData, description: e.target.value })}
              placeholder="Any special instructions or notes for the lab technician..."
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOrderDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleOrderTest} variant="contained" startIcon={<AddIcon />}>
            Order Test
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default LabTestList;
