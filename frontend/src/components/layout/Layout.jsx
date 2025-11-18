import { useState } from "react";
import { Outlet } from "react-router-dom";
import { Box, useMediaQuery, useTheme } from "@mui/material";
import Header from "./Header";
import Sidebar from "./Sidebar";
import MobileNav from "./MobileNav";

function Layout() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const [sidebarOpen, setSidebarOpen] = useState(!isMobile);

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <Box
      sx={{
        display: "flex",
        minHeight: "100vh",
        bgcolor: "background.default",
      }}
    >
      {/* Sidebar - Desktop only */}
      {!isMobile && (
        <Sidebar open={sidebarOpen} onToggle={handleSidebarToggle} />
      )}

      {/* Main Content Area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          display: "flex",
          flexDirection: "column",
          width: "100%",
          minHeight: "100vh",
        }}
      >
        {/* Header */}
        <Header onMenuClick={handleSidebarToggle} />

        {/* Page Content */}
        <Box
          sx={{
            flexGrow: 1,
            p: { xs: 2, sm: 3 },
            mt: { xs: 7, sm: 8 },
            mb: { xs: 7, sm: 0 },
          }}
        >
          <Outlet />
        </Box>
      </Box>

      {/* Mobile Navigation - Mobile only */}
      {isMobile && <MobileNav />}
    </Box>
  );
}

export default Layout;
