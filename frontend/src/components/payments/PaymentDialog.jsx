import { useState, useEffect, useCallback } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Typography,
  Box,
  Divider,
  Alert,
  CircularProgress,
} from "@mui/material";
import { toast } from "sonner";
import medicineOrderPaymentService from "../../services/medicineOrderPaymentService";

const PAYMENT_METHODS = [
  { value: "CASH", label: "Cash" },
  { value: "CREDIT_CARD", label: "Credit Card" },
  { value: "DEBIT_CARD", label: "Debit Card" },
  { value: "UPI", label: "UPI" },
  { value: "NET_BANKING", label: "Net Banking" },
  { value: "CHEQUE", label: "Cheque" },
  { value: "WALLET", label: "Wallet" },
  { value: "OTHER", label: "Other" },
];

function PaymentDialog({ open, onClose, order, onPaymentSuccess }) {
  const [loading, setLoading] = useState(false);
  const [remainingBalance, setRemainingBalance] = useState(0);
  const [totalPaid, setTotalPaid] = useState(0);
  const [formData, setFormData] = useState({
    orderId: "",
    amount: "",
    paymentMethod: "CASH",
    transactionId: "",
    cardLastFourDigits: "",
    bankName: "",
    chequeNumber: "",
    chequeDate: "",
    upiId: "",
    notes: "",
  });

  const fetchPaymentDetails = useCallback(async () => {
    if (!order) return;
    try {
      const [balance, paid] = await Promise.all([
        medicineOrderPaymentService.getRemainingBalance(order.id),
        medicineOrderPaymentService.getTotalPaidForOrder(order.id),
      ]);
      setRemainingBalance(balance);
      setTotalPaid(paid);
      setFormData((prev) => ({
        ...prev,
        amount: balance.toString(),
      }));
    } catch (error) {
      console.error("Error fetching payment details:", error);
    }
  }, [order]);

  useEffect(() => {
    if (order && open) {
      setFormData((prev) => ({
        ...prev,
        orderId: order.id,
        amount: "",
      }));
      fetchPaymentDetails();
    }
  }, [order, open, fetchPaymentDetails]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async () => {
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      toast.error("Please enter a valid amount");
      return;
    }

    if (parseFloat(formData.amount) > remainingBalance) {
      toast.error("Payment amount exceeds remaining balance");
      return;
    }

    setLoading(true);
    try {
      const paymentData = {
        orderId: formData.orderId,
        amount: parseFloat(formData.amount),
        paymentMethod: formData.paymentMethod,
        transactionId: formData.transactionId || null,
        cardLastFourDigits: formData.cardLastFourDigits || null,
        bankName: formData.bankName || null,
        chequeNumber: formData.chequeNumber || null,
        chequeDate: formData.chequeDate || null,
        upiId: formData.upiId || null,
        notes: formData.notes || null,
      };

      const result =
        await medicineOrderPaymentService.recordPayment(paymentData);
      toast.success(
        `Payment recorded successfully! Receipt: ${result.receiptNumber}`
      );
      onPaymentSuccess && onPaymentSuccess(result);
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to record payment");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const showCardFields = ["CREDIT_CARD", "DEBIT_CARD"].includes(
    formData.paymentMethod
  );
  const showChequeFields = formData.paymentMethod === "CHEQUE";
  const showUpiFields = formData.paymentMethod === "UPI";
  const showBankFields = ["NET_BANKING", "CHEQUE"].includes(
    formData.paymentMethod
  );

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Record Payment</DialogTitle>
      <DialogContent>
        {order && (
          <>
            <Box sx={{ mb: 3, mt: 1 }}>
              <Typography variant="subtitle2" color="text.secondary">
                Order Details
              </Typography>
              <Typography variant="h6">{order.orderNumber}</Typography>
              <Typography variant="body2">
                Patient: {order.patientName}
              </Typography>
            </Box>

            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2} sx={{ mb: 2 }}>
              <Grid item xs={4}>
                <Typography variant="body2" color="text.secondary">
                  Total Amount
                </Typography>
                <Typography variant="h6">
                  ${order.finalAmount?.toFixed(2)}
                </Typography>
              </Grid>
              <Grid item xs={4}>
                <Typography variant="body2" color="text.secondary">
                  Already Paid
                </Typography>
                <Typography variant="h6" color="success.main">
                  ${totalPaid?.toFixed(2)}
                </Typography>
              </Grid>
              <Grid item xs={4}>
                <Typography variant="body2" color="text.secondary">
                  Balance Due
                </Typography>
                <Typography variant="h6" color="error.main">
                  ${remainingBalance?.toFixed(2)}
                </Typography>
              </Grid>
            </Grid>

            {remainingBalance <= 0 ? (
              <Alert severity="success" sx={{ mb: 2 }}>
                This order is fully paid!
              </Alert>
            ) : (
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Amount"
                    name="amount"
                    type="number"
                    value={formData.amount}
                    onChange={handleChange}
                    required
                    inputProps={{ min: 0, max: remainingBalance, step: 0.01 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth required>
                    <InputLabel>Payment Method</InputLabel>
                    <Select
                      name="paymentMethod"
                      value={formData.paymentMethod}
                      onChange={handleChange}
                      label="Payment Method"
                    >
                      {PAYMENT_METHODS.map((method) => (
                        <MenuItem key={method.value} value={method.value}>
                          {method.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                {showCardFields && (
                  <>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Card Last 4 Digits"
                        name="cardLastFourDigits"
                        value={formData.cardLastFourDigits}
                        onChange={handleChange}
                        inputProps={{ maxLength: 4 }}
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Transaction ID"
                        name="transactionId"
                        value={formData.transactionId}
                        onChange={handleChange}
                      />
                    </Grid>
                  </>
                )}

                {showUpiFields && (
                  <>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="UPI ID"
                        name="upiId"
                        value={formData.upiId}
                        onChange={handleChange}
                        placeholder="example@upi"
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Transaction ID"
                        name="transactionId"
                        value={formData.transactionId}
                        onChange={handleChange}
                      />
                    </Grid>
                  </>
                )}

                {showChequeFields && (
                  <>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Cheque Number"
                        name="chequeNumber"
                        value={formData.chequeNumber}
                        onChange={handleChange}
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Cheque Date"
                        name="chequeDate"
                        type="datetime-local"
                        value={formData.chequeDate}
                        onChange={handleChange}
                        InputLabelProps={{ shrink: true }}
                      />
                    </Grid>
                  </>
                )}

                {showBankFields && (
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Bank Name"
                      name="bankName"
                      value={formData.bankName}
                      onChange={handleChange}
                    />
                  </Grid>
                )}

                {formData.paymentMethod === "NET_BANKING" && (
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Transaction ID"
                      name="transactionId"
                      value={formData.transactionId}
                      onChange={handleChange}
                    />
                  </Grid>
                )}

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Notes"
                    name="notes"
                    value={formData.notes}
                    onChange={handleChange}
                    multiline
                    rows={2}
                  />
                </Grid>
              </Grid>
            )}
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || remainingBalance <= 0}
        >
          {loading ? <CircularProgress size={24} /> : "Record Payment"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default PaymentDialog;
