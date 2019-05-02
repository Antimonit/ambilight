#include <jni.h>
#include <stdio.h>
#include <windows.h>
#include <wingdi.h>
#include <iostream>
//#include "AmbilightGdi.h"

// g++ ambilight_AmbilightGdi.cpp -o ambilight_AmbilightGdi.dll
// -shared -m64
// -I "C:\Program Files\Java\jdk1.8.0_25\include"
// -I "C:\Program Files\Java\jdk1.8.0_25\include\win32"
// -lgdi32

namespace ambilight_gdi {

	int left, top, right, bottom, width, height;

	HDC     hScreen;
	HDC     hDC;
	HBITMAP hBitmap;
	HGDIOBJ old_obj;

	boolean prepared;

	void PrepareBounds() {
		left   = GetSystemMetrics(SM_XVIRTUALSCREEN);
		top    = GetSystemMetrics(SM_YVIRTUALSCREEN);
		right  = GetSystemMetrics(SM_CXVIRTUALSCREEN);
		bottom = GetSystemMetrics(SM_CYVIRTUALSCREEN);
		width  = right - left;
		height = bottom - top;
	}

	void BlitFullSize() {
		// BitBlt function
		// https://msdn.microsoft.com/en-us/library/windows/desktop/dd183370(v=vs.85).aspx
		// BLACKNESS | CAPTUREBLT | DSTINVERT | MERGECOPY | MERGEPAINT | NOMIRRORBITMAP | NOTSRCCOPY | NOTSRCERASE
		// PATCOPY | PATINVERT | PATPAINT | SRCAND | SRCCOPY | SRCERASE | SRCINVERT | SRCPAINT | WHITENESS
		hScreen = GetDC(NULL);
		hDC     = CreateCompatibleDC(hScreen);
		hBitmap = CreateCompatibleBitmap(hScreen, width, height);
		old_obj = SelectObject(hDC, hBitmap);
		BOOL bRet = BitBlt(hDC, left, top, width, height, hScreen, left, top, SRCCOPY);
	}
	void StretchFullSize() {
		// StretchBlt function
		// https://msdn.microsoft.com/en-us/library/windows/desktop/dd145120(v=vs.85).aspx
		// SetStretchBltMode function
		// https://msdn.microsoft.com/en-us/library/windows/desktop/dd145089(v=vs.85).aspx
		// BLACKONWHITE | COLORONCOLOR | HALFTONE | WHITEONBLACK
		hScreen = GetDC(NULL);
		hDC     = CreateCompatibleDC(hScreen);
		hBitmap = CreateCompatibleBitmap(hScreen, width, height);
		old_obj = SelectObject(hDC, hBitmap);
		SetStretchBltMode(hDC, HALFTONE);
		StretchBlt(hDC, left, top, width, height, hScreen, left, top, width, height, SRCCOPY);
	}
	void StretchDownscaled() {
		hScreen = GetDC(NULL);
		hDC     = CreateCompatibleDC(hScreen);
		hBitmap = CreateCompatibleBitmap(hScreen, width/4, height/4);
		old_obj = SelectObject(hDC, hBitmap);
		SetStretchBltMode(hDC, COLORONCOLOR);
		StretchBlt(hDC, left, top, width/4, height/4, hScreen, left, top, width, height, SRCCOPY);
	}

	int GetPixelData(int x, int y) {
		COLORREF pixel = GetPixel(hDC, x, y);
		int red = (int) GetRValue(pixel);
		int green = (int) GetGValue(pixel);
		int blue = (int) GetBValue(pixel);
	//	printf("x=%d y=%d | #%.2X%.2X%.2X R:%d G:%d B:%d\n", x, y, red, green, blue, red, green, blue);
		return 0xFF << 24 | red << 16 | green << 8 | blue;
	}

	void SaveBitmapToClipboard() {
		OpenClipboard(NULL);
		EmptyClipboard();
		SetClipboardData(CF_BITMAP, hBitmap);
		CloseClipboard();
	}

	void CleanUpScreenShot() {
		SelectObject(hDC, old_obj);
		DeleteDC(hDC);
		ReleaseDC(NULL, hScreen);
		DeleteObject(hBitmap);
	}


	// ----------------------------------------------------------------- //


	static const int SAMPLE_COUNT_X = 32;
	static const int SAMPLE_COUNT_Y = 32;
	static const int SAMPLE_COUNT = SAMPLE_COUNT_X * SAMPLE_COUNT_Y;
	int** pixelOffset;

	int ledCount;


	static void nativeSaveScreenshotToClipboard(JNIEnv* env, jobject obj) {
		BlitFullSize();
		SaveBitmapToClipboard();
		CleanUpScreenShot();
	}

	static jbyteArray NewJavaBytes(JNIEnv* env, int r, int g, int b) {
		jbyteArray retVal = env->NewByteArray(3);
		jbyte *buf = env->GetByteArrayElements(retVal, NULL);
		buf[0] = (char) r;
		buf[1] = (char) g;
		buf[2] = (char) b;
		env->ReleaseByteArrayElements(retVal, buf, 0);
		return retVal;
	}

	BYTE* ScreenData = NULL;

	inline int PosB(int x, int y) {
		return ScreenData[4 * ((y * width) + x)];
	}
	inline int PosG(int x, int y) {
		return ScreenData[4 * ((y * width) + x) + 1];
	}
	inline int PosR(int x, int y) {
		return ScreenData[4 * ((y * width) + x) + 2];
	}

	inline int OffsetPosB(int offset) {
		return ScreenData[4 * offset];
	}
	inline int OffsetPosG(int offset) {
		return ScreenData[4 * offset + 1];
	}
	inline int OffsetPosR(int offset) {
		return ScreenData[4 * offset + 2];
	}

	jobjectArray nativeGetScreenSegmentsColorBytes(JNIEnv* env, jobject obj) {

		BlitFullSize();

		jclass arrayElemType = env->FindClass("[B");

		jobjectArray testArray = env->NewObjectArray(ledCount,
													arrayElemType,
													env->NewByteArray(1) );

		BITMAPINFOHEADER bmi = {0};
		bmi.biSize = sizeof(BITMAPINFOHEADER);
		bmi.biPlanes = 1;
		bmi.biBitCount = 32;
		bmi.biWidth = width;
		bmi.biHeight = -height;
		bmi.biCompression = BI_RGB;
		bmi.biSizeImage = 0;// 3 * ScreenX * ScreenY;

		GetDIBits(hDC, hBitmap, 0, height, ScreenData, (BITMAPINFO*)&bmi, DIB_RGB_COLORS);

		for (int ledNum = 0; ledNum < ledCount; ledNum++) {
			int r = 0;
			int g = 0;
			int b = 0;

			for (int sampleNum = 0; sampleNum < SAMPLE_COUNT; sampleNum++) {
				r += OffsetPosR(pixelOffset[ledNum][sampleNum]);
				g += OffsetPosG(pixelOffset[ledNum][sampleNum]);
				b += OffsetPosB(pixelOffset[ledNum][sampleNum]);
			}

			r = r / SAMPLE_COUNT;
			g = g / SAMPLE_COUNT;
			b = b / SAMPLE_COUNT;

			jbyteArray ledColorBytes = NewJavaBytes(env, r, g, b);
			env->SetObjectArrayElement(testArray, ledNum, ledColorBytes);
			env->DeleteLocalRef(ledColorBytes);
		}

//		System.arraycopy(ledColor, 0, ledColorOld, 0, ledColor.length);
//		strcpy((char*) buf, src);

		CleanUpScreenShot();

		env->DeleteGlobalRef(arrayElemType);

		return testArray;
	}

	void nativeInit(JNIEnv* env, jobject obj, jint ledsWidth, jint ledsHeight, jobjectArray jleds) {
		ledCount = env->GetArrayLength(jleds);
		jintArray dim = (jintArray) env->GetObjectArrayElement(jleds, 0);
		int coordCount = env->GetArrayLength(dim);	// should be 2 {x, y}
		env->DeleteLocalRef(dim);

		int **leds = new int*[ledCount];

		for (int i = 0; i < ledCount; ++i) {
			jintArray oneDim = (jintArray) env->GetObjectArrayElement(jleds, i);
			jint *led = env->GetIntArrayElements(oneDim, 0);

			leds[i] = new int[coordCount];
			for (int j = 0; j < coordCount; ++j) {
				leds[i][j] = led[j];
			}

			env->ReleaseIntArrayElements(oneDim, led, JNI_ABORT);
			env->DeleteLocalRef(oneDim);
		}

		PrepareBounds();
		ScreenData = new BYTE[4 * width * height];

		float range, step, start;
		int *x = new int[SAMPLE_COUNT_X];
		int *y = new int[SAMPLE_COUNT_Y];

		pixelOffset = new int*[ledCount];
//		pixelOffsetX = new int*[ledCount];
//		pixelOffsetY = new int*[ledCount];

		for (int i = 0; i < ledCount; i++) {
			// --- for columns -----
			range = (float) width / ledsWidth;
			step = range / SAMPLE_COUNT_X;
			start = range * (float) leds[i][0] + step * 0.5f;

			for (int col = 0; col < SAMPLE_COUNT_X; col++) {
				x[col] = (int) (start + step * (float) col);
			}

			// ----- for rows -----
			range = (float) height / ledsHeight;
			step = range / SAMPLE_COUNT_Y;
			start = range * (float) leds[i][1] + step * 0.5f;

			for (int row = 0; row < SAMPLE_COUNT_Y; row++) {
				y[row] = (int) (start + step * (float) row);
			}

			pixelOffset[i] = new int[SAMPLE_COUNT];
			// Get offset to each pixel within full screen capture
			for (int row = 0; row < SAMPLE_COUNT_Y; row++) {
				for (int col = 0; col < SAMPLE_COUNT_X; col++) {
					pixelOffset[i][row * SAMPLE_COUNT_X + col] = y[row] * width + x[col];
				}
			}
		}

		delete[] x;
		delete[] y;
		for (int i = 0; i < ledCount; ++i) {
			delete[] leds[i];
		}
		delete[] leds;
	}

	static JNINativeMethod method_table[] = {
		{ "nativeInit", "(II[[I)V",  (void *) nativeInit },
		{ "nativeGetScreenSegmentsColorBytes", "()[[B",  (void *) nativeGetScreenSegmentsColorBytes },
		{ "nativeSaveScreenshotToClipboard", "()V",  (void *) nativeSaveScreenshotToClipboard }
	};
}

using namespace ambilight_gdi;

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}

	jclass clazz = env->FindClass("me/khol/ambilight/AmbilightGdi");
	if (clazz) {
		env->RegisterNatives(clazz, method_table, sizeof(method_table) / sizeof(method_table[0]));
		env->DeleteLocalRef(clazz);
		return JNI_VERSION_1_6;
	} else {
		return -1;
	}
}
