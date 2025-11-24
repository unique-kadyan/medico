import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  MenuItem,
  CircularProgress,
} from "@mui/material";
import { ArrowBack as BackIcon, Send as SendIcon } from "@mui/icons-material";
import { toast } from "sonner";
import medicationRequestService from "../../services/medicationRequestService";

const dosageForms = [
  "Tablet",
  "Capsule",
  "Syrup",
  "Injection",
  "Cream",
  "Ointment",
  "Drops",
  "Inhaler",
  "Patch",
  "Suppository",
  "Powder",
  "Solution",
  "Suspension",
  "Gel",
  "Spray",
  "Other",
];

function MedicationRequestForm() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    medicationName: "",
    description: "",
    manufacturer: "",
    dosageForm: "",
    strength: "",
    requestReason: "",
    estimatedCost: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.medicationName.trim()) {
      toast.error("Medication name is required");
      return;
    }

    if (!formData.requestReason.trim()) {
      toast.error("Request reason is required");
      return;
    }

    try {
      setLoading(true);
      await medicationRequestService.create({
        ...formData,
        estimatedCost: formData.estimatedCost
          ? parseFloat(formData.estimatedCost)
          : null,
      });
      toast.success(
        "Medication request submitted successfully! Awaiting approval."
      );
      navigate("/medication-requests");
    } catch (error) {
      console.error("Error creating medication request:", error);
      toast.error("Failed to submit medication request");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate("/medication-requests")}
          variant="outlined"
        >
          Back
        </Button>
        <Typography variant="h4" fontWeight={600}>
          Request New Medication
        </Typography>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Submit a request for a new medication to be added to the hospital
            inventory. This request will be reviewed by a pharmacist or
            administrator.
          </Typography>

          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  required
                  label="Medication Name"
                  name="medicationName"
                  value={formData.medicationName}
                  onChange={handleChange}
                  placeholder="e.g., Amoxicillin"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Manufacturer"
                  name="manufacturer"
                  value={formData.manufacturer}
                  onChange={handleChange}
                  placeholder="e.g., Pfizer"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  select
                  label="Dosage Form"
                  name="dosageForm"
                  value={formData.dosageForm}
                  onChange={handleChange}
                >
                  <MenuItem value="">Select dosage form</MenuItem>
                  {dosageForms.map((form) => (
                    <MenuItem key={form} value={form}>
                      {form}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Strength"
                  name="strength"
                  value={formData.strength}
                  onChange={handleChange}
                  placeholder="e.g., 500mg, 10ml"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Estimated Cost"
                  name="estimatedCost"
                  type="number"
                  value={formData.estimatedCost}
                  onChange={handleChange}
                  InputProps={{
                    startAdornment: <Typography sx={{ mr: 1 }}>$</Typography>,
                  }}
                  placeholder="0.00"
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="Brief description of the medication and its uses"
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  required
                  multiline
                  rows={4}
                  label="Reason for Request"
                  name="requestReason"
                  value={formData.requestReason}
                  onChange={handleChange}
                  placeholder="Explain why this medication is needed (e.g., for specific patient treatment, new protocol requirement, etc.)"
                />
              </Grid>

              <Grid item xs={12}>
                <Box display="flex" gap={2} justifyContent="flex-end">
                  <Button
                    variant="outlined"
                    onClick={() => navigate("/medication-requests")}
                    disabled={loading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    startIcon={
                      loading ? <CircularProgress size={20} /> : <SendIcon />
                    }
                    disabled={loading}
                  >
                    {loading ? "Submitting..." : "Submit Request"}
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

export default MedicationRequestForm;
