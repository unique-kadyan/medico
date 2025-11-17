import api from "./api";

const EMERGENCY_ROOM_BASE_URL = "/emergency-rooms";

export const emergencyRoomService = {
  createEmergencyRoom: async (roomData) => {
    const response = await api.post(EMERGENCY_ROOM_BASE_URL, roomData);
    return response.data;
  },

  getAllEmergencyRooms: async () => {
    const response = await api.get(EMERGENCY_ROOM_BASE_URL);
    return response.data;
  },

  getActiveEmergencyRooms: async () => {
    const response = await api.get(`${EMERGENCY_ROOM_BASE_URL}/active`);
    return response.data;
  },

  getAvailableRooms: async () => {
    const response = await api.get(`${EMERGENCY_ROOM_BASE_URL}/available`);
    return response.data;
  },

  getRoomsByStatus: async (status) => {
    const response = await api.get(
      `${EMERGENCY_ROOM_BASE_URL}/status/${status}`
    );
    return response.data;
  },

  getRoomsByFloor: async (floorNumber) => {
    const response = await api.get(
      `${EMERGENCY_ROOM_BASE_URL}/floor/${floorNumber}`
    );
    return response.data;
  },

  getEmergencyRoomById: async (id) => {
    const response = await api.get(`${EMERGENCY_ROOM_BASE_URL}/${id}`);
    return response.data;
  },

  getEmergencyRoomByNumber: async (roomNumber) => {
    const response = await api.get(
      `${EMERGENCY_ROOM_BASE_URL}/room-number/${roomNumber}`
    );
    return response.data;
  },

  getAvailableRoomCount: async () => {
    const response = await api.get(
      `${EMERGENCY_ROOM_BASE_URL}/stats/available-count`
    );
    return response.data;
  },

  getOccupiedRoomCount: async () => {
    const response = await api.get(
      `${EMERGENCY_ROOM_BASE_URL}/stats/occupied-count`
    );
    return response.data;
  },

  updateEmergencyRoom: async (id, roomData) => {
    const response = await api.put(
      `${EMERGENCY_ROOM_BASE_URL}/${id}`,
      roomData
    );
    return response.data;
  },

  updateRoomStatus: async (id, status) => {
    const response = await api.put(`${EMERGENCY_ROOM_BASE_URL}/${id}/status`, {
      status,
    });
    return response.data;
  },

  updateOccupancy: async (id, occupancy) => {
    const response = await api.put(
      `${EMERGENCY_ROOM_BASE_URL}/${id}/occupancy`,
      { occupancy }
    );
    return response.data;
  },

  deactivateEmergencyRoom: async (id) => {
    const response = await api.put(
      `${EMERGENCY_ROOM_BASE_URL}/${id}/deactivate`
    );
    return response.data;
  },

  deleteEmergencyRoom: async (id) => {
    await api.delete(`${EMERGENCY_ROOM_BASE_URL}/${id}`);
  },
};

export default emergencyRoomService;
