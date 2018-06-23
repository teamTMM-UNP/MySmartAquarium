## My Smart Aquarium-APK (Android)

This project aims to manage an aquarium remotely via Android app and Esp32 chip.
It use AWS IoT APIs to publish to and subscribe from MQTT topics. Authentication of the connection is done with Amazon Cognito. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

It is created for:

  - Learning mobile computing
  - Learning to work with Arduino, particularly ESP32 chip
  - Developing a project for UniParthenope (in specific for mobile computing exam)
  
## Requirements

* AndroidStudio or Eclipse
* Android API 10 or greater

## How to use

Import the MySmartAquarium project into your IDE.
   - If you are using Android Studio:
      * From the Welcome screen, click on "Import project".
      * Browse to the MySmartAquarium directory and press OK.
      * Accept the messages about adding Gradle to the project.
      * If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.
      
      
Import the libraries :
   - If you use Android Studio, Gradle will take care of downloading these dependencies for you.
   - else you will need to download the AWS SDK for Android (http://aws.amazon.com/mobile/sdk/) and extract and copy these jars into the 'libs' directory for the project:
      * aws-android-sdk-core-X.X.X.jar
      * aws-android-sdk-iot-X.X.X.jar
