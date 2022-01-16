***
# KuraIotMqtt
***
## Table of Contents
1. [General Info](#general-info)
2. [Development Environment](#development-environment)
3. [System Requirements](#system-requirements)
4. [Build sources](#build-sources)
5. [Tested](#tested)
6. [Binary package](#binary-package)
7. [List of IoT device](#list-of-iot-devices)
8. [Topics Publish](#topics-publish)
9. [Installation](#installation)
10. [Links](#links)
***
## General Info
It is a simple Kura component to scheduler the switching on/off of relays. We will use Mqtt for sending messages and we can choose different devices.
***
## Development Environment
* Eclipse Neon Version: 2019-09 R (4.13.0). An additional plugin, mToolkit, is needed to allow remote connectivity to an OSGi framework on a Kura-enabled target device. To install mToolkit into Eclipse, use the following steps: 
  + Open the Help | Install New Software… menu.
  + Add the following URL as an update site based on your version of Eclipse http://mtoolkit-neon.s3-website-us-east-1.amazonaws.com
  + Install the “mToolkit” feature (you need to uncheck the Group items by category checkbox in order to see the feature).
  + Restart Eclipse. In the menu Window | Show View | Other, there should be an mToolkit | Frameworks option. If so, the plugin has been installed correctly.
* Development platform Linux (Debian 10).
***
## System Requirements
* Eclipse Kura 4.1.0 is compatible with Java 8 and [OSGi R6](https://docs.osgi.org/specification/).
* Zigbee2Mqtt.
***
## Build sources
The only requisite to build from sources is an already  
installed [Eclipse Kura User Workspace](https://www.eclipse.org/kura/downloads.php).
***  
## Tested
* Raspberry pi 3 and 4.
* Eclipse Kura 4.1.0.
* Gateway Zigbee CC2531
***
## Binary package
The binary package ready for the installation can be
found in folder "**resources/dp**".
***
## List of IoT devices
A list of IoT devices used within the project:
* Sonoff20 (Tasmota)
* TuyaZigbeeSensor (Zigbee2Mqtt)
***
## Topics Publish
The publish topic is created automatically and the device has to be configured with this same topic. 
1. Sonoff20
  + Topic publish: Dnomaid/stat/Router_1/"Type device"_"Number Device"/Relay_1/Power.
  + Ejemplo:> Type device: Sonoff20; Number Device: 1.
  +    Topic publish: Dnomaid/stat/Router_1/Sonoff20_1/Relay_1/Power.
2. XiaomiZNCZ04LM
  + Topic publish: Dnomaid/mix/CC2531_1/"Type device"_"Number Device"/RelaySensorClimate_1/Power.
  + Ejemplo:> Type device: XiaomiZNCZ04LM; Number Device: 2.
  +    Topic publish: Dnomaid/mix/CC2531_1/XiaomiZNCZ04LM_2/RelaySensorClimate_1/Power.
***
## Installation

***
## Links
Android application:
* https://github.com/DnomaidGit/AndroidIotMqtt
* https://play.google.com/store/apps/details?id=com.dnomaid.mqtt&gl=ES
***
