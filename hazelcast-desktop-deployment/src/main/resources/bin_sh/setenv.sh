#!/bin/bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"

#
# Set JAVA_HOME to the Java home (root) directory
#
if [ "`uname`" == "Darwin" ]; then
   # Mac
   export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
   #export JAVA_HOME=`/usr/libexec/java_home -v 11`
   #export JAVA_HOME=`/usr/libexec/java_home -v 12`
elif [ "`uname`" == "Linux" ]; then
   export JAVA_HOME=/apps/products/jdk1.8.0_211
else
   export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.8.0_211"
fi

CODEBASE_URL=file://localhost/$BASE_DIR/

JAVA=$JAVA_HOME/bin/java

export PATH=$JAVA_HOME/bin:$PATH

DESKTOP_HOME=$BASE_DIR
NAF_HOME=$DESKTOP_HOME

LOG_DIR=$BASE_DIR/log

# log directory
if [ ! -d $LOG_DIR ]; then
  mkdir -p $LOG_DIR
fi

HAZELCAST_CLIENT_CONFIG_FILE=$DESKTOP_HOME/etc/hazelcast-client.xml 

JAVA_OPTS="-Xms256m -Xmx1024m -client 
-DcodeBaseURL=$CODEBASE_URL \
-DpreferenceURL=etc/desktop.properties \
-Dhazelcast.client.config=$HAZELCAST_CLIENT_CONFIG_FILE
-Dhazelcast.diagnostics.metric.distributed.datastructures=true \
-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"

# 
# class path
#
PLUGIN_JARS=$NAF_HOME/plugins/*
LIB_JARS=$NAF_HOME/lib/*
NAF_JARS=$NAF_HOME/lib/naf/*
PADO_JARS=$NAF_HOME/lib/pado/*
DEMO_JARS=$NAF_HOME/lib/demo/*

export CLASSPATH=$DESKTOP_HOME:$DESKTOP_HOME/classes:$PLUGIN_JARS:$LIB_JARS:$PADO_JARS:$NAF_JARS:$DEMO_JARS
