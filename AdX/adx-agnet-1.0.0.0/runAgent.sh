#!/bin/bash
#
# Usage
#   sh ./runServer.sh
#

TACAA_HOME=`pwd`
echo $TACAA_HOME
echo $CLASSPATH

java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-1.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-2.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-3.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-4.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-5.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-6.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-7.conf&
java -cp "lib/*" tau.tac.adx.agentware.Main -config config/aw-8.conf&
