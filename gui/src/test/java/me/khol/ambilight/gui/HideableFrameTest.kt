package me.khol.ambilight.gui

import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Test
import java.util.concurrent.Callable

class HideableFrameTest : AssertJSwingJUnitTestCase() {

	private lateinit var window: FrameFixture
	private lateinit var frame: HideableFrame

	override fun onSetUp() {
		frame = GuiActionRunner.execute(Callable {
			HideableFrame("test", "/icon-inner_border.png")
		})
		window = FrameFixture(robot(), frame)
		window.show()
	}

	@Test
	fun shouldCopyTextInLabelWhenClickingButton() {
		println("test started")

		println(frame.isFocusOwner)
		frame.transferFocus()
		println(frame.isFocusOwner)
//		frame.resetLocation()
//		window.slider().slideToMinimum()
//		window.textBox("textToCopy").enterText("Some random text")
//		window.button("copyButton").click()
//		window.label("copiedText").requireText("Some random text")
	}
}