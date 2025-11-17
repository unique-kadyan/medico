import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Button,
  Alert,
} from "@mui/material";
import {
  PersonAdd as AdmitIcon,
  Visibility as ViewIcon,
  LocalHospital as HospitalIcon,
  EmergencyShare as EmergencyIcon,
  CheckCircle as AvailableIcon,
} from "@mui/icons-material";
import emergencyRoomService from "../../services/emergencyRoomService";
import emergencyPatientService from "../../services/emergencyPatientService";

function EmergencyDashboard() {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [currentPatients, setCurrentPatients] = useState([]);
  const [criticalPatients, setCriticalPatients] = useState([]);
  const [stats, setStats] = useState({ available: 0, occupied: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const roomStatusColors = {
    AVAILABLE: "success",
    OCCUPIED: "warning",
    CLEANING: "info",
    MAINTENANCE: "error",
    RESERVED: "default",
  };

  const conditionColors = {
    CRITICAL: "error",
    SERIOUS: "warning",
    STABLE: "success",
    IMPROVING: "info",
    DISCHARGED: "default",
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [
        roomsData,
        patientsData,
        criticalData,
        availableCount,
        occupiedCount,
      ] = await Promise.all([
        emergencyRoomService.getActiveEmergencyRooms(),
        emergencyPatientService.getCurrentPatients(),
        emergencyPatientService.getPatientsByCondition("CRITICAL"),
        emergencyRoomService.getAvailableRoomCount(),
        emergencyRoomService.getOccupiedRoomCount(),
      ]);

      setRooms(roomsData);
      setCurrentPatients(patientsData);
      setCriticalPatients(criticalData);
      setStats({
        available: availableCount.availableCount,
        occupied: occupiedCount.occupiedCount,
      });
      setError("");
    } catch (err) {
      setError("Failed to fetch emergency room data");
      console.error(err);
    } finally {
      setLoading(false);
    }
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
          Emergency Room Dashboard
        </Typography>
        <Button
          variant="contained"
          startIcon={<AdmitIcon />}
          onClick={() => navigate("/emergency/admit")}
        >
          Admit Patient
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      {criticalPatients.length > 0 && (
        <Alert severity="error" sx={{ mb: 3 }}>
          <Typography variant="subtitle1" fontWeight={600}>
            {criticalPatients.length} Critical Patient(s) Requiring Immediate
            Attention
          </Typography>
        </Alert>
      )}

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Box>
                  <Typography color="text.secondary" variant="body2">
                    Available Rooms
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {stats.available}
                  </Typography>
                </Box>
                <AvailableIcon sx={{ fontSize: 48, color: "success.main" }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Box>
                  <Typography color="text.secondary" variant="body2">
                    Occupied Rooms
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {stats.occupied}
                  </Typography>
                </Box>
                <HospitalIcon sx={{ fontSize: 48, color: "warning.main" }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Box>
                  <Typography color="text.secondary" variant="body2">
                    Current Patients
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {currentPatients.length}
                  </Typography>
                </Box>
                <AdmitIcon sx={{ fontSize: 48, color: "primary.main" }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Box>
                  <Typography color="text.secondary" variant="body2">
                    Critical Patients
                  </Typography>
                  <Typography variant="h4" fontWeight={600}>
                    {criticalPatients.length}
                  </Typography>
                </Box>
                <EmergencyIcon sx={{ fontSize: 48, color: "error.main" }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Room Status
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Room Number</TableCell>
                    <TableCell>Floor</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Occupancy</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        Loading...
                      </TableCell>
                    </TableRow>
                  ) : rooms.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        No emergency rooms found
                      </TableCell>
                    </TableRow>
                  ) : (
                    rooms.map((room) => (
                      <TableRow key={room.id}>
                        <TableCell>{room.roomNumber}</TableCell>
                        <TableCell>{room.floorNumber}</TableCell>
                        <TableCell>
                          <Chip
                            label={room.status}
                            color={roomStatusColors[room.status]}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          {room.currentOccupancy} / {room.capacity}
                        </TableCell>
                        <TableCell>
                          <IconButton
                            size="small"
                            onClick={() =>
                              navigate(`/emergency/rooms/${room.id}`)
                            }
                            title="View Room"
                          >
                            <ViewIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} lg={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Current Patients (By Triage Priority)
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Patient</TableCell>
                    <TableCell>Room</TableCell>
                    <TableCell>Triage</TableCell>
                    <TableCell>Condition</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        Loading...
                      </TableCell>
                    </TableRow>
                  ) : currentPatients.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        No patients currently in emergency
                      </TableCell>
                    </TableRow>
                  ) : (
                    currentPatients.map((patient) => (
                      <TableRow key={patient.id}>
                        <TableCell>{patient.patientName}</TableCell>
                        <TableCell>{patient.emergencyRoomNumber}</TableCell>
                        <TableCell>
                          <Chip
                            label={`Level ${patient.triageLevel}`}
                            color={
                              patient.triageLevel <= 2 ? "error" : "warning"
                            }
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={patient.condition}
                            color={conditionColors[patient.condition]}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <IconButton
                            size="small"
                            onClick={() =>
                              navigate(`/emergency/patients/${patient.id}`)
                            }
                            title="View Patient"
                          >
                            <ViewIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}

export default EmergencyDashboard;
