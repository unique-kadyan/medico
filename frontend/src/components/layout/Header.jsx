import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  Menu,
  MenuItem,
  Avatar,
  Badge,
  Tooltip,
  Divider,
  Button,
} from "@mui/material";
import {
  Menu as MenuIcon,
  Notifications as NotificationsIcon,
  AccountCircle,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  ClearAll as ClearAllIcon,
} from "@mui/icons-material";
import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { logout } from "../../store/slices/authSlice";

function Header({ onMenuClick }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const [anchorEl, setAnchorEl] = useState(null);
  const [notifAnchorEl, setNotifAnchorEl] = useState(null);
  const [settingsAnchorEl, setSettingsAnchorEl] = useState(null);
  const [notifications, setNotifications] = useState([
    { id: 1, message: "New appointment booked", read: false },
    { id: 2, message: "Low stock alert: Aspirin", read: false },
    { id: 3, message: "Patient discharge completed", read: false },
  ]);

  const handleProfileMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleNotifMenuOpen = (event) => {
    setNotifAnchorEl(event.currentTarget);
  };

  const handleSettingsMenuOpen = (event) => {
    setSettingsAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setNotifAnchorEl(null);
    setSettingsAnchorEl(null);
  };

  const handleClearAllNotifications = () => {
    setNotifications([]);
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  return (
    <AppBar
      position="fixed"
      sx={{
        zIndex: (theme) => theme.zIndex.drawer + 1,
        bgcolor: "white",
        color: "text.primary",
        boxShadow: 1,
      }}
    >
      <Toolbar>
        <IconButton
          edge="start"
          color="inherit"
          aria-label="menu"
          onClick={onMenuClick}
          sx={{ mr: 2, display: { sm: "block" } }}
        >
          <MenuIcon />
        </IconButton>

        <Box sx={{ display: "flex", alignItems: "center", flexGrow: 1 }}>
          {user?.hospitalLogoUrl ? (
            <Avatar
              src={user.hospitalLogoUrl}
              alt={user.hospitalName || "Hospital"}
              variant="rounded"
              sx={{ width: 40, height: 40, mr: 1.5 }}
            />
          ) : (
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: 1,
                bgcolor: "primary.main",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                mr: 1.5,
              }}
            >
              <Typography sx={{ fontSize: 20 }}>🏥</Typography>
            </Box>
          )}
          <Box>
            <Typography
              variant="h6"
              component="div"
              sx={{
                fontWeight: 700,
                lineHeight: 1.2,
                background: "linear-gradient(45deg, #2196f3 30%, #21cbf3 90%)",
                backgroundClip: "text",
                textFillColor: "transparent",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
              }}
            >
              {user?.hospitalName || "Medico"}
            </Typography>
            <Typography
              variant="caption"
              sx={{ display: { xs: "none", sm: "block" }, lineHeight: 1 }}
              color="text.secondary"
            >
              {user?.hospitalCode
                ? `Code: ${user.hospitalCode}`
                : "Hospital Management System"}
            </Typography>
          </Box>
        </Box>

        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          {/* Notifications */}
          <Tooltip title="Notifications">
            <IconButton color="inherit" onClick={handleNotifMenuOpen}>
              <Badge badgeContent={notifications.length} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>

          <Tooltip title="Settings">
            <IconButton color="inherit" onClick={handleSettingsMenuOpen}>
              <SettingsIcon />
            </IconButton>
          </Tooltip>

          <Tooltip title="Account">
            <IconButton onClick={handleProfileMenuOpen} sx={{ p: 0, ml: 1 }}>
              <Avatar sx={{ bgcolor: "primary.main" }}>
                {user?.name?.charAt(0) || "U"}
              </Avatar>
            </IconButton>
          </Tooltip>
        </Box>

        <Menu
          anchorEl={notifAnchorEl}
          open={Boolean(notifAnchorEl)}
          onClose={handleMenuClose}
          PaperProps={{
            sx: { width: 320, maxHeight: 400 },
          }}
        >
          <Box
            sx={{
              px: 2,
              py: 1.5,
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <Typography variant="h6" fontWeight={600}>
              Notifications
            </Typography>
            {notifications.length > 0 && (
              <Button
                size="small"
                startIcon={<ClearAllIcon />}
                onClick={(e) => {
                  e.stopPropagation();
                  handleClearAllNotifications();
                }}
              >
                Clear All
              </Button>
            )}
          </Box>
          <Divider />
          {notifications.length === 0 ? (
            <MenuItem disabled>
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{ textAlign: "center", width: "100%", py: 2 }}
              >
                No notifications
              </Typography>
            </MenuItem>
          ) : (
            notifications.map((notif) => (
              <MenuItem key={notif.id} onClick={handleMenuClose}>
                <Typography variant="body2">{notif.message}</Typography>
              </MenuItem>
            ))
          )}
        </Menu>

        <Menu
          anchorEl={settingsAnchorEl}
          open={Boolean(settingsAnchorEl)}
          onClose={handleMenuClose}
          transformOrigin={{ horizontal: "right", vertical: "top" }}
          anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
        >
          <MenuItem onClick={handleMenuClose}>
            <AccountCircle sx={{ mr: 1 }} /> Account Settings
          </MenuItem>
          <MenuItem onClick={handleMenuClose}>
            <NotificationsIcon sx={{ mr: 1 }} /> Notification Preferences
          </MenuItem>
          <Divider />
          <MenuItem onClick={handleMenuClose}>
            <SettingsIcon sx={{ mr: 1 }} /> System Settings
          </MenuItem>
        </Menu>

        {/* Profile Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
          onClick={handleMenuClose}
          transformOrigin={{ horizontal: "right", vertical: "top" }}
          anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
        >
          <MenuItem disabled>
            <Box>
              <Typography variant="subtitle2">
                {user?.name || "User"}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {user?.email || "user@example.com"}
              </Typography>
            </Box>
          </MenuItem>
          <MenuItem>
            <AccountCircle sx={{ mr: 1 }} /> Profile
          </MenuItem>
          <MenuItem>
            <SettingsIcon sx={{ mr: 1 }} /> Settings
          </MenuItem>
          <MenuItem onClick={handleLogout}>
            <LogoutIcon sx={{ mr: 1 }} /> Logout
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}

export default Header;
