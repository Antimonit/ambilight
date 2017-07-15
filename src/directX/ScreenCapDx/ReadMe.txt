Screen Capture: This application captuers the contents of the screen using DirectX. Start the application and click anywhere on the client area - the screen at that moment would be captured and saved to file Desktop.bmp

This Capturing Uses the FrontBuffer of the DirectX Application to get the content of the Screen.

By design, the Front Buffer accessing is slow - and can not be used in performance critical applications.

And also this capturing technique doesnot capture the mouse.

However the content quality produced would be good with the MPG4 codec.

For performance applications try considering the WindowsMediaEncoderAPI.

WindowsMediaEncoderAPI includes a ScreenCapture Codec and can give optimal results - and also captures the mouse.