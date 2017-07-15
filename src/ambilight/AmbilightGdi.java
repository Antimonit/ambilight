/*
javac ambilight\AmbilightGdi.java
javah -jni ambilight.AmbilightGdi
g++ ambilight_AmbilightGdi.cpp -o ambilight_AmbilightGdi.dll -shared -m64 -I "C:\Program Files\Java\jdk1.8.0_25\include" -I "C:\Program Files\Java\jdk1.8.0_25\include\win32" -lgdi32
g++ ambilight_AmbilightGdi.cpp -o ambilight_AmbilightGdi.dll -shared -m64 -I "C:\Program Files\Java\jdk1.8.0_25\include" -I "C:\Program Files\Java\jdk1.8.0_25\include\win32" -lgdi32 -static-libgcc -static-libstdc++
g++ ambilight_AmbilightGdi.cpp -o ambilight_AmbilightGdi.dll -shared -m64 -I "C:\Program Files\Java\jdk1.8.0_25\include" -I "C:\Program Files\Java\jdk1.8.0_25\include\win32" -lgdi32 -static-libgcc -static-libstdc++
*/

package ambilight;

import java.io.File;
import java.io.IOException;

public class AmbilightGdi implements Ambilight {

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


	public native void nativeInit(int ledsWidth, int ledsHeight, int[][] leds);


	public native byte[][] nativeGetScreenSegmentsColorBytes();

	public native void nativeSaveScreenshotToClipboard();



	@Override
	public void init(int ledsWidth, int ledsHeight, int[][] leds) {
		nativeInit(ledsWidth, ledsHeight, leds);
	}

	@Override
	public byte[][] getScreenSegmentsColors() {
		return nativeGetScreenSegmentsColorBytes();
	}

	public void saveScreenshotToClipboard() {
		nativeSaveScreenshotToClipboard();
	}

}
