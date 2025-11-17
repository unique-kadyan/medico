import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  CircularProgress,
} from "@mui/material";
import {
  TrendingUp,
  People,
  LocalHospital,
  CalendarMonth,
  Medication,
  Visibility,
} from "@mui/icons-material";
import {
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { useNavigate } from "react-router-dom";
import patientService from "../../services/patientService";
import appointmentService from "../../services/appointmentService";
import dashboardService from "../../services/dashboardService";
import { usePermissions } from "../../hooks/usePermissions";
import { PERMISSIONS } from "../../utils/permissions";

const COLORS = ["#2196f3", "#4caf50", "#ff9800", "#f44336", "#9c27b0"];

function Dashboard() {
  const navigate = useNavigate();
  const { can, canAny } = usePermissions();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalPatients: 0,
    totalDoctors: 0,
    todayAppointments: 0,
    lowStockMedications: 0,
  });
  const [recentPatients, setRecentPatients] = useState([]);
  const [upcomingAppointments, setUpcomingAppointments] = useState([]);
  const [monthlyPatients, setMonthlyPatients] = useState([]);
  const [appointmentsByType, setAppointmentsByType] = useState([]);

  const fetchDashboardData = useCallback(async () => {
    try {
      const promises = [];
      const promiseTypes = [];

      if (can(PERMISSIONS.VIEW_SYSTEM_STATS)) {
        promises.push(dashboardService.getDashboardStats());
        promiseTypes.push("stats");
      }

      if (
        canAny([
          PERMISSIONS.VIEW_ALL_PATIENTS,
          PERMISSIONS.VIEW_ASSIGNED_PATIENTS,
        ])
      ) {
        promises.push(patientService.getAllPatients({ limit: 5 }));
        promiseTypes.push("patients");
      }

      if (can(PERMISSIONS.VIEW_APPOINTMENTS)) {
        promises.push(appointmentService.getTodaysAppointments());
        promiseTypes.push("appointments");
      }

      const results = await Promise.all(promises);

      results.forEach((result, index) => {
        const type = promiseTypes[index];

        if (type === "stats") {
          setStats({
            totalPatients: result.totalPatients || 0,
            totalDoctors: result.totalDoctors || 0,
            todayAppointments: result.todayAppointments || 0,
            lowStockMedications: result.lowStockMedications || 0,
          });
          setMonthlyPatients(result.monthlyPatients || []);
          setAppointmentsByType(result.appointmentsByType || []);
        } else if (type === "patients") {
          setRecentPatients(result.slice(0, 5) || []);
        } else if (type === "appointments") {
          setUpcomingAppointments(result.slice(0, 5) || []);
        }
      });
    } catch (error) {
      // Silently handle errors to avoid showing permission errors to user
      console.error("Error fetching dashboard data:", error);
    } finally {
      setLoading(false);
    }
  }, [can, canAny]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const StatCard = ({ title, value, icon, color, trend }) => (
    <Card sx={{ height: "100%", position: "relative", overflow: "visible" }}>
      <CardContent>
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
          }}
        >
          <Box>
            <Typography color="text.secondary" variant="body2" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4" fontWeight={700}>
              {value}
            </Typography>
            {trend && (
              <Box sx={{ display: "flex", alignItems: "center", mt: 1 }}>
                <TrendingUp
                  sx={{ fontSize: 16, color: "success.main", mr: 0.5 }}
                />
                <Typography variant="caption" color="success.main">
                  {trend}
                </Typography>
              </Box>
            )}
          </Box>
          <Box
            sx={{
              width: 60,
              height: 60,
              borderRadius: 2,
              bgcolor: `${color}.50`,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "60vh",
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Welcome back! Here&apos;s what&apos;s happening today.
        </Typography>
      </Box>

      {can(PERMISSIONS.VIEW_SYSTEM_STATS) && (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard
              title="Total Patients"
              value={stats.totalPatients}
              icon={<People sx={{ fontSize: 32, color: "primary.main" }} />}
              color="primary"
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard
              title="Total Doctors"
              value={stats.totalDoctors}
              icon={
                <LocalHospital sx={{ fontSize: 32, color: "success.main" }} />
              }
              color="success"
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard
              title="Today's Appointments"
              value={stats.todayAppointments}
              icon={
                <CalendarMonth sx={{ fontSize: 32, color: "warning.main" }} />
              }
              color="warning"
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard
              title="Low Stock Alerts"
              value={stats.lowStockMedications}
              icon={<Medication sx={{ fontSize: 32, color: "error.main" }} />}
              color="error"
            />
          </Grid>
        </Grid>
      )}

      {can(PERMISSIONS.VIEW_SYSTEM_STATS) && (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Patient Trends
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={monthlyPatients}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="patients"
                      stroke="#2196f3"
                      strokeWidth={2}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Appointments by Type
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={appointmentsByType}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={(entry) => entry.name}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {appointmentsByType.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={COLORS[index % COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      <Grid container spacing={3}>
        {canAny([
          PERMISSIONS.VIEW_ALL_PATIENTS,
          PERMISSIONS.VIEW_ASSIGNED_PATIENTS,
        ]) && (
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 2,
                  }}
                >
                  <Typography variant="h6" fontWeight={600}>
                    Recent Patients
                  </Typography>
                  <IconButton
                    size="small"
                    onClick={() => navigate("/patients")}
                  >
                    <Visibility />
                  </IconButton>
                </Box>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Name</TableCell>
                        <TableCell>Age</TableCell>
                        <TableCell>Phone</TableCell>
                        <TableCell>Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {recentPatients.length > 0 ? (
                        recentPatients.map((patient) => (
                          <TableRow key={patient.id} hover>
                            <TableCell>{patient.name}</TableCell>
                            <TableCell>{patient.age}</TableCell>
                            <TableCell>{patient.phone}</TableCell>
                            <TableCell>
                              <Chip
                                label="Active"
                                size="small"
                                color="success"
                              />
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={4} align="center">
                            <Typography variant="body2" color="text.secondary">
                              No recent patients
                            </Typography>
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        )}

        {can(PERMISSIONS.VIEW_APPOINTMENTS) && (
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 2,
                  }}
                >
                  <Typography variant="h6" fontWeight={600}>
                    Upcoming Appointments
                  </Typography>
                  <IconButton
                    size="small"
                    onClick={() => navigate("/appointments")}
                  >
                    <Visibility />
                  </IconButton>
                </Box>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Patient</TableCell>
                        <TableCell>Doctor</TableCell>
                        <TableCell>Time</TableCell>
                        <TableCell>Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {upcomingAppointments.length > 0 ? (
                        upcomingAppointments.map((appointment) => (
                          <TableRow key={appointment.id} hover>
                            <TableCell>{appointment.patientName}</TableCell>
                            <TableCell>{appointment.doctorName}</TableCell>
                            <TableCell>{appointment.time}</TableCell>
                            <TableCell>
                              <Chip
                                label={appointment.status}
                                size="small"
                                color={
                                  appointment.status === "SCHEDULED"
                                    ? "primary"
                                    : "default"
                                }
                              />
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={4} align="center">
                            <Typography variant="body2" color="text.secondary">
                              No upcoming appointments
                            </Typography>
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}

export default Dashboard;
