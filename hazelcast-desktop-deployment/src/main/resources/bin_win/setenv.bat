@echo off

REM 
REM Set JAVA to the java executable of your choice.
REM Java 1.8 is recommended.
REM
@set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_102"

REM
REM Set CODEBASE_URL  to this desktop installation directory. 
REM You must follow the conventions shown below. The trailing '/' is required.
REM
@set CODEBASE_URL=file:///C:/Work/hazelcast-desktop_0.1.0-SNAPSHOT/


REM ------------------------------------------------------
REM Do NOT edit below this line
REM ------------------------------------------------------

@set SYSTEM_NAME=Desktop

@set JAVA=%JAVA_HOME%\bin\java

@pushd ..
@set BASE_DIR=%CD%
@popd

@set DESKTOP_HOME=%BASE_DIR%
@set NAF_HOME=%DESKTOP_HOME%

@set LOG_DIR=%BASE_DIR%\log

REM log directory
if not exist "%LOG_DIR%" (
  @mkdir %LOG_DIR%
)

@set HAZELCAST_CLIENT_CONFIG_FILE="%DESKTOP_HOME%\etc\hazelcast-client.xml"

echo %JAVA_OPTS%
REM 
REM class path
REM
@set PLUGIN_JARS=%NAF_HOME%/plugins/*
@set LIB_JARS=%NAF_HOME%/lib/*
@set NAF_JARS=%NAF_HOME%/lib/naf/*
@set PADO_JARS=%NAF_HOME%/lib/pado/*
@set DEMO_JARS=%NAF_HOME%/lib/demo/*

@set CLASSPATH=%DESKTOP_HOME%;%DESKTOP_HOME%/classes;%PLUGIN_JARS%;%LIB_JARS%;%NAF_JARS%;%PADO_JARS%;%DEMO_JARS%
