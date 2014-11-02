package com.ikalagaming.core;

/**
 * A distinct chunk of the program with a specific purpose and methods for
 * managing its state and interacting with the main program.
 *
 * @author Ches Burks
 *
 */
public interface Node {
	/**
	 * Which version of the node this is. This changes periodically for each
	 * node subclass as they are changed and updated.
	 */
	final double version = 0.0;

	/**
	 * Deactivates the node and halts all of its operations. The node is still
	 * loaded in memory but not active. Calls {@link #onDisable()}.
	 *
	 * @return true if the node has been successfully disabled
	 */
	public boolean disable();

	/**
	 * Activates the node and enables it to perform its normal functions. Calls
	 * {@link #onEnable()}.
	 *
	 * @return true if the node was successfully enabled
	 */
	public boolean enable();

	/**
	 * Returns the type of node this is. This is a string that describes the
	 * node, such as "Graphics" or "AI".
	 *
	 * @return a string descriptor of the node
	 */
	public String getType();

	/**
	 * Returns this classes version number. This changes periodically for each
	 * node subclass as they are changed and updated.
	 *
	 * @return the version
	 */
	public double getVersion();

	/**
	 * Returns true if the node is enabled, and false otherwise.
	 *
	 * @return true if the node is enabled
	 */
	public boolean isEnabled();

	/**
	 * This method is called when the node is disabled.
	 */
	public void onDisable();

	/**
	 * This method is called when the node is enabled. Initialization tends to
	 * be performed here.
	 */
	public void onEnable();

	/**
	 * Called when the node is loaded into memory. The node may or may not be
	 * enabled at this time.
	 */
	public void onLoad();

	/**
	 * Called when the node is unloaded from memory.
	 */
	public void onUnload();

	/**
	 * Disables and then enables the node.
	 *
	 * @return true if the node restarted successfully
	 */
	public boolean reload();

	/**
	 * Stores a reference to the NodeManager that is handling this node.
	 *
	 * @param parent the parent node manager
	 */
	public void setNodeManager(NodeManager parent);

	/**
	 * Returns the current node manager reference, if it exists.
	 * @return the parent node manager
	 */
	public NodeManager getNodeManager();
}