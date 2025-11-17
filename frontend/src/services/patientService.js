import api from './api';

const patientService = {
  // Get all patients
  getAllPatients: async (params = {}) => {
    const response = await api.get('/patients', { params });
    return response.data;
  },

  // Get patient by ID
  getPatientById: async (id) => {
    const response = await api.get(`/patients/${id}`);
    return response.data;
  },

  // Search patients
  searchPatients: async (query) => {
    const response = await api.get('/patients/search', {
      params: { query },
    });
    return response.data;
  },

  // Get patients by name
  getPatientsByName: async (name) => {
    const response = await api.get(`/patients/name/${name}`);
    return response.data;
  },

  // Get patients by phone
  getPatientsByPhone: async (phone) => {
    const response = await api.get(`/patients/phone/${phone}`);
    return response.data;
  },

  // Create patient
  createPatient: async (patientData) => {
    const response = await api.post('/patients', patientData);
    return response.data;
  },

  // Update patient
  updatePatient: async (id, patientData) => {
    const response = await api.put(`/patients/${id}`, patientData);
    return response.data;
  },

  // Delete patient
  deletePatient: async (id) => {
    const response = await api.delete(`/patients/${id}`);
    return response.data;
  },

  // Get patient statistics
  getPatientStatistics: async () => {
    const response = await api.get('/async/patients/statistics');
    return response.data;
  },

  // Batch create patients
  createPatientsBatch: async (patients) => {
    const response = await api.post('/async/patients/batch', patients);
    return response.data;
  },
};

export default patientService;
