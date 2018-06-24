/*
 *  MySmartAquarium 
 *  That app controls all your aquariums from remote, doesn't matter where you are.  
 *  Using AWS IOT technologies and ESP32 Microcontroller
 *  
 *  Developed by Ciro De Vita and Gennaro Mellone
 *  Project is part of courses "Web Technologies" and "Mobile Computing" at
 *  Science and Technologies Department of University of Napoli Parthenope.
 * 
 */

#include <Arduino.h>
#include <StaticThreadController.h>
#include <Thread.h>
#include <ThreadController.h>
#include <DHT_U.h>
#include <DHT.h>
#include <AWS_IOT.h>
#include <WiFi.h>
#include "DHT.h"
#include <ArduinoJson.h>
#include <math.h>
#include <Wire.h>
#include "RTClib.h"
#include <Esp.h>
#include <WebServer.h>
#include <EEPROM.h>
#include <U8g2lib.h>

#ifdef U8X8_HAVE_HW_SPI
  #include <SPI.h>
#endif
#ifdef U8X8_HAVE_HW_I2C
  #include <Wire.h>
#endif
#define VER "1.0.0"
#define EEPROM_SIZE 512
# define COUNT_PRECISION 50

//-----------PIN-----------------
#define DHTTYPE DHT11
#define TEMP_EXT 13
#define TEMP_INT1 36
#define TEMP_INT2 37
#define LED 26
//#define LUMINOSITY 27
#define FEED 5
#define TERMOS 23
#define LIGHT 12
#define OPT_1 18
//-------------------------------

U8G2_SSD1306_128X64_NONAME_1_SW_I2C u8g2(U8G2_R0, /* clock=*/ 15, /* data=*/ 4, /* reset=*/ 16); //PageBuffer OLED Display
RTC_DS1307 rtc;

//---------THREAD----------------
Thread server_t = Thread();
Thread local = Thread();
//-------------------------------

//---------AWS-------------------
AWS_IOT aq;
char HOST_ADDRESS[] = "xxxxxxxxxxx";
char CLIENT_ID[] = "esp32";
char TOPIC_NAME[] = "B4E62D8C3C65";
char SUBSCRIBE_ITEM[] = "ITEM"; 

//-------------------------------
//-------- DATA ---------
typedef struct data_d
{
    char tsid[50];
    char tpass[50];
    bool IS_SETUP;
    int temp_ok;
    int sun_time;
    int feed_tot;
    int feed_rem;
    int actual_day;
    
} DATA;

DATA data_aq;

//--------------------------
//--------GLOBAL-----------------
String csid;
String cpass;
int ERR_COUNT = 0;
int status = WL_IDLE_STATUS;
char payload[512];
char rcvdPayload[512];
int msgReceived=0;
DHT dht(TEMP_EXT,DHTTYPE);
WebServer server(80);
String ID;
const char* ssid = "SmartAquarium - ";
const char* passphrase = "perfavorefunziona";
String st;
String content;
int statusCode;
bool general_err = true;
bool wifi_err = true;
bool aws_err = true;
bool sub_err = true;
int term_active = 1;
int opt_active = 1;
int feed_total;
int led_active = 0;
// For live scan
int t ;
int h;
int bulb = 0;
int feed = 0;
float temp;
float temp2;
float temp_mean = 0.00;
int temp_correct = 10;
int temp_mode[40];
int count_mode;
bool flag_term = false;
bool flag_light = false;
bool is_up = false;
int count = 0; //To sync eeprom save
int is_light = 0;
char humidity[5], temperature[5], bulb_c[5], temp_c[10], temp2_c[10], temp_mean_c[10], term_active_c[5], opt_active_c[5], feed_c[5], led_active_c[5];
char temp_ok_c[5], sun_time_c[5], feed_tot_c[5];
//-------------------------------

//------FUNCTIONS----------------
// --------------------- LIVE DATA ----------------------------
void live_scan(){
  if (digitalRead(LED)) led_active = 0;
    else if (!digitalRead(LED)) led_active = 1;
    
    if (digitalRead(LIGHT)) bulb = 1;
    else if (!digitalRead(LIGHT)) bulb = 0;

    if (digitalRead(FEED)) feed = 1;
    else if (!digitalRead(FEED)) feed = 0;

    if (digitalRead(OPT_1)) opt_active = 1;
    else if (!digitalRead(OPT_1)) opt_active = 0;

    if (digitalRead(TERMOS)) term_active = 1;
    else if (!digitalRead(TERMOS)) term_active = 0;
    

    
    h = dht.readHumidity();
    t = dht.readTemperature();
    temp = float(Thermistor(analogRead(TEMP_INT1)));
    temp2 = float(Thermistor(analogRead(TEMP_INT2)));
    temp_mean = (temp + temp2)/2;

     // Calculate the mode of temperatures, every 2 seconds reads, at COUNT_PRECISION will estimate the correct temperature (Thanks calcolo numerico :-) )
      temp_mode[(int)temp_mean] = temp_mode[(int)temp_mean] + 1;
      count_mode = count_mode + 1;
      //Serial.println(count_mode);
      
      
    if (count_mode >= COUNT_PRECISION){
      
      
      int max_t = 0;
      int index;
      for(int i = 0; i<40; i++){// Read the array of temperature's mode
        if(temp_mode[i]>max_t){
          max_t = temp_mode[i];
          index = i;
          }
        temp_mode[i] = 0; //reset array for next data
        }
        
        temp_correct = index;
        count_mode = 0;
      }
    //is_light = analogRead(LUMINOSITY);
    
    
    sprintf(temperature, "%d", t);
    sprintf(humidity, "%d", h);
    sprintf(temp_c, "%.2f", temp);
    sprintf(temp2_c, "%.2f", temp2);
    sprintf(temp_mean_c,"%.2f", temp_mean);
    sprintf(bulb_c, "%d", bulb);
    sprintf(led_active_c, "%d", led_active);
    sprintf(feed_c, "%d", feed);
    sprintf(opt_active_c, "%d", opt_active);
    sprintf(term_active_c, "%d", term_active);

    sprintf(temp_ok_c, "%d", data_aq.temp_ok);
    sprintf(sun_time_c, "%d", data_aq.sun_time);
    sprintf(feed_tot_c, "%d", data_aq.feed_tot);
  
  }
//-------------------- NORMAL SCREEN -------------------------------------------
void normal_screen (int temperature,int temp_to_reach){

  char buf[10];
  char buf1[10];
  char buf2[10];
  sprintf(buf, "%d", temperature);
  sprintf(buf1, "%d", temp_to_reach);
  u8g2.firstPage();
  
  do {
    //WATER TEMP
    u8g2.setFont(u8g2_font_logisoso20_tf);
    int pos = 53;
    u8g2.drawStr(pos,45,buf);
    u8g2.drawGlyph(pos+(12*2)+1,45,0xb0);
    // DESIDERED WATER TEMP
    u8g2.setFont(u8g2_font_profont22_tf);
    u8g2.drawStr(95,45,buf1);
    u8g2.drawGlyph(95+23,45,0xb0);
    //WIFI SYMBOL
    if (!wifi_err){
    u8g2.setFont(u8g2_font_open_iconic_www_2x_t);
    u8g2.drawGlyph(2, 16, 0x48);// WIFI SYMBOL
     }
    //-----------
    
    //AWS SYMBOL
    if (!aws_err){
    u8g2.setFont(u8g2_font_t0_15b_tr);
    u8g2.drawStr(26,13,"AWS");
     }

    if (!sub_err){
    //SUBSCRIBE SYMBOL
    u8g2.setFont(u8g2_font_open_iconic_other_2x_t);
    u8g2.drawGlyph(56, 16, 0x42);// SUBSCRIBE SYMBOL
     }

    if (bulb){
    //LIGHT SYMBOL
    u8g2.setFont(u8g2_font_open_iconic_weather_2x_t);
    u8g2.drawGlyph(110, 16, 0x45);// LIGHT SYMBOL
     }
    
    if (!general_err){
    //GENERAL SYMBOL
    u8g2.setFont(u8g2_font_open_iconic_app_4x_t);
    u8g2.drawGlyph(10, 55, 0x47);// GENERAL SYMBOL OK
     }
    else{
    u8g2.setFont(u8g2_font_open_iconic_embedded_4x_t);
    u8g2.drawGlyph(10, 55, 0x47);// GENERAL SYMBOL ERR
      }
       // FOOD
    u8g2.setFont(u8g2_font_open_iconic_thing_2x_t);
    u8g2.drawGlyph(53, 64, 0x4d);// FOOD SYMBOL
    u8g2.setFont(u8g2_font_profont22_tf);
    sprintf(buf2, "%d", data_aq.feed_tot);
    u8g2.drawStr(69,63,buf2);
    if (term_active){
    u8g2.setFont(u8g2_font_open_iconic_thing_2x_t);
    u8g2.drawGlyph(91, 64, 0x4e);// TERMOSTAT SYMBOL
     }
    if (opt_active){ //OPTIONAL
    u8g2.setFont(u8g2_font_open_iconic_other_2x_t);
    u8g2.drawGlyph(110, 64, 0x41);// OPTIONAL SYMBOL
     }
    
  } while ( u8g2.nextPage() );
  //delay(1000);
 }
//--------------------- MYSUBCALL -------------------------------------------
//Recieve messages(subscribe) and change the status of LED
void mySubCallBackHandler (char *topicName, int payloadLen, char *payLoad)
{
    strncpy(rcvdPayload,payLoad,payloadLen);
    rcvdPayload[payloadLen] = 0;
    msgReceived = 1;

    Serial.print("[AWS]   Message recived: ");
    Serial.println(rcvdPayload);

    if(rcvdPayload[2] == 'T'){ //Optimal Temp
      char temp2[3];
      temp2[0] = rcvdPayload[0];
      temp2[1] = rcvdPayload[1];
      temp2[2] = '\n';
      data_aq.temp_ok = atoi(temp2);
      count ++;
      }
      
      if(rcvdPayload[1] == 'F'){ //How many food per day
      char temp2[2];
      temp2[0] = rcvdPayload[0];
      temp2[1] = '\n';
      data_aq.feed_tot = atoi(temp2);
      count++;
      }
      
      if(rcvdPayload[2] == 'S'){ //How many hours of light
      char temp2[3];
      temp2[0] = rcvdPayload[0];
      temp2[1] = rcvdPayload[1];
      temp2[2] = '\n';
      data_aq.sun_time = atoi(temp2);
      count++;
      }

    //led 
    if(strcmp(rcvdPayload, "LED_ON") == 0)
    {
      digitalWrite(LED,LOW);
    }

    else if(strcmp(rcvdPayload, "LED_OFF") == 0)
    {
      digitalWrite(LED,HIGH);
    }
    //-----------------------

    //cibo
    if(strcmp(rcvdPayload, "FEED") == 0)
    {
      digitalWrite(FEED,HIGH);
      delay(50);
      digitalWrite(FEED,LOW);
    }

    if(strcmp(rcvdPayload, "PRESA1_ON") == 0)
    {
      digitalWrite(TERMOS,HIGH);
      flag_term = true;
    }

    if(strcmp(rcvdPayload, "PRESA1_OFF") == 0)
    {
      digitalWrite(TERMOS,LOW);
      flag_term = false;
    }

    if(strcmp(rcvdPayload, "PRESA2_ON") == 0)
    {
      digitalWrite(LIGHT,HIGH);
      flag_light = true;
    }

    if(strcmp(rcvdPayload, "PRESA2_OFF") == 0)
    {
      digitalWrite(LIGHT,LOW);
      flag_light = false;
    }

    if(strcmp(rcvdPayload, "PRESA3_ON") == 0)
    {
      digitalWrite(OPT_1,HIGH);
    }

    if(strcmp(rcvdPayload, "PRESA3_OFF") == 0)
    {
      digitalWrite(OPT_1,LOW);
    }

    if (count >= 3){
      delay(50);
     EEPROM_SAVE();
      count = 0;
      }
    
}


//convert the value from an anaologRead call of pin with a thermistor  to it to a temperature
double Thermistor(int RawADC) {
 double Temp;
 Temp = log(10000.0*((4096.0/RawADC-1)));
 Temp = 1 / (0.001129148 + (0.000234125 + (0.0000000876741 * Temp * Temp ))* Temp );
 Temp = Temp - 273.15;            // Convert Kelvin to Celcius
 //Temp = (Temp * 9.0)/ 5.0 + 32.0; // Convert Celcius to Fahrenheit
 return Temp;
}

static const char HEX_CHAR_ARRAY[17] = "0123456789ABCDEF";

static String byteToHexString(uint8_t* buf, uint8_t length, String strSeperator="-") {
  String dataString = "";
  for (uint8_t i = 0; i < length; i++) {
    byte v = buf[i] / 16;
    byte w = buf[i] % 16;
    if (i>0) {
      dataString += strSeperator;
    }
    dataString += String(HEX_CHAR_ARRAY[v]);
    dataString += String(HEX_CHAR_ARRAY[w]);
  }
  dataString.toUpperCase();
  return dataString;
}
// --------------------- CHIP ID -----------------------------------------
String getChipID() 
{
  uint64_t chipid;
  chipid=ESP.getEfuseMac();//The chip ID is essentially its MAC address(length: 6 bytes).
  int chipid_size = 6;
  uint8_t chipid_arr[chipid_size];
  for (uint8_t i=0; i < chipid_size; i++) 
  {
    chipid_arr[i] = (chipid >> (8 * i)) & 0xff;
  }
  return byteToHexString(chipid_arr, chipid_size, "");
}

// --------------------- LOCAL Func -----------------------------------------
void local_func()
{
  DateTime now = rtc.now();
  int total_hours = 7 + data_aq.sun_time;
  int mean_hours0 = (total_hours + data_aq.sun_time)/2; //MIDDLE FOOD
  int mean_hours1 = (7+mean_hours0)/2; //Morning FOOD
  int mean_hours2 = (total_hours+mean_hours0)/2; //Evening FOOD
  
  live_scan();//Scan for sensors
  normal_screen(temp_correct,data_aq.temp_ok); //Print on Display

  
// ------------------- GENERAL SIDE ------------------------------
  if(now.day()!=data_aq.actual_day){//If the day is done 

    data_aq.feed_rem = data_aq.feed_tot; //Refull feed counter
    data_aq.actual_day = now.day();//Change day
    EEPROM_SAVE();
    } 
    
    else {
 //---------------------- FOOD SIDE ------------------------

      
    if (data_aq.feed_tot == 1){
      if((mean_hours0 == now.hour()) && data_aq.feed_rem == 1){ //If is time and not feed yet, feed!
        
        digitalWrite(FEED,HIGH);
        delay(50);              // FEEDING
        digitalWrite(FEED,LOW);

        data_aq.feed_rem = data_aq.feed_rem - 1;
        EEPROM_SAVE(); //Save data for eventual crashes.
        }
      }

      if (data_aq.feed_tot == 2){

        if(((mean_hours1 == now.hour()) || (mean_hours2 == now.hour())) && data_aq.feed_rem > 0){ //If is time and not feed yet, feed!
        digitalWrite(FEED,HIGH);
        delay(50);              // FEEDING
        digitalWrite(FEED,LOW);

        data_aq.feed_rem = data_aq.feed_rem - 1;
        EEPROM_SAVE(); //Save data for eventual crashes.
        }
      }

      if (data_aq.feed_tot == 3){

        if (((mean_hours1 == now.hour()) || (mean_hours2 == now.hour()) || (mean_hours0 == now.hour())) && data_aq.feed_rem > 0){ //If is time and not feed yet, feed!
        digitalWrite(FEED,HIGH);
        delay(50);              // FEEDING
        digitalWrite(FEED,LOW);

        data_aq.feed_rem = data_aq.feed_rem - 1;
        EEPROM_SAVE(); //Save data for eventual crashes.
        }
       if (data_aq.feed_tot > 3) general_err = true; // If others, error!
      }
    // --------------------------- TEMPERATURE SIDE ---------------------------
      if(!flag_term){ //If don't active term by app
        
      if (temp_correct > data_aq.temp_ok+3){
        digitalWrite(TERMOS,LOW);
        general_err = true;
        //YELLOW LED
        }
       else if ((temp_correct < data_aq.temp_ok-2) && (t < data_aq.temp_ok-2)){
          digitalWrite(TERMOS,HIGH);
          general_err = true;
        }
        else{
          digitalWrite(TERMOS,LOW);
          general_err = false;
          }
      }
      else { //If active by app
        if (temp_correct > data_aq.temp_ok+5){ //If temp is really high
          digitalWrite(TERMOS,LOW);
          general_err = true;
          }
        }
      //----------------------- LIGHT SIDE -------------------------------------
     
      if(now.hour()>19 && now.hour()<=(data_aq.sun_time+7)){
        if(!flag_light){ //If light is OFF by app
            digitalWrite(LIGHT,LOW);
            }
           else{
           digitalWrite(LIGHT,HIGH);
           is_up = true;
           }
          }
       else if (is_up) {
          digitalWrite(LIGHT,LOW);
          is_up = false;
            }
    
    }

}

// --------------------- Server Func -----------------------------------------
void server_func()
{
    if (isnan(h) || isnan(t)) 
    {
        Serial.println("Failed to read from DHT sensor!");
    }
    
    else
    {
        StaticJsonBuffer<500> JSONbuffer;
        JsonObject& JSONencoder = JSONbuffer.createObject();

        JSONencoder["ID"] = ID;
        JSONencoder["Humidity"] = humidity;
        JSONencoder["Temperature"] = temperature;
        JSONencoder["Light"] = bulb_c;
        JSONencoder["Term"] = term_active_c;
        JSONencoder["Opt"] = opt_active_c;
        JSONencoder["Feed"] = feed_c;
        JSONencoder["Led"] = led_active_c;
        JSONencoder["Water Temperature 1"] = temp_c;
        JSONencoder["Water Temperature 2"] = temp2_c;
        JSONencoder["Water Temperature Mean"] = temp_mean_c;

        JSONencoder["Temp_set"] = temp_ok_c;
        JSONencoder["Feed_set"] = feed_tot_c;
        JSONencoder["Sun_set"] = sun_time_c;
     
        char JSONmessageBuffer[300];
        JSONencoder.printTo(JSONmessageBuffer, sizeof(JSONmessageBuffer));
        
        if(aq.publish(TOPIC_NAME,JSONmessageBuffer) == 0)   
        {        
            Serial.print("[AWS]    Publish Message:");   
            Serial.println(JSONmessageBuffer);
            ERR_COUNT = 0;
        }
        else
        {
            Serial.println("[AWS]    Publish failed");
            ERR_COUNT++;

            if  (ERR_COUNT == 10) {
              general_err = true;
              wifi_err = true;
              aws_err = true;
              sub_err = true;
              normal_screen(temp_correct,data_aq.temp_ok);
              Serial.println("[AWS]    Failed to reach AWS");
              Serial.println("[WIFI]    Reconnection...");
              WiFi.begin(csid.c_str(), cpass.c_str());
              if(testWifi()){
                wifi_err = false;
                Serial.println("[WIFI]    Reconnected!");
                Serial.println("[AWS]    Reconnection...");
                aws_connect(10);
                general_err = false;
                }
              else{
                Serial.println("[WIFI]    Reconnection failed! Retrying...");
                  }
                  
              ERR_COUNT = 0;
            }
        }     
    } 
}
// ---------------------- AWS CONNECTION   -----------------------------------------
void aws_connect(int x){
  while (x>=0){
  if (aq.connect(HOST_ADDRESS,CLIENT_ID)== 0){
        Serial.println("[AWS]    Connected!");
        aws_err = false;
        normal_screen(temp_correct,data_aq.temp_ok);         
        //Items to Subscribe
        item_subscribe(SUBSCRIBE_ITEM,10); //ITEM,sec for retry
        return;
         }
     else {
        Serial.println("[WIFI]    Connection failed! Retrying...");
        x--;
    }
    }
  }

// ----------------------ITEM SUBSCRIBE    -----------------------------------------
void item_subscribe(char item[],int x){
  while (x>=0){
    if (aq.subscribe(item,mySubCallBackHandler)==0){ //If subscription ok
      Serial.printf("\n[SUBSCRIBE %s]   Success\n",item);
      sub_err = false;
      normal_screen(temp_correct,data_aq.temp_ok);
      return;
      }
     else {
      Serial.printf("\n[SUBSCRIBE %s]   Failed! Retrying...\n",item);
      delay(1000);
      x--;
      }
    }
    Serial.printf("\n[SUBSCRIBE %s]   Failed! Maybe AWS Problem!\n",item);
  }
// --------------------- TEST WIFI -----------------------------------------
bool testWifi(void) {
  int c = 0;
  Serial.println("Waiting for Wifi to connect");  
  while ( c < 20 ) {
    if (WiFi.status() == WL_CONNECTED) { return true; } 
    delay(500);
    Serial.print(WiFi.status());    
    c++;
  }
  Serial.println("");
  Serial.println("Connect timed out, opening AP");
  return false;
} 
// --------------------- LAUNCH WEB -----------------------------------------
void launchWeb(int webtype) {
  Serial.println("");
  Serial.println("WiFi ");
  Serial.print("Local IP: ");
  Serial.println(WiFi.localIP());
  Serial.print("SoftAP IP: ");
  Serial.println(WiFi.softAPIP());
  createWebServer(webtype);
  // Start the server
  server.begin();
  Serial.println("Server started"); 
}
// --------------------- SETUP AP -----------------------------------------
void setupAP(void) {
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(100);
  int n = WiFi.scanNetworks();
  Serial.println("scan done");
  if (n == 0)
    Serial.println("no networks found");
  else
  {
    Serial.print(n);
    Serial.println(" networks found");
    for (int i = 0; i < n; ++i)
     {
      // Print SSID and RSSI for each network found
      Serial.print(i + 1);
      Serial.print(": ");
      Serial.print(WiFi.SSID(i));
      Serial.print(" (");
      Serial.print(WiFi.RSSI(i));
      Serial.print(")");
      Serial.println((WiFi.encryptionType(i) == 7)?" ":"*");
      delay(10);
     }
  }
  Serial.println(""); 
  st = "<ol>";
  for (int i = 0; i < n; ++i)
    {
      // Print SSID and RSSI for each network found
      st += "<li>";
      st += WiFi.SSID(i);
      st += " (";
      st += WiFi.RSSI(i);
      st += ")";
      st += (WiFi.encryptionType(i) == 7)?" ":"*";
      st += "</li>";
    }
  st += "</ol>";
  delay(100);
  char temp1[ID.length()]; // Create custom AP name
  ID = String("MySmartAquarium - "+ID);
  ID.toCharArray(temp1,ID.length());
  Serial.println(temp1);
  WiFi.softAP(temp1, passphrase); // Create soft AP with name
  Serial.println("softap");
  launchWeb(1);
  Serial.println("over");
}


// --------------------- CREATE WEB SERVER -----------------------------------------
void createWebServer(int webtype)
{
  if ( webtype == 1 ) { // If Wifi is AP
    server.on("/", []() {
        IPAddress ip = WiFi.softAPIP();
        String ipStr = String(ip[0]) + '.' + String(ip[1]) + '.' + String(ip[2]) + '.' + String(ip[3]);
        content = "<!DOCTYPE HTML>\r\n<html>Hello from ESP8266 at ";
        content += ipStr;
        content += "<p>";
        content += st;
        content += "</p><form method='get' action='setting'><label>SSID: </label><input name='ssid' length=32><input name='pass' length=64><input type='submit'></form>";
        content += "</html>";
        server.send(200, "text/html", content);  
    });
    server.on("/setting", []() {
        String qsid = server.arg("ssid");
        String qpass = server.arg("pass");
        if (qsid.length() > 0 && qpass.length() > 0) {
          strcpy(data_aq.tsid,qsid.c_str());
          strcpy(data_aq.tpass,qpass.c_str());
          data_aq.IS_SETUP = true; //flag that won't setup anymore! (till clear eeprom)
          EEPROM_SAVE(); //Save all struct on eeprom
          content = "{\"Success\":\"OK\"}";
          statusCode = 200;
          server.send(statusCode, "application/json", content);
          restart_all(10); //RESET ESP to reload in NORMAL mode
          
        } else {
          content = "{\"Error\":\"404 not found\"}";
          statusCode = 404;
          Serial.println("Sending 404");
          server.send(statusCode, "application/json", content);
        }
        
    });
  } else if (webtype == 0) { //if Wifi is NORMAL !!!NEVER USED!!!
    server.on("/", []() {
      IPAddress ip = WiFi.localIP();
      String ipStr = String(ip[0]) + '.' + String(ip[1]) + '.' + String(ip[2]) + '.' + String(ip[3]);
      server.send(200, "application/json", "{\"IP\":\"" + ipStr + "\"}");
    });
    server.on("/cleareeprom", []() {
      content = "<!DOCTYPE HTML>\r\n<html>";
      content += "<p>Clearing the EEPROM</p></html>";
      server.send(200, "text/html", content);
      Serial.println("clearing eeprom");
      for (int i = 0; i < 96; ++i) { EEPROM.write(i, 0); }
      EEPROM.commit();
    });
  }
}
//-------------------------------
// ------------ RESTART ---------
void restart_all(int x){
  
  while(x>=0){
    Serial.print("Restart in ");
    Serial.println(x);
    delay(1000); //Wait 1 sec each
    x--;
      }
      
  Serial.println("Restarting aquarium...");
  ESP.restart();
}
// --------------- EEPROM SAVE ----------
void EEPROM_SAVE(){
  
  Serial.println("[EEPROM]  Writing data");
  EEPROM.put(0,data_aq);
  EEPROM.commit();
  Serial.println("[EEPROM]  Writing done!");
 }
 
// --------------- EEPROM LOAD ----------
void EEPROM_LOAD(){
  
  Serial.println("[EEPROM]  Reading data");
  EEPROM.get(0, data_aq);
  Serial.println("[EEPROM]  Reading done!");
    
 }


//---------------------------  SETUP --------------------------------------------
void setup()
{
  ID = getChipID();
  ID.toCharArray(TOPIC_NAME, ID.length()+2);


  Serial.begin(115200);
  u8g2.begin();
  EEPROM.begin(EEPROM_SIZE);
  pinMode(LED,OUTPUT);
  pinMode(TERMOS,OUTPUT);
  pinMode(LIGHT,OUTPUT);
  pinMode(FEED,OUTPUT);
  pinMode(OPT_1,OUTPUT);
  //pinMode(LUMINOSITY,INPUT);
  //digitalWrite(LED,LOW);
  delay(10);
  if (! rtc.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  }
  if (! rtc.isrunning()) {
    Serial.println("RTC is NOT running!");
    // rtc.adjust(DateTime(2014, 1, 21, 3, 0, 0));
  }
  
  Serial.println();
  Serial.println();
  Serial.println("Startup");
  Serial.println(TOPIC_NAME);
  Serial.printf("\nVer. %s\n",VER);
  
  EEPROM_LOAD(); // Load data
  
  csid = data_aq.tsid;
  cpass = data_aq.tpass;
  feed_total = data_aq.feed_tot;
  
  Serial.print("[DATA]  SSID =");
  Serial.println(csid);
  Serial.print("[DATA]  PASS =");
  Serial.println(cpass);
  Serial.print("[DATA]  IS_SETUP =");
  //data_aq.IS_SETUP = false; // LEAVE COMMENTED!!  just for debug
  Serial.println(data_aq.IS_SETUP);
  Serial.print("[DATA]  TEMP =");
  Serial.println(data_aq.temp_ok);
  Serial.print("[DATA]  FEED =");
  Serial.println(data_aq.feed_tot);
  Serial.print("[DATA]  DAY =");
  Serial.println(data_aq.sun_time);
  
  if (data_aq.IS_SETUP == false){
    Serial.println("[WIFI]    First start! Open AP Mode");
    setupAP(); //If never configured before
    }
    
  else {
  if ( csid.length() > 1 ) {
      normal_screen(temp_correct,data_aq.temp_ok);
      WiFi.mode(WIFI_STA);
      WiFi.begin(csid.c_str(), cpass.c_str());
      if (!testWifi()) // If can't connect to WIFI
        {
        Serial.printf("\n[WIFI]    Connection Problem");
        //setupAP();
        }

      else //If all OK!
        { 
        Serial.printf("\n[WIFI]    Connected\n");
        wifi_err = false;
        normal_screen(temp_correct,data_aq.temp_ok);
        //AWS connection
        aws_connect(10);
        general_err = false;
        normal_screen(temp_correct,data_aq.temp_ok);
        //--------DHT---------------------
        dht.begin();
        //--------------------------------


        //-------THREAD-------------------
        server_t.onRun(server_func);
        server_t.setInterval(3000 / portTICK_RATE_MS);

        local.onRun(local_func);
        local.setInterval(3000 / portTICK_RATE_MS); //Main thread, where screen is updated and local events handled. Every 10sec

      } 
  }
  
  else{
    Serial.println("[DATA]   Error loading data. Opening AP mode");
    //setupAP();
    }
  }
 }
//----------------------------------------------



//----------LOOP-------------------------------
void loop()
{
  if(server_t.shouldRun())
    server_t.run();
    
  if(local.shouldRun())
    local.run();
    
  server.handleClient();
}
//---------------------------------------------











