#!/bin/sh
java $JAVA_OPTS -classpath @INSTALL_PATH@;@INSTALL_PATH@\lib\openbp-cockpit-@APP_VER@.jar -DrootDir=@INSTALL_PATH@ org.openbp.cockpit.Cockpit
