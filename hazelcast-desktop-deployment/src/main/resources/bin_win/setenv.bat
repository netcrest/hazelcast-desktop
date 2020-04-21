@echo off

@pushd ..
@set BASE_DIR=%CD%
@popd

REM Set Hazelcast Installation Path
REM @set HAZELCAST_HOME=

REM Set Hazelcast Addon Installation Path
REM @set PADOGRID_HOME=

REM Set the Hazelcast major version number. Currenlty supported: 3 or 4.
@set HAZELCAST_MAJOR_VERSION_NUMBER=3

REM 
REM Set JAVA to the java executable of your choice.
REM Java 1.8 is recommended.
REM
REM @set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_144"

REM
REM Set CODEBASE_URL  to this desktop installation directory. 
REM You must follow the conventions shown below. The trailing '/' is required.
REM
REM @set CODEBASE_URL=file:///C:/Users/dpark/Work/git/Hazelcast/hazelcast-desktop/deploy/hazelcast-desktop_0.1.4-SNAPSHOT/
@set CODEBASE_URL=file:///%BASE_DIR%/

REM ------------------------------------------------------
REM Do NOT edit below this line
REM ------------------------------------------------------

@set SYSTEM_NAME=Desktop

@set JAVA="%JAVA_HOME%\bin\java"

@set DESKTOP_HOME=%BASE_DIR%
@set NAF_HOME=%DESKTOP_HOME%

@set LOG_DIR=%BASE_DIR%\log

REM log directory
if not exist "%LOG_DIR%" (
  @mkdir %LOG_DIR%
)

@set HAZELCAST_CLIENT_CONFIG_FILE="%DESKTOP_HOME%\etc\hazelcast-client.xml"

@set MAJOR_VERSION_DIR=v%HAZELCAST_MAJOR_VERSION_NUMBER%

@set SHARED_CACHE_CLASS=com.netcrest.pado.ui.swing.pado.hazelcast.%MAJOR_VERSION_DIR%.HazelcastSharedCacheV%HAZELCAST_MAJOR_VERSION_NUMBER%

@set JAVA_OPTS=-Xms256m -Xmx1024m -client ^
-DcodeBaseURL=%CODEBASE_URL% ^
-DpreferenceURL=etc/desktop.properties ^
-Dhazelcast.client.config=%HAZELCAST_CLIENT_CONFIG_FILE% ^
-Dhazelcast.diagnostics.metric.distributed.datastructures=true ^
-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl ^
-Dpado.sharedCache.class=%SHARED_CACHE_CLASS%

REM 
REM class path
REM
@set PLUGIN_JARS=%NAF_HOME%/plugins/*
@set LIB_JARS=%NAF_HOME%/lib/*;%NAF_HOME%/lib/%MAJOR_VERSION_DIR%/*
@set NAF_JARS=%NAF_HOME%/lib/naf/*
@set PADO_JARS=%NAF_HOME%/lib/pado/*
@set DEMO_JARS=%NAF_HOME%/lib/demo/*

@set CLASSPATH=%DESKTOP_HOME%;%DESKTOP_HOME%/classes;%PLUGIN_JARS%;%LIB_JARS%;%NAF_JARS%;%PADO_JARS%;%DEMO_JARS%
@set CLASSPATH=%CLASSPATH%;%HAZELCAST_HOME%/lib/*;%PADOGRID_HOME%/hazelcast/plugins/%MAJOR_VERSION_DIR%/*;%PADOGRID_HOME%/lib/*;%PADOGRID_HOME%/hazelcast/lib/%MAJOR_VERSION_DIR%/*
