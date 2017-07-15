//#include <jni.h>
#include <stdio.h>
#include <windows.h>
#include <wingdi.h>
#include <D3D9.h>
#include <stdio.h>
#include <iostream>

void SaveBitmap(char *szFilename, HBITMAP hBitmap) {
    HDC                 hdc=NULL;
    FILE*               fp=NULL;
    LPVOID              pBuf=NULL;
    BITMAPINFO          bmpInfo;
    BITMAPFILEHEADER    bmpFileHeader;

    do{

        hdc=GetDC(NULL);
        ZeroMemory(&bmpInfo,sizeof(BITMAPINFO));
        bmpInfo.bmiHeader.biSize=sizeof(BITMAPINFOHEADER);
        GetDIBits(hdc,hBitmap,0,0,NULL,&bmpInfo,DIB_RGB_COLORS);

        if(bmpInfo.bmiHeader.biSizeImage<=0)
            bmpInfo.bmiHeader.biSizeImage=bmpInfo.bmiHeader.biWidth*abs(bmpInfo.bmiHeader.biHeight)*(bmpInfo.bmiHeader.biBitCount+7)/8;

        if((pBuf=malloc(bmpInfo.bmiHeader.biSizeImage))==NULL)
        {
            std::cout << "Unable to Allocate Bitmap Memory" << std::endl;
            // MessageBox(NULL,_ T("Unable to Allocate Bitmap Memory"),_T("Error"),MB_OK|MB_ICONERROR);
            break;
        }
        
        bmpInfo.bmiHeader.biCompression=BI_RGB;
        GetDIBits(hdc,hBitmap,0,bmpInfo.bmiHeader.biHeight,pBuf,&bmpInfo,DIB_RGB_COLORS);   

        if((fp=fopen(szFilename,"wb"))==NULL)
        {
            std::cout << "Unable to Create Bitmap File" << std::endl;
            // MessageBox(NULL,_T("Unable to Create Bitmap File"),_T("Error"),MB_OK|MB_ICONERROR);
            break;
        }

        bmpFileHeader.bfReserved1=0;
        bmpFileHeader.bfReserved2=0;
        bmpFileHeader.bfSize=sizeof(BITMAPFILEHEADER)+sizeof(BITMAPINFOHEADER)+bmpInfo.bmiHeader.biSizeImage;
        bmpFileHeader.bfType='MB';
        bmpFileHeader.bfOffBits=sizeof(BITMAPFILEHEADER)+sizeof(BITMAPINFOHEADER);

        std::cout << "Writing to file" << std::endl;

        fwrite(&bmpFileHeader,sizeof(BITMAPFILEHEADER),1,fp);
        fwrite(&bmpInfo.bmiHeader,sizeof(BITMAPINFOHEADER),1,fp);
        fwrite(pBuf,bmpInfo.bmiHeader.biSizeImage,1,fp);

    }while(false);

    if(hdc)
        ReleaseDC(NULL,hdc);

    if(pBuf)
        free(pBuf);

    if(fp)
        fclose(fp);
}

// void dump_buffer() {
//     HDC Device = GetDC(NULL);
//     // HDC hDest = CreateCompatibleDC(hdc); // create a device context to use yourself

//     IDirect3DSurface9* pRenderTarget=NULL;
//     IDirect3DSurface9* pDestTarget=NULL;

//     const char file[] = "Picture.bmp";
//     // sanity checks.
//     if (Device == NULL) {
//         printf("Device is null\n");
//         return;
//     }

//     // get the render target surface.
//     HRESULT hr = Device->GetRenderTarget(0, &pRenderTarget);
//     // get the current adapter display mode.
//     //hr = pDirect3D->GetAdapterDisplayMode(D3DADAPTER_DEFAULT,&d3ddisplaymode);

//     // create a destination surface.
//     hr = Device->CreateOffscreenPlainSurface(DisplayMde.Width,
//                          DisplayMde.Height,
//                          DisplayMde.Format,
//                          D3DPOOL_SYSTEMMEM,
//                          &pDestTarget,
//                          NULL);
//     //copy the render target to the destination surface.
//     hr = Device->GetRenderTargetData(pRenderTarget, pDestTarget);
//     //save its contents to a bitmap file.
//     hr = D3DXSaveSurfaceToFile(file,
//                               D3DXIFF_BMP,
//                               pDestTarget,
//                               NULL,
//                               NULL);

//     // clean up.
//     pRenderTarget->Release();
//     pDestTarget->Release();
// }


int main (int argc, char **argv) {
    // dump_buffer();

    int height = GetSystemMetrics(SM_CYVIRTUALSCREEN);
    int width = GetSystemMetrics(SM_CXVIRTUALSCREEN);

    HDC hScreen = GetDC(NULL);
    HDC hDC     = CreateCompatibleDC(hScreen);
    HBITMAP hBitmap = CreateCompatibleBitmap(hScreen, width, height);

    char* name = "bitmap3.bmp";
    SaveBitmap(name, hBitmap);
    return 0;
}

