import api from "./api";

const hospitalService = {
  register: async (hospitalData) => {
    const response = await api.post("/hospitals/register", hospitalData);
    return response.data;
  },

  getSubscriptionPlans: async () => {
    const response = await api.get("/hospitals/subscription-plans");
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/hospitals/${id}`);
    return response.data;
  },

  getByCode: async (code) => {
    const response = await api.get(`/hospitals/code/${code}`);
    return response.data;
  },

  update: async (id, hospitalData) => {
    const response = await api.put(`/hospitals/${id}`, hospitalData);
    return response.data;
  },

  upgradePlan: async (hospitalId, plan) => {
    const response = await api.post(
      `/hospitals/${hospitalId}/upgrade?plan=${plan}`
    );
    return response.data;
  },

  getStatistics: async (hospitalId) => {
    const response = await api.get(`/hospitals/${hospitalId}/statistics`);
    return response.data;
  },

  checkCodeAvailability: async (code) => {
    try {
      await api.get(`/hospitals/code/${code}`);
      return false;
    } catch {
      return true;
    }
  },

  uploadLogo: async (hospitalId, file) => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post(`/hospitals/${hospitalId}/logo`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  deleteLogo: async (hospitalId) => {
    const response = await api.delete(`/hospitals/${hospitalId}/logo`);
    return response.data;
  },
};

export default hospitalService;
