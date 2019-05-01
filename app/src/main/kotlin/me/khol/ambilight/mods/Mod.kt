package me.khol.ambilight.mods

import me.khol.ambilight.LedColor

/**
 * Mods are composed into a stream where one mod is applied after another.
 * Each mod maps values produced by the previous mod and supply it to the next mod.
 */
interface Mod {

	/**
	 * Processes an array of colors and returns an array of colors.
	 *
	 * Most of the mods will return the same array that was passed as an argument but it is
	 * absolutely fine to swap the array for another one to be consumed by following mods.
	 */
	fun update(colors: Array<LedColor>): Array<LedColor>
}

/**
 * Template for [Mods][Mod] that operate each color separately.
 */
abstract class DiscreteMod : Mod {

	/**
	 * Skip [update] if the condition is false.
	 */
	open fun isUseful(): Boolean = true

	final override fun update(colors: Array<LedColor>): Array<LedColor> {
		if (isUseful()) {
			for (color in colors) {
				update(color)
			}
		}
		return colors
	}

	/**
	 * Processes each color separately.
	 */
	abstract fun update(led: LedColor)
}
