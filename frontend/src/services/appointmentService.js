import api from './api';

const appointmentService = {
  // Get all appointments
  getAllAppointments: async (params = {}) => {
    const response = await api.get('/appointments', { params });
    return response.data;
  },

  // Get appointment by ID
  getAppointmentById: async (id) => {
    const response = await api.get(`/appointments/${id}`);
    return response.data;
  },

  // Get appointments by patient ID
  getAppointmentsByPatient: async (patientId) => {
    const response = await api.get(`/appointments/patient/${patientId}`);
    return response.data;
  },

  // Get appointments by doctor ID
  getAppointmentsByDoctor: async (doctorId) => {
    const response = await api.get(`/appointments/doctor/${doctorId}`);
    return response.data;
  },

  // Get appointments by status
  getAppointmentsByStatus: async (status) => {
    const response = await api.get(`/appointments/status/${status}`);
    return response.data;
  },

  // Get today's appointments
  getTodaysAppointments: async () => {
    const response = await api.get('/appointments/today');
    return response.data;
  },

  // Get upcoming appointments
  getUpcomingAppointments: async () => {
    const response = await api.get('/appointments/upcoming');
    return response.data;
  },

  // Create appointment
  createAppointment: async (appointmentData) => {
    const response = await api.post('/appointments', appointmentData);
    return response.data;
  },

  // Update appointment
  updateAppointment: async (id, appointmentData) => {
    const response = await api.put(`/appointments/${id}`, appointmentData);
    return response.data;
  },

  // Update appointment status
  updateAppointmentStatus: async (id, status) => {
    const response = await api.patch(`/appointments/${id}/status`, { status });
    return response.data;
  },

  // Cancel appointment
  cancelAppointment: async (id) => {
    const response = await api.delete(`/appointments/${id}`);
    return response.data;
  },
};

export default appointmentService;
