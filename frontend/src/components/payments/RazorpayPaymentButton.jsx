import { useState } from "react";
import { Button, CircularProgress } from "@mui/material";
import { Payment as PaymentIcon } from "@mui/icons-material";
import razorpayService from "../../services/razorpayService";

const RazorpayPaymentButton = ({
  orderId,
  onSuccess,
  onError,
  onCancel,
  disabled = false,
  variant = "contained",
  color = "primary",
  size = "medium",
  fullWidth = false,
  label = "Pay with UPI/Card",
}) => {
  const [loading, setLoading] = useState(false);

  const handlePayment = async () => {
    setLoading(true);

    try {
      // Create order on backend
      const orderData = await razorpayService.createOrder(orderId);

      // Open Razorpay checkout
      await razorpayService.openCheckout(orderData, {
        onSuccess: (result) => {
          setLoading(false);
          if (onSuccess) {
            onSuccess(result);
          }
        },
        onError: (error) => {
          setLoading(false);
          if (onError) {
            onError(error);
          }
        },
        onDismiss: () => {
          setLoading(false);
          if (onCancel) {
            onCancel();
          }
        },
      });
    } catch (error) {
      setLoading(false);
      if (onError) {
        onError(error);
      }
    }
  };

  return (
    <Button
      variant={variant}
      color={color}
      size={size}
      fullWidth={fullWidth}
      disabled={disabled || loading}
      onClick={handlePayment}
      startIcon={
        loading ? (
          <CircularProgress size={20} color="inherit" />
        ) : (
          <PaymentIcon />
        )
      }
    >
      {loading ? "Processing..." : label}
    </Button>
  );
};

export default RazorpayPaymentButton;
