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
} from '@mui/material';
import {
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { toast } from 'sonner';
import { useSelector } from 'react-redux';
import api from '../../services/api';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';

function MedicationRequestList() {
  const { can } = usePermissions();
  const user = useSelector((state) => state.auth.user);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [reviewAction, setReviewAction] = useState('');
  const [reviewNotes, setReviewNotes] = useState('');

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const response = await api.get('/medication-requests');
      setRequests(response.data);
    } catch (error) {
      toast.error('Failed to load medication requests');
      console.error('Error fetching medication requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleReviewClick = (request, action) => {
    setSelectedRequest(request);
    setReviewAction(action);
    setReviewNotes('');
    setReviewDialogOpen(true);
  };

  const handleReviewSubmit = async () => {
    try {
      const endpoint = reviewAction === 'approve' ? 'approve' : 'reject';
      await api.put(`/medication-requests/${selectedRequest.id}/${endpoint}`, {
        reviewerId: user.userId,
        reviewNotes: reviewNotes,
      });
      toast.success(`Request ${reviewAction}d successfully`);
      setReviewDialogOpen(false);
      setSelectedRequest(null);
      setReviewNotes('');
      fetchRequests();
    } catch (error) {
      toast.error(`Failed to ${reviewAction} request`);
      console.error(`Error ${reviewAction}ing request:`, error);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'APPROVED':
        return 'success';
      case 'REJECTED':
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
          Medication Requests
        </Typography>
      </Box>

      <Card>
        <CardContent>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>Request ID</strong></TableCell>
                  <TableCell><strong>Medication</strong></TableCell>
                  <TableCell><strong>Patient</strong></TableCell>
                  <TableCell><strong>Doctor</strong></TableCell>
                  <TableCell><strong>Quantity</strong></TableCell>
                  <TableCell><strong>Request Date</strong></TableCell>
                  <TableCell><strong>Status</strong></TableCell>
                  <TableCell align="center"><strong>Actions</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {requests.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
                        No medication requests found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  requests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell>#{request.id}</TableCell>
                      <TableCell>
                        {request.medicationName || `Med ID: ${request.medicationId}`}
                      </TableCell>
                      <TableCell>
                        {request.patientName || `Patient ID: ${request.patientId}`}
                      </TableCell>
                      <TableCell>
                        {request.doctorName || `Doctor ID: ${request.requestedBy}`}
                      </TableCell>
                      <TableCell>{request.quantity}</TableCell>
                      <TableCell>
                        {request.requestDate ? new Date(request.requestDate).toLocaleDateString() : '-'}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={request.status || 'PENDING'}
                          size="small"
                          color={getStatusColor(request.status)}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Box display="flex" gap={0.5} justifyContent="center">
                          <IconButton size="small" color="primary">
                            <ViewIcon />
                          </IconButton>
                          {can(PERMISSIONS.APPROVE_MEDICATION_REQUEST) && request.status === 'PENDING' && (
                            <>
                              <IconButton
                                size="small"
                                color="success"
                                onClick={() => handleReviewClick(request, 'approve')}
                                title="Approve"
                              >
                                <ApproveIcon />
                              </IconButton>
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() => handleReviewClick(request, 'reject')}
                                title="Reject"
                              >
                                <RejectIcon />
                              </IconButton>
                            </>
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

      {/* Review Dialog */}
      <Dialog
        open={reviewDialogOpen}
        onClose={() => setReviewDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {reviewAction === 'approve' ? 'Approve' : 'Reject'} Medication Request
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={2}>
            <Typography variant="body2">
              <strong>Medication:</strong> {selectedRequest?.medicationName || `ID: ${selectedRequest?.medicationId}`}
            </Typography>
            <Typography variant="body2">
              <strong>Quantity:</strong> {selectedRequest?.quantity}
            </Typography>
            <Typography variant="body2">
              <strong>Patient:</strong> {selectedRequest?.patientName || `ID: ${selectedRequest?.patientId}`}
            </Typography>

            <TextField
              fullWidth
              multiline
              rows={4}
              label="Review Notes"
              value={reviewNotes}
              onChange={(e) => setReviewNotes(e.target.value)}
              placeholder="Enter review notes or reason..."
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReviewDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleReviewSubmit}
            variant="contained"
            color={reviewAction === 'approve' ? 'success' : 'error'}
            startIcon={reviewAction === 'approve' ? <ApproveIcon /> : <RejectIcon />}
          >
            {reviewAction === 'approve' ? 'Approve' : 'Reject'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default MedicationRequestList;
