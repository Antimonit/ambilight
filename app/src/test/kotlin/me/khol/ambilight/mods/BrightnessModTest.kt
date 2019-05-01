package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class BrightnessModTest : ModTest {

	@Test
	fun `does not update colors at full brightness`() {
		val mod = BrightnessMod(1f)

		val color = LedColor(0f, 127f, 255f)
		val originalColor = color.copy()
		mod.update(color)

		Assertions.assertThat(originalColor).isEqualTo(color)
	}

	@Test
	fun `updates colors at reduced brightness`() {
		val mod = BrightnessMod(0.5f)

		val color = LedColor(0f, 127f, 255f)
		val originalColor = color.copy()
		mod.update(color)

		Assertions.assertThat(originalColor).isNotEqualTo(color)
	}

	@Test
	fun `completely dims colors at zero brightness`() {
		val mod = BrightnessMod(0f)

		val color = LedColor(0f, 127f, 255f)
		val expectedColor = LedColor(0f, 0f, 0f)
		mod.update(color)

		Assertions.assertThat(expectedColor).isEqualTo(color)
	}
}