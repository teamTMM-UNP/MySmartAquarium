## My Smart Aquarium (WEB)

This project aims to manage an aquarium remotely via Web and Esp32 chip.
It use AWS IoT APIs to publish to and subscribe from MQTT topics. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

It is created for:

  - Learning mobile computing
  - Learning to work with Arduino, particularly ESP32 chip
  - Developing a project for UniParthenope (in specific for mobile computing exam)
  
  # Installation (FLASK)
  
  ### First of all, clone the repository and create a virtual environment
  ~~~sh
  $ git clone https://github.com/teamTMM-UNP/MySmartAquarium.git
  $ cd MySmartAquarium/mysmartaquarium-WEB
  $ virtualenv venv
  $ . venv/bin/activate
  ~~~
  
  ### Install the requirements
  ~~~sh
  $ pip install -r requirements.txt
  ~~~
  
  ### Run database
  ~~~sh
  $ flask db init
  $ flask db migrate
  $ flask db upgrade
  ~~~
  
  
  ### Installation (AWS-SDK-JS)
  **NOTE:** AWS IoT SDK will only support Node version 4 or above.
  
  You can check your node version by 
  ~~~sh
  node -v
  ~~~
  
  Install Aws-sdk-iot-device from github:
  ~~~sh
  $ git clone https://github.com/aws/aws-iot-device-sdk-js.git
  $ cd aws-iot-device-js
  $ npm install
  ~~~
  
  Copy all files from 'aws-iot-device-js' to 'MySmartAquarium/mysmartaquarium-WEB'.
  
  Modify the file aws-configuration.js with your AWS credentials.
  
  Install broswerify:
  ~~~sh
  $ npm install broswerify
  ~~~
  
  And run the following command:
  ~~~sh
  $ npm run-script browserize app/static/js/js/index.js
  ~~~
  
  ### Run server
  ~~~sh
  $ flask run -h 0.0.0.0
  ~~~
  
  Now the app is running on local server (http://127.0.0.1:5000/login)
  
