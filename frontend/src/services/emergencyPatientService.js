import api from "./api";

const EMERGENCY_PATIENT_BASE_URL = "/emergency-patients";

export const emergencyPatientService = {
  admitPatient: async (patientData) => {
    const response = await api.post(EMERGENCY_PATIENT_BASE_URL, patientData);
    return response.data;
  },

  getAllEmergencyPatients: async () => {
    const response = await api.get(EMERGENCY_PATIENT_BASE_URL);
    return response.data;
  },

  getCurrentPatients: async () => {
    const response = await api.get(`${EMERGENCY_PATIENT_BASE_URL}/current`);
    return response.data;
  },

  getPatientsByRoom: async (roomId) => {
    const response = await api.get(
      `${EMERGENCY_PATIENT_BASE_URL}/room/${roomId}`
    );
    return response.data;
  },

  getPatientsByCondition: async (condition) => {
    const response = await api.get(
      `${EMERGENCY_PATIENT_BASE_URL}/condition/${condition}`
    );
    return response.data;
  },

  getPatientsRequiringMonitoring: async () => {
    const response = await api.get(`${EMERGENCY_PATIENT_BASE_URL}/monitoring`);
    return response.data;
  },

  getPatientsByDoctor: async (doctorId) => {
    const response = await api.get(
      `${EMERGENCY_PATIENT_BASE_URL}/doctor/${doctorId}`
    );
    return response.data;
  },

  getPatientHistory: async (patientId) => {
    const response = await api.get(
      `${EMERGENCY_PATIENT_BASE_URL}/patient/${patientId}/history`
    );
    return response.data;
  },

  getEmergencyPatientById: async (id) => {
    const response = await api.get(`${EMERGENCY_PATIENT_BASE_URL}/${id}`);
    return response.data;
  },

  updateEmergencyPatient: async (id, patientData) => {
    const response = await api.put(
      `${EMERGENCY_PATIENT_BASE_URL}/${id}`,
      patientData
    );
    return response.data;
  },

  updateCondition: async (id, condition) => {
    const response = await api.put(
      `${EMERGENCY_PATIENT_BASE_URL}/${id}/condition`,
      { condition }
    );
    return response.data;
  },

  dischargePatient: async (id, dischargeNotes) => {
    const response = await api.put(
      `${EMERGENCY_PATIENT_BASE_URL}/${id}/discharge`,
      { dischargeNotes }
    );
    return response.data;
  },

  transferToRoom: async (id, newRoomId) => {
    const response = await api.put(
      `${EMERGENCY_PATIENT_BASE_URL}/${id}/transfer`,
      { newRoomId }
    );
    return response.data;
  },
};

export default emergencyPatientService;
