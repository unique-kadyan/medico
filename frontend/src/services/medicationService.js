import api from './api';

const medicationService = {
  // Get all medications
  getAllMedications: async (params = {}) => {
    const response = await api.get('/medications', { params });
    return response.data;
  },

  // Get medication by ID
  getMedicationById: async (id) => {
    const response = await api.get(`/medications/${id}`);
    return response.data;
  },

  // Search medications
  searchMedications: async (query) => {
    const response = await api.get('/medications/search', {
      params: { query },
    });
    return response.data;
  },

  // Get low stock medications
  getLowStockMedications: async (threshold = 10) => {
    const response = await api.get('/medications/low-stock', {
      params: { threshold },
    });
    return response.data;
  },

  // Get medications by category
  getMedicationsByCategory: async (category) => {
    const response = await api.get(`/medications/category/${category}`);
    return response.data;
  },

  // Create medication
  createMedication: async (medicationData) => {
    const response = await api.post('/medications', medicationData);
    return response.data;
  },

  // Update medication
  updateMedication: async (id, medicationData) => {
    const response = await api.put(`/medications/${id}`, medicationData);
    return response.data;
  },

  // Delete medication
  deleteMedication: async (id) => {
    const response = await api.delete(`/medications/${id}`);
    return response.data;
  },

  // Update stock
  updateStock: async (id, quantity) => {
    const response = await api.patch(`/medications/${id}/stock`, { quantity });
    return response.data;
  },

  // Get inventory alerts
  getInventoryAlerts: async () => {
    const response = await api.get('/async/medications/inventory-alerts');
    return response.data;
  },

  // Batch import medications
  importMedicationsBatch: async (medications) => {
    const response = await api.post('/async/medications/batch-import', medications);
    return response.data;
  },
};

export default medicationService;
