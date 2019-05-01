package me.khol.ambilight.mods

import me.khol.ambilight.LedColor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class TemperatureModTest: ModTest {

	@Test
	fun `low temperature shifts colors towards red`() {
		val mod = TemperatureMod(1000)

		val color = LedColor(127f, 127f, 127f)
		mod.update(color)

		Assertions.assertThat(color.r).isGreaterThan(color.g)
		Assertions.assertThat(color.r).isGreaterThan(color.b)
	}

	@Test
	fun `high temperature shifts colors towards blue`() {
		val mod = TemperatureMod(10000)

		val color = LedColor(127f, 127f, 127f)
		mod.update(color)

		Assertions.assertThat(color.b).isGreaterThan(color.r)
		Assertions.assertThat(color.b).isGreaterThan(color.g)
	}
}