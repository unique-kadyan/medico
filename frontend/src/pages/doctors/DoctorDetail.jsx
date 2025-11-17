import { Typography, Box, Card, CardContent } from '@mui/material';

function DoctorDetail() {
  return (
    <Box>
      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight={700}>Doctor Profile</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Doctor profile details will be displayed here...
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}

export default DoctorDetail;
