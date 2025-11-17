import { Box, Typography, Paper, Grid } from '@mui/material';

function Reports() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Reports
      </Typography>

      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Hospital Reports Dashboard
            </Typography>
            <Typography variant="body1" color="text.secondary">
              View and generate various hospital reports including patient records, appointments, medications, and more.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Patient Reports
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Access patient admission records, discharge summaries, and medical history reports.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Appointment Reports
            </Typography>
            <Typography variant="body2" color="text.secondary">
              View appointment statistics, doctor schedules, and appointment history.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Medication Reports
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Monitor medication inventory, dispensing records, and stock levels.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Financial Reports
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Generate billing reports, revenue analysis, and financial summaries.
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}

export default Reports;
