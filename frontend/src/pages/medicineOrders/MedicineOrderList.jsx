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
  Add as AddIcon,
  Search as SearchIcon,
  Visibility as ViewIcon,
  CheckCircle as ConfirmIcon,
  LocalShipping as DeliverIcon,
  Cancel as CancelIcon,
  Payment as PaymentIcon,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { useSelector } from "react-redux";
import medicineOrderService from "../../services/medicineOrderService";
import { usePermissions } from "../../hooks/usePermissions";
import PaymentDialog from "../../components/payments/PaymentDialog";

function MedicineOrderList() {
  const navigate = useNavigate();
  const { isAnyRole } = usePermissions();
  const { user } = useSelector((state) => state.auth);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [tabValue, setTabValue] = useState(0);
  const [statusDialog, setStatusDialog] = useState({
    open: false,
    order: null,
  });
  const [selectedStatus, setSelectedStatus] = useState("");
  const [paymentDialog, setPaymentDialog] = useState({
    open: false,
    order: null,
  });

  const isPharmacistOrAdmin = isAnyRole(["PHARMACIST", "ADMIN"]);

  useEffect(() => {
    fetchOrders();
  }, [tabValue]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      let data;
      if (isPharmacistOrAdmin) {
        switch (tabValue) {
          case 0:
            data = await medicineOrderService.getAllOrders();
            break;
          case 1:
            data = await medicineOrderService.getPendingOrders();
            break;
          case 2:
            data = await medicineOrderService.getOrdersByStatus("PROCESSING");
            break;
          case 3:
            data =
              await medicineOrderService.getOrdersByStatus("READY_FOR_PICKUP");
            break;
          default:
            data = await medicineOrderService.getAllOrders();
        }
      } else {
        data = await medicineOrderService.getMyOrders();
      }
      setOrders(data || []);
    } catch (error) {
      toast.error("Failed to fetch orders");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async () => {
    if (!statusDialog.order || !selectedStatus) return;

    try {
      switch (selectedStatus) {
        case "CONFIRMED":
          await medicineOrderService.confirmOrder(statusDialog.order.id);
          break;
        case "PROCESSING":
          await medicineOrderService.processOrder(statusDialog.order.id);
          break;
        case "READY_FOR_PICKUP":
          await medicineOrderService.markReadyForPickup(statusDialog.order.id);
          break;
        case "DELIVERED":
          await medicineOrderService.deliverOrder(statusDialog.order.id);
          break;
        default:
          await medicineOrderService.updateOrderStatus(
            statusDialog.order.id,
            selectedStatus
          );
      }
      toast.success("Order status updated successfully");
      setStatusDialog({ open: false, order: null });
      setSelectedStatus("");
      fetchOrders();
    } catch (error) {
      toast.error("Failed to update order status");
      console.error(error);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (window.confirm("Are you sure you want to cancel this order?")) {
      try {
        await medicineOrderService.cancelOrder(orderId);
        toast.success("Order cancelled successfully");
        fetchOrders();
      } catch (error) {
        toast.error("Failed to cancel order");
        console.error(error);
      }
    }
  };

  const handlePaymentSuccess = () => {
    setPaymentDialog({ open: false, order: null });
    fetchOrders();
  };

  const getStatusColor = (status) => {
    const colors = {
      PENDING: "warning",
      CONFIRMED: "info",
      PROCESSING: "primary",
      READY_FOR_PICKUP: "secondary",
      DELIVERED: "success",
      CANCELLED: "error",
    };
    return colors[status] || "default";
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
      order.patientName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.prescriptionNumber
        ?.toLowerCase()
        .includes(searchQuery.toLowerCase())
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
            Medicine Orders
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {isPharmacistOrAdmin
              ? "Manage medicine orders from prescriptions"
              : "View your medicine orders"}
          </Typography>
        </div>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate("/medicine-orders/new")}
        >
          New Order
        </Button>
      </Box>

      {isPharmacistOrAdmin && (
        <Tabs
          value={tabValue}
          onChange={(e, newValue) => setTabValue(newValue)}
          sx={{ mb: 3 }}
        >
          <Tab label="All Orders" />
          <Tab label="Pending" />
          <Tab label="Processing" />
          <Tab label="Ready for Pickup" />
        </Tabs>
      )}

      <Card>
        <CardContent>
          <TextField
            fullWidth
            placeholder="Search by order number, patient name, or prescription..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ mb: 3 }}
          />

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Order #</TableCell>
                  <TableCell>Patient</TableCell>
                  <TableCell>Prescription</TableCell>
                  <TableCell>Order Date</TableCell>
                  <TableCell>Total</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Payment</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredOrders.length > 0 ? (
                  filteredOrders.map((order) => (
                    <TableRow key={order.id} hover>
                      <TableCell>
                        <Typography fontWeight={600}>
                          {order.orderNumber}
                        </Typography>
                      </TableCell>
                      <TableCell>{order.patientName}</TableCell>
                      <TableCell>
                        {order.prescriptionNumber || "Walk-in"}
                      </TableCell>
                      <TableCell>
                        {new Date(order.orderDate).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <Typography fontWeight={600}>
                          ${order.finalAmount?.toFixed(2)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={order.status?.replace(/_/g, " ")}
                          size="small"
                          color={getStatusColor(order.status)}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={order.paymentStatus}
                          size="small"
                          color={getPaymentStatusColor(order.paymentStatus)}
                          variant="outlined"
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
                        {isPharmacistOrAdmin &&
                          order.status !== "DELIVERED" &&
                          order.status !== "CANCELLED" && (
                            <>
                              <IconButton
                                size="small"
                                onClick={() => {
                                  setStatusDialog({ open: true, order });
                                  setSelectedStatus("");
                                }}
                                color="primary"
                                title="Update Status"
                              >
                                <ConfirmIcon />
                              </IconButton>
                              {order.paymentStatus === "PENDING" && (
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
                            </>
                          )}
                        {order.status !== "DELIVERED" &&
                          order.status !== "CANCELLED" && (
                            <IconButton
                              size="small"
                              onClick={() => handleCancelOrder(order.id)}
                              color="error"
                              title="Cancel Order"
                            >
                              <CancelIcon />
                            </IconButton>
                          )}
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={8} align="center">
                      <Typography variant="body2" color="text.secondary" py={4}>
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

      {/* Status Update Dialog */}
      <Dialog
        open={statusDialog.open}
        onClose={() => setStatusDialog({ open: false, order: null })}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Update Order Status</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Order: {statusDialog.order?.orderNumber}
          </Typography>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>New Status</InputLabel>
            <Select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              label="New Status"
            >
              <MenuItem value="CONFIRMED">Confirmed</MenuItem>
              <MenuItem value="PROCESSING">Processing</MenuItem>
              <MenuItem value="READY_FOR_PICKUP">Ready for Pickup</MenuItem>
              <MenuItem value="DELIVERED">Delivered</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusDialog({ open: false, order: null })}>
            Cancel
          </Button>
          <Button
            onClick={handleStatusUpdate}
            variant="contained"
            disabled={!selectedStatus}
          >
            Update
          </Button>
        </DialogActions>
      </Dialog>

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

export default MedicineOrderList;
