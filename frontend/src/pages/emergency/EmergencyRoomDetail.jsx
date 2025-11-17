import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  Chip,
  Divider,
  Alert,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@mui/material";
import {
  ArrowBack as BackIcon,
  Edit as EditIcon,
  CheckCircle as CheckCircleIcon,
} from "@mui/icons-material";
import emergencyRoomService from "../../services/emergencyRoomService";

function EmergencyRoomDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [openStatusDialog, setOpenStatusDialog] = useState(false);
  const [newStatus, setNewStatus] = useState("");
  const [updating, setUpdating] = useState(false);

  const roomStatusColors = {
    AVAILABLE: "success",
    OCCUPIED: "warning",
    CLEANING: "info",
    MAINTENANCE: "error",
    RESERVED: "default",
  };

  const statusOptions = [
    "AVAILABLE",
    "OCCUPIED",
    "CLEANING",
    "MAINTENANCE",
    "RESERVED",
  ];

  useEffect(() => {
    fetchRoomDetails();
  }, [id]);

  const fetchRoomDetails = async () => {
    try {
      setLoading(true);
      const data = await emergencyRoomService.getEmergencyRoomById(id);
      setRoom(data);
      setNewStatus(data.status);
      setError("");
    } catch (err) {
      setError("Failed to fetch room details");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async () => {
    try {
      setUpdating(true);
      await emergencyRoomService.updateEmergencyRoomStatus(id, newStatus);
      setOpenStatusDialog(false);
      fetchRoomDetails();
      setError("");
    } catch (err) {
      setError("Failed to update room status");
      console.error(err);
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography>Loading room details...</Typography>
      </Box>
    );
  }

  if (!room) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Room not found</Alert>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/emergency")}
          sx={{ mt: 2 }}
        >
          Back to Emergency Dashboard
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
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Button startIcon={<BackIcon />} onClick={() => navigate("/emergency")}>
            Back
          </Button>
          <Typography variant="h4" fontWeight={600}>
            {room.roomNumber} - {room.roomName}
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<EditIcon />}
          onClick={() => setOpenStatusDialog(true)}
        >
          Update Status
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Room Information
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography color="text.secondary" variant="body2">
                  Room Number
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {room.roomNumber}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography color="text.secondary" variant="body2">
                  Room Name
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {room.roomName}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography color="text.secondary" variant="body2">
                  Floor Number
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {room.floorNumber}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography color="text.secondary" variant="body2">
                  Location
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {room.location}
                </Typography>
              </Grid>

              <Grid item xs={6}>
                <Typography color="text.secondary" variant="body2">
                  Status
                </Typography>
                <Chip
                  label={room.status}
                  color={roomStatusColors[room.status]}
                  sx={{ mt: 0.5 }}
                />
              </Grid>

              <Grid item xs={6}>
                <Typography color="text.secondary" variant="body2">
                  Active
                </Typography>
                <Chip
                  icon={<CheckCircleIcon />}
                  label={room.isActive ? "Active" : "Inactive"}
                  color={room.isActive ? "success" : "default"}
                  sx={{ mt: 0.5 }}
                />
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Capacity & Occupancy
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="text.secondary" variant="body2">
                      Total Capacity
                    </Typography>
                    <Typography variant="h4" fontWeight={600}>
                      {room.capacity} {room.capacity === 1 ? "bed" : "beds"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography color="text.secondary" variant="body2">
                      Current Occupancy
                    </Typography>
                    <Typography variant="h4" fontWeight={600}>
                      {room.currentOccupancy} / {room.capacity}
                    </Typography>
                    <Typography
                      variant="body2"
                      color={
                        room.currentOccupancy === room.capacity
                          ? "error"
                          : room.currentOccupancy > 0
                          ? "warning.main"
                          : "success.main"
                      }
                    >
                      {room.currentOccupancy === room.capacity
                        ? "Full"
                        : room.currentOccupancy > 0
                        ? "Partially Occupied"
                        : "Empty"}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Typography color="text.secondary" variant="body2">
                  Last Cleaned
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {room.lastCleanedAt
                    ? new Date(room.lastCleanedAt).toLocaleString()
                    : "Not recorded"}
                </Typography>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Equipment
            </Typography>
            <Divider sx={{ mb: 2 }} />

            {room.equipment ? (
              <List>
                {room.equipment.split(",").map((item, index) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={item.trim()}
                      primaryTypographyProps={{ fontWeight: 500 }}
                    />
                  </ListItem>
                ))}
              </List>
            ) : (
              <Typography color="text.secondary">
                No equipment information available
              </Typography>
            )}
          </Paper>
        </Grid>

        {room.notes && (
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Notes
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body1">{room.notes}</Typography>
            </Paper>
          </Grid>
        )}
      </Grid>

      <Dialog open={openStatusDialog} onClose={() => setOpenStatusDialog(false)}>
        <DialogTitle>Update Room Status</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 2 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={newStatus}
              label="Status"
              onChange={(e) => setNewStatus(e.target.value)}
            >
              {statusOptions.map((status) => (
                <MenuItem key={status} value={status}>
                  {status}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenStatusDialog(false)}>Cancel</Button>
          <Button
            onClick={handleUpdateStatus}
            variant="contained"
            disabled={updating || newStatus === room.status}
          >
            {updating ? "Updating..." : "Update"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default EmergencyRoomDetail;
