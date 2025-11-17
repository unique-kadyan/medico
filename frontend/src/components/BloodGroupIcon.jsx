import { Box, Typography } from '@mui/material';
import { Bloodtype as BloodtypeIcon } from '@mui/icons-material';

const BloodGroupIcon = ({ bloodGroup, size = 'medium' }) => {
  if (!bloodGroup) return null;

  // Parse blood group (e.g., "A_POSITIVE" -> "A+")
  const formatBloodGroup = (bg) => {
    const formatted = bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
    return formatted;
  };

  // Get color based on blood type
  const getBloodGroupColor = (bg) => {
    const type = bg.split('_')[0];
    const colors = {
      'A': '#ef4444',    // Red
      'B': '#3b82f6',    // Blue
      'AB': '#8b5cf6',   // Purple
      'O': '#22c55e',    // Green
    };
    return colors[type] || '#6b7280';
  };

  const iconSize = {
    small: 32,
    medium: 40,
    large: 48,
  };

  const fontSize = {
    small: '0.75rem',
    medium: '0.875rem',
    large: '1rem',
  };

  const formatted = formatBloodGroup(bloodGroup);
  const color = getBloodGroupColor(bloodGroup);

  return (
    <Box
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        width: iconSize[size],
        height: iconSize[size],
        borderRadius: '50%',
        bgcolor: `${color}15`,
        border: `2px solid ${color}`,
      }}
    >
      <BloodtypeIcon
        sx={{
          position: 'absolute',
          fontSize: iconSize[size] * 0.5,
          color: `${color}40`,
        }}
      />
      <Typography
        sx={{
          fontSize: fontSize[size],
          fontWeight: 700,
          color: color,
          position: 'relative',
          zIndex: 1,
          textShadow: '0 0 2px rgba(255,255,255,0.8)',
        }}
      >
        {formatted}
      </Typography>
    </Box>
  );
};

export default BloodGroupIcon;
