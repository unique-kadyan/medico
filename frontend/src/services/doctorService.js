import api from './api';

const doctorService = {
  // Get all doctors
  getAllDoctors: async (params = {}) => {
    const response = await api.get('/doctors', { params });
    return response.data;
  },

  // Get doctor by ID
  getDoctorById: async (id) => {
    const response = await api.get(`/doctors/${id}`);
    return response.data;
  },

  // Search doctors
  searchDoctors: async (query) => {
    const response = await api.get('/doctors/search', {
      params: { query },
    });
    return response.data;
  },

  // Get doctors by specialization
  getDoctorsBySpecialization: async (specialization) => {
    const response = await api.get(`/doctors/specialization/${specialization}`);
    return response.data;
  },

  // Get available doctors
  getAvailableDoctors: async (date) => {
    const response = await api.get('/doctors/available', {
      params: { date },
    });
    return response.data;
  },

  // Create doctor
  createDoctor: async (doctorData) => {
    const response = await api.post('/doctors', doctorData);
    return response.data;
  },

  // Update doctor
  updateDoctor: async (id, doctorData) => {
    const response = await api.put(`/doctors/${id}`, doctorData);
    return response.data;
  },

  // Delete doctor
  deleteDoctor: async (id) => {
    const response = await api.delete(`/doctors/${id}`);
    return response.data;
  },
};

export default doctorService;
