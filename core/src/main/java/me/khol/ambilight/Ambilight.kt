package me.khol.ambilight

abstract class Ambilight {

	abstract fun getScreenSegmentsColors(): Array<LedColor>

	fun benchmarkOnce() {
		val start = System.currentTimeMillis()
		getScreenSegmentsColors()
		val end = System.currentTimeMillis()

		val duration = end - start

		println("Time spent: $duration ms.")
	}

	fun benchmarkRepeatedly(ms: Int) {
		val start = System.currentTimeMillis()
		var counter = 0
		while (true) {
			val current = System.currentTimeMillis()
			if (current - start > ms) {
				break
			}
			getScreenSegmentsColors()
			counter++
		}
		println("During " + ms + "ms, called " + counter + " times.")
	}
}
