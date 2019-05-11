package me.khol.ambilight.gui

import me.khol.ambilight.LedConfig

import javax.swing.*
import java.awt.*

class PreviewPanel(private val config: LedConfig) : JPanel() {

	private var colors: List<Color> = List(config.ledCount) { Color.BLACK }

	fun setColors(segmentColors: Array<ByteArray>) {
		colors = segmentColors.map { color: ByteArray ->
			val red = ((color[0] + 256) % 256).toShort()
			val green = ((color[1] + 256) % 256).toShort()
			val blue = ((color[2] + 256) % 256).toShort()
			Color(red.toInt(), green.toInt(), blue.toInt())
		}

		repaint()
	}

	public override fun paintComponent(g: Graphics) {
		super.paintComponent(g)
		val g2d = g as Graphics2D

		config.forEachIndexed { index, (x, y) ->
			drawRect(g2d, colors[index], x, y)
		}
	}

	private fun drawRect(g2d: Graphics2D, rgb: Color, ledX: Int, ledY: Int) {
		val previewWidth = width + 1
		val previewHeight = height
		val left = previewWidth * ledX / config.ledsWidth - 1
		val top = previewHeight * ledY / config.ledsHeight
		val width = previewWidth * (ledX + 1) / config.ledsWidth - previewWidth * ledX / config.ledsWidth
		val height = previewHeight * (ledY + 1) / config.ledsHeight - previewHeight * ledY / config.ledsHeight

		g2d.color = rgb
		g2d.fillRect(left, top, width, height)
		g2d.color = Color.BLACK
		g2d.drawRect(left, top, width, height)
	}
}
