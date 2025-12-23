import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  CircularProgress,
  Divider,
  Alert,
  Paper,
  Autocomplete,
} from "@mui/material";
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  ArrowBack as BackIcon,
} from "@mui/icons-material";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import medicineOrderService from "../../services/medicineOrderService";
import prescriptionService from "../../services/prescriptionService";
import medicationService from "../../services/medicationService";
import patientService from "../../services/patientService";

function MedicineOrderForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const prescriptionIdParam = searchParams.get("prescriptionId");
  const patientIdParam = searchParams.get("patientId");

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [patients, setPatients] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [medications, setMedications] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [selectedPrescription, setSelectedPrescription] = useState(null);

  const [formData, setFormData] = useState({
    patientId: patientIdParam || "",
    prescriptionId: prescriptionIdParam || "",
    deliveryAddress: "",
    contactPhone: "",
    notes: "",
    discountAmount: 0,
    items: [],
  });

  const [newItem, setNewItem] = useState({
    medicationId: "",
    quantity: 1,
    dosage: "",
    frequency: "",
    duration: "",
    instructions: "",
  });

  const fetchInitialData = useCallback(async () => {
    setLoading(true);
    try {
      const [patientsData, medicationsData] = await Promise.all([
        patientService.getAllPatients(),
        medicationService.getAllMedications(),
      ]);
      setPatients(patientsData || []);
      setMedications(medicationsData || []);

      if (patientIdParam) {
        const patient = patientsData?.find(
          (p) => p.id === parseInt(patientIdParam)
        );
        if (patient) {
          setSelectedPatient(patient);
          setFormData((prev) => ({ ...prev, patientId: patient.id }));
        }
      }
    } catch (error) {
      console.error("Error fetching initial data:", error);
      toast.error("Failed to load data");
    } finally {
      setLoading(false);
    }
  }, [patientIdParam]);

  const fetchPatientPrescriptions = useCallback(async (patientId) => {
    try {
      const data =
        await prescriptionService.getUndispensedByPatientId(patientId);
      setPrescriptions(data || []);

      if (prescriptionIdParam) {
        const prescription = data?.find(
          (p) => p.id === parseInt(prescriptionIdParam)
        );
        if (prescription) {
          setSelectedPrescription(prescription);
          setFormData((prev) => ({ ...prev, prescriptionId: prescription.id }));
        }
      }
    } catch (error) {
      console.error("Error fetching prescriptions:", error);
      setPrescriptions([]);
    }
  }, [prescriptionIdParam]);

  const loadPrescriptionItems = useCallback((prescription) => {
    if (prescription?.items) {
      const orderItems = prescription.items.map((item) => ({
        medicationId: item.medicationId,
        medicationName:
          item.medicationName ||
          medications.find((m) => m.id === item.medicationId)?.name,
        prescriptionItemId: item.id,
        quantity: item.quantity,
        unitPrice:
          medications.find((m) => m.id === item.medicationId)?.unitPrice || 0,
        dosage: item.dosage,
        frequency: item.frequency,
        duration: item.duration,
        instructions: item.instructions,
      }));
      setFormData((prev) => ({ ...prev, items: orderItems }));
    }
  }, [medications]);

  useEffect(() => {
    fetchInitialData();
  }, [fetchInitialData]);

  useEffect(() => {
    if (selectedPatient) {
      fetchPatientPrescriptions(selectedPatient.id);
    }
  }, [selectedPatient, fetchPatientPrescriptions]);

  useEffect(() => {
    if (selectedPrescription) {
      loadPrescriptionItems(selectedPrescription);
    }
  }, [selectedPrescription, loadPrescriptionItems]);

  const handlePatientChange = (event, newValue) => {
    setSelectedPatient(newValue);
    setSelectedPrescription(null);
    setPrescriptions([]);
    setFormData((prev) => ({
      ...prev,
      patientId: newValue?.id || "",
      prescriptionId: "",
      items: [],
    }));
  };

  const handlePrescriptionChange = (event) => {
    const prescriptionId = event.target.value;
    const prescription = prescriptions.find((p) => p.id === prescriptionId);
    setSelectedPrescription(prescription);
    setFormData((prev) => ({ ...prev, prescriptionId }));
  };

  const handleAddItem = () => {
    if (!newItem.medicationId || !newItem.quantity) {
      toast.error("Please select medication and quantity");
      return;
    }

    const medication = medications.find((m) => m.id === newItem.medicationId);
    if (!medication) return;

    if (medication.stockQuantity < newItem.quantity) {
      toast.error(`Insufficient stock. Available: ${medication.stockQuantity}`);
      return;
    }

    const orderItem = {
      ...newItem,
      medicationName: medication.name,
      unitPrice: medication.unitPrice,
    };

    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, orderItem],
    }));

    setNewItem({
      medicationId: "",
      quantity: 1,
      dosage: "",
      frequency: "",
      duration: "",
      instructions: "",
    });
  };

  const handleRemoveItem = (index) => {
    setFormData((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index),
    }));
  };

  const calculateTotals = () => {
    const total = formData.items.reduce(
      (sum, item) => sum + (item.unitPrice || 0) * item.quantity,
      0
    );
    const discount = parseFloat(formData.discountAmount) || 0;
    return {
      total,
      discount,
      final: total - discount,
    };
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.patientId) {
      toast.error("Please select a patient");
      return;
    }

    if (formData.items.length === 0) {
      toast.error("Please add at least one medication");
      return;
    }

    setSubmitting(true);
    try {
      const orderRequest = {
        patientId: formData.patientId,
        prescriptionId: formData.prescriptionId || null,
        deliveryAddress: formData.deliveryAddress,
        contactPhone: formData.contactPhone,
        notes: formData.notes,
        discountAmount: parseFloat(formData.discountAmount) || 0,
        items: formData.items.map((item) => ({
          medicationId: item.medicationId,
          prescriptionItemId: item.prescriptionItemId || null,
          quantity: item.quantity,
          dosage: item.dosage,
          frequency: item.frequency,
          duration: item.duration ? parseInt(item.duration) : null,
          instructions: item.instructions,
        })),
      };

      await medicineOrderService.createOrder(orderRequest);
      toast.success("Medicine order created successfully");
      navigate("/medicine-orders");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to create order");
      console.error(error);
    } finally {
      setSubmitting(false);
    }
  };

  const totals = calculateTotals();

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: "flex", alignItems: "center", mb: 3, gap: 2 }}>
        <IconButton onClick={() => navigate("/medicine-orders")}>
          <BackIcon />
        </IconButton>
        <div>
          <Typography variant="h4" fontWeight={700}>
            New Medicine Order
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Create an order based on prescription or walk-in
          </Typography>
        </div>
      </Box>

      <form onSubmit={handleSubmit}>
        <Grid container spacing={3}>
          {/* Patient and Prescription Selection */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Patient Information
                </Typography>

                <Autocomplete
                  options={patients}
                  getOptionLabel={(option) =>
                    `${option.firstName} ${option.lastName} (ID: ${option.id})`
                  }
                  value={selectedPatient}
                  onChange={handlePatientChange}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Select Patient"
                      required
                      fullWidth
                      sx={{ mb: 2 }}
                    />
                  )}
                />

                {selectedPatient && prescriptions.length > 0 && (
                  <FormControl fullWidth sx={{ mb: 2 }}>
                    <InputLabel>Prescription (Optional)</InputLabel>
                    <Select
                      value={formData.prescriptionId}
                      onChange={handlePrescriptionChange}
                      label="Prescription (Optional)"
                    >
                      <MenuItem value="">Walk-in Order</MenuItem>
                      {prescriptions.map((prescription) => (
                        <MenuItem key={prescription.id} value={prescription.id}>
                          {prescription.prescriptionNumber} -{" "}
                          {new Date(
                            prescription.prescriptionDate
                          ).toLocaleDateString()}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}

                {selectedPatient && prescriptions.length === 0 && (
                  <Alert severity="info" sx={{ mb: 2 }}>
                    No undispensed prescriptions found for this patient. You can
                    create a walk-in order.
                  </Alert>
                )}

                <TextField
                  fullWidth
                  label="Delivery Address"
                  value={formData.deliveryAddress}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      deliveryAddress: e.target.value,
                    })
                  }
                  multiline
                  rows={2}
                  sx={{ mb: 2 }}
                />

                <TextField
                  fullWidth
                  label="Contact Phone"
                  value={formData.contactPhone}
                  onChange={(e) =>
                    setFormData({ ...formData, contactPhone: e.target.value })
                  }
                  sx={{ mb: 2 }}
                />

                <TextField
                  fullWidth
                  label="Notes"
                  value={formData.notes}
                  onChange={(e) =>
                    setFormData({ ...formData, notes: e.target.value })
                  }
                  multiline
                  rows={2}
                />
              </CardContent>
            </Card>
          </Grid>

          {/* Add Medications */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Add Medications
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <FormControl fullWidth>
                      <InputLabel>Medication</InputLabel>
                      <Select
                        value={newItem.medicationId}
                        onChange={(e) =>
                          setNewItem({
                            ...newItem,
                            medicationId: e.target.value,
                          })
                        }
                        label="Medication"
                      >
                        {medications
                          .filter((m) => m.stockQuantity > 0)
                          .map((medication) => (
                            <MenuItem key={medication.id} value={medication.id}>
                              {medication.name} - $
                              {medication.unitPrice?.toFixed(2)} (Stock:{" "}
                              {medication.stockQuantity})
                            </MenuItem>
                          ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      label="Quantity"
                      type="number"
                      value={newItem.quantity}
                      onChange={(e) =>
                        setNewItem({
                          ...newItem,
                          quantity: parseInt(e.target.value) || 1,
                        })
                      }
                      inputProps={{ min: 1 }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      label="Dosage"
                      value={newItem.dosage}
                      onChange={(e) =>
                        setNewItem({ ...newItem, dosage: e.target.value })
                      }
                      placeholder="e.g., 1 tablet"
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      label="Frequency"
                      value={newItem.frequency}
                      onChange={(e) =>
                        setNewItem({ ...newItem, frequency: e.target.value })
                      }
                      placeholder="e.g., Twice daily"
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      label="Duration (days)"
                      type="number"
                      value={newItem.duration}
                      onChange={(e) =>
                        setNewItem({ ...newItem, duration: e.target.value })
                      }
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Instructions"
                      value={newItem.instructions}
                      onChange={(e) =>
                        setNewItem({ ...newItem, instructions: e.target.value })
                      }
                      placeholder="e.g., Take after meals"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <Button
                      variant="outlined"
                      startIcon={<AddIcon />}
                      onClick={handleAddItem}
                      fullWidth
                    >
                      Add to Order
                    </Button>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Order Items */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Order Items
                </Typography>

                {formData.items.length > 0 ? (
                  <>
                    <TableContainer>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell>Medication</TableCell>
                            <TableCell>Quantity</TableCell>
                            <TableCell>Unit Price</TableCell>
                            <TableCell>Subtotal</TableCell>
                            <TableCell>Dosage</TableCell>
                            <TableCell>Frequency</TableCell>
                            <TableCell align="right">Action</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {formData.items.map((item, index) => (
                            <TableRow key={index}>
                              <TableCell>{item.medicationName}</TableCell>
                              <TableCell>{item.quantity}</TableCell>
                              <TableCell>
                                ${item.unitPrice?.toFixed(2)}
                              </TableCell>
                              <TableCell>
                                ${(item.unitPrice * item.quantity).toFixed(2)}
                              </TableCell>
                              <TableCell>{item.dosage || "-"}</TableCell>
                              <TableCell>{item.frequency || "-"}</TableCell>
                              <TableCell align="right">
                                <IconButton
                                  size="small"
                                  onClick={() => handleRemoveItem(index)}
                                  color="error"
                                >
                                  <DeleteIcon />
                                </IconButton>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>

                    <Divider sx={{ my: 2 }} />

                    <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                      <Paper sx={{ p: 2, minWidth: 300 }}>
                        <Grid container spacing={1}>
                          <Grid item xs={6}>
                            <Typography>Subtotal:</Typography>
                          </Grid>
                          <Grid item xs={6}>
                            <Typography align="right">
                              ${totals.total.toFixed(2)}
                            </Typography>
                          </Grid>
                          <Grid item xs={6}>
                            <Typography>Discount:</Typography>
                          </Grid>
                          <Grid item xs={6}>
                            <TextField
                              size="small"
                              type="number"
                              value={formData.discountAmount}
                              onChange={(e) =>
                                setFormData({
                                  ...formData,
                                  discountAmount: e.target.value,
                                })
                              }
                              inputProps={{ min: 0, step: 0.01 }}
                              sx={{ width: "100%" }}
                            />
                          </Grid>
                          <Grid item xs={12}>
                            <Divider />
                          </Grid>
                          <Grid item xs={6}>
                            <Typography variant="h6" fontWeight={700}>
                              Total:
                            </Typography>
                          </Grid>
                          <Grid item xs={6}>
                            <Typography
                              variant="h6"
                              fontWeight={700}
                              align="right"
                              color="primary"
                            >
                              ${totals.final.toFixed(2)}
                            </Typography>
                          </Grid>
                        </Grid>
                      </Paper>
                    </Box>
                  </>
                ) : (
                  <Alert severity="info">
                    No items added yet. Add medications from the form above or
                    select a prescription.
                  </Alert>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Submit Button */}
          <Grid item xs={12}>
            <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
              <Button
                variant="outlined"
                onClick={() => navigate("/medicine-orders")}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                disabled={submitting || formData.items.length === 0}
              >
                {submitting ? <CircularProgress size={24} /> : "Create Order"}
              </Button>
            </Box>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}

export default MedicineOrderForm;
