import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  MenuItem,
  CircularProgress,
  FormControlLabel,
  Checkbox,
} from "@mui/material";
import { ArrowBack as BackIcon, Save as SaveIcon } from "@mui/icons-material";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import medicationService from "../../services/medicationService";

const CATEGORIES = [
  "ANTIBIOTIC",
  "PAINKILLER",
  "VITAMIN",
  "ANTIHISTAMINE",
  "ANTACID",
  "ANTISEPTIC",
  "ANTI_INFLAMMATORY",
  "OTHER",
  "General",
];
const DOSAGE_FORMS = [
  "TABLET",
  "CAPSULE",
  "SYRUP",
  "INJECTION",
  "CREAM",
  "OINTMENT",
  "DROPS",
  "INHALER",
  "OTHER",
  "Tablet",
];

function MedicationForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    medicationCode: "",
    name: "",
    genericName: "",
    category: "ANTIBIOTIC",
    manufacturer: "",
    description: "",
    dosageForm: "TABLET",
    strength: "",
    unitPrice: "",
    stockQuantity: "",
    reorderLevel: "",
    reorderQuantity: "",
    expiryDate: "",
    batchNumber: "",
    requiresPrescription: false,
    sideEffects: "",
    contraindications: "",
    storageInstructions: "",
  });

  const fetchMedication = useCallback(async () => {
    try {
      const data = await medicationService.getMedicationById(id);
      const fetchedCategory = data.category || "OTHER";
      const fetchedDosageForm = data.dosageForm || "TABLET";
      setFormData({
        medicationCode: data.medicationCode || "",
        name: data.name || "",
        genericName: data.genericName || "",
        category: CATEGORIES.includes(fetchedCategory)
          ? fetchedCategory
          : "OTHER",
        manufacturer: data.manufacturer || "",
        description: data.description || "",
        dosageForm: DOSAGE_FORMS.includes(fetchedDosageForm)
          ? fetchedDosageForm
          : "OTHER",
        strength: data.strength || "",
        unitPrice: data.unitPrice || "",
        stockQuantity: data.stockQuantity ?? "",
        reorderLevel: data.reorderLevel || "",
        reorderQuantity: data.reorderQuantity || "",
        expiryDate: data.expiryDate || "",
        batchNumber: data.batchNumber || "",
        requiresPrescription: data.requiresPrescription || false,
        sideEffects: data.sideEffects || "",
        contraindications: data.contraindications || "",
        storageInstructions: data.storageInstructions || "",
      });
    } catch (error) {
      toast.error("Failed to fetch medication details");
      console.error("Error fetching medication:", error);
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      fetchMedication();
    }
  }, [id, fetchMedication]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const submitData = {
        ...formData,
        unitPrice: parseFloat(formData.unitPrice),
        stockQuantity: parseInt(formData.stockQuantity),
        reorderLevel: formData.reorderLevel
          ? parseInt(formData.reorderLevel)
          : null,
        reorderQuantity: formData.reorderQuantity
          ? parseInt(formData.reorderQuantity)
          : null,
      };

      if (id) {
        await medicationService.updateMedication(id, submitData);
        toast.success("Medication updated successfully");
      } else {
        await medicationService.createMedication(submitData);
        toast.success("Medication created successfully");
      }
      navigate("/medications");
    } catch (error) {
      const errorMessage =
        error.response?.data?.message || error.message || "An error occurred";
      toast.error(
        id
          ? `Failed to update medication: ${errorMessage}`
          : `Failed to create medication: ${errorMessage}`
      );
      console.error("Error saving medication:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/medications")}
        >
          Back to Medications
        </Button>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight={700} gutterBottom>
            {id ? "Edit Medication" : "Add New Medication"}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {id ? "Update medication information" : "Enter medication details"}
          </Typography>

          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Medication Code"
                  name="medicationCode"
                  value={formData.medicationCode}
                  onChange={handleChange}
                  required
                  disabled={!!id}
                  helperText={
                    id
                      ? "Cannot change medication code"
                      : "Unique identifier for this medication"
                  }
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Medication Name"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Generic Name"
                  name="genericName"
                  value={formData.genericName}
                  onChange={handleChange}
                />
              </Grid>

              {/* Category */}
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Category"
                  name="category"
                  value={formData.category}
                  onChange={handleChange}
                  required
                >
                  {CATEGORIES.map((category) => (
                    <MenuItem key={category} value={category}>
                      {category.replace("_", " ")}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Dosage Form"
                  name="dosageForm"
                  value={formData.dosageForm}
                  onChange={handleChange}
                  required
                >
                  {DOSAGE_FORMS.map((form) => (
                    <MenuItem key={form} value={form}>
                      {form}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Strength"
                  name="strength"
                  value={formData.strength}
                  onChange={handleChange}
                  placeholder="e.g., 500mg, 10ml"
                />
              </Grid>

              {/* Manufacturer */}
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Manufacturer"
                  name="manufacturer"
                  value={formData.manufacturer}
                  onChange={handleChange}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Batch Number"
                  name="batchNumber"
                  value={formData.batchNumber}
                  onChange={handleChange}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Unit Price"
                  name="unitPrice"
                  type="number"
                  inputProps={{ step: "0.01", min: "0" }}
                  value={formData.unitPrice}
                  onChange={handleChange}
                  required
                />
              </Grid>

              {/* Stock Quantity */}
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Stock Quantity"
                  name="stockQuantity"
                  type="number"
                  inputProps={{ min: "0" }}
                  value={formData.stockQuantity}
                  onChange={handleChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Reorder Level"
                  name="reorderLevel"
                  type="number"
                  inputProps={{ min: "0" }}
                  value={formData.reorderLevel}
                  onChange={handleChange}
                  helperText="Alert when stock falls below this level"
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Reorder Quantity"
                  name="reorderQuantity"
                  type="number"
                  inputProps={{ min: "0" }}
                  value={formData.reorderQuantity}
                  onChange={handleChange}
                  helperText="Suggested reorder amount"
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Expiry Date"
                  name="expiryDate"
                  type="date"
                  value={formData.expiryDate}
                  onChange={handleChange}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formData.requiresPrescription}
                      onChange={handleChange}
                      name="requiresPrescription"
                    />
                  }
                  label="Requires Prescription"
                />
              </Grid>

              {/* Description */}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  multiline
                  rows={2}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Side Effects"
                  name="sideEffects"
                  value={formData.sideEffects}
                  onChange={handleChange}
                  multiline
                  rows={2}
                  placeholder="List common side effects..."
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Contraindications"
                  name="contraindications"
                  value={formData.contraindications}
                  onChange={handleChange}
                  multiline
                  rows={2}
                  placeholder="List contraindications and warnings..."
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Storage Instructions"
                  name="storageInstructions"
                  value={formData.storageInstructions}
                  onChange={handleChange}
                  multiline
                  rows={2}
                  placeholder="Storage temperature, conditions, etc..."
                />
              </Grid>

              <Grid item xs={12}>
                <Box
                  sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}
                >
                  <Button
                    variant="outlined"
                    onClick={() => navigate("/medications")}
                    disabled={loading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={
                      loading ? <CircularProgress size={20} /> : <SaveIcon />
                    }
                    disabled={loading}
                  >
                    {id ? "Update Medication" : "Create Medication"}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}

export default MedicationForm;
