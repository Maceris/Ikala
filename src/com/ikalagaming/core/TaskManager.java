
package com.ikalagaming.core;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.ikalagaming.core.packages.PackageState;

/**
 * Displays various information about the program.
 * 
 * @author Ches Burks
 *
 */
public class TaskManager extends JFrame {

	private static final long serialVersionUID = -4427516209866980363L;
	private JPanel contentPane;
	private JTable table;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private long delay = 1000;// low long to wait between updates
	private DefaultTableModel model;
	private Map<String, PackageState> packages =
			new HashMap<String, PackageState>();
	private final int maxTick = 10;
	private int tickCount = 0;// counts down to refreshing package list
	JPopupMenu popupMenu;

	/**
	 * Updates and displays information about the program
	 */
	public void tick() {
		if (tickCount == 0) {
			updatePackageNames();
			tickCount = maxTick;
		}
		--tickCount;

		PackageState currentState;
		String name = "";
		for (int i = 0; i < model.getRowCount(); ++i) {
			name = (String) model.getValueAt(i, 0);
			currentState =
					Game.getPackageManager().getPackage(name).getPackageState();
			if (!model.getValueAt(i, 1).equals(currentState)) {
				model.setValueAt(currentState, i, 1);
			}
		}
	}

	private void updatePackageNames() {
		Set<String> packageNames =
				Game.getPackageManager().getLoadedPackages().keySet();
		for (String s : packageNames) {
			if (!packages.containsKey(s)) {
				packages.put(s, Game.getPackageManager().getPackage(s)
						.getPackageState());
				boolean exists = false;
				for (int i = 0; i < model.getRowCount(); ++i) {
					if (model.getValueAt(i, 0).equals(s)) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					model.addRow(new Object[] {s, packages.get(s)});
				}
			}
		}
		for (String s : packages.keySet()) {
			if (!packageNames.contains(s)) {
				packages.remove(s);
				for (int i = 0; i < model.getRowCount(); ++i) {
					if (model.getValueAt(i, 0).equals(s)) {
						model.removeRow(i);
						break;
					}
				}
			}
		}
		packageNames = null;

	}

	/**
	 * Returns the length of time to wait between refreshes.
	 * 
	 * @return the delay time in ms
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Create the frame.
	 * 
	 * @param owner the game this task manager handles
	 */
	public TaskManager(Game owner) {
		setTitle("KOI Task Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 465);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNewPackage = new JMenuItem("New Package");
		mnFile.add(mntmNewPackage);

		JMenuItem mntmExitTaskManager = new JMenuItem("Exit Task Manager");
		mnFile.add(mntmExitTaskManager);

		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		JMenuItem mntmUpdateNow = new JMenuItem("Update now");
		mnView.add(mntmUpdateNow);

		JMenu mnUpdateSpeed = new JMenu("Update Speed");
		mnView.add(mnUpdateSpeed);

		JRadioButtonMenuItem rdbtnmntmHigh = new JRadioButtonMenuItem("High");
		rdbtnmntmHigh.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					delay = 500;
				}
			}
		});
		buttonGroup.add(rdbtnmntmHigh);
		mnUpdateSpeed.add(rdbtnmntmHigh);

		JRadioButtonMenuItem rdbtnmntmNormal =
				new JRadioButtonMenuItem("Normal");
		rdbtnmntmNormal.setSelected(true);
		rdbtnmntmNormal.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					delay = 1000;
				}
			}
		});
		buttonGroup.add(rdbtnmntmNormal);
		mnUpdateSpeed.add(rdbtnmntmNormal);

		JRadioButtonMenuItem rdbtnmntmLow = new JRadioButtonMenuItem("Low");
		rdbtnmntmLow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					delay = 2000;
				}
			}
		});
		buttonGroup.add(rdbtnmntmLow);
		mnUpdateSpeed.add(rdbtnmntmLow);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("Packages", null, scrollPane, null);

		table = new JTable();
		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] {
				"Package Name", "Status"}));
		table.getColumnModel().getColumn(0).setPreferredWidth(102);

		popupMenu = new JPopupMenu();

		JMenuItem mntmEnable = new JMenuItem("Enable");
		popupMenu.add(mntmEnable);

		JMenuItem mntmDisable = new JMenuItem("Disable");
		popupMenu.add(mntmDisable);

		JMenuItem mntmUnload = new JMenuItem("Unload");
		popupMenu.add(mntmUnload);

		JMenuItem mntmReload = new JMenuItem("Reload");
		popupMenu.add(mntmReload);

		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JTable source = (JTable) e.getSource();
					int row = source.rowAtPoint(e.getPoint());
					int column = source.columnAtPoint(e.getPoint());

					if (!source.isRowSelected(row))
						source.changeSelection(row, column, false, false);

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		scrollPane.setViewportView(table);
		model = (DefaultTableModel) table.getModel();

		Updater update = new Updater(this);
		update.start();
	}

}

class Updater extends Thread {
	TaskManager manager;

	public Updater(TaskManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {
		while (true) {
			manager.tick();
			try {
				sleep(manager.getDelay());
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}