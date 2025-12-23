import { useState, useEffect, useCallback } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Divider,
  Alert,
  CircularProgress,
  Grid,
} from "@mui/material";
import { loadStripe } from "@stripe/stripe-js";
import {
  Elements,
  PaymentElement,
  useStripe,
  useElements,
} from "@stripe/react-stripe-js";
import { toast } from "sonner";
import stripeService from "../../services/stripeService";

function CheckoutForm({ orderId, amount, onSuccess, onCancel }) {
  const stripe = useStripe();
  const elements = useElements();
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setIsProcessing(true);
    setErrorMessage(null);

    try {
      const { error, paymentIntent } = await stripe.confirmPayment({
        elements,
        confirmParams: {
          return_url: window.location.href,
        },
        redirect: "if_required",
      });

      if (error) {
        setErrorMessage(error.message);
        toast.error(error.message);
      } else if (paymentIntent && paymentIntent.status === "succeeded") {
        await stripeService.confirmPayment(
          orderId,
          paymentIntent.id,
          paymentIntent.id
        );
        toast.success("Payment successful!");
        onSuccess && onSuccess(paymentIntent);
      }
    } catch (err) {
      setErrorMessage("An unexpected error occurred.");
      toast.error("Payment failed. Please try again.");
      console.error(err);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <Box sx={{ mb: 3 }}>
        <PaymentElement />
      </Box>

      {errorMessage && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errorMessage}
        </Alert>
      )}

      <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
        <Button onClick={onCancel} disabled={isProcessing}>
          Cancel
        </Button>
        <Button
          type="submit"
          variant="contained"
          disabled={!stripe || isProcessing}
        >
          {isProcessing ? (
            <CircularProgress size={24} color="inherit" />
          ) : (
            `Pay $${parseFloat(amount).toFixed(2)}`
          )}
        </Button>
      </Box>
    </form>
  );
}

function StripePaymentDialog({ open, onClose, order, onPaymentSuccess }) {
  const [loading, setLoading] = useState(true);
  const [stripePromise, setStripePromise] = useState(null);
  const [clientSecret, setClientSecret] = useState(null);
  const [amount, setAmount] = useState(0);
  const [error, setError] = useState(null);

  const initializePayment = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const [config, paymentIntent] = await Promise.all([
        stripeService.getConfig(),
        stripeService.createPaymentIntent(order.id),
      ]);

      const stripe = await loadStripe(config.publishableKey);
      setStripePromise(stripe);
      setClientSecret(paymentIntent.clientSecret);
      setAmount(paymentIntent.amount);
    } catch (err) {
      console.error("Error initializing payment:", err);
      setError(err.response?.data?.error || "Failed to initialize payment");
      toast.error("Failed to initialize payment");
    } finally {
      setLoading(false);
    }
  }, [order]);

  useEffect(() => {
    if (open && order) {
      initializePayment();
    }
  }, [open, order, initializePayment]);

  const handleSuccess = (paymentIntent) => {
    onPaymentSuccess && onPaymentSuccess(paymentIntent);
    onClose();
  };

  const options = {
    clientSecret,
    appearance: {
      theme: "stripe",
      variables: {
        colorPrimary: "#1976d2",
        colorBackground: "#ffffff",
        colorText: "#30313d",
        colorDanger: "#df1b41",
        fontFamily: "Roboto, sans-serif",
        borderRadius: "8px",
      },
    },
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Typography variant="h6" fontWeight={600}>
          Pay Online
        </Typography>
      </DialogTitle>
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

            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Order Total
                </Typography>
                <Typography variant="h6">
                  ${order.finalAmount?.toFixed(2)}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Amount to Pay
                </Typography>
                <Typography variant="h6" color="primary.main">
                  ${parseFloat(amount || order.finalAmount || 0).toFixed(2)}
                </Typography>
              </Grid>
            </Grid>

            {loading ? (
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  py: 4,
                }}
              >
                <CircularProgress />
                <Typography sx={{ ml: 2 }}>Initializing payment...</Typography>
              </Box>
            ) : error ? (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            ) : clientSecret && stripePromise ? (
              <Elements stripe={stripePromise} options={options}>
                <CheckoutForm
                  orderId={order.id}
                  amount={amount}
                  onSuccess={handleSuccess}
                  onCancel={onClose}
                />
              </Elements>
            ) : null}

            <Box sx={{ mt: 3 }}>
              <Alert severity="info" icon={false}>
                <Typography variant="caption" color="text.secondary">
                  <strong>Test Card:</strong> 4242 4242 4242 4242
                  <br />
                  <strong>Expiry:</strong> Any future date (e.g., 12/25)
                  <br />
                  <strong>CVC:</strong> Any 3 digits (e.g., 123)
                </Typography>
              </Alert>
            </Box>
          </>
        )}
      </DialogContent>
      {(loading || error) && (
        <DialogActions>
          <Button onClick={onClose}>Close</Button>
          {error && (
            <Button variant="contained" onClick={initializePayment}>
              Retry
            </Button>
          )}
        </DialogActions>
      )}
    </Dialog>
  );
}

export default StripePaymentDialog;
