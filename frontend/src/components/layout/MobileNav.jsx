import { Paper, BottomNavigation, BottomNavigationAction } from "@mui/material";
import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  Medication as MedicationIcon,
  CalendarMonth as CalendarIcon,
  LocalHospital as DoctorIcon,
} from "@mui/icons-material";
import { useNavigate, useLocation } from "react-router-dom";

function MobileNav() {
  const navigate = useNavigate();
  const location = useLocation();

  const getActiveValue = () => {
    const path = location.pathname;
    if (path.startsWith("/dashboard")) return 0;
    if (path.startsWith("/patients")) return 1;
    if (path.startsWith("/medications")) return 2;
    if (path.startsWith("/appointments")) return 3;
    if (path.startsWith("/doctors")) return 4;
    return 0;
  };

  const handleChange = (event, newValue) => {
    const paths = [
      "/dashboard",
      "/patients",
      "/medications",
      "/appointments",
      "/doctors",
    ];
    navigate(paths[newValue]);
  };

  return (
    <Paper
      sx={{
        position: "fixed",
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 1000,
      }}
      elevation={3}
    >
      <BottomNavigation
        value={getActiveValue()}
        onChange={handleChange}
        showLabels
      >
        <BottomNavigationAction label="Dashboard" icon={<DashboardIcon />} />
        <BottomNavigationAction label="Patients" icon={<PeopleIcon />} />
        <BottomNavigationAction label="Meds" icon={<MedicationIcon />} />
        <BottomNavigationAction label="Appts" icon={<CalendarIcon />} />
        <BottomNavigationAction label="Doctors" icon={<DoctorIcon />} />
      </BottomNavigation>
    </Paper>
  );
}

export default MobileNav;
