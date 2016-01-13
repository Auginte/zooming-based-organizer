Installing Auginte
==================

Binary releases as well as compressed sources are available at:
https://github.com/Auginte/zooming-based-organizer/releases

These instructions are for Desktop version af Auginte.

Desktop version on Ubuntu 15.10
-------------------------------

Using Ubuntu as example, same procedure should also work on other Debian based Linux Distributions


1. Ensure Oracle JRE 1.7+ is installed

```
java -version
```

If not, install:

```
sudo apt-get install python-software-properties
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

2. Download Application: https://github.com/Auginte/zooming-based-organizer/releases,
   For example `auginte-desktop-assembly-0.8.6.jar`
3. Run application: `java -jar auginte-desktop-assembly-0.8.6.jar`
   You should see Logo while loading and new Auginte window with further instructions


Running from source
-------------------

1. Download sbt: http://www.scala-sbt.org/download.html
2. Run `sbt "project auginteDesktop" run`


Using custom Database
---------------------

```
java -jar auginte-desktop-assembly-0.8.6.jar --dbConnection=remote --dbName=localhost/databaseName --dbUser=root --dbPassword=databasePassword
``

Compatible with `OrinetDB` 2.1.4


Clearing/Backing up settings
----------------------------

OrientDB database files are saved into `~/.auginte`
Close your application, move/remove `.auginte` folder and start `Auginte` again.


References
----------

* https://www.digitalocean.com/community/tutorials/how-to-install-java-on-ubuntu-with-apt-get