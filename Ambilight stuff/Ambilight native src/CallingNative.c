#include <jni.h>
#include <stdio.h>
#include <windows.h>
#include <wingdi.h>
#include "CallingNative.h"
#include <D3D9.h>

int left, top, right, bottom, width, height;

HDC     hScreen;
HDC     hDC;
HBITMAP hBitmap;
HGDIOBJ old_obj;

boolean prepared;

// gcc CallingNative.c -o CallingNative.dll -shared -m64 -I "C:\Program Files\Java\jdk1.8.0_25\include" -I "C:\Program Files\Java\jdk1.8.0_25\include\win32" -lgdi32

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

void PrintPixelData(int i, int j) {
    COLORREF pixel = GetPixel(hDC, i, j);
    int red = (int) GetRValue(pixel);
    int green = (int) GetGValue(pixel);
    int blue = (int) GetBValue(pixel);
    // printf("#%.2X%.2X%.2X R:%d G:%d B:%d\n", red, green, blue, red, green, blue);
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

void GetScreenShot(void) {
    PrepareBounds();

    BlitFullSize();
    // StretchFullSize();
    // StretchDownscaled();

    // for (int count = 0; count < 20; count++) {
    //     for (int i = 0; i < 256; ++i) {
    //         PrintPixelData(count, count);
    //     }
    // }
    
    SaveBitmapToClipboard();

    CleanUpScreenShot();
}


extern IDirect3DDevice9* g_pd3dDevice;
void GetScreenShot2() {
    IDirect3DSurface9* pSurface;

}


JNIEXPORT void JNICALL Java_CallingNative_nativeSaveScreenshotToClipboard(JNIEnv* env, jobject obj) {
    GetScreenShot();
}

JNIEXPORT void JNICALL Java_CallingNative_nativeGetVoid(JNIEnv* env, jobject obj) {
	printf("Hello World!\n");
	return;
}

JNIEXPORT jboolean JNICALL Java_CallingNative_nativeGetBoolean(JNIEnv* env, jobject obj) {
	return (3 > 2) ? JNI_TRUE: JNI_FALSE;
}

jint val = 0;
JNIEXPORT jint JNICALL Java_CallingNative_nativeGetInteger(JNIEnv* env, jobject obj) {
    val ++;
	return val;
}


