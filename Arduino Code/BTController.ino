#define led1Pin 11
#define led2Pin 10
#define led3Pin 9

boolean state = false;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println(">> START<<");  
  pinMode(led1Pin, OUTPUT);
  pinMode(led2Pin, OUTPUT);
  pinMode(led3Pin, OUTPUT);
  digitalWrite(led1Pin, HIGH);
  digitalWrite(led2Pin, HIGH);
  digitalWrite(led3Pin, HIGH);

}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()>0) {
    char in;
    in = Serial.read();
    Serial.write(in);
    
    switch(in) {
      case '1': //Switch
        if(state) {
          digitalWrite(led1Pin, HIGH);
          state = false;
        }  
        else {
          digitalWrite(led1Pin, LOW);
          state = true;
        }  
        break;
      case '2': //Blink
        digitalWrite(led1Pin, LOW);
        delay(500);
        digitalWrite(led1Pin, HIGH);
        delay(500);
        digitalWrite(led1Pin, LOW);
        break;
      case '3': //Glow
        analogWrite(led1Pin, 0);
        delay(500);
        for(int i=1;i<256;i+=3) {
          analogWrite(led1Pin, i);
          delay(5);
        }
        delay(50);
        for(int i=255;i>0;i-=3) {
          analogWrite(led1Pin, i);
          delay(5);
        }
        analogWrite(led1Pin, 0);
        break;
      case 'A': //LED1 ON
        digitalWrite(led1Pin, LOW);
        break;
      case 'a': //LED1 OFF
        digitalWrite(led1Pin, HIGH);
        break;
      case 'B': //LED2 ON
        digitalWrite(led2Pin, LOW);
        break;
      case 'b': //LED2 OFF
        digitalWrite(led2Pin, HIGH);
        break;
      case 'C': //LED3 ON
        digitalWrite(led3Pin, LOW);
        break;
      case 'c': //LED3 OFF
        digitalWrite(led3Pin, HIGH);
        break;
      default:
        break;
        
    }
  }
}
