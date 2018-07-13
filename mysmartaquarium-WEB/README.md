## My Smart Aquarium (WEB)

This project aims to manage an aquarium remotely via Web and Esp32 chip.
It use AWS IoT APIs to publish to and subscribe from MQTT topics. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

It is created for:

  - Learning mobile computing
  - Learning to work with Arduino, particularly ESP32 chip
  - Developing a project for UniParthenope (in specific for mobile computing exam)
  
  # Installation
  
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
  
  ### Run server
  ~~~sh
  $ flask run -h 0.0.0.0
  ~~~
  
  
