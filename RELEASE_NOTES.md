# Hazelcast-Desktop

©2019-2023 Netcrest Technologies, LLC. All rights reserved.
https://github.com/netcrest/hazelcast-desktop

## Version 0.1.14-SNAPSHOT

### Release Date: 09/25/2023

- Initial snapshot.

---

## Version 0.1.13

### Release Date: 09/25/2023

- Added templates for Java 19 - Java 21.

---

## Version 0.1.12

### Release Date: 07/09/2022

- Added templates for Java 1.8 - Java 18.

----

## Version 0.1.11

### Release Date: 05/04/2022

- Removed Hazelcast artifacts from the distribution.

----

## Version 0.1.10

### Release Date: 05/01/2022

- Added v5 module in prepartion for the new SQL support in 5.x.

----

## Version 0.1.9

### Release Date: 09/17/2021

- Added support for Hazelcast 5.x. Only the build setenv.sh file has been updated to share v4 with v5.

----

## Version 0.1.8

### Release Date: 08/30/2021

- Added instance name and remove avro dependencies that conflict with Hazelcast jars.

----

## Version 0.1.7

### Release Date: 05/21/2020

- Replaced assembly includes with padogrid-addon artifacts.

----

## Version 0.1.6

### Release Date: 04/20/2020

- Fixed PadoGrid name issues in standalone version.

----

## Version 0.1.5

### Release Date: 04/04/2020

- Added support for PadoGrid

----

## Version 0.1.4

### Release Date: 12/03/2019

- Added support for Hazelcast 4.0.

----

## Version 0.1.3

### Release Date: 07/01/2019

- Removed GemFire dependency.
- Removed non-relevant check boxes.

----

## Version 0.1.2

### Release Date: 06/28/2019

- Added hazelcast-addon build support. With this enhancement, hazelcast-desktop
  is now a part of the hazelcast-addon build process which includes 
  hazelcast-desktop as an app to its distribution file.
- Addes support for Cygwin.

----

## Version 0.1.1
### Release Date: 06/11/2019

- The desktop now first connects to the cluster without credentials before supplying
  credentials. This is to prevent the cluster with security disabled from rejecting 
  the clients that supply credentials.

----

## Version 0.1.0
### Release Date: 05/27/2019

- Initial release.
