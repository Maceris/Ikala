package com.ikalagaming.permissions;

/**
 * Represents an object that can be assigned permissions
 * 
 * @author Ches Burks
 * 
 */
public interface PermissionHolder {

	/**
	 * Checks if this object contains an override for the specified
	 * {@link Permission}
	 * 
	 * @param perm Permission to check
	 * @return true if the permission is set, otherwise false
	 */
	public boolean isPermissionSet(Permission perm);

	/**
	 * Gets the value of the specified permission, if set.
	 * <p>
	 * If a the permission is not overridden on this object, the default value
	 * of the permission will be returned
	 * 
	 * @param perm Permission to get
	 * @return Value of the permission
	 */
	public boolean hasPermission(Permission perm);

	/**
	 * Recalculates the permissions for this object, if the attachments have
	 * changed values.
	 * <p>
	 * This should very rarely need to be called from a plugin.
	 */
	public void recalculatePermissions();
}
