## MySmartAquarium-Arduino-ESP32

This project aims to manage an aquarium remotely via Android app and Esp32 chip.
It use AWS IoT APIs to publish to and subscribe from MQTT topics. Authentication of the connection is done with Amazon Cognito. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

It is created for:

  - Learning mobile computing
  - Learning to work with Arduino, particularly ESP32 chip
  - Developing a project for UniParthenope (in specific for mobile computing exam)
  
  
## AWS IoT

This template is based on and contains the code from the [aws-iot-device-sdk-embedded-C](https://github.com/aws/aws-iot-device-sdk-embedded-C) project with an ESP32 port.

Define the below parameters in the sketch:
```
HOST_ADDRESS
TOPIC_NAME
```
HOST_ADDRESS can be find in AWS IoT console. This is APIs endpoint and it is used to estabilish a connection with MQTT server to publish and subscribe topic.

TOPIC_NAME is used to identify a specific aquarium, indeed when an user register an aquarium from Android app, he must insert the ID that is the same of TOPIC_NAME

*Certificates*
```
  root-CA.crt
  certificate.pem.crt
  private.pem.key
```
Above certificates needs to be stored in the file aws_iot_certificates.c as arrays. Check the file for more information.
 
![](https://exploreembedded.com/wiki/images/b/b9/ESP32_AWS_IOT_Certificates.png)

This certificates allow a user to publish and subscribe a topic.

* Generating certificates:

1. Open the AWS Iot console
2. Go to Security menu and click on Create Certificate to register the certificates. 
3. Click on the activate button to activate the certificates and download the certificate,private key and root CA as name them as below.

```
root-CA.crt
certificate.pem.crt
private.pem.key
```
