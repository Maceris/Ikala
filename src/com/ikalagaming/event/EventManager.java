package com.ikalagaming.event;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ikalagaming.core.Package;
import com.ikalagaming.core.PackageManager;
import com.ikalagaming.logging.ErrorCode;
import com.ikalagaming.logging.LoggingLevel;

/**
 * Manages events and listeners.
 */
public class EventManager implements Package {

	private EventDispatcher dispatcher;
	//private ResourceBundle resourceBundle;
	private boolean enabled = false;
	private final double version = 0.1;
	private PackageManager packageManager;
	private HashMap<Class<? extends Event>, HandlerList> handlerMap;
	private String packageName = "event-manager";

	/**
	 * Registers event listeners in the supplied listener.
	 *
	 * @param listener
	 *            The listener to register
	 * @throws Exception
	 *             If there is an exception registering
	 */
	public void registerEventListeners(Listener listener) throws Exception {
		for (Map.Entry<Class<? extends Event>, Set<EventListener>> entry
				: createRegisteredListeners(listener).entrySet()) {
			getEventListeners(entry.getKey())
			.registerAll(entry.getValue());
		}
	}

	/**
	 * Returns a {@link HandlerList} for a give event type. Creates one
	 * if none exist.
	 *
	 * @param type the type of event to find handlers for
	 */
	private HandlerList getEventListeners(Class<? extends Event> type){
		if (!handlerMap.containsKey(type)){
			handlerMap.put(type, new HandlerList());
		}
		return handlerMap.get(type);
	}

	/**
	 * Sends the {@link Event event} to all of its listeners.
	 *
	 * @param event
	 *            The event to fire
	 * @throws IllegalStateException
	 *             if the element cannot be added at this time due to capacity
	 *             restrictions
	 */
	public void fireEvent(Event event) throws IllegalStateException {
		if (!enabled){
			return;
		}
		try {
			dispatcher.dispatchEvent(event);
		} catch (IllegalStateException illegalState) {
			throw illegalState;
		} catch (Exception e) {
			packageManager.getLogger().logError(
					ErrorCode.EVENT_QUEUE_FULL,
					LoggingLevel.WARNING,
					"EventManager.fireEvent(Event)");
		}
	}

	/**
	 * Creates {@link EventListener EventListeners} for a given {@link Listener
	 * listener}.
	 *
	 * @param listener
	 *            The listener to create EventListenrs for
	 * @return A map of events to a set of EventListeners belonging to it
	 */
	private Map<Class<? extends Event>, Set<EventListener>>
	createRegisteredListeners(Listener listener) {

		Map<Class<? extends Event>, Set<EventListener>> toReturn =
				new HashMap<Class<? extends Event>, Set<EventListener>>();
		Set<Method> methods;
		try {
			Method[] publicMethods = listener.getClass().getMethods();
			methods =
					new HashSet<Method>(publicMethods.length, Float.MAX_VALUE);
			for (Method method : publicMethods) {
				methods.add(method);
			}
			for (Method method : listener.getClass().getDeclaredMethods()) {
				methods.add(method);
			}
		} catch (NoClassDefFoundError e) {
			return toReturn;
		}

		// search the methods for listeners
		for (final Method method : methods) {
			final EventHandler handlerAnnotation = method
					.getAnnotation(EventHandler.class);
			if (handlerAnnotation == null)
				continue;
			final Class<?> checkClass;
			if (method.getParameterTypes().length != 1
					|| !Event.class.isAssignableFrom(checkClass = method
					.getParameterTypes()[0])) {
				continue;
			}
			final Class<? extends Event> eventClass = checkClass
					.asSubclass(Event.class);
			method.setAccessible(true);
			Set<EventListener> eventSet = toReturn.get(eventClass);
			if (eventSet == null) {
				eventSet = new HashSet<EventListener>();
				// add the listener methods to the list of events
				toReturn.put(eventClass, eventSet);
			}

			// creates a class to execute the listener for the event
			EventExecutor executor = new EventExecutor() {
				public void execute(Listener listener, Event event)
						throws EventException {
					try {
						if (!eventClass.isAssignableFrom(event.getClass())) {
							return;
						}
						method.invoke(listener, event);
					}
					catch (Throwable t) {
						EventException evtExcept = new EventException(t);
						throw evtExcept;
					}
				}
			};

			eventSet.add(new EventListener(listener, executor));

		}
		return toReturn;
	}

	/**
	 * Returns the handlerlist for the given event.
	 *
	 * @param event the class to find handlers for
	 * @return the handlerlist for that class
	 */
	public HandlerList getHandlers(Event event){
		return getEventListeners(event.getClass());
	}

	@Override
	public String getType() {
		return packageName;
	}

	@Override
	public double getVersion() {
		return version;
	}

	@Override
	public boolean enable() {
		if (isEnabled()){
			return false;
		}
		try {
			this.onEnable();
		} catch (Exception e) {
			packageManager.getLogger().logError(ErrorCode.PACKAGE_ENABLE_FAIL,
					LoggingLevel.SEVERE,
					"EventManager.enable()");
			// better safe than sorry (probably did not initialize correctly)
			this.enabled = false;
			return false;
		}
		this.enabled = true;
		return true;
	}

	@Override
	public boolean disable() {
		if (!isEnabled()){
			return false;
		}
		try {
			this.onDisable();
		} catch (Exception e) {
			packageManager.getLogger().logError(ErrorCode.PACKAGE_DISABLE_FAIL,
					LoggingLevel.SEVERE,
					"EventManager.disable()");
			this.enabled = true;
			return false;
		}
		this.enabled = false;
		return true;
	}

	@Override
	public boolean reload() {
		if (this.enabled) {
			this.disable();
		}
		this.enable();
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void onEnable() {
		dispatcher = new EventDispatcher(this);
		dispatcher.start();
		handlerMap = new HashMap<Class<? extends Event>, HandlerList>();
	}

	@Override
	public void onDisable() {
		for (HandlerList l : handlerMap.values()){
			l.unregisterAll();
		}
		handlerMap.clear();

		dispatcher.terminate();
		try {
			dispatcher.join();
		} catch (InterruptedException e) {
			packageManager.getLogger().logError(ErrorCode.THREAD_INTERRUPTED,
					LoggingLevel.WARNING,
					"EventManager.onDisable()");
		}

	}

	@Override
	public void onLoad() {
	}

	@Override
	public void onUnload() {
		//this.resourceBundle = null;
		this.packageManager = null;
	}

	@Override
	public void setPackageManager(PackageManager parent) {
		this.packageManager = parent;
	}

	@Override
	public PackageManager getPackageManager(){
		return this.packageManager;
	}
}
