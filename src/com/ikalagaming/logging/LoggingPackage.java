package com.ikalagaming.logging;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ikalagaming.core.Localization;
import com.ikalagaming.core.Package;
import com.ikalagaming.core.PackageManager;
import com.ikalagaming.core.ResourceLocation;

/**
 * Handles reporting and logging errors.
 *
 * @author Ches Burks
 *
 */
public class LoggingPackage implements Package {

	private ResourceBundle resourceBundle;
	private boolean enabled = false;
	private final double version = 0.1;
	private PackageManager packageManager;
	private String packageName = "logging";
	private LogDispatcher dispatcher;
	private String newLog = "";
	/**
	 * Only logs events that are of this level or higher
	 */
	private LoggingLevel threshold = LoggingLevel.ALL;
	/**
	 * Logs the provided error. Attempts to use localized names for the
	 * error code and logging level. This only logs errors that are above
	 * or equal to the threshold.
	 *
	 * @param eCode The error code
	 * @param level what level is the requested log
	 * @param details additional information about the error
	 */
	public void logError(ErrorCode eCode, LoggingLevel level, String details) {
		newLog = "";
		if (!enabled){
			System.err.println(eCode.getName()
					+ " "
					+ level.getName()
					+ " "
					+ details);
			return;
		}
		if (level.intValue() < threshold.intValue()) {
			return;
		}
		String errorMessage;
		try {
			errorMessage = ResourceBundle.getBundle(
					ResourceLocation.ErrorCodes, Localization.getLocale())
					.getString(eCode.getName());
		} catch (Exception e) {
			errorMessage = eCode.getName();
		}
		try {
			newLog = resourceBundle.getString("level_prefix")
					+ level.getLocalizedName()
					+ resourceBundle.getString("level_postfix")
					+ errorMessage
					+ " " + details;
		}
		catch (Exception e){
			System.err.println(level.getName());
			System.err.println(details);
			e.printStackTrace(System.err);//we need to know what broke the log
		}
		dispatcher.log(newLog);

	}

	/**
	 * Logs the provided error. Attempts to use localized names for the
	 * logging level. This only logs information that is above
	 * or equal to the logging threshold.
	 *
	 * @param level what level is the requested log
	 * @param details what to log
	 */
	public void log(LoggingLevel level, String details) {
		newLog = "";
		if (!enabled){
			System.out.println(level.getName() + " " + details);
			return;
		}
		if (level.intValue() < threshold.intValue()) {
			return;
		}
		try {
			newLog = resourceBundle.getString("level_prefix")
					+ level.getLocalizedName()
					+ resourceBundle.getString("level_postfix")
					+ " "
					+ details;
		}
		catch (Exception e){
			System.err.println(level.getName());
			System.err.println(details);
			e.printStackTrace(System.err);//we need to know what broke the log
		}
		dispatcher.log(newLog);
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
		this.enabled = true;
		try {
			this.onEnable();
		} catch (Exception e) {
			logError(ErrorCode.PACKAGE_ENABLE_FAIL,
					LoggingLevel.SEVERE,
					"LoggingPackage.enable()");
			// better safe than sorry (probably did not initialize correctly)
			this.enabled = false;
			return false;
		}
		return true;
	}

	@Override
	public boolean disable() {
		this.enabled = false;
		try {
			this.onDisable();
		} catch (Exception e) {
			logError(ErrorCode.PACKAGE_DISABLE_FAIL,
					LoggingLevel.SEVERE,
					"LoggingPackage.enable()");
			return false;
		}
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
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onLoad() {
		try {
			resourceBundle = ResourceBundle.getBundle(
					ResourceLocation.LoggingPackage, Localization.getLocale());
		} catch (MissingResourceException missingResource) {
			logError(ErrorCode.LOCALE_RESOURCE_NOT_FOUND,
					LoggingLevel.SEVERE,
					"LoggingPackage.onLoad()");
		}
		dispatcher = new LogDispatcher(this);
		dispatcher.start();
	}

	@Override
	public void onUnload() {
		this.resourceBundle = null;
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