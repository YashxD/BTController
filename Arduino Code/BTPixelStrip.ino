  #include <Adafruit_NeoPixel.h>

#define pixelPin 6

//char in[9];
int input;

Adafruit_NeoPixel strip = Adafruit_NeoPixel(60, pixelPin, NEO_GRB + NEO_KHZ800);

void setup() {
  Serial.begin(9600);
  strip.begin();
  strip.show();
  //currR = currG = currB = 0;
}

void loop() {
  /*if(Serial.available()>0) {
    input = Serial.read();
    map(input,0,400,0,255);
    setStripColor(strip.Color(input,0,0));
  }*/
  for(int i=0; i<255; i++) {
    setStripColor(strip.Color(i,0,0));
    delay(10);
  }
  for(int i=255; i>0; i--) {
    setStripColor(strip.Color(i,0,0));
    delay(10);
  }
}

void setStripColor(uint32_t c) {
  for(uint16_t i=20; i<30; i++) {
    strip.setPixelColor(i,c);
  } 
  strip.show();
}
