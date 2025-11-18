import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  IconButton,
  TextField,
  MenuItem,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from "@mui/material";
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  PlayArrow as StartIcon,
  Done as CompleteIcon,
} from "@mui/icons-material";
import { useSelector } from "react-redux";
import otRequestService from "../../services/otRequestService";

function OTRequestList() {
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const [otRequests, setOtRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filterStatus, setFilterStatus] = useState("ALL");
  const [actionDialog, setActionDialog] = useState({
    open: false,
    type: "",
    requestId: null,
  });
  const [actionNotes, setActionNotes] = useState("");

  const statusColors = {
    PENDING: "warning",
    APPROVED: "info",
    REJECTED: "error",
    IN_PROGRESS: "primary",
    COMPLETED: "success",
    CANCELLED: "default",
  };

  const fetchOTRequests = useCallback(async () => {
    try {
      setLoading(true);
      let data;
      if (filterStatus === "ALL") {
        data = await otRequestService.getAllOTRequests();
      } else {
        data = await otRequestService.getOTRequestsByStatus(filterStatus);
      }
      setOtRequests(data);
      setError("");
    } catch (err) {
      setError("Failed to fetch OT requests");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [filterStatus]);

  useEffect(() => {
    fetchOTRequests();
  }, [fetchOTRequests]);

  const handleAction = (type, requestId) => {
    setActionDialog({ open: true, type, requestId });
    setActionNotes("");
  };

  const handleCloseDialog = () => {
    setActionDialog({ open: false, type: "", requestId: null });
    setActionNotes("");
  };

  const handleConfirmAction = async () => {
    try {
      const { type, requestId } = actionDialog;

      switch (type) {
        case "approve":
          await otRequestService.approveOTRequest(requestId, actionNotes);
          break;
        case "reject":
          if (!actionNotes.trim()) {
            setError("Rejection reason is required");
            return;
          }
          await otRequestService.rejectOTRequest(requestId, actionNotes);
          break;
        case "start":
          await otRequestService.startSurgery(requestId);
          break;
        case "complete":
          await otRequestService.completeSurgery(requestId, actionNotes);
          break;
        default:
          break;
      }

      handleCloseDialog();
      fetchOTRequests();
    } catch (err) {
      setError(`Failed to ${actionDialog.type} OT request`);
      console.error(err);
    }
  };

  const canApprove = user?.role === "ADMIN" || user?.role === "NURSE";
  const canStart =
    user?.role === "ADMIN" || user?.role === "NURSE" || user?.role === "DOCTOR";
  const canComplete = user?.role === "ADMIN" || user?.role === "DOCTOR";

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString();
  };

  return (
    <Box>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 3,
        }}
      >
        <Typography variant="h4" fontWeight={600}>
          OT Requests
        </Typography>
        {user?.role === "DOCTOR" && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/ot-requests/new")}
          >
            New OT Request
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <TextField
            select
            fullWidth
            label="Filter by Status"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <MenuItem value="ALL">All Requests</MenuItem>
            <MenuItem value="PENDING">Pending</MenuItem>
            <MenuItem value="APPROVED">Approved</MenuItem>
            <MenuItem value="REJECTED">Rejected</MenuItem>
            <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
            <MenuItem value="COMPLETED">Completed</MenuItem>
            <MenuItem value="CANCELLED">Cancelled</MenuItem>
          </TextField>
        </Grid>
      </Grid>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Patient</TableCell>
              <TableCell>Surgeon</TableCell>
              <TableCell>Surgery Type</TableCell>
              <TableCell>Scheduled Time</TableCell>
              <TableCell>OT Room</TableCell>
              <TableCell>Emergency</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  Loading...
                </TableCell>
              </TableRow>
            ) : otRequests.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  No OT requests found
                </TableCell>
              </TableRow>
            ) : (
              otRequests.map((request) => (
                <TableRow key={request.id}>
                  <TableCell>{request.patientName}</TableCell>
                  <TableCell>{request.surgeonName}</TableCell>
                  <TableCell>{request.surgeryType}</TableCell>
                  <TableCell>
                    {formatDateTime(request.scheduledStartTime)}
                  </TableCell>
                  <TableCell>{request.otRoomNumber || "TBA"}</TableCell>
                  <TableCell>
                    {request.isEmergency ? (
                      <Chip label="Emergency" color="error" size="small" />
                    ) : (
                      <Chip label="Scheduled" color="default" size="small" />
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={request.status}
                      color={statusColors[request.status]}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: "flex", gap: 1 }}>
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/ot-requests/${request.id}`)}
                        title="View Details"
                      >
                        <ViewIcon />
                      </IconButton>

                      {request.status === "PENDING" && canApprove && (
                        <>
                          <IconButton
                            size="small"
                            color="success"
                            onClick={() => handleAction("approve", request.id)}
                            title="Approve"
                          >
                            <ApproveIcon />
                          </IconButton>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleAction("reject", request.id)}
                            title="Reject"
                          >
                            <RejectIcon />
                          </IconButton>
                        </>
                      )}

                      {request.status === "APPROVED" && canStart && (
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleAction("start", request.id)}
                          title="Start Surgery"
                        >
                          <StartIcon />
                        </IconButton>
                      )}

                      {request.status === "IN_PROGRESS" && canComplete && (
                        <IconButton
                          size="small"
                          color="success"
                          onClick={() => handleAction("complete", request.id)}
                          title="Complete Surgery"
                        >
                          <CompleteIcon />
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

      <Dialog
        open={actionDialog.open}
        onClose={handleCloseDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {actionDialog.type === "approve" && "Approve OT Request"}
          {actionDialog.type === "reject" && "Reject OT Request"}
          {actionDialog.type === "start" && "Start Surgery"}
          {actionDialog.type === "complete" && "Complete Surgery"}
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            multiline
            rows={4}
            label={
              actionDialog.type === "reject"
                ? "Rejection Reason (Required)"
                : actionDialog.type === "complete"
                  ? "Post-Operative Notes"
                  : "Notes (Optional)"
            }
            value={actionNotes}
            onChange={(e) => setActionNotes(e.target.value)}
            margin="normal"
            required={actionDialog.type === "reject"}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleConfirmAction} variant="contained">
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default OTRequestList;
