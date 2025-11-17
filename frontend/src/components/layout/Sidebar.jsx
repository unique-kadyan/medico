import { useState, useEffect } from 'react';
import {
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Box,
  Divider,
  Typography,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  Medication as MedicationIcon,
  CalendarMonth as CalendarIcon,
  LocalHospital as DoctorIcon,
  Assessment as ReportsIcon,
  PersonAdd as NurseIcon,
  Science as LabIcon,
  AssignmentInd as AssignmentIcon,
  HowToReg as ApprovalIcon,
  MedicalServices as OTIcon,
  EmergencyShare as EmergencyIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { usePermissions } from '../../hooks/usePermissions';
import { PERMISSIONS } from '../../utils/permissions';
import dashboardService from '../../services/dashboardService';

const drawerWidth = 260;

function Sidebar({ open }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { can, canAny, isAnyRole } = usePermissions();
  const [stats, setStats] = useState({
    totalPatients: 0,
    todayAppointments: 0,
  });

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const data = await dashboardService.getDashboardStats();
      setStats({
        totalPatients: data.totalPatients || 0,
        todayAppointments: data.todayAppointments || 0,
      });
    } catch (error) {
      console.error('Error fetching sidebar stats:', error);
    }
  };

  const handleNavigation = (path) => {
    navigate(path);
  };

  const allMenuItems = [
    {
      text: 'Dashboard',
      icon: <DashboardIcon />,
      path: '/dashboard',
      show: true
    },
    {
      text: 'Patients',
      icon: <PeopleIcon />,
      path: '/patients',
      show: canAny([PERMISSIONS.VIEW_ALL_PATIENTS, PERMISSIONS.VIEW_ASSIGNED_PATIENTS, PERMISSIONS.VIEW_OWN_PROFILE])
    },
    {
      text: 'Doctors',
      icon: <DoctorIcon />,
      path: '/doctors',
      show: can(PERMISSIONS.VIEW_DOCTORS)
    },
    {
      text: 'Nurses',
      icon: <NurseIcon />,
      path: '/nurses',
      show: can(PERMISSIONS.VIEW_NURSES)
    },
    {
      text: 'Pharmacists',
      icon: <AssignmentIcon />,
      path: '/pharmacists',
      show: can(PERMISSIONS.VIEW_PHARMACISTS)
    },
    {
      text: 'Lab Technicians',
      icon: <LabIcon />,
      path: '/lab-technicians',
      show: can(PERMISSIONS.VIEW_LAB_TECHNICIANS)
    },
    {
      text: 'Receptionists',
      icon: <AssignmentIcon />,
      path: '/receptionists',
      show: can(PERMISSIONS.VIEW_RECEPTIONISTS)
    },
    {
      text: 'Doctor Assignments',
      icon: <AssignmentIcon />,
      path: '/doctor-assignments',
      show: can(PERMISSIONS.ASSIGN_DOCTOR_TO_PATIENT)
    },
    {
      text: 'Nurse Assignments',
      icon: <AssignmentIcon />,
      path: '/nurse-assignments',
      show: can(PERMISSIONS.ASSIGN_DOCTOR_TO_PATIENT)
    },
    {
      text: 'Appointments',
      icon: <CalendarIcon />,
      path: '/appointments',
      show: canAny([PERMISSIONS.VIEW_APPOINTMENTS, PERMISSIONS.VIEW_OWN_APPOINTMENTS])
    },
    {
      text: 'Medications',
      icon: <MedicationIcon />,
      path: '/medications',
      show: can(PERMISSIONS.VIEW_MEDICATIONS) || isAnyRole(['PHARMACIST', 'ADMIN'])
    },
    {
      text: 'Med Requests',
      icon: <AssignmentIcon />,
      path: '/medication-requests',
      show: canAny([PERMISSIONS.VIEW_MEDICATION_REQUESTS, PERMISSIONS.APPROVE_MEDICATION_REQUEST])
    },
    {
      text: 'Lab Tests',
      icon: <LabIcon />,
      path: '/lab-tests',
      show: canAny([PERMISSIONS.VIEW_LAB_TESTS, PERMISSIONS.VIEW_OWN_LAB_TESTS, PERMISSIONS.UPLOAD_LAB_RESULTS])
    },
    {
      text: 'OT Requests',
      icon: <OTIcon />,
      path: '/ot-requests',
      show: isAnyRole(['DOCTOR', 'NURSE', 'ADMIN'])
    },
    {
      text: 'Emergency',
      icon: <EmergencyIcon />,
      path: '/emergency',
      show: isAnyRole(['DOCTOR', 'NURSE', 'ADMIN'])
    },
    {
      text: 'Pending Approvals',
      icon: <ApprovalIcon />,
      path: '/approvals',
      show: can(PERMISSIONS.VIEW_PENDING_REGISTRATIONS)
    },
    {
      text: 'Reports',
      icon: <ReportsIcon />,
      path: '/reports',
      show: canAny([PERMISSIONS.VIEW_PATIENT_REPORTS, PERMISSIONS.VIEW_MONITORING])
    },
  ];

  const menuItems = allMenuItems.filter(item => item.show);

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: open ? drawerWidth : 0,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
          bgcolor: 'background.paper',
          borderRight: '1px solid',
          borderColor: 'divider',
          transform: open ? 'translateX(0)' : `translateX(-${drawerWidth}px)`,
          transition: (theme) =>
            theme.transitions.create('transform', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.enteringScreen,
            }),
        },
      }}
    >
      <Box sx={{ height: 64 }} />

      <Box sx={{ overflow: 'auto', pt: 2 }}>
        <Typography
          variant="caption"
          sx={{ px: 3, py: 1, color: 'text.secondary', fontWeight: 600 }}
        >
          MAIN MENU
        </Typography>
        <List>
          {menuItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <ListItemButton
                key={item.text}
                onClick={() => handleNavigation(item.path)}
                sx={{
                  mx: 1.5,
                  mb: 0.5,
                  borderRadius: 2,
                  bgcolor: isActive ? 'primary.main' : 'transparent',
                  color: isActive ? 'white' : 'text.primary',
                  '&:hover': {
                    bgcolor: isActive ? 'primary.dark' : 'action.hover',
                  },
                  '& .MuiListItemIcon-root': {
                    color: isActive ? 'white' : 'text.secondary',
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontSize: '0.875rem',
                    fontWeight: isActive ? 600 : 500,
                  }}
                />
              </ListItemButton>
            );
          })}
        </List>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ px: 3, py: 2 }}>
          <Typography variant="caption" color="text.secondary" fontWeight={600}>
            QUICK STATS
          </Typography>
          <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            <Box
              sx={{
                p: 2,
                borderRadius: 2,
                bgcolor: 'primary.50',
                border: '1px solid',
                borderColor: 'primary.100',
              }}
            >
              <Typography variant="h4" color="primary.main" fontWeight={700}>
                {stats.totalPatients}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Total Patients
              </Typography>
            </Box>
            <Box
              sx={{
                p: 2,
                borderRadius: 2,
                bgcolor: 'success.50',
                border: '1px solid',
                borderColor: 'success.100',
              }}
            >
              <Typography variant="h4" color="success.main" fontWeight={700}>
                {stats.todayAppointments}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Today&apos;s Appointments
              </Typography>
            </Box>
          </Box>
        </Box>
      </Box>
    </Drawer>
  );
}

export default Sidebar;
