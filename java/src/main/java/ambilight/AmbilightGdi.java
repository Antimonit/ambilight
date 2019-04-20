package ambilight;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * This implementation does not perform any computation itself but delegates all calls to a
 * pre-compiled C library via JNI bridge.
 * <p>
 * Because this code runs pretty much all the time it is crucial to make the performance as good as
 * possible. C code performs much better than Java code.
 * <p>
 * In order for this class to work, a C library must be built first. There are 3 steps to be done.
 * <ul>
 * <li> Compile the Java class:
 * <ul>{@code $javac ambilight\AmbilightGdi.java}</ul>
 * <li> Generate C header needed to implement native methods:
 * <ul>{@code $javah -jni ambilight.AmbilightGdi}</ul>
 * <li> Compile C code into a library:
 * <ul>{@code $g++ ambilight_AmbilightGdi.cpp -o ambilight_AmbilightGdi.dll -shared -m64
 * -I "C:\Program Files\Java\jdk1.8.0_25\include"
 * -I "C:\Program Files\Java\jdk1.8.0_25\include\win32"
 * -lgdi32}</ul>
 * <i>Note: you might also need extra compilation flags: -static-libgcc -static-libstdc++</i>
 * </ul>
 */
public class AmbilightGdi extends Ambilight {

	private static final String DLL_NAME = "AmbilightGdi.dll";

	static {
		try {
			File dir = new File(".");
			File dllFile = new File(dir.getCanonicalPath() + File.separator + DLL_NAME);
			String dllFilePath = dllFile.getCanonicalPath();
			System.out.println("Loading dll: " + dllFilePath);
			System.load(dllFilePath);
		} catch (IOException e) {
			System.out.println("Failed to load dll: " + DLL_NAME);
		}
	}

	public AmbilightGdi(int ledsWidth, int ledsHeight, int[][] leds) {
		nativeInit(ledsWidth, ledsHeight, leds);
	}

	@NotNull
	@Override
	public byte[][] getScreenSegmentsColors() {
		return nativeGetScreenSegmentsColorBytes();
	}

	public void saveScreenshotToClipboard() {
		nativeSaveScreenshotToClipboard();
	}

	public native void nativeInit(int ledsWidth, int ledsHeight, int[][] leds);

	public native byte[][] nativeGetScreenSegmentsColorBytes();

	public native void nativeSaveScreenshotToClipboard();
}
