/*
javac CallingNative.java
javah -jni CallingNative

gcc CallingNative.c -o CallingNative.dll -shared -m64 -I "C:\Program Files\Java\jdk1.8.0_25\include" -I "C:\Program Files\Java\jdk1.8.0_25\include\win32" -lgdi32
 -fPIC

 */


public class CallingNative {

	private native void nativeGetVoid();
	private native boolean nativeGetBoolean();
	private native int nativeGetInteger();
	private native void nativeSaveScreenshotToClipboard();

	static {
		System.loadLibrary("CallingNative");
	}


	static public void main(String argv[]) {
		CallingNative callingNative = new CallingNative();
		callingNative.doStuff();
	}


	public CallingNative() {

	}

	public void doStuff() {
		nativeNothing();
		nativeBoolean();
		nativeInteger();
		nativeInteger();
		nativeInteger();

		saveScreenshotOnce();
		saveScreenshotRepeatedly();
	}

	public void saveScreenshotOnce() {
		long start = System.currentTimeMillis();
		nativeSaveScreenshotToClipboard();
		long end = System.currentTimeMillis();

		long duration = end - start;

		System.out.println("Time spent: " + duration + " ms.");
	}

	public void saveScreenshotRepeatedly() {
		long start = System.currentTimeMillis();
		int counter = 0;
		while (true) {
			long current = System.currentTimeMillis();
			if (current - start > 1000) {
				break;
			}
			nativeSaveScreenshotToClipboard();
			counter++;
		}

//		new Color(0, 99, 177);
//		new Color(57, 119, 156);
//		new Color(0x39779C);
//		new Color(0x52C5E9);
//		new Color(0x1A4F7C);
//		new Color(0x0063B1);

		System.out.println("During one second, called " + counter + " times.");
	}

	public void nativeNothing() {
		nativeGetVoid();
	}

	public void nativeBoolean() {
		System.out.println(nativeGetBoolean());
	}

	public void nativeInteger() {
		System.out.println(nativeGetInteger());
	}



}
