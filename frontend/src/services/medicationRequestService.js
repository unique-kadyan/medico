import api from "./api";

const medicationRequestService = {
  getAll: async () => {
    const response = await api.get("/medication-requests");
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/medication-requests/${id}`);
    return response.data;
  },

  getPending: async () => {
    const response = await api.get("/medication-requests/pending");
    return response.data;
  },

  getMyRequests: async () => {
    const response = await api.get("/medication-requests/my-requests");
    return response.data;
  },

  getByStatus: async (status) => {
    const response = await api.get(`/medication-requests/status/${status}`);
    return response.data;
  },

  getByDoctor: async (doctorId) => {
    const response = await api.get(`/medication-requests/doctor/${doctorId}`);
    return response.data;
  },

  create: async (requestData) => {
    const response = await api.post("/medication-requests", requestData);
    return response.data;
  },

  approve: async (id, reviewNotes = null) => {
    const response = await api.put(`/medication-requests/${id}/approve`, {
      reviewNotes,
    });
    return response.data;
  },

  reject: async (id, reviewNotes) => {
    const response = await api.put(`/medication-requests/${id}/reject`, {
      reviewNotes,
    });
    return response.data;
  },
};

export default medicationRequestService;
