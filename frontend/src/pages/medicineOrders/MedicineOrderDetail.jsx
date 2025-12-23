import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Divider,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  ButtonGroup,
} from "@mui/material";
import {
  ArrowBack as BackIcon,
  Print as PrintIcon,
  CheckCircle as ConfirmIcon,
  Cancel as CancelIcon,
  Payment as PaymentIcon,
  CreditCard as OnlinePaymentIcon,
  AccountBalance as UpiIcon,
} from "@mui/icons-material";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import medicineOrderService from "../../services/medicineOrderService";
import { usePermissions } from "../../hooks/usePermissions";
import StripePaymentDialog from "../../components/payments/StripePaymentDialog";
import razorpayService from "../../services/razorpayService";

function MedicineOrderDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { isAnyRole } = usePermissions();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statusDialog, setStatusDialog] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState("");
  const [stripePaymentDialog, setStripePaymentDialog] = useState(false);
  const [razorpayLoading, setRazorpayLoading] = useState(false);

  const isPharmacistOrAdmin = isAnyRole(["PHARMACIST", "ADMIN"]);

  const fetchOrder = useCallback(async () => {
    setLoading(true);
    try {
      const data = await medicineOrderService.getOrderById(id);
      setOrder(data);
    } catch (error) {
      toast.error("Failed to fetch order details");
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchOrder();
  }, [fetchOrder]);

  const handleStatusUpdate = async () => {
    if (!selectedStatus) return;

    try {
      switch (selectedStatus) {
        case "CONFIRMED":
          await medicineOrderService.confirmOrder(order.id);
          break;
        case "PROCESSING":
          await medicineOrderService.processOrder(order.id);
          break;
        case "READY_FOR_PICKUP":
          await medicineOrderService.markReadyForPickup(order.id);
          break;
        case "DELIVERED":
          await medicineOrderService.deliverOrder(order.id);
          break;
        default:
          await medicineOrderService.updateOrderStatus(
            order.id,
            selectedStatus
          );
      }
      toast.success("Order status updated successfully");
      setStatusDialog(false);
      setSelectedStatus("");
      fetchOrder();
    } catch (error) {
      toast.error("Failed to update order status");
      console.error(error);
    }
  };

  const handleCancelOrder = async () => {
    if (window.confirm("Are you sure you want to cancel this order?")) {
      try {
        await medicineOrderService.cancelOrder(order.id);
        toast.success("Order cancelled successfully");
        fetchOrder();
      } catch (error) {
        toast.error("Failed to cancel order");
        console.error(error);
      }
    }
  };

  const handleMarkAsPaid = async () => {
    try {
      await medicineOrderService.markAsPaid(order.id);
      toast.success("Order marked as paid");
      fetchOrder();
    } catch (error) {
      toast.error("Failed to update payment status");
      console.error(error);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const handleStripePaymentSuccess = () => {
    setStripePaymentDialog(false);
    fetchOrder();
  };

  const handleRazorpayPayment = async () => {
    setRazorpayLoading(true);
    try {
      const orderData = await razorpayService.createOrder(order.id);
      await razorpayService.openCheckout(orderData, {
        onSuccess: () => {
          setRazorpayLoading(false);
          toast.success("Payment successful!");
          fetchOrder();
        },
        onError: (error) => {
          setRazorpayLoading(false);
          toast.error(error.description || "Payment failed");
        },
        onDismiss: () => {
          setRazorpayLoading(false);
        },
      });
    } catch (error) {
      setRazorpayLoading(false);
      toast.error("Failed to initiate payment");
      console.error(error);
    }
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

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!order) {
    return (
      <Box sx={{ textAlign: "center", p: 4 }}>
        <Typography variant="h6">Order not found</Typography>
        <Button onClick={() => navigate("/medicine-orders")} sx={{ mt: 2 }}>
          Back to Orders
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
          <IconButton onClick={() => navigate("/medicine-orders")}>
            <BackIcon />
          </IconButton>
          <div>
            <Typography variant="h4" fontWeight={700}>
              Order Details
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {order.orderNumber}
            </Typography>
          </div>
        </Box>
        <Box sx={{ display: "flex", gap: 1 }}>
          <Button
            variant="outlined"
            startIcon={<PrintIcon />}
            onClick={handlePrint}
          >
            Print
          </Button>
          {isPharmacistOrAdmin &&
            order.status !== "DELIVERED" &&
            order.status !== "CANCELLED" && (
              <>
                <Button
                  variant="contained"
                  startIcon={<ConfirmIcon />}
                  onClick={() => setStatusDialog(true)}
                >
                  Update Status
                </Button>
                {order.paymentStatus === "PENDING" && (
                  <>
                    <Button
                      variant="contained"
                      color="success"
                      startIcon={<PaymentIcon />}
                      onClick={handleMarkAsPaid}
                    >
                      Mark as Paid
                    </Button>
                    <ButtonGroup variant="contained">
                      <Button
                        color="primary"
                        startIcon={<OnlinePaymentIcon />}
                        onClick={() => setStripePaymentDialog(true)}
                      >
                        Card
                      </Button>
                      <Button
                        color="secondary"
                        startIcon={<UpiIcon />}
                        onClick={handleRazorpayPayment}
                        disabled={razorpayLoading}
                      >
                        {razorpayLoading ? "Processing..." : "UPI"}
                      </Button>
                    </ButtonGroup>
                  </>
                )}
              </>
            )}
          {order.status !== "DELIVERED" && order.status !== "CANCELLED" && (
            <>
              {order.paymentStatus === "PENDING" && !isPharmacistOrAdmin && (
                <ButtonGroup variant="contained">
                  <Button
                    color="primary"
                    startIcon={<OnlinePaymentIcon />}
                    onClick={() => setStripePaymentDialog(true)}
                  >
                    Pay with Card
                  </Button>
                  <Button
                    color="secondary"
                    startIcon={<UpiIcon />}
                    onClick={handleRazorpayPayment}
                    disabled={razorpayLoading}
                  >
                    {razorpayLoading ? "Processing..." : "Pay with UPI"}
                  </Button>
                </ButtonGroup>
              )}
              <Button
                variant="outlined"
                color="error"
                startIcon={<CancelIcon />}
                onClick={handleCancelOrder}
              >
                Cancel Order
              </Button>
            </>
          )}
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* Order Status */}
        <Grid item xs={12}>
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
                  <Typography variant="h6" fontWeight={600}>
                    Order Status
                  </Typography>
                  <Box sx={{ display: "flex", gap: 2, mt: 1 }}>
                    <Chip
                      label={order.status?.replace(/_/g, " ")}
                      color={getStatusColor(order.status)}
                      size="medium"
                    />
                    <Chip
                      label={`Payment: ${order.paymentStatus}`}
                      color={getPaymentStatusColor(order.paymentStatus)}
                      variant="outlined"
                      size="medium"
                    />
                  </Box>
                </Box>
                <Box sx={{ textAlign: "right" }}>
                  <Typography variant="body2" color="text.secondary">
                    Order Date
                  </Typography>
                  <Typography fontWeight={600}>
                    {new Date(order.orderDate).toLocaleString()}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Patient Information */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Patient Information
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Name
                  </Typography>
                  <Typography fontWeight={500}>{order.patientName}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Patient ID
                  </Typography>
                  <Typography fontWeight={500}>{order.patientId}</Typography>
                </Grid>
                {order.prescriptionNumber && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">
                      Prescription #
                    </Typography>
                    <Typography fontWeight={500}>
                      {order.prescriptionNumber}
                    </Typography>
                  </Grid>
                )}
                {order.contactPhone && (
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Contact Phone
                    </Typography>
                    <Typography fontWeight={500}>
                      {order.contactPhone}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Delivery Information */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: "100%" }}>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Delivery & Processing
              </Typography>
              <Grid container spacing={2}>
                {order.deliveryAddress && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">
                      Delivery Address
                    </Typography>
                    <Typography fontWeight={500}>
                      {order.deliveryAddress}
                    </Typography>
                  </Grid>
                )}
                {order.processedByName && (
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Processed By
                    </Typography>
                    <Typography fontWeight={500}>
                      {order.processedByName}
                    </Typography>
                  </Grid>
                )}
                {order.processedDate && (
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Processed Date
                    </Typography>
                    <Typography fontWeight={500}>
                      {new Date(order.processedDate).toLocaleString()}
                    </Typography>
                  </Grid>
                )}
                {order.deliveryDate && (
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Delivery Date
                    </Typography>
                    <Typography fontWeight={500}>
                      {new Date(order.deliveryDate).toLocaleString()}
                    </Typography>
                  </Grid>
                )}
                {order.notes && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">
                      Notes
                    </Typography>
                    <Typography fontWeight={500}>{order.notes}</Typography>
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Order Items */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Order Items
              </Typography>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Medication</TableCell>
                      <TableCell>Code</TableCell>
                      <TableCell>Quantity</TableCell>
                      <TableCell>Unit Price</TableCell>
                      <TableCell>Subtotal</TableCell>
                      <TableCell>Dosage</TableCell>
                      <TableCell>Frequency</TableCell>
                      <TableCell>Duration</TableCell>
                      <TableCell>Instructions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {order.items?.map((item, index) => (
                      <TableRow key={index}>
                        <TableCell>
                          <Typography fontWeight={600}>
                            {item.medicationName}
                          </Typography>
                        </TableCell>
                        <TableCell>{item.medicationCode}</TableCell>
                        <TableCell>{item.quantity}</TableCell>
                        <TableCell>${item.unitPrice?.toFixed(2)}</TableCell>
                        <TableCell>
                          <Typography fontWeight={600}>
                            ${item.subtotal?.toFixed(2)}
                          </Typography>
                        </TableCell>
                        <TableCell>{item.dosage || "-"}</TableCell>
                        <TableCell>{item.frequency || "-"}</TableCell>
                        <TableCell>
                          {item.duration ? `${item.duration} days` : "-"}
                        </TableCell>
                        <TableCell>{item.instructions || "-"}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                <Paper sx={{ p: 2, minWidth: 300 }}>
                  <Grid container spacing={1}>
                    <Grid item xs={6}>
                      <Typography>Subtotal:</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography align="right">
                        ${order.totalAmount?.toFixed(2)}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography>Discount:</Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography align="right" color="error">
                        -${order.discountAmount?.toFixed(2)}
                      </Typography>
                    </Grid>
                    <Grid item xs={12}>
                      <Divider />
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="h6" fontWeight={700}>
                        Total:
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography
                        variant="h6"
                        fontWeight={700}
                        align="right"
                        color="primary"
                      >
                        ${order.finalAmount?.toFixed(2)}
                      </Typography>
                    </Grid>
                  </Grid>
                </Paper>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Status Update Dialog */}
      <Dialog
        open={statusDialog}
        onClose={() => setStatusDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Update Order Status</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Current Status: {order.status?.replace(/_/g, " ")}
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
          <Button onClick={() => setStatusDialog(false)}>Cancel</Button>
          <Button
            onClick={handleStatusUpdate}
            variant="contained"
            disabled={!selectedStatus}
          >
            Update
          </Button>
        </DialogActions>
      </Dialog>

      {/* Stripe Payment Dialog */}
      <StripePaymentDialog
        open={stripePaymentDialog}
        onClose={() => setStripePaymentDialog(false)}
        order={order}
        onPaymentSuccess={handleStripePaymentSuccess}
      />
    </Box>
  );
}

export default MedicineOrderDetail;
