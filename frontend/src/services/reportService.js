import api from "./api";

const reportService = {
  getDashboardStats: async () => {
    const response = await api.get("/dashboard/stats");
    return response.data;
  },

  getRevenueReport: async (startDate, endDate) => {
    const response = await api.get("/billing/reports/revenue", {
      params: { startDate, endDate },
    });
    return response.data;
  },

  getInventoryReport: async () => {
    const response = await api.get("/inventory/reports/summary");
    return response.data;
  },

  getInsuranceClaimsReport: async (startDate, endDate) => {
    const response = await api.get("/insurance/reports/claims", {
      params: { startDate, endDate },
    });
    return response.data;
  },

  getPatients: async () => {
    const response = await api.get("/patients");
    return response.data;
  },

  getAppointments: async () => {
    const response = await api.get("/appointments");
    return response.data;
  },

  getMedications: async () => {
    const response = await api.get("/medications");
    return response.data;
  },
};

export default reportService;
