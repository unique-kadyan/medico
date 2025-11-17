import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Paper,
  Grid,
  Chip,
  Button,
  Divider,
  Alert,
} from "@mui/material";
import {
  ArrowBack as BackIcon,
} from "@mui/icons-material";
import otRequestService from "../../services/otRequestService";

function OTRequestDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [otRequest, setOtRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const statusColors = {
    PENDING: "warning",
    APPROVED: "info",
    REJECTED: "error",
    IN_PROGRESS: "primary",
    COMPLETED: "success",
    CANCELLED: "default",
  };

  useEffect(() => {
    fetchOTRequest();
  }, [id]);

  const fetchOTRequest = async () => {
    try {
      setLoading(true);
      const data = await otRequestService.getOTRequestById(id);
      setOtRequest(data);
      setError("");
    } catch (err) {
      setError("Failed to fetch OT request details");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <Box>
        <Typography>Loading...</Typography>
      </Box>
    );
  }

  if (error || !otRequest) {
    return (
      <Box>
        <Alert severity="error">{error || "OT Request not found"}</Alert>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/ot-requests")}
          sx={{ mt: 2 }}
        >
          Back to List
        </Button>
      </Box>
    );
  }

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
          OT Request Details
        </Typography>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/ot-requests")}
        >
          Back to List
        </Button>
      </Box>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
              <Typography variant="h6">Status:</Typography>
              <Chip
                label={otRequest.status}
                color={statusColors[otRequest.status]}
                size="medium"
              />
              {otRequest.isEmergency && (
                <Chip label="Emergency" color="error" size="medium" />
              )}
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary">
              Patient
            </Typography>
            <Typography variant="body1" gutterBottom>
              {otRequest.patientName}
            </Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary">
              Surgeon
            </Typography>
            <Typography variant="body1" gutterBottom>
              {otRequest.surgeonName}
            </Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary">
              Surgery Type
            </Typography>
            <Typography variant="body1" gutterBottom>
              {otRequest.surgeryType?.replace(/_/g, " ")}
            </Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary">
              OT Room Number
            </Typography>
            <Typography variant="body1" gutterBottom>
              {otRequest.otRoomNumber || "To Be Assigned"}
            </Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary">
              Scheduled Start Time
            </Typography>
            <Typography variant="body1" gutterBottom>
              {formatDateTime(otRequest.scheduledStartTime)}
            </Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary">
              Estimated Duration
            </Typography>
            <Typography variant="body1" gutterBottom>
              {otRequest.estimatedDuration} minutes
            </Typography>
          </Grid>

          {otRequest.actualStartTime && (
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle2" color="text.secondary">
                Actual Start Time
              </Typography>
              <Typography variant="body1" gutterBottom>
                {formatDateTime(otRequest.actualStartTime)}
              </Typography>
            </Grid>
          )}

          {otRequest.actualEndTime && (
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle2" color="text.secondary">
                Actual End Time
              </Typography>
              <Typography variant="body1" gutterBottom>
                {formatDateTime(otRequest.actualEndTime)}
              </Typography>
            </Grid>
          )}

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary">
              Surgery Purpose
            </Typography>
            <Typography variant="body1" gutterBottom>
              {otRequest.surgeryPurpose}
            </Typography>
          </Grid>

          {otRequest.anesthesiaType && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Anesthesia Type
              </Typography>
              <Typography variant="body1" gutterBottom>
                {otRequest.anesthesiaType}
              </Typography>
            </Grid>
          )}

          {otRequest.requiredInstruments && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Required Instruments
              </Typography>
              <Typography
                variant="body1"
                gutterBottom
                sx={{ whiteSpace: "pre-line" }}
              >
                {otRequest.requiredInstruments}
              </Typography>
            </Grid>
          )}

          {otRequest.requiredMedications && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Required Medications
              </Typography>
              <Typography
                variant="body1"
                gutterBottom
                sx={{ whiteSpace: "pre-line" }}
              >
                {otRequest.requiredMedications}
              </Typography>
            </Grid>
          )}

          {otRequest.surgeryNotes && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Surgery Notes
              </Typography>
              <Typography
                variant="body1"
                gutterBottom
                sx={{ whiteSpace: "pre-line" }}
              >
                {otRequest.surgeryNotes}
              </Typography>
            </Grid>
          )}

          {otRequest.approvalNotes && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Approval Notes
              </Typography>
              <Typography
                variant="body1"
                gutterBottom
                sx={{ whiteSpace: "pre-line" }}
              >
                {otRequest.approvalNotes}
              </Typography>
            </Grid>
          )}

          {otRequest.rejectionReason && (
            <Grid item xs={12}>
              <Alert severity="error">
                <Typography variant="subtitle2">Rejection Reason</Typography>
                <Typography variant="body2">
                  {otRequest.rejectionReason}
                </Typography>
              </Alert>
            </Grid>
          )}

          {otRequest.postOperativeNotes && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Post-Operative Notes
              </Typography>
              <Typography
                variant="body1"
                gutterBottom
                sx={{ whiteSpace: "pre-line" }}
              >
                {otRequest.postOperativeNotes}
              </Typography>
            </Grid>
          )}

          {otRequest.approvedBy && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" color="text.secondary">
                Approved By
              </Typography>
              <Typography variant="body1" gutterBottom>
                {otRequest.approvedBy} at {formatDateTime(otRequest.approvedAt)}
              </Typography>
            </Grid>
          )}

          <Grid item xs={12}>
            <Typography variant="caption" color="text.secondary">
              Created: {formatDateTime(otRequest.createdAt)} | Last Updated:{" "}
              {formatDateTime(otRequest.updatedAt)}
            </Typography>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
}

export default OTRequestDetail;
