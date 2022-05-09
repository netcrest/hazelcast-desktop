# Hazelcast Desktop

©2019-2022 Netcrest Technologies, LLC. All rights reserved.

## Introduction

This distribution contains Hazelcast Desktop powered by Pado Desktop.

## License
Apache License, Version 2.0

## Installing Hazelcast Desktop

You can install Hazelcast Desktop as a PadoGrid app or a standalone app.

### As PadoGrid App

Install the desktop app by running the PadoGrid's `create_app` as follows:

```console
create_app -app desktop
```

### As Standalone App

You must first download and build PadoGrid. Hazelcast Desktop depends on PadoGrid's Hazelcast addon library.

**PadoGrid Repo:** [https://github.com/padogrid/padogrid](https://github.com/padogrid/padogrid). 

Once the PadoGrid libary has been built and installed in your local Maven repository, edit the `pom.xml` file in the root (parent) directory to include the correct version number for `<padogrid.version>`. For example, the following includes version 0.9.14 in the `pom.xml` file.

```xml
<!-- In pom.xml (root directory) -->
<properties>
   ...
   <padogrid.version>0.9.14</padogrid.version>
</properties>
```

To build the desktop binary, run the Maven command as follows:

```console
mvn install
```

Upon successful build, you can find the Hazelcast Desktop distribution in the following `assembly` directory:

```console
tree hazelcast-desktop-deployment/target/assembly
```

**Output:**

```console
hazelcast-desktop-deployment/target/assembly
├── hazelcast-desktop_0.1.11-SNPASHOT.tar.gz
└── hazelcast-desktop_0.1.11-SNPASHOT.zip
```

#### Installing Hazelcast Desktop

1. Unzip the `hazelcast-desktop_<version>.zip` file anywhere in the file system. It will create the `hazelcast-desktop_<version>` root directory.
2. Edit `bin_win\setenv.bat` or `bin_sh/setenv.sh` to include the correct paths for **JAVA_HOME** and **CODEBASE_URL**.

## Java Versions

Hazelcast Desktop runs on Java 1.8 or later versions.

## Hazelcast Versions

Hazelcast Desktop supports Hazelcast 3.x and 4.x. It has been tested with Hazelcast 3.12.x, 4.x, and 5.x.


## Running Hazelcast Desktop

By default, Hazelcast Desktop shows the login window during startup. To use the login window, all of Hazelcast client configuration must be done in the `etc/pado.properties` file. To disable the login window and use the `etc/hazelcast-client.xml` file instead, set the `hazelcast.client.config.file.enabled` property to `true` in the `etc/pado.properties` as follows:

```console
hazelcast.client.config.file.enabled=true
```

### Application Library Files

Place all the application specific jar files in the `plugins/` directory, which is part of `CLASSPATH`.

### Linux/macOS

If you are running Hazelcast Desktop as a standalone and not as a desktop app in a `hazelcast-addon` workspace, then you must set the environment variables in `bin_sh/setenv.sh`.

- HAZELCAST_HOME
- PADOGRID_HOME
- HAZELCAST_MAJOR_VERSION_NUMBER
- JAVA_HOME

```console
cd bin_sh
./desktop
```

### Windows

Edit `bin_win\setenv.bat` and set the following environment variables:

- HAZELCAST_HOME
- PADOGRID_HOME
- HAZELCAST_MAJOR_VERSION_NUMBER
- JAVA_HOME

The following steps creates the Hazelcast Desktop shortcut on Windows.

```console
cd bin_win
create_shortcut.bat
```

You can use the shortcut to launch "Desktop" or start the desktop manually as follows:

```console
cd bin_win
./desktop.bat
```

## Login

The login dialog may require a valid app ID, user name, and password. If you are running Hazelcast running out of the box, then authentication is disabled. In that case, you can enter any user name and password. App ID is currently not supported.


## Desktop Templates

For Java 1.8, 11, and 12, Hazelcast Desktop initially opens with the pre-configured worksheet named *HazelcastExplorer*. If for some reason, the desktop does not display the HazelcastExplorer worksheet then you can open the appropriate template by selecting the pulldown menu, `File/Open...` The templates are included in the `etc/` directory. You can also layout the screen yourself as described below.

## Screen Layout

Hazelcast Desktop is comprised of GUI components (Java Beans) displayed by the Pado Desktop framework. You can lay out the screen by dividing the screen into "quad" panels. To create a quad, right-click on an empty quad (black background) and select one of the options in the popup menu. Each quad can host a single instance of any of the beans found in the Bean Bar. To add an instance of a GUI component, simply select one of the bean icons in the Bean Bar and then click on one of the empty quad panels.

## Saving Screens

Hazelcast Destop automatically saves the screen when it terminates such that it is reinstated when you restart it next time. You can also save the screen by selecting the pulldown menu, `File/Save As...` Note that Hazelcast Destkop uses the file extension `.desktop`.

## Screenshot

![Desktop Screenshot](https://github.com/padogrid/padogrid/raw/develop/images/desktop-screenshot.png)
