## MySmartAquarium-Arduino-ESP32

This project aims to manage an aquarium remotely via Android app and Esp32 chip.
It use AWS IoT APIs to publish to and subscribe from MQTT topics. Authentication of the connection is done with Amazon Cognito. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

It is created for:

  - Learning mobile computing
  - Learning to work with Arduino, particularly ESP32 chip
  - Developing a project for UniParthenope (in specific for mobile computing exam)
  
  
## How to install

  1. Download the project or clone the repository from Github.
  2. Downolad the AWS IoT Library from (https://github.com/ExploreEmbedded/Hornbill-Examples).
  3. Open Arduino IDE and define the below parameters in the sketch:

```
HOST_ADDRESS
TOPIC_NAME
SUBSCRIBE_ITEM
```

  HOST_ADDRESS can be find in AWS IoT console. This is APIs endpoint and it is used to estabilish a connection with MQTT server to publish and subscribe topic.

  TOPIC_NAME is used to identify a specific aquarium, indeed when an user register an aquarium from Android app, he must insert the ID that is the same of TOPIC_NAME.

  SUBSCRIBE_ITEM is used to subscribe elements in AWS Iot and it is used in the Android App to send the settings and modify the state of the aquarium.

  4. Download the below certificates from AWS Iot console:

```
  root-CA.crt
  certificate.pem.crt
  private.pem.key
```
  Above certificates needs to be stored in the file aws_iot_certificates.c(AWS_IOT library) as arrays.

  This certificates allow a user to publish and subscribe a topic.

  5. Run the sketch.


## Library used

Download the following library:

  * AWS Iot https://github.com/ExploreEmbedded/Hornbill-Examples
  * Thread https://github.com/ivanseidel/ArduinoThread
  * ArduinoJson https://github.com/bblanchon/ArduinoJson
  * Adafriut https://github.com/espressif/arduino-esp32
  * Reading Thermistor - NTC 10K Analog https://playground.arduino.cc/ComponentLib/Thermistor2
  * WebServer-esp32 https://github.com/zhouhan0126/WebServer-esp32


## Schematic
