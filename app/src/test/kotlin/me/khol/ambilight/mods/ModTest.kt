package me.khol.ambilight.mods

import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import me.khol.ambilight.LedColor
import org.junit.jupiter.api.Test

internal interface ModTest {

	fun Array<LedColor>.copy() = Array(size) { get(it).copy() }
}

internal class DiscreteModTest {

	private val colors = Array(10) { LedColor() }

	@Test
	fun `not isUseful skips update`() {
		val mod = spyk<DiscreteMod>()
		every { mod.isUseful() } answers { false }

		mod.update(colors)

		verify(exactly = 0) { mod.update(any<LedColor>()) }
	}

	@Test
	fun `isUseful invokes update`() {
		val mod = spyk<DiscreteMod>()
		every { mod.isUseful() } answers { true }

		mod.update(colors)

		verify(exactly = 10) { mod.update(any<LedColor>()) }
	}
}