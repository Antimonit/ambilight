package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class SaturationModTest: ModTest {

	@Test
	fun `does not update colors at 100% saturation`() {
		val mod = SaturationMod(1f)

		val color = LedColor(0f, 127f, 255f)
		val originalColor = color.copy()
		mod.update(color)

		Assertions.assertThat(originalColor).isEqualTo(color)
	}

	@Test
	fun `converts to grayscale at 0% saturation`() {
		val mod = SaturationMod(0f)

		val color = LedColor(0f, 127f, 255f)
		mod.update(color)

		Assertions.assertThat(setOf(color.r, color.g, color.b).size).isEqualTo(1)
	}
}