package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class SmoothnessModTest : ModTest {

	@Test
	fun `zero smoothness returns input colors`() {
		val mod = SmoothnessMod(1, 0)

		val colors = arrayOf(LedColor(0f, 127f, 255f))
		val expectedColors = colors.copy()
		val resultColors = mod.update(colors)

		Assertions.assertThat(expectedColors).isEqualTo(resultColors)
	}

	@Test
	fun `calling update multiple times produces different colors`() {
		val mod = SmoothnessMod(1, 100)

		val colors = arrayOf(LedColor(0f, 127f, 255f))
		val firstPass = mod.update(colors).copy()
		val secondPass = mod.update(colors).copy()

		Assertions.assertThat(firstPass).isNotEqualTo(secondPass)
	}
}