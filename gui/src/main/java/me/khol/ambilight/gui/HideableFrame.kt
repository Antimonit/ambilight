package me.khol.ambilight.gui

import java.awt.*
import java.awt.event.*
import javax.swing.*

open class HideableFrame(title: String, iconPath: String) : JFrame(title) {

	protected var minimizeAutomatically: Boolean = false
	private lateinit var trayIcon: TrayIcon
	private lateinit var mouseClickPoint: Point

	init {
		val icon = ImageIcon(this.javaClass.getResource(iconPath)).image
		iconImage = icon

		if (SystemTray.isSupported()) {

			val popup = PopupMenu().apply {
				add(MenuItem("Open").apply {
					addActionListener {
						showFrame()
					}
				})
				add(MenuItem("Exit").apply {
					addActionListener {
						SystemTray.getSystemTray().remove(trayIcon)
						System.exit(0)
					}
				})
			}

			trayIcon = TrayIcon(icon, title, popup).apply {
				isImageAutoSize = true
				addMouseListener(object : MouseAdapter() {
					override fun mouseClicked(e: MouseEvent) {
						super.mouseClicked(e)
						if (e.button == MouseEvent.BUTTON1) {
							if (isShowing) {
								hideFrame()
							} else {
								showFrame()
							}
						}
					}
				})
			}

			try {
				SystemTray.getSystemTray().add(trayIcon)
			} catch (e: AWTException) {
				e.printStackTrace()
			}

			addWindowStateListener { e ->
				isVisible = e.newState and Frame.ICONIFIED != Frame.ICONIFIED
			}

			minimizeAutomatically = true
			addWindowFocusListener(object : WindowFocusListener {
				override fun windowGainedFocus(e: WindowEvent) {
					if (minimizeAutomatically) {
						showFrame()
					}
				}

				override fun windowLostFocus(e: WindowEvent) {
					if (minimizeAutomatically) {
						hideFrame()
					}
				}
			})

			defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE

		} else {
			println("system tray not supported")
			defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
		}

		addMouseListener(object : MouseAdapter() {
			override fun mousePressed(e: MouseEvent) {
				mouseClickPoint = e.point
			}
		})

		addMouseMotionListener(object : MouseAdapter() {
			override fun mouseDragged(e: MouseEvent) {
				val x = location.x + e.x - mouseClickPoint.x
				val y = location.y + e.y - mouseClickPoint.y
				setLocation(x, y)
			}
		})
	}

	fun resetLocation() {
		setLocation(
			Toolkit.getDefaultToolkit().screenSize.width - width - 16,
			Toolkit.getDefaultToolkit().screenSize.height - height - 56
		)
	}


	protected fun showFrame() {
		extendedState = JFrame.NORMAL
		isVisible = true
		toFront()
//		requestFocus();
	}

	protected fun hideFrame() {
		extendedState = JFrame.ICONIFIED
		isVisible = false
	}

	protected fun exit() {
		if (SystemTray.isSupported()) {
			SystemTray.getSystemTray().remove(trayIcon)
		}
		System.exit(0)
	}
}