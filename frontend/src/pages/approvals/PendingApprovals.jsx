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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid,
  IconButton,
  Tabs,
  Tab,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Visibility as ViewIcon,
  Description as DocumentIcon,
} from '@mui/icons-material';
import pendingUserService from '../../services/pendingUserService';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';

const PendingApprovals = () => {
  const { can } = usePermissions();

  const [pendingUsers, setPendingUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [currentTab, setCurrentTab] = useState(0);
  const [successMessage, setSuccessMessage] = useState('');

  const roleFilters = [
    { label: 'All', value: 'ALL' },
    { label: 'Doctors', value: 'DOCTOR' },
    { label: 'Doctor Supervisors', value: 'DOCTOR_SUPERVISOR' },
    { label: 'Nurses', value: 'NURSE' },
    { label: 'Nurse Managers', value: 'NURSE_MANAGER' },
    { label: 'Nurse Supervisors', value: 'NURSE_SUPERVISOR' },
    { label: 'Receptionists', value: 'RECEPTIONIST' },
  ];

  useEffect(() => {
    fetchPendingUsers();
  }, []);

  useEffect(() => {
    filterUsers();
  }, [currentTab, pendingUsers]);

  const fetchPendingUsers = async () => {
    try {
      setLoading(true);
      const data = await pendingUserService.getAllPendingUsers();
      setPendingUsers(data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch pending registrations');
    } finally {
      setLoading(false);
    }
  };

  const filterUsers = () => {
    const selectedFilter = roleFilters[currentTab].value;
    if (selectedFilter === 'ALL') {
      setFilteredUsers(pendingUsers);
    } else {
      setFilteredUsers(pendingUsers.filter(user => user.requestedRole === selectedFilter));
    }
  };

  const handleViewDetails = (user) => {
    setSelectedUser(user);
    setViewDialogOpen(true);
  };

  const handleApprove = async (userId) => {
    try {
      const result = await pendingUserService.approveRegistration(userId);
      setSuccessMessage(result.message || 'Registration approved successfully');
      fetchPendingUsers();
      setViewDialogOpen(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to approve registration');
    }
  };

  const handleRejectClick = (user) => {
    setSelectedUser(user);
    setRejectionReason('');
    setRejectDialogOpen(true);
  };

  const handleRejectConfirm = async () => {
    if (!rejectionReason.trim()) {
      setError('Rejection reason is required');
      return;
    }

    try {
      const result = await pendingUserService.rejectRegistration(selectedUser.id, rejectionReason);
      setSuccessMessage(result.message || 'Registration rejected successfully');
      fetchPendingUsers();
      setRejectDialogOpen(false);
      setViewDialogOpen(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reject registration');
    }
  };

  const getRoleChipColor = (role) => {
    switch (role) {
      case 'DOCTOR':
      case 'DOCTOR_SUPERVISOR':
        return 'primary';
      case 'NURSE':
      case 'NURSE_MANAGER':
      case 'NURSE_SUPERVISOR':
        return 'secondary';
      case 'RECEPTIONIST':
        return 'info';
      default:
        return 'default';
    }
  };

  const formatRoleName = (role) => {
    return role.split('_').map(word =>
      word.charAt(0) + word.slice(1).toLowerCase()
    ).join(' ');
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  if (!can(PERMISSIONS.VIEW_PENDING_REGISTRATIONS)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          You don&apos;t have permission to view pending registrations
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Pending Registration Approvals
      </Typography>

      {successMessage && (
        <Alert severity="success" onClose={() => setSuccessMessage('')} sx={{ mb: 2 }}>
          {successMessage}
        </Alert>
      )}

      {error && (
        <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Card>
        <CardContent>
          <Tabs
            value={currentTab}
            onChange={(e, newValue) => setCurrentTab(newValue)}
            variant="scrollable"
            scrollButtons="auto"
            sx={{ mb: 2 }}
          >
            {roleFilters.map((filter) => (
              <Tab key={filter.value} label={filter.label} />
            ))}
          </Tabs>

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : filteredUsers.length === 0 ? (
            <Typography variant="body1" color="textSecondary" align="center" sx={{ p: 4 }}>
              No pending registrations found
            </Typography>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Username</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Phone</TableCell>
                    <TableCell>Requested Role</TableCell>
                    <TableCell>Documents</TableCell>
                    <TableCell>Requested At</TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredUsers.map((user) => (
                    <TableRow key={user.id} hover>
                      <TableCell>{user.username}</TableCell>
                      <TableCell>
                        {user.firstName} {user.lastName}
                      </TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>{user.phone || 'N/A'}</TableCell>
                      <TableCell>
                        <Chip
                          label={formatRoleName(user.requestedRole)}
                          color={getRoleChipColor(user.requestedRole)}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          icon={<DocumentIcon />}
                          label={user.documentCount}
                          size="small"
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>{formatDate(user.requestedAt)}</TableCell>
                      <TableCell align="center">
                        <IconButton
                          color="info"
                          size="small"
                          onClick={() => handleViewDetails(user)}
                          title="View Details"
                        >
                          <ViewIcon />
                        </IconButton>
                        {can(PERMISSIONS.APPROVE_DOCTOR_REGISTRATION) ||
                         can(PERMISSIONS.APPROVE_NURSE_REGISTRATION) ||
                         can(PERMISSIONS.APPROVE_RECEPTIONIST_REGISTRATION) ||
                         can(PERMISSIONS.APPROVE_SUPERVISOR_REGISTRATION) ? (
                          <IconButton
                            color="success"
                            size="small"
                            onClick={() => handleApprove(user.id)}
                            title="Approve"
                          >
                            <ApproveIcon />
                          </IconButton>
                        ) : null}
                        {can(PERMISSIONS.REJECT_REGISTRATION) && (
                          <IconButton
                            color="error"
                            size="small"
                            onClick={() => handleRejectClick(user)}
                            title="Reject"
                          >
                            <RejectIcon />
                          </IconButton>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* View Details Dialog */}
      <Dialog
        open={viewDialogOpen}
        onClose={() => setViewDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Registration Details</DialogTitle>
        <DialogContent>
          {selectedUser && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Username
                </Typography>
                <Typography variant="body1">{selectedUser.username}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Email
                </Typography>
                <Typography variant="body1">{selectedUser.email}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  First Name
                </Typography>
                <Typography variant="body1">{selectedUser.firstName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Last Name
                </Typography>
                <Typography variant="body1">{selectedUser.lastName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Phone
                </Typography>
                <Typography variant="body1">{selectedUser.phone || 'N/A'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Requested Role
                </Typography>
                <Chip
                  label={formatRoleName(selectedUser.requestedRole)}
                  color={getRoleChipColor(selectedUser.requestedRole)}
                  size="small"
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="body2" color="textSecondary">
                  Requested At
                </Typography>
                <Typography variant="body1">{formatDate(selectedUser.requestedAt)}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  Uploaded Documents ({selectedUser.documentCount})
                </Typography>
                {selectedUser.documents && selectedUser.documents.map((doc) => (
                  <Chip
                    key={doc.id}
                    icon={<DocumentIcon />}
                    label={doc.fileName}
                    size="small"
                    sx={{ mr: 1, mb: 1 }}
                  />
                ))}
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          {can(PERMISSIONS.REJECT_REGISTRATION) && (
            <Button
              onClick={() => {
                setRejectDialogOpen(true);
                setViewDialogOpen(false);
              }}
              color="error"
              startIcon={<RejectIcon />}
            >
              Reject
            </Button>
          )}
          {(can(PERMISSIONS.APPROVE_DOCTOR_REGISTRATION) ||
            can(PERMISSIONS.APPROVE_NURSE_REGISTRATION) ||
            can(PERMISSIONS.APPROVE_RECEPTIONIST_REGISTRATION) ||
            can(PERMISSIONS.APPROVE_SUPERVISOR_REGISTRATION)) && (
            <Button
              onClick={() => handleApprove(selectedUser.id)}
              color="success"
              variant="contained"
              startIcon={<ApproveIcon />}
            >
              Approve
            </Button>
          )}
          <Button onClick={() => setViewDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Reject Dialog */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Reject Registration</DialogTitle>
        <DialogContent>
          <Typography variant="body2" gutterBottom>
            Please provide a reason for rejecting this registration:
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={4}
            value={rejectionReason}
            onChange={(e) => setRejectionReason(e.target.value)}
            placeholder="Enter rejection reason..."
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleRejectConfirm}
            color="error"
            variant="contained"
            disabled={!rejectionReason.trim()}
          >
            Confirm Rejection
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PendingApprovals;
