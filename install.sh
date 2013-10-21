#! /bin/bash
sudo cp ./gnome-screensaver /etc/pam.d/
sudo cp ./notify-listener.sh /usr/local/bin/
sudo cp ./notify-daemon.sh /usr/local/bin/
sudo mkdir /usr/local/bin/lockers/
sudo chmod +x ./locker-listener.jar
sudo chmod +x ./locker-daemon.jar
sudo chmod +x ./env-logger.jar
sudo cp ./locker-listener.jar /usr/local/bin/lockers/
sudo cp ./locker-daemon.jar /usr/local/bin/lockers/
sudo cp ./env-logger.jar /usr/local/bin/lockers/

