package com.ikalagaming.entity.component;

/**
 * A useful part of an entity. All entities are essentially a generic thing that
 * has various components added to it to make something unique. These are like
 * interfaces that can be added and removed from entities dynamically to modify
 * their behavior.
 *
 * @author Ches Burks
 *
 */
public abstract class Component {

	/**
	 * Returns the type of this component. Each different component should have
	 * a different componentType so that they do not have a collision and get
	 * confused for each other.
	 *
	 * @return the string representing what component this is
	 */
	public abstract String getType();
}
