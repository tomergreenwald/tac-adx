#!/bin/bash
#
# Usage
#   sh ./runMe.sh -file [filename]
#

TACAA_HOME=`dirname $0`
LIB=${TACAA_HOME}/lib
CLASSPATH=.
for i in $( ls ${LIB}/*.jar ); do
    CLASSPATH=${CLASSPATH}:$i
done


java -cp $CLASSPATH se.sics.tasim.logtool.Main $*