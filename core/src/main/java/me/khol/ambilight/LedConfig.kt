package me.khol.ambilight

class LedConfig : Iterable<Led> {

	private val leds: Array<Led> = arrayOf( // bottom centered WITH leg
		Led(4, 7), Led(3, 7), Led(2, 7), Led(1, 7), Led(0, 7), // bottom left
		Led(0, 6), Led(0, 5), Led(0, 4), Led(0, 3), Led(0, 2), Led(0, 1), // left
		Led(0, 0), Led(1, 0), Led(2, 0), Led(3, 0), Led(4, 0), Led(5, 0), Led(6, 0), // top left
		Led(7, 0), Led(8, 0), Led(9, 0), Led(10, 0), Led(11, 0), Led(12, 0), Led(13, 0), // top right
		Led(13, 1), Led(13, 2), Led(13, 3), Led(13, 4), Led(13, 5), Led(13, 6), // right
		Led(13, 7), Led(12, 7), Led(11, 7), Led(10, 7), Led(9, 7), // bottom right
		Led(8, 7), Led(7, 7), Led(6, 7), Led(5, 7)    // leg
	) // 40 leds

	val ledsWidth = 14
	val ledsHeight = 8
	val ledArray = leds.map { intArrayOf(it.x, it.y) }.toTypedArray()

	val ledCount: Int
		get() = leds.size

	override operator fun iterator() = leds.iterator()
	operator fun get(index: Int) = leds[index]
}

data class Led(val x: Int, val y: Int)
