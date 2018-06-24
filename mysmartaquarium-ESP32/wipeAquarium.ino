/*
 *  wipeAquarium 
 *  Is used to wipe and factory reset MySmartAquarium device ESP32 EEPROM.
 *  
 *  Developed by Ciro De Vita and Gennaro Mellone.
 *  
 */

#include <Arduino.h>
#include <EEPROM.h>

#define EEPROM_SIZE 512

//-------- DATA ---------
typedef struct data_d
{
    char tsid[50];
    char tpass[50];
    bool IS_SETUP;
    
} DATA;

DATA data_aq;

void setup() {
  Serial.begin(115200);
  Serial.println("");
  delay(2000);
  EEPROM.begin(512);

  // READ OLD DATA
  EEPROM.get(0, data_aq);
  Serial.printf("\n\n\nOLD DATA\n\n");
  Serial.print("[DATA]  SSID = ");
  Serial.println(data_aq.tsid);
  Serial.print("[DATA]  PASS = ");
  Serial.println(data_aq.tpass);
  Serial.print("[DATA]  IS_SETUP = ");
  Serial.println(data_aq.IS_SETUP);
  Serial.print("[DATA]  temp_ok = ");
  Serial.println(data_aq.temp_ok);
  Serial.print("[DATA]  sun_time = ");
  Serial.println(data_aq.sun_time);
  Serial.print("[DATA]  feed_tot = ");
  Serial.println(data_aq.feed_tot);
  Serial.print("[DATA]  feed_rem = ");
  Serial.println(data_aq.feed_rem);
  Serial.print("[DATA]  actual_day = ");
  Serial.println(data_aq.actual_day);
  
  // CLEAN ALL VARIABLES
  for( int i = 0; i < sizeof(data_aq.tsid);  ++i )
   data_aq.tsid[i] = (char)0;
     for( int i = 0; i < sizeof(data_aq.tpass);  ++i )
   data_aq.tpass[i] = (char)0;
   
    data_aq.IS_SETUP = false;
    data_aq.temp_ok = 0;
    data_aq.sun_time = 0;
    data_aq.feed_tot = 0;
    data_aq.feed_rem = 0;
    data_aq.actual_day = 0;

  EEPROM.put(0, data_aq);
  EEPROM.commit();

  
  // READ NEW DATA
  EEPROM.get(0, data_aq);
  Serial.printf("\n\n\nOLD DATA\n\n");
  Serial.print("[DATA]  SSID = ");
  Serial.println(data_aq.tsid);
  Serial.print("[DATA]  PASS = ");
  Serial.println(data_aq.tpass);
  Serial.print("[DATA]  IS_SETUP = ");
  Serial.println(data_aq.IS_SETUP);
  Serial.print("[DATA]  temp_ok = ");
  Serial.println(data_aq.temp_ok);
  Serial.print("[DATA]  sun_time = ");
  Serial.println(data_aq.sun_time);
  Serial.print("[DATA]  feed_tot = ");
  Serial.println(data_aq.feed_tot);
  Serial.print("[DATA]  feed_rem = ");
  Serial.println(data_aq.feed_rem);
  Serial.print("[DATA]  actual_day = ");
  Serial.println(data_aq.actual_day);

}

void loop() {
}
