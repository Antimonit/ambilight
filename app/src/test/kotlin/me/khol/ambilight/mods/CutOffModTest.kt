package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CutOffModTest : ModTest {

	@Test
	fun `does not update colors at minimum value`() {
		val mod = CutOffMod(0)

		val color = LedColor(0f, 127f, 255f)
		val originalColor = color.copy()
		mod.update(color)

		Assertions.assertThat(originalColor).isEqualTo(color)
	}

	@Test
	fun `updates colors at reduced at other value`() {
		val mod = CutOffMod(1)

		val color = LedColor(0f, 127f, 255f)
		val originalColor = color.copy()
		mod.update(color)

		Assertions.assertThat(originalColor).isNotEqualTo(color)
	}

	@Test
	fun `completely dims colors at maximum value`() {
		val mod = CutOffMod(255)

		val color = LedColor(0f, 127f, 255f)
		val expectedColor = LedColor(0f, 0f, 0f)
		mod.update(color)

		Assertions.assertThat(expectedColor).isEqualTo(color)
	}
}