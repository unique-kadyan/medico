import api from "./api";

const prescriptionService = {
  // Get all prescriptions
  getAllPrescriptions: async () => {
    const response = await api.get("/prescriptions");
    return response.data;
  },

  // Get prescription by ID
  getPrescriptionById: async (id) => {
    const response = await api.get(`/prescriptions/${id}`);
    return response.data;
  },

  // Get prescription by number
  getPrescriptionByNumber: async (prescriptionNumber) => {
    const response = await api.get(
      `/prescriptions/number/${prescriptionNumber}`
    );
    return response.data;
  },

  // Get prescriptions by patient ID
  getPrescriptionsByPatientId: async (patientId) => {
    const response = await api.get(`/prescriptions/patient/${patientId}`);
    return response.data;
  },

  // Get undispensed prescriptions for a patient
  getUndispensedByPatientId: async (patientId) => {
    const response = await api.get(
      `/prescriptions/patient/${patientId}/undispensed`
    );
    return response.data;
  },

  // Get all undispensed prescriptions
  getUndispensedPrescriptions: async () => {
    const response = await api.get("/prescriptions/undispensed");
    return response.data;
  },

  // Create prescription
  createPrescription: async (prescriptionData) => {
    const response = await api.post("/prescriptions", prescriptionData);
    return response.data;
  },

  // Update prescription
  updatePrescription: async (id, prescriptionData) => {
    const response = await api.put(`/prescriptions/${id}`, prescriptionData);
    return response.data;
  },

  // Mark prescription as dispensed
  dispensePrescription: async (id) => {
    const response = await api.put(`/prescriptions/${id}/dispense`);
    return response.data;
  },

  // Delete prescription
  deletePrescription: async (id) => {
    const response = await api.delete(`/prescriptions/${id}`);
    return response.data;
  },
};

export default prescriptionService;
