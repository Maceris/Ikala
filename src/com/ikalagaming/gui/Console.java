package com.ikalagaming.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import com.ikalagaming.core.Localization;
import com.ikalagaming.core.Package;
import com.ikalagaming.core.PackageManager;
import com.ikalagaming.core.ResourceLocation;
import com.ikalagaming.core.events.PackageEvent;
import com.ikalagaming.event.EventHandler;
import com.ikalagaming.event.Listener;
import com.ikalagaming.logging.ErrorCode;
import com.ikalagaming.logging.LoggingLevel;

/**
 * A simple console.
 *
 * @author Ches Burks
 *
 */
public class Console extends WindowAdapter implements Package, Listener{
	private ResourceBundle resourceBundle;
	private String windowTitle;
	private int width = 300;
	private int height = 200;
	private int maxLineCount = 150;
	private Color background = new Color(2,3,2);
	private Color foreground = new Color(2,200,2);

	private JFrame frame;
	private JTextArea textArea;
	private JTextField inputArea;

	private PackageManager packageManager;
	private String packageName = "console";
	private boolean enabled = false;
	private final double version = 0.1;

	private void init(){
		frame = new JFrame(windowTitle);
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setBackground(background);
		frame.setForeground(foreground);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(background);
		textArea.setForeground(foreground);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		inputArea = new JTextField();
		inputArea.setEditable(true);
		inputArea.setBackground(background);
		inputArea.setForeground(foreground);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea),
				BorderLayout.CENTER);
		frame.getContentPane().add(inputArea, BorderLayout.SOUTH);

		frame.setVisible(true);

	}

	/**
	 * Returns window width.
	 * This is the width of the frame the console is in.
	 *
	 * @return the width of the frame
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the frame width.
	 * This is the width of the frame the console is in.
	 *
	 * @param width The new width
	 */
	public void setWidth(int width) {
		this.width = width;
		frame.setSize(width, frame.getHeight());
	}

	/**
	 * Returns the window height.
	 * This is the height of the frame the console is in.
	 *
	 * @return the height of the frame
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the frame height.
	 * This is the height of the frame the console is in.
	 *
	 * @param height The new height
	 */
	public void setHeight(int height) {
		this.height = height;
		frame.setSize(frame.getWidth(), height);
	}

	/**
	 * Returns the maximum number of lines that are
	 * stored in the window.
	 *
	 * @return the max number of lines
	 */
	public int getMaxLineCount() {
		return maxLineCount;
	}

	/**
	 * Sets the maximum number of lines stored in the window.
	 *
	 * @param maxLineCount the maximum number of lines to store
	 */
	public void setMaxLineCount(int maxLineCount) {
		this.maxLineCount = maxLineCount;
	}

	/**
	 * Returns the window title.
	 *
	 * @return the String that is used as the title
	 */
	public String getWindowTitle() {
		return windowTitle;
	}

	/**
	 * Sets the title of the window.
	 *
	 * @param windowTitle the String to use as the title
	 */
	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
		frame.setTitle(windowTitle);
	}

	/**
	 * Adds a String to the bottom of the console.
	 * Removes the top lines if/while they exceed the maximum line count.
	 *
	 * @param message The message to append
	 */
	public synchronized void appendMessage(String message){
		this.textArea.append(message+System.lineSeparator());

		while (this.textArea.getLineCount() >= maxLineCount){
			int end;
			try {
				end = this.textArea.getLineEndOffset(0);
				this.textArea.replaceRange("", 0, end);
			} catch (BadLocationException e) {
				packageManager.getLogger().logError(
						ErrorCode.EXCEPTION,
						LoggingLevel.WARNING, "Console.appendMessage(String)");
			}
		}
	}

	@Override
	public boolean disable() {
		onDisable();
		enabled = false;
		return true;
	}

	@Override
	public boolean enable() {
		onEnable();
		enabled = true;
		return true;
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void onDisable() {
		frame.setVisible(false);
		frame.dispose();

	}

	@Override
	public void onEnable() {
		init();
	}

	@Override
	public void onLoad() {
		try {
			resourceBundle = ResourceBundle.getBundle(ResourceLocation.Console,
					Localization.getLocale());
		}
		catch (MissingResourceException missingResource){
			packageManager.getLogger().logError(ErrorCode.LOCALE_NOT_FOUND,
					LoggingLevel.WARNING, "Console.onLoad()");
		}
		try{
			windowTitle = resourceBundle.getString("title");
		}
		catch (MissingResourceException missingResource){
			packageManager.getLogger().logError(ErrorCode.LOCALE_NOT_FOUND,
					LoggingLevel.WARNING, "Console.onLoad()");
		}
		catch (ClassCastException classCast){
			packageManager.getLogger().logError(
					ErrorCode.LOCALE_RESOURCE_WRONG_TYPE,
					LoggingLevel.WARNING, "Console.onLoad()");
		}
	}

	@Override
	public void onUnload() {
		resourceBundle = null;
	}

	@Override
	public boolean reload() {
		if(!disable()){
			//failed to disable
			return false;
		}
		if (!enable()){
			//failed to enable
			return false;
		}
		return true;
	}

	@Override
	public void setPackageManager(PackageManager parent) {
		this.packageManager = parent;
	}

	@Override
	public PackageManager getPackageManager() {
		return this.packageManager;
	}

	/**
	 * Called when a package event is sent out by the event system.
	 * @param event the event that was fired
	 */
	@EventHandler
	public void onPackageEvent(PackageEvent event){
		if (event.getTo() != packageName){
			return;
		}
		String callMethod = "call";
		String onLoad = "onLoad";
		String onUnload = "onUnload";
		String enable = "enable";
		String disable = "disable";


		try{
			ResourceBundle packageBundle;
			packageBundle = packageManager.getResourceBundle();
			callMethod = packageBundle.getString("CMD_CALL");
			onLoad = packageBundle.getString("ARG_ON_LOAD");
			onUnload = packageBundle.getString("ARG_ON_UNLOAD");
			enable = packageBundle.getString("ARG_ENABLE");
			disable = packageBundle.getString("ARG_DISABLE");
		}
		catch (MissingResourceException missingResource){
			packageManager.getLogger().logError(
					ErrorCode.LOCALE_RESOURCE_NOT_FOUND,
					LoggingLevel.WARNING,
					"PackageManager.loadPackage(Package) load");
		}
		catch (ClassCastException classCast){
			packageManager.getLogger().logError(
					ErrorCode.LOCALE_RESOURCE_WRONG_TYPE,
					LoggingLevel.WARNING,
					"PackageManager.loadPackage(Package) load");
		}

		if (event.getMessage().startsWith(callMethod)){
			String trimmed = event.getMessage().replaceFirst(callMethod, "");
			trimmed = trimmed.replaceFirst(" ", "");
			if (trimmed.startsWith(onLoad)){
				onLoad();
			}
			else if (trimmed.startsWith(onUnload)){
				onUnload();
			}
			else if (trimmed.startsWith(enable)){
				enable();
			}
			else if (trimmed.startsWith(disable)){
				disable();
			}
		}
	}

}
