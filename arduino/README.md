# Arduino

The Arduino program expects to be communicating with the WS2812B LED strip. 

Depending on your display size and density of LEDs on your LED strip you will probably also need to
tweak `NUM_LEDS` and `NUM_DATA` constants.

It should be possible to use other LED strips too. Just make sure you update the relevant part of 
the code. Furthermore, some LED strips do not follow RGB ordering of colors and may have them 
shuffled like GRB. Check the documentation of your strip and update the code if necessary.

## How to install ambilight program on Arduino
- Download Arduino IDE from the [official website](https://www.arduino.cc/en/Main/Software).
- Load `ambilight_arduino.ino` in the Arduino IDE.
- Download `FastLED` library.
  - Go to `Sketch` &rightarrow; `Include Library` &rightarrow; `Manage Libraries...`.
  - Search for `FastLED` developed by Daniel Garcia.
  - Click on the library result and press `Install`.
- Connect your board via USB.
- `Tools` &rightarrow; `Port: "?"` &rightarrow; choose port at which your board is connected.
- `Tools` &rightarrow; `Board: "?"` &rightarrow; choose your board type.
- `Sketch` &rightarrow; `Verify/Compile` sketch. Status bar should eventually change to `Done compiling`.
- `Sketch` &rightarrow; `Upload` sketch. Status bar should eventually change to `Done Uploading`.
 
At this point the Arduino is ready to be connected to and you can close the Arduino IDE.

## How does it work?
Arduino communicates with Java application via serial connection by transmitting arrays of bytes
in both directions. Arrays may contain information about both synchronization and actual data for 
LED strip. 

Each time Java wants to update displayed colors, it sends a byte array where two first bytes must 
contain bytes 'o', 'z'. These bytes are used for synchronization and verification purposes. The rest 
is color data for each LED on the strip where each color is specified by three bytes (in order red, 
green and blue colors). 

When Arduino receives an expected number of bytes, it sends 'y' byte back to notify Java that it is 
ready to read more. This does not mean that Java will transmit more data immediately but rather when 
Java is about to send the data and it still has not received 'y' confirmation it will just drop 
computed data and wait until the next screen update.  
 
When Arduino reads the stream of data from the serial connection, it transforms received data 
to a format accepted by the FastLED library which does all the heavy lifting.

There is some extra logic to fade colors out if there was no data received from Java for a longer
period of time. This was done in order to turn the lights off if Java application terminates 
abruptly.