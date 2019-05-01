package ambilight.mods

import ambilight.LedColor

abstract class Mod(val ledCount: Int) {

	open fun isUseful(): Boolean = true

	open fun update(colors: Array<LedColor>): Array<LedColor> {
		if (isUseful()) {
			for (i in 0 until ledCount) {
				update(colors[i])
			}
		}
		return colors
	}

	open fun update(led: LedColor) {
		// empty
	}
}
