import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Chip,
  TextField,
  Button,
  LinearProgress,
} from "@mui/material";
import {
  People as PeopleIcon,
  LocalHospital as HospitalIcon,
  Event as EventIcon,
  Medication as MedicationIcon,
  AttachMoney as MoneyIcon,
  Warning as WarningIcon,
  TrendingUp as TrendingUpIcon,
  Inventory as InventoryIcon,
} from "@mui/icons-material";
import { toast } from "sonner";
import reportService from "../../services/reportService";

function TabPanel({ children, value, index }) {
  return value === index ? <Box sx={{ py: 3 }}>{children}</Box> : null;
}

function StatCard({ title, value, icon, color = "primary", subtitle }) {
  return (
    <Card>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
            <Typography variant="h4" fontWeight={600}>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="caption" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              backgroundColor: `${color}.light`,
              borderRadius: 2,
              p: 1.5,
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
}

function Reports() {
  const [tabValue, setTabValue] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [dashboardStats, setDashboardStats] = useState(null);
  const [revenueReport, setRevenueReport] = useState(null);
  const [inventoryReport, setInventoryReport] = useState(null);
  const [insuranceReport, setInsuranceReport] = useState(null);
  const [patients, setPatients] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [medications, setMedications] = useState([]);

  const today = new Date();
  const thirtyDaysAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
  const [startDate, setStartDate] = useState(
    thirtyDaysAgo.toISOString().split("T")[0]
  );
  const [endDate, setEndDate] = useState(today.toISOString().split("T")[0]);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  useEffect(() => {
    if (tabValue === 1) fetchPatientData();
    if (tabValue === 2) fetchAppointmentData();
    if (tabValue === 3) fetchMedicationData();
    if (tabValue === 4) fetchFinancialData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabValue]);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await reportService.getDashboardStats();
      setDashboardStats(data);
    } catch (err) {
      console.error("Error fetching dashboard stats:", err);
      setError("Failed to load dashboard stats");
    } finally {
      setLoading(false);
    }
  };

  const fetchPatientData = async () => {
    try {
      setLoading(true);
      const data = await reportService.getPatients();
      setPatients(Array.isArray(data) ? data : []);
    } catch {
      toast.error("Failed to load patient data");
    } finally {
      setLoading(false);
    }
  };

  const fetchAppointmentData = async () => {
    try {
      setLoading(true);
      const data = await reportService.getAppointments();
      setAppointments(Array.isArray(data) ? data : []);
    } catch {
      toast.error("Failed to load appointment data");
    } finally {
      setLoading(false);
    }
  };

  const fetchMedicationData = async () => {
    try {
      setLoading(true);
      const [meds, inventory] = await Promise.all([
        reportService.getMedications(),
        reportService.getInventoryReport().catch(() => null),
      ]);
      setMedications(Array.isArray(meds) ? meds : []);
      setInventoryReport(inventory);
    } catch {
      toast.error("Failed to load medication data");
    } finally {
      setLoading(false);
    }
  };

  const fetchFinancialData = async () => {
    try {
      setLoading(true);
      const [revenue, insurance] = await Promise.all([
        reportService.getRevenueReport(startDate, endDate).catch(() => null),
        reportService
          .getInsuranceClaimsReport(startDate, endDate)
          .catch(() => null),
      ]);
      setRevenueReport(revenue);
      setInsuranceReport(insurance);
    } catch {
      toast.error("Failed to load financial data");
    } finally {
      setLoading(false);
    }
  };

  const getAppointmentStats = () => {
    const total = appointments.length;
    const completed = appointments.filter(
      (a) => a.status === "COMPLETED"
    ).length;
    const scheduled = appointments.filter(
      (a) => a.status === "SCHEDULED"
    ).length;
    const cancelled = appointments.filter(
      (a) => a.status === "CANCELLED"
    ).length;
    return { total, completed, scheduled, cancelled };
  };

  const getMedicationStats = () => {
    const total = medications.length;
    const lowStock = medications.filter(
      (m) => m.stockQuantity < (m.reorderLevel || 10)
    ).length;
    const expired = medications.filter(
      (m) => m.expiryDate && new Date(m.expiryDate) < new Date()
    ).length;
    const totalValue = medications.reduce(
      (sum, m) => sum + (m.unitPrice || 0) * (m.stockQuantity || 0),
      0
    );
    return { total, lowStock, expired, totalValue };
  };

  const getPatientStats = () => {
    const total = patients.length;
    const active = patients.filter((p) => p.active !== false).length;
    const thisMonth = patients.filter((p) => {
      const created = new Date(p.createdAt);
      const now = new Date();
      return (
        created.getMonth() === now.getMonth() &&
        created.getFullYear() === now.getFullYear()
      );
    }).length;
    return { total, active, thisMonth };
  };

  const renderAppointmentsByType = () => {
    if (!dashboardStats?.appointmentsByType) return null;
    const data = dashboardStats.appointmentsByType;

    if (Array.isArray(data)) {
      return data.map((item, index) => (
        <TableRow key={index}>
          <TableCell>{item.name || item.type || "Unknown"}</TableCell>
          <TableCell align="right">{item.value || item.count || 0}</TableCell>
        </TableRow>
      ));
    }

    if (typeof data === "object") {
      return Object.entries(data).map(([type, count]) => (
        <TableRow key={type}>
          <TableCell>{type}</TableCell>
          <TableCell align="right">{count}</TableCell>
        </TableRow>
      ));
    }

    return null;
  };

  const renderMonthlyPatients = () => {
    if (!dashboardStats?.monthlyPatients) return null;
    const data = dashboardStats.monthlyPatients;

    if (Array.isArray(data)) {
      return data.map((item, index) => {
        if (typeof item === "object") {
          return (
            <TableRow key={index}>
              <TableCell>{item.month || `Month ${index + 1}`}</TableCell>
              <TableCell align="right">
                {item.patients || item.count || item.value || 0}
              </TableCell>
            </TableRow>
          );
        }
        const date = new Date();
        date.setMonth(date.getMonth() - (data.length - 1 - index));
        return (
          <TableRow key={index}>
            <TableCell>
              {date.toLocaleString("default", {
                month: "short",
                year: "numeric",
              })}
            </TableCell>
            <TableCell align="right">{item}</TableCell>
          </TableRow>
        );
      });
    }

    return null;
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Reports
      </Typography>

      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(e, newValue) => setTabValue(newValue)}
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab
            label="Overview"
            icon={<TrendingUpIcon />}
            iconPosition="start"
          />
          <Tab label="Patients" icon={<PeopleIcon />} iconPosition="start" />
          <Tab label="Appointments" icon={<EventIcon />} iconPosition="start" />
          <Tab
            label="Medications"
            icon={<MedicationIcon />}
            iconPosition="start"
          />
          <Tab label="Financial" icon={<MoneyIcon />} iconPosition="start" />
        </Tabs>
      </Paper>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {error && (
        <Paper sx={{ p: 3, mb: 2, bgcolor: "error.light" }}>
          <Typography color="error.dark">{error}</Typography>
        </Paper>
      )}

      {/* Overview Tab */}
      <TabPanel value={tabValue} index={0}>
        {dashboardStats ? (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={3}>
              <StatCard
                title="Total Patients"
                value={dashboardStats.totalPatients || 0}
                icon={<PeopleIcon color="primary" />}
                color="primary"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <StatCard
                title="Total Doctors"
                value={dashboardStats.totalDoctors || 0}
                icon={<HospitalIcon color="success" />}
                color="success"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <StatCard
                title="Today's Appointments"
                value={dashboardStats.todayAppointments || 0}
                icon={<EventIcon color="info" />}
                color="info"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <StatCard
                title="Low Stock Items"
                value={dashboardStats.lowStockMedications || 0}
                icon={<WarningIcon color="warning" />}
                color="warning"
              />
            </Grid>

            {dashboardStats.appointmentsByType && (
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Appointments by Type
                    </Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Type</TableCell>
                            <TableCell align="right">Count</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>{renderAppointmentsByType()}</TableBody>
                      </Table>
                    </TableContainer>
                  </CardContent>
                </Card>
              </Grid>
            )}

            {dashboardStats.monthlyPatients && (
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Monthly Patient Registrations
                    </Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Month</TableCell>
                            <TableCell align="right">New Patients</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>{renderMonthlyPatients()}</TableBody>
                      </Table>
                    </TableContainer>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        ) : (
          <Box display="flex" justifyContent="center" py={4}>
            <CircularProgress />
          </Box>
        )}
      </TabPanel>

      {/* Patient Reports Tab */}
      <TabPanel value={tabValue} index={1}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={4}>
            <StatCard
              title="Total Patients"
              value={getPatientStats().total}
              icon={<PeopleIcon color="primary" />}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatCard
              title="Active Patients"
              value={getPatientStats().active}
              icon={<PeopleIcon color="success" />}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatCard
              title="Registered This Month"
              value={getPatientStats().thisMonth}
              icon={<TrendingUpIcon color="info" />}
            />
          </Grid>

          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Recent Patients
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Patient ID</TableCell>
                        <TableCell>Name</TableCell>
                        <TableCell>Gender</TableCell>
                        <TableCell>Blood Group</TableCell>
                        <TableCell>Phone</TableCell>
                        <TableCell>Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {patients.slice(0, 10).map((patient) => (
                        <TableRow key={patient.id}>
                          <TableCell>{patient.patientId}</TableCell>
                          <TableCell>
                            {patient.firstName} {patient.lastName}
                          </TableCell>
                          <TableCell>{patient.gender || "-"}</TableCell>
                          <TableCell>{patient.bloodGroup || "-"}</TableCell>
                          <TableCell>{patient.phone || "-"}</TableCell>
                          <TableCell>
                            <Chip
                              label={
                                patient.active !== false ? "Active" : "Inactive"
                              }
                              size="small"
                              color={
                                patient.active !== false ? "success" : "default"
                              }
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Appointment Reports Tab */}
      <TabPanel value={tabValue} index={2}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Total Appointments"
              value={getAppointmentStats().total}
              icon={<EventIcon color="primary" />}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Completed"
              value={getAppointmentStats().completed}
              icon={<EventIcon color="success" />}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Scheduled"
              value={getAppointmentStats().scheduled}
              icon={<EventIcon color="info" />}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Cancelled"
              value={getAppointmentStats().cancelled}
              icon={<EventIcon color="error" />}
            />
          </Grid>

          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Recent Appointments
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Date</TableCell>
                        <TableCell>Time</TableCell>
                        <TableCell>Patient</TableCell>
                        <TableCell>Doctor</TableCell>
                        <TableCell>Reason</TableCell>
                        <TableCell>Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {appointments.slice(0, 10).map((apt) => (
                        <TableRow key={apt.id}>
                          <TableCell>
                            {apt.appointmentDate
                              ? new Date(
                                  apt.appointmentDate
                                ).toLocaleDateString()
                              : "-"}
                          </TableCell>
                          <TableCell>{apt.appointmentTime || "-"}</TableCell>
                          <TableCell>{apt.patientName || "-"}</TableCell>
                          <TableCell>{apt.doctorName || "-"}</TableCell>
                          <TableCell>{apt.reason || "-"}</TableCell>
                          <TableCell>
                            <Chip
                              label={apt.status || "PENDING"}
                              size="small"
                              color={
                                apt.status === "COMPLETED"
                                  ? "success"
                                  : apt.status === "CANCELLED"
                                    ? "error"
                                    : "warning"
                              }
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Medication Reports Tab */}
      <TabPanel value={tabValue} index={3}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Total Medications"
              value={getMedicationStats().total}
              icon={<MedicationIcon color="primary" />}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Low Stock"
              value={getMedicationStats().lowStock}
              icon={<WarningIcon color="warning" />}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Expired"
              value={getMedicationStats().expired}
              icon={<WarningIcon color="error" />}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
            <StatCard
              title="Total Stock Value"
              value={`$${getMedicationStats().totalValue.toLocaleString()}`}
              icon={<InventoryIcon color="success" />}
            />
          </Grid>

          {inventoryReport && (
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Inventory Summary
                  </Typography>
                  <Box display="flex" flexDirection="column" gap={1}>
                    <Typography variant="body2">
                      <strong>Total Stock Value:</strong> $
                      {inventoryReport.totalStockValue?.toLocaleString() || 0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Low Stock Items:</strong>{" "}
                      {inventoryReport.lowStockItems || 0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Out of Stock:</strong>{" "}
                      {inventoryReport.outOfStockItems || 0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Expiring Soon:</strong>{" "}
                      {inventoryReport.expiringSoon || 0}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          )}

          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Medication Inventory
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Name</TableCell>
                        <TableCell>Category</TableCell>
                        <TableCell>Stock</TableCell>
                        <TableCell>Unit Price</TableCell>
                        <TableCell>Expiry Date</TableCell>
                        <TableCell>Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {medications.slice(0, 10).map((med) => (
                        <TableRow key={med.id}>
                          <TableCell>{med.name}</TableCell>
                          <TableCell>{med.category || "-"}</TableCell>
                          <TableCell>{med.stockQuantity || 0}</TableCell>
                          <TableCell>
                            ${med.unitPrice?.toFixed(2) || "0.00"}
                          </TableCell>
                          <TableCell>
                            {med.expiryDate
                              ? new Date(med.expiryDate).toLocaleDateString()
                              : "-"}
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={
                                med.stockQuantity < (med.reorderLevel || 10)
                                  ? "Low Stock"
                                  : "In Stock"
                              }
                              size="small"
                              color={
                                med.stockQuantity < (med.reorderLevel || 10)
                                  ? "warning"
                                  : "success"
                              }
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Financial Reports Tab */}
      <TabPanel value={tabValue} index={4}>
        <Box display="flex" gap={2} mb={3} alignItems="center">
          <TextField
            type="date"
            label="Start Date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <TextField
            type="date"
            label="End Date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <Button variant="contained" onClick={fetchFinancialData}>
            Generate Report
          </Button>
        </Box>

        <Grid container spacing={3}>
          {revenueReport && (
            <>
              <Grid item xs={12} sm={4}>
                <StatCard
                  title="Total Revenue"
                  value={`$${revenueReport.totalRevenue?.toLocaleString() || 0}`}
                  icon={<MoneyIcon color="success" />}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <StatCard
                  title="Collected"
                  value={`$${revenueReport.totalCollected?.toLocaleString() || 0}`}
                  icon={<MoneyIcon color="primary" />}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <StatCard
                  title="Outstanding"
                  value={`$${revenueReport.totalOutstanding?.toLocaleString() || 0}`}
                  icon={<MoneyIcon color="warning" />}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Invoice Summary
                    </Typography>
                    <Box display="flex" flexDirection="column" gap={1}>
                      <Typography variant="body2">
                        <strong>Paid Invoices:</strong>{" "}
                        {revenueReport.paidInvoices || 0}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Pending Invoices:</strong>{" "}
                        {revenueReport.pendingInvoices || 0}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Overdue Invoices:</strong>{" "}
                        {revenueReport.overdueInvoices || 0}
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </>
          )}

          {insuranceReport && (
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Insurance Claims
                  </Typography>
                  <Box display="flex" flexDirection="column" gap={1}>
                    <Typography variant="body2">
                      <strong>Total Claimed:</strong> $
                      {insuranceReport.totalClaimedAmount?.toLocaleString() ||
                        0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Total Settled:</strong> $
                      {insuranceReport.totalSettledAmount?.toLocaleString() ||
                        0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Total Rejected:</strong> $
                      {insuranceReport.totalRejectedAmount?.toLocaleString() ||
                        0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Pending Claims:</strong>{" "}
                      {insuranceReport.pendingClaims || 0}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Settlement Rate:</strong>{" "}
                      {insuranceReport.settlementRate?.toFixed(1) || 0}%
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          )}

          {!revenueReport && !insuranceReport && !loading && (
            <Grid item xs={12}>
              <Paper sx={{ p: 4, textAlign: "center" }}>
                <Typography color="text.secondary">
                  Select a date range and click &quot;Generate Report&quot; to
                  view financial data.
                </Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      </TabPanel>
    </Box>
  );
}

export default Reports;
