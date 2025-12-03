import api from "./api";

const stripeService = {
  // Get Stripe publishable key
  getConfig: async () => {
    const response = await api.get("/stripe/config");
    return response.data;
  },

  // Create Payment Intent for embedded payment
  createPaymentIntent: async (orderId) => {
    const response = await api.post(`/stripe/create-payment-intent/${orderId}`);
    return response.data;
  },

  // Create Checkout Session for redirect-based payment
  createCheckoutSession: async (orderId, successUrl, cancelUrl) => {
    const response = await api.post(`/stripe/create-checkout-session/${orderId}`, {
      successUrl,
      cancelUrl,
    });
    return response.data;
  },

  // Confirm payment after successful Stripe payment
  confirmPayment: async (orderId, paymentIntentId, transactionId) => {
    const response = await api.post("/stripe/confirm-payment", {
      orderId: orderId.toString(),
      paymentIntentId,
      transactionId,
    });
    return response.data;
  },

  // Get Payment Intent status
  getPaymentIntentStatus: async (paymentIntentId) => {
    const response = await api.get(`/stripe/payment-intent/${paymentIntentId}`);
    return response.data;
  },
};

export default stripeService;
