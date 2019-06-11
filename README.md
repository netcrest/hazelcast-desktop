# Hazelcast Desktop

©2019 Netcrest Technologies, LLC. All rights reserved.

## Introduction

This distribution contains Hazelcast Desktop powered by Pado Desktop.

## License
Apache License, Version 2.0


## Downloading Hazelcast Desktop

Hazelcast Desktop is open source and downloadable from https://github.com/netcrest/hazelcast-desktop


## Compiling Hazelcast Desktop

To compile Hazelcast Desktop, you must first obtain the hazelcast-addon library provided by Hazelcast Consulting team. This library comes in the form of a single jar file which must be installed in the local repository by executing the following:

```
mvn install:install-file -Dfile=<path-to-hazelcast-addon-file> -DgroupId=org.hazelcast.addon \
    -DartifactId=hazelcast-addon -Dversion=<version> -Dpackaging=jar
```

Once the hazelcast-addon libary has been installed in your local repository, edit the `pom.xml` file in the root (parent) directory to include the correct version number for `<hazelcast-addon.version>`. For example, the following includes version 0.1.2 in the pom.xml file.

```xml
<!-- In pom.xml (root directory) -->
<properties>
   ...
   <hazelcast-addon.version>0.1.2</hazelcast-addon.version>
</properties>
```

To build the desktop binary, run the maven command as follows:

```
mvn -DskipTests install
```

Upon successful build, you can find the Hazelcast Desktop distribution in the following `assembly` directory:

```
ls hazelcast-desktop-deployment/target/assembly/
hazelcast-desktop_0.1.0-SNAPSHOT.tar.gz	hazelcast-desktop_0.1.0-SNAPSHOT.zip
```

## Installing Hazelcast Desktop

1. Unzip the `hazelcast-desktop_<version>.zip` file anywhere in the file system. It will create the `hazelcast-desktop_<version>` root directory.
2. Edit `bin_win\setenv.bat` or `bin_sh/setenv.sh` to include the correct paths for **JAVA_HOME** and **CODEBASE_URL**.

## Java Versions

Hazelcast Desktop runs on Java 1.8 or later versions.


## Running Hazelcast Desktop

By default, Hazelcast Desktop shows the login window during startup. To use the login window, all of Hazelcat client configuration must be done in the `etc/pado.properties` file. To disable the login window and use the `etc/hazelcast-client.xml` file instead, set the `hazelcast.client.config.file.enabled` properpty to `true` in the `etc/pado.properties` as follows:

```
hazelcast.client.config.file.enabled=true
```

### Linux/MacOS
Edit ```bin_sh\setenv.sh``` to include the correct paths for **JAVA_HOME**. 

```
cd bin_sh
./desktop
```

### Windows
Edit `bin_win\setenv.bat` to include the correct paths for **JAVA_HOME** and **CODEBASE_URL**.
The following steps creates the Hazelcast Desktop shortcut on Windows.

```
cd bin_win
create_shortcut.bat
```

You can use the shortcut to launch "Desktop" or start the desktop manually as follows:

```
cd bin_win
./desktop.bat
```

## Login

The login dialog may require a valid app ID, user name, and password. If you are running Hazelcast running out of the box, then authentication is disabled. In that case, you can enter any user name and password. App ID is currently not supported.


## Desktop Templates

For Java 1.8, 11, and 12, Hazelcast Desktop initially opens with the pre-configured worksheet named *HazelcastExplorer*. If for some reason, the desktop does not display the HazelcastExplorer worksheet then you can open the appropriate template by selecting the pulldown menu, `File/Open...` The templates are included in the `etc/` directory. You can also layout the screen yourself as described below.

## Screen Layout

Hazelcast Desktop is comprised of GUI components (Java Beans) displayed by the Pado Desktop framework. You can lay out the screen by dividing the screen into "quad" panels. To create a quad, right-click on an empty quad (black background) and select one of the options in the popup menu. Each quad can host a single instance of any of the beans found in the Bean Bar. To add an instance of a GUI component, simply select one of the bean icons in the Bean Bar and then click on one of the empty quad panels.

## Saving Screen

Hazelcast Destop automatically saves the screen when it terminates such that it is reinstated when you restart it next time. You can also save the screen by selecting the pulldown menu, `File/Save As...` Note that Hazelcast Destkop uses the file extension `.desktop`.
