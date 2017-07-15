package ambilight.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HideableFrame extends JFrame {

	protected boolean minimizeAutomatically;
	private TrayIcon trayIcon;
	private Point mouseClickPoint;

	public HideableFrame(String title, String iconPath) {
		super(title);

		Image icon = new ImageIcon(this.getClass().getResource(iconPath)).getImage();
		setIconImage(icon);

		if (SystemTray.isSupported()) {

			MenuItem openItem = new MenuItem("Open");
			openItem.addActionListener(e -> {
				showFrame();
			});

			MenuItem exitItem = new MenuItem("Exit");
			exitItem.addActionListener(e -> {
				SystemTray.getSystemTray().remove(trayIcon);
				System.exit(0);
			});

			PopupMenu popup = new PopupMenu();
			popup.add(openItem);
			popup.add(exitItem);

			trayIcon = new TrayIcon(icon, title, popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (isShowing()) {
							hideFrame();
						} else {
							showFrame();
						}
					}
				}
			});


			try {
				SystemTray.getSystemTray().add(trayIcon);
			} catch (AWTException e) {
				e.printStackTrace();
			}

			addWindowStateListener(new WindowStateListener() {
				@Override
				public void windowStateChanged(WindowEvent e) {
					if ((e.getNewState() & ICONIFIED) == ICONIFIED) {
						setVisible(false);
					} else {
						setVisible(true);
					}
				}
			});

			minimizeAutomatically = true;
			addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowGainedFocus(WindowEvent e) {
					if (minimizeAutomatically)
						showFrame();
				}

				@Override
				public void windowLostFocus(WindowEvent e) {
					if (minimizeAutomatically)
						hideFrame();
				}
			});

			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		} else {
			System.out.println("system tray not supported");
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		}

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				mouseClickPoint = e.getPoint();
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				int X = getLocation().x + e.getX() - mouseClickPoint.x;
				int Y = getLocation().y + e.getY() - mouseClickPoint.y;
				setLocation(X, Y);
			}
		});

	}

	public void resetLocation() {
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width - getWidth() - 16,
					Toolkit.getDefaultToolkit().getScreenSize().height - getHeight() - 56);
	}

	public void setMinimizeAutomatically(boolean minimizeAutomatically) {
		this.minimizeAutomatically = minimizeAutomatically;
	}

	protected void showFrame() {
//		System.out.println("Show");
		setExtendedState(JFrame.NORMAL);
		setVisible(true);
		toFront();
//		requestFocus();
	}

	protected void hideFrame() {
//		System.out.println("Hide");
		setExtendedState(JFrame.ICONIFIED);
		setVisible(false);
	}

	protected void exit() {
		if (trayIcon != null)
			SystemTray.getSystemTray().remove(trayIcon);
		System.exit(0);
	}

}