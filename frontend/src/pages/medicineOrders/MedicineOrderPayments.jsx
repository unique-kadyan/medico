import { useState, useEffect } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Chip,
  InputAdornment,
  CircularProgress,
  Tabs,
  Tab,
  Grid,
  Paper,
} from "@mui/material";
import {
  Search as SearchIcon,
  Payment as PaymentIcon,
  Visibility as ViewIcon,
  Receipt as ReceiptIcon,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import medicineOrderService from "../../services/medicineOrderService";
import medicineOrderPaymentService from "../../services/medicineOrderPaymentService";
import PaymentDialog from "../../components/payments/PaymentDialog";

function MedicineOrderPayments() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [recentPayments, setRecentPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [tabValue, setTabValue] = useState(0);
  const [paymentDialog, setPaymentDialog] = useState({
    open: false,
    order: null,
  });
  const [stats, setStats] = useState({
    pendingPayments: 0,
    todayCollection: 0,
  });

  useEffect(() => {
    fetchData();
  }, [tabValue]);

  const fetchData = async () => {
    setLoading(true);
    try {
      let ordersData;
      switch (tabValue) {
        case 0: // Payment Pending
          ordersData = await medicineOrderService.getPaymentPendingOrders();
          break;
        case 1: // All Orders
          ordersData = await medicineOrderService.getAllOrders();
          break;
        default:
          ordersData = await medicineOrderService.getPaymentPendingOrders();
      }
      setOrders(ordersData || []);

      // Fetch recent payments (my collections)
      const payments = await medicineOrderPaymentService.getMyCollections();
      setRecentPayments(payments?.slice(0, 10) || []);

      // Calculate stats
      const pendingOrders =
        await medicineOrderService.getPaymentPendingOrders();
      const pendingTotal =
        pendingOrders?.reduce((sum, o) => sum + (o.finalAmount || 0), 0) || 0;

      // Get today's collections
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const todayPayments =
        payments?.filter((p) => new Date(p.paymentDate) >= today) || [];
      const todayTotal = todayPayments.reduce(
        (sum, p) => sum + (p.amount || 0),
        0
      );

      setStats({
        pendingPayments: pendingTotal,
        todayCollection: todayTotal,
      });
    } catch (error) {
      toast.error("Failed to fetch data");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handlePaymentSuccess = () => {
    fetchData();
  };

  const getPaymentStatusColor = (status) => {
    const colors = {
      PENDING: "warning",
      PAID: "success",
      FAILED: "error",
      REFUNDED: "info",
    };
    return colors[status] || "default";
  };

  const filteredOrders = orders.filter(
    (order) =>
      order.orderNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.patientName?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
        <CircularProgress />
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
        <div>
          <Typography variant="h4" fontWeight={700}>
            Payment Collection
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Collect payments for medicine orders
          </Typography>
        </div>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <Paper
            sx={{
              p: 3,
              bgcolor: "warning.50",
              border: "1px solid",
              borderColor: "warning.200",
            }}
          >
            <Typography variant="body2" color="text.secondary">
              Pending Payments
            </Typography>
            <Typography variant="h4" fontWeight={700} color="warning.main">
              ${stats.pendingPayments.toFixed(2)}
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper
            sx={{
              p: 3,
              bgcolor: "success.50",
              border: "1px solid",
              borderColor: "success.200",
            }}
          >
            <Typography variant="body2" color="text.secondary">
              Today&apos;s Collection
            </Typography>
            <Typography variant="h4" fontWeight={700} color="success.main">
              ${stats.todayCollection.toFixed(2)}
            </Typography>
          </Paper>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Orders List */}
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent>
              <Tabs
                value={tabValue}
                onChange={(e, newValue) => setTabValue(newValue)}
                sx={{ mb: 2 }}
              >
                <Tab label="Payment Pending" />
                <Tab label="All Orders" />
              </Tabs>

              <TextField
                fullWidth
                placeholder="Search by order number or patient name..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
                sx={{ mb: 2 }}
              />

              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Order #</TableCell>
                      <TableCell>Patient</TableCell>
                      <TableCell>Amount</TableCell>
                      <TableCell>Payment</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredOrders.length > 0 ? (
                      filteredOrders.map((order) => (
                        <TableRow key={order.id} hover>
                          <TableCell>
                            <Typography variant="body2" fontWeight={600}>
                              {order.orderNumber}
                            </Typography>
                            <Typography
                              variant="caption"
                              color="text.secondary"
                            >
                              {new Date(order.orderDate).toLocaleDateString()}
                            </Typography>
                          </TableCell>
                          <TableCell>{order.patientName}</TableCell>
                          <TableCell>
                            <Typography fontWeight={600}>
                              ${order.finalAmount?.toFixed(2)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={order.paymentStatus}
                              size="small"
                              color={getPaymentStatusColor(order.paymentStatus)}
                            />
                          </TableCell>
                          <TableCell align="right">
                            <IconButton
                              size="small"
                              onClick={() =>
                                navigate(`/medicine-orders/${order.id}`)
                              }
                              color="info"
                              title="View Details"
                            >
                              <ViewIcon />
                            </IconButton>
                            {order.paymentStatus !== "PAID" && (
                              <IconButton
                                size="small"
                                onClick={() =>
                                  setPaymentDialog({ open: true, order })
                                }
                                color="success"
                                title="Collect Payment"
                              >
                                <PaymentIcon />
                              </IconButton>
                            )}
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={5} align="center">
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            py={4}
                          >
                            No orders found
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

        {/* Recent Payments */}
        <Grid item xs={12} lg={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                <ReceiptIcon sx={{ mr: 1, verticalAlign: "middle" }} />
                My Recent Collections
              </Typography>

              {recentPayments.length > 0 ? (
                <Box sx={{ mt: 2 }}>
                  {recentPayments.map((payment) => (
                    <Paper
                      key={payment.id}
                      sx={{
                        p: 2,
                        mb: 1,
                        bgcolor: "grey.50",
                        borderLeft: "4px solid",
                        borderColor: "success.main",
                      }}
                    >
                      <Box
                        sx={{
                          display: "flex",
                          justifyContent: "space-between",
                        }}
                      >
                        <Typography variant="body2" fontWeight={600}>
                          {payment.receiptNumber}
                        </Typography>
                        <Typography
                          variant="body2"
                          color="success.main"
                          fontWeight={600}
                        >
                          ${payment.amount?.toFixed(2)}
                        </Typography>
                      </Box>
                      <Typography variant="caption" color="text.secondary">
                        {payment.patientName} - {payment.orderNumber}
                      </Typography>
                      <Box sx={{ mt: 0.5 }}>
                        <Chip
                          label={payment.paymentMethod?.replace(/_/g, " ")}
                          size="small"
                          variant="outlined"
                        />
                        <Typography
                          variant="caption"
                          color="text.secondary"
                          sx={{ ml: 1 }}
                        >
                          {new Date(payment.paymentDate).toLocaleString()}
                        </Typography>
                      </Box>
                    </Paper>
                  ))}
                </Box>
              ) : (
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ mt: 2 }}
                >
                  No recent payments
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Payment Dialog */}
      <PaymentDialog
        open={paymentDialog.open}
        onClose={() => setPaymentDialog({ open: false, order: null })}
        order={paymentDialog.order}
        onPaymentSuccess={handlePaymentSuccess}
      />
    </Box>
  );
}

export default MedicineOrderPayments;
