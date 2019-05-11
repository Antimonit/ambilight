# Ambilight

This is a homemade implementation of an Ambilight setup, similar to what Phillips does with their
Ambilight TVs.

## Structure
The project is composed of three components:

![Components](extra/components_200.png)

**Executable component**
* The main component and contains majority of code in the project. It is 
written mostly in Kotlin and compiles into an executable Java application.
It retrieves screen contents, displays a graphical interface through which it is possible to control
the app and sends data to Arduino component.

**Arduino component**
* Small program that is compiled for and runs on an Arduino. 
It receives data over serial connection and transform it to a format accepted by FastLED library. 
The library then manages low-level communication with LED strips.

**LED strip component**
* Pure hardware component with no code whatsoever. LED strips needs to be cut into segments of 
dimensions of the display, re-soldered together in the corners and wired up to Arduino.  

### Retrieving screen contents
The key part of the application is periodic retrieval and processing of colors displayed on the 
screen. In the best scenario, this should be able to run as often as the screen refreshes. This can
be quite a work-intensive process and may put a big load on the CPU, especially if we want 
a reasonable performance.

Implementing this in Java is possible but not optimal. The only way to retrieve screen contents is 
through `java.awt.Robot` class which is not optimized for such use case. It is capable of running 
at 20 FPS while CPU is under heavy load.

Thus I decided to do the CPU-intensive computations in a C/C++ world that is much closer to the
hardware. The application does not perform any computation itself but delegate all calls to a 
pre-compiled C++ library via JNI bridge.

The native library utilizes Windows GDI to retrieve screen contents. It does not perform as well 
as DirectX or OpenGL do but the complexity of implementation is much lower. It is capable of 
running at 30 FPS with CPU utilization several times smaller compared to pure Java implementation. 
 
### Communication with Arduino
Java application communicates with Arduino using serial connection. Serial port is not necessary as
serial connection can simply be emulated using USB connection.

Instructions on how to install the program on Arduino is thoroughly described in README.md of 
arduino module.

### LED 
There are many different kinds of LED strips available on the market and can be purchased online 
for just a few dollars. Personally I can safely vouch only for WS2812B strip working well but all similar strips should not 
cause any problems. Some strips may have swapped color channels but FastLED library handles all 
that. Check with the FastLED library whether the strip is supported or not. 
