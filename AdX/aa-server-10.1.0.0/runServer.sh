#!/bin/bash
#
# Usage
#   sh ./runServer.sh
#

TACAA_HOME=`pwd`
LIB=${TACAA_HOME}/lib
CLASSPATH=.
for i in $( ls ${LIB}/*.jar ); do
    CLASSPATH=${CLASSPATH}:$i
done
echo $TACAA_HOME
echo $CLASSPATH

java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -cp $CLASSPATH se.sics.tasim.sim.Main
#java -cp $CLASSPATH se.sics.tasim.sim.Main
