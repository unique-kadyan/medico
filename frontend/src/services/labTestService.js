import api from "./api";

const labTestService = {
  getAllLabTests: async () => {
    const response = await api.get("/lab-tests");
    return response.data;
  },

  getLabTestById: async (id) => {
    const response = await api.get(`/lab-tests/${id}`);
    return response.data;
  },

  getLabTestsByPatient: async (patientId) => {
    const response = await api.get(`/lab-tests/patient/${patientId}`);
    return response.data;
  },

  getLabTestsByDoctor: async (doctorId) => {
    const response = await api.get(`/lab-tests/doctor/${doctorId}`);
    return response.data;
  },

  getLabTestsByStatus: async (status) => {
    const response = await api.get(`/lab-tests/status/${status}`);
    return response.data;
  },

  orderLabTest: async (labTestData) => {
    const response = await api.post("/lab-tests", labTestData);
    return response.data;
  },

  updateLabTest: async (id, labTestData) => {
    const response = await api.put(`/lab-tests/${id}`, labTestData);
    return response.data;
  },

  uploadResults: async (id, resultsData) => {
    const response = await api.put(`/lab-tests/${id}/results`, resultsData);
    return response.data;
  },

  deleteLabTest: async (id) => {
    const response = await api.delete(`/lab-tests/${id}`);
    return response.data;
  },
};

export default labTestService;
