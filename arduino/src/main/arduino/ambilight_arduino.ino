#include "FastLED.h"

#define DATA_PIN 3
#define NUM_LEDS 40 // 40
#define NUM_DATA 122 // NUM_LED * 3 + 2
#define RECON_TIME 1000 // after x seconds idle time, send afk again.
#define DISCONNECT_TIME 1000 // after 1 second of no information from computer, turn of leds

#define DISCONNECT_FADE_DURATION 2000 // fade over 2 seconds
#define DISCONNECT_FADE_UPDATE_PER_SECOND 30 // updating leds 30 times per second

CRGB leds[NUM_LEDS];


uint8_t led_color[NUM_DATA];
int index = 0;
unsigned long cur_time = 0;
unsigned long last_afk_time = 0;
unsigned long last_connect_time = 0;
unsigned long last_fade_time = 0;
bool disconnected = true;
bool disconnecting = false;

void setup() {
  // sanity check delay - allows reprogramming if accidentally blowing power w/leds
  delay(2000);
  FastLED.addLeds<WS2812B, DATA_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setBrightness(0);
  FastLED.show();
  disconnected = true;

  Serial.begin(115200);
  Serial.print("ozy"); // Send ACK string to host
}

void loop() {
  if (Serial.available() > 0) {
    readAndShow();
  } else {
    cur_time = millis();
    if (cur_time - last_afk_time > RECON_TIME){
      Serial.write('y');
      last_afk_time = cur_time;
      index = 0;
    }
    if (cur_time - last_connect_time > DISCONNECT_TIME && !disconnected) {

      int fadeProgress = (cur_time - last_connect_time - DISCONNECT_TIME) * 255 / DISCONNECT_FADE_DURATION;

      if (fadeProgress > 255) {
        FastLED.setBrightness(0);
        FastLED.show();
        disconnected = true;
      } else if (cur_time - last_fade_time > 1000 / DISCONNECT_FADE_UPDATE_PER_SECOND) {
        FastLED.setBrightness(255 - fadeProgress);
        FastLED.show();
        last_fade_time = cur_time;
        disconnecting = true;
      }
    }
  }
}

void readAndShow() {
  led_color[index++] = (uint8_t)Serial.read();

  if (index >= NUM_DATA) {
    Serial.write('y');
    last_afk_time = millis();
    last_connect_time = millis();
    index = 0;
    if (disconnected || disconnecting) {
      FastLED.setBrightness(255);
      disconnected = false;
      disconnecting = false;
    }

    if (led_color[0] == 'o' && led_color[1] == 'z') {
      for (int i = 0; i < NUM_LEDS; i++){
         int led_index = i*3 + 2;
         leds[i] = CRGB(led_color[led_index + 0],
                        led_color[led_index + 1],
                        led_color[led_index + 2]);
      }
    } else if (led_color[0] == 'o' && led_color[1] == 'y') {

    } else {
      leds[0] = CRGB(255,0,0);
      leds[1] = CRGB(255,255,0);
      leds[2] = CRGB(0,255,0);
      leds[3] = CRGB(0,255,255);
      leds[4] = CRGB(0,0,255);
    }
    FastLED.show();
  }
}

