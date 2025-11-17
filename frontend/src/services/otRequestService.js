import api from "./api";

const OT_REQUEST_BASE_URL = "/ot-requests";

export const otRequestService = {
  createOTRequest: async (otRequestData) => {
    const response = await api.post(OT_REQUEST_BASE_URL, otRequestData);
    return response.data;
  },

  getAllOTRequests: async () => {
    const response = await api.get(OT_REQUEST_BASE_URL);
    return response.data;
  },

  getOTRequestById: async (id) => {
    const response = await api.get(`${OT_REQUEST_BASE_URL}/${id}`);
    return response.data;
  },

  getOTRequestsBySurgeon: async (surgeonId) => {
    const response = await api.get(
      `${OT_REQUEST_BASE_URL}/surgeon/${surgeonId}`
    );
    return response.data;
  },

  getOTRequestsByPatient: async (patientId) => {
    const response = await api.get(
      `${OT_REQUEST_BASE_URL}/patient/${patientId}`
    );
    return response.data;
  },

  getOTRequestsByStatus: async (status) => {
    const response = await api.get(`${OT_REQUEST_BASE_URL}/status/${status}`);
    return response.data;
  },

  getPendingOTRequests: async () => {
    const response = await api.get(`${OT_REQUEST_BASE_URL}/pending`);
    return response.data;
  },

  getEmergencyPendingRequests: async () => {
    const response = await api.get(`${OT_REQUEST_BASE_URL}/emergency-pending`);
    return response.data;
  },

  getOTRequestsByDateRange: async (startDate, endDate) => {
    const response = await api.get(`${OT_REQUEST_BASE_URL}/date-range`, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  updateOTRequest: async (id, otRequestData) => {
    const response = await api.put(
      `${OT_REQUEST_BASE_URL}/${id}`,
      otRequestData
    );
    return response.data;
  },

  approveOTRequest: async (id, notes) => {
    const response = await api.put(`${OT_REQUEST_BASE_URL}/${id}/approve`, {
      notes,
    });
    return response.data;
  },

  rejectOTRequest: async (id, rejectionReason) => {
    const response = await api.put(`${OT_REQUEST_BASE_URL}/${id}/reject`, {
      rejectionReason,
    });
    return response.data;
  },

  startSurgery: async (id) => {
    const response = await api.put(`${OT_REQUEST_BASE_URL}/${id}/start`);
    return response.data;
  },

  completeSurgery: async (id, postOperativeNotes) => {
    const response = await api.put(`${OT_REQUEST_BASE_URL}/${id}/complete`, {
      postOperativeNotes,
    });
    return response.data;
  },

  cancelOTRequest: async (id, reason) => {
    const response = await api.put(`${OT_REQUEST_BASE_URL}/${id}/cancel`, {
      reason,
    });
    return response.data;
  },

  deleteOTRequest: async (id) => {
    await api.delete(`${OT_REQUEST_BASE_URL}/${id}`);
  },
};

export default otRequestService;
