import api from "./api";

const assignmentService = {
  assignDoctorToPatient: async (assignmentData) => {
    const response = await api.post("/assignments", assignmentData);
    return response.data;
  },

  getDoctorAssignments: async (doctorId) => {
    const response = await api.get(`/assignments/doctor/${doctorId}`);
    return response.data;
  },

  getPatientAssignments: async (patientId) => {
    const response = await api.get(`/assignments/patient/${patientId}`);
    return response.data;
  },

  getDoctorPatientCount: async (doctorId) => {
    const response = await api.get(`/assignments/doctor/${doctorId}/count`);
    return response.data;
  },

  removeAssignment: async (assignmentId) => {
    const response = await api.delete(`/assignments/${assignmentId}`);
    return response.data;
  },
};

export default assignmentService;
