import api from "./api";

const medicineOrderService = {
  // Create a new medicine order
  createOrder: async (orderData) => {
    const response = await api.post("/medicine-orders", orderData);
    return response.data;
  },

  // Get all orders (admin/pharmacist)
  getAllOrders: async () => {
    const response = await api.get("/medicine-orders");
    return response.data;
  },

  // Get order by ID
  getOrderById: async (id) => {
    const response = await api.get(`/medicine-orders/${id}`);
    return response.data;
  },

  // Get order by order number
  getOrderByNumber: async (orderNumber) => {
    const response = await api.get(`/medicine-orders/number/${orderNumber}`);
    return response.data;
  },

  // Get orders by patient ID
  getOrdersByPatientId: async (patientId) => {
    const response = await api.get(`/medicine-orders/patient/${patientId}`);
    return response.data;
  },

  // Get current user's orders (patient)
  getMyOrders: async () => {
    const response = await api.get("/medicine-orders/my-orders");
    return response.data;
  },

  // Get orders by prescription ID
  getOrdersByPrescriptionId: async (prescriptionId) => {
    const response = await api.get(
      `/medicine-orders/prescription/${prescriptionId}`
    );
    return response.data;
  },

  // Get orders by status
  getOrdersByStatus: async (status) => {
    const response = await api.get(`/medicine-orders/status/${status}`);
    return response.data;
  },

  // Get pending orders
  getPendingOrders: async () => {
    const response = await api.get("/medicine-orders/pending");
    return response.data;
  },

  // Get payment pending orders
  getPaymentPendingOrders: async () => {
    const response = await api.get("/medicine-orders/payment-pending");
    return response.data;
  },

  // Update order status
  updateOrderStatus: async (id, status) => {
    const response = await api.put(`/medicine-orders/${id}/status`, { status });
    return response.data;
  },

  // Confirm order
  confirmOrder: async (id) => {
    const response = await api.put(`/medicine-orders/${id}/confirm`);
    return response.data;
  },

  // Process order
  processOrder: async (id) => {
    const response = await api.put(`/medicine-orders/${id}/process`);
    return response.data;
  },

  // Mark order as ready for pickup
  markReadyForPickup: async (id) => {
    const response = await api.put(`/medicine-orders/${id}/ready`);
    return response.data;
  },

  // Deliver order
  deliverOrder: async (id) => {
    const response = await api.put(`/medicine-orders/${id}/deliver`);
    return response.data;
  },

  // Update payment status
  updatePaymentStatus: async (id, paymentStatus) => {
    const response = await api.put(`/medicine-orders/${id}/payment-status`, {
      paymentStatus,
    });
    return response.data;
  },

  // Mark as paid
  markAsPaid: async (id) => {
    const response = await api.put(`/medicine-orders/${id}/pay`);
    return response.data;
  },

  // Cancel order
  cancelOrder: async (id) => {
    const response = await api.put(`/medicine-orders/${id}/cancel`);
    return response.data;
  },

  // Delete order (admin only)
  deleteOrder: async (id) => {
    const response = await api.delete(`/medicine-orders/${id}`);
    return response.data;
  },

  // Count pending orders
  countPendingOrders: async () => {
    const response = await api.get("/medicine-orders/count/pending");
    return response.data;
  },
};

export default medicineOrderService;
