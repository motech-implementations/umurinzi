#!/usr/bin/env bash

sudo apt-get update -qq

#Installation and configuration of ActiveMQ
sudo apt-get install -y activemq -qq
sudo ln -s /etc/activemq/instances-available/main /etc/activemq/instances-enabled/main
sudo sed -e 's/<broker /<broker schedulerSupport="true" /' -i /etc/activemq/instances-enabled/main/activemq.xml
sudo service activemq restart
