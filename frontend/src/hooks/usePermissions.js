import { useSelector } from "react-redux";
import {
  hasPermission,
  hasAnyPermission,
  hasAllPermissions,
  hasRole,
  hasAnyRole,
} from "../utils/permissions";

export const usePermissions = () => {
  const user = useSelector((state) => state.auth.user);
  const permissions = user?.permissions || [];
  const role = user?.role;

  return {
    can: (permission) => hasPermission(permission, permissions),
    canAny: (permissionList) => hasAnyPermission(permissionList, permissions),

    canAll: (permissionList) => hasAllPermissions(permissionList, permissions),
    isRole: (roleName) => hasRole(roleName, role),

    isAnyRole: (roleList) => hasAnyRole(roleList, role),

    getPermissions: () => permissions,

    getRole: () => role,
    isAuthenticated: () => !!user,
  };
};
