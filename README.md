![MySmartAquarium Logo](https://github.com/teamTMM-UNP/MySmartAquarium/blob/master/graphic/logo_medium.png)
![MySmartAquarium Name](https://github.com/teamTMM-UNP/MySmartAquarium/blob/master/graphic/name.png)

This project aims to manage an aquarium remotely via Android app and Esp32 chip. It use AWS IoT APIs to publish to and subscribe from MQTT topics. Authentication of the connection is done with Amazon Cognito. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

To better organize the project, it is divided in two subfolders.

### Built With

* [Amazon IoT](https://aws.amazon.com/iot/) - IoT used.
* [ESP32](http://esp32.net/) - ESP32 Microcontroller used.

### Main Functions

* **Temperature**
* **Feed**
* **Lighting**
* **Alarms**

### MySmartAquarium - ESP32

This part is the core of project, made with ESP32 Microcontroller and coded with Arduino IDE.
Project and full documentation can be found at:

[MySmartAquarium-ESP32](https://github.com/teamTMM-UNP/MySmartAquarium/tree/master/mysmartaquarium-ESP32)

### MySmartAquarium - APK (Android)

With this app it is possible to handle aquariums remotely, just by using an Android device.

[MySmartAquarium-APK](https://github.com/teamTMM-UNP/MySmartAquarium/tree/master/mysmartaquarium-APK)

### MySmartAquarium - WebSite

With the website it is possible to handle aquariums remotely, just by using a browser.

### Authors

* **Ciro Giuseppe De Vita** - *Founders* - [Github](https://github.com/ciro97)
* **Gennaro Mellone** - *Founders* - [Github](https://github.com/Shottyno)

Project developed during the courses *Mobile Computings* and *Web Technologies* at *Department of Science and Technologies - University of Napoli "Parthenope"*.

See also the list of [contributors](https://github.com/teamTMM-UNP/MySmartAquarium/blob/master/CONTRIBUTORS.md) who participated in this project.
