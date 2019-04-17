# Ambilight

This is a homemade implementation of an Ambilight setup, similar to what Phillips does with their
Ambilight TVs.

## Structure
The project is composed of several components.

The entry point of the application is written in **Java**. When the application starts it opens 
a GUI window through which it is possible to control the application and alter its parameters.

Retrieval and processing of colors displayed on the screen can be quite a work-intensive process 
and may put a big load on the CPU, especially if we want reasonable performance.

When the algorithm is implemented in Java, the application 


 * This implementation does not perform any computation itself but delegates all calls to a
 * pre-compiled C library via JNI bridge.
 * <p>
 * Because this code runs pretty much all the time it is crucial to make the performance as good as
 * possible. C code performs much better than Java code.
 
initializes **serial connection** to **Arduino**.

