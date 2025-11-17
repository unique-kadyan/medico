import { Typography, Box, Card, CardContent } from '@mui/material';

function AppointmentForm() {
  return (
    <Box>
      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight={700}>Book Appointment</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Appointment booking form will be implemented here...
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}

export default AppointmentForm;
