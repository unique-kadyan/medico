import api from "./api";

const medicineOrderPaymentService = {
  // Record a payment for an order
  recordPayment: async (paymentData) => {
    const response = await api.post("/medicine-order-payments", paymentData);
    return response.data;
  },

  // Get payment by ID
  getPaymentById: async (id) => {
    const response = await api.get(`/medicine-order-payments/${id}`);
    return response.data;
  },

  // Get payment by receipt number
  getPaymentByReceiptNumber: async (receiptNumber) => {
    const response = await api.get(
      `/medicine-order-payments/receipt/${receiptNumber}`
    );
    return response.data;
  },

  // Get payments for a specific order
  getPaymentsByOrderId: async (orderId) => {
    const response = await api.get(`/medicine-order-payments/order/${orderId}`);
    return response.data;
  },

  // Get total paid for an order
  getTotalPaidForOrder: async (orderId) => {
    const response = await api.get(
      `/medicine-order-payments/order/${orderId}/total-paid`
    );
    return response.data;
  },

  // Get remaining balance for an order
  getRemainingBalance: async (orderId) => {
    const response = await api.get(
      `/medicine-order-payments/order/${orderId}/balance`
    );
    return response.data;
  },

  // Get payments by date range
  getPaymentsByDateRange: async (startDate, endDate) => {
    const response = await api.get("/medicine-order-payments/date-range", {
      params: { startDate, endDate },
    });
    return response.data;
  },

  // Get current user's collections
  getMyCollections: async () => {
    const response = await api.get("/medicine-order-payments/my-collections");
    return response.data;
  },

  // Refund a payment
  refundPayment: async (id, refundAmount, reason) => {
    const response = await api.post(`/medicine-order-payments/${id}/refund`, {
      refundAmount,
      reason,
    });
    return response.data;
  },

  // Verify a payment
  verifyPayment: async (id) => {
    const response = await api.post(`/medicine-order-payments/${id}/verify`);
    return response.data;
  },

  // Get payment summary
  getPaymentSummary: async (startDate, endDate) => {
    const response = await api.get("/medicine-order-payments/summary", {
      params: { startDate, endDate },
    });
    return response.data;
  },
};

export default medicineOrderPaymentService;
