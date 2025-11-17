import PropTypes from "prop-types";
import { usePermissions } from "../../hooks/usePermissions";

const PermissionGuard = ({
  children,
  permission,
  anyPermission,
  allPermissions,
  role,
  anyRole,
  fallback = null,
}) => {
  const { can, canAny, canAll, isRole, isAnyRole } = usePermissions();

  if (permission && !can(permission)) {
    return fallback;
  }

  if (anyPermission && !canAny(anyPermission)) {
    return fallback;
  }

  if (allPermissions && !canAll(allPermissions)) {
    return fallback;
  }

  if (role && !isRole(role)) {
    return fallback;
  }

  if (anyRole && !isAnyRole(anyRole)) {
    return fallback;
  }

  return children;
};

PermissionGuard.propTypes = {
  children: PropTypes.node.isRequired,
  permission: PropTypes.string,
  anyPermission: PropTypes.arrayOf(PropTypes.string),
  allPermissions: PropTypes.arrayOf(PropTypes.string),
  role: PropTypes.string,
  anyRole: PropTypes.arrayOf(PropTypes.string),
  fallback: PropTypes.node,
};

export default PermissionGuard;
