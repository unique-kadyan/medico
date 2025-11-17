import api from "./api";

const pendingUserService = {
  getAllPendingUsers: async () => {
    const response = await api.get("/pending-users");
    return response.data;
  },

  getPendingUsersByRole: async (role) => {
    const response = await api.get(`/pending-users/role/${role}`);
    return response.data;
  },

  getPendingUserById: async (id) => {
    const response = await api.get(`/pending-users/${id}`);
    return response.data;
  },

  approveRegistration: async (id) => {
    const response = await api.post(`/pending-users/${id}/approve`);
    return response.data;
  },

  rejectRegistration: async (id, reason) => {
    const response = await api.post(`/pending-users/${id}/reject`, { reason });
    return response.data;
  },

  getDocuments: async (id) => {
    const response = await api.get(`/pending-users/${id}/documents`);
    return response.data;
  },

  registerWithDocuments: async (formData) => {
    const response = await api.post("/auth/register-with-documents", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },
};

export default pendingUserService;
