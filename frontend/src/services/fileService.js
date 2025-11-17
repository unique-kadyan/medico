import api from "./api";

const fileService = {
  uploadFile: async (file, subDirectory = "lab-tests") => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("subDirectory", subDirectory);

    const response = await api.post("/files/upload", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  getFileUrl: (filePath) => {
    return `${api.defaults.baseURL}/files/download?path=${encodeURIComponent(
      filePath
    )}`;
  },
};

export default fileService;
