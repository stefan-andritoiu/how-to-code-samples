# Storage unit flood detector in Java*

## Introduction

This storage unit flood detector application is part of a series of how-to Internet of Things (IoT) code sample exercises using the Intel® IoT Developer Kit, Intel® Edison development platform, cloud platforms, APIs, and other technologies.

From this exercise, developers will learn how to:<br>
- Connect the Intel® Edison development platform, a computing platform designed for prototyping and producing IoT and wearable computing products.<br>
- Interface with the Intel® Edison platform IO and sensor repository using MRAA and UPM from the Intel® IoT Developer Kit, a complete hardware and software solution to help developers explore the IoT and implement innovative projects.<br>
- Run this code sample in Intel® System Studio IoT Edition. Intel® System Studio IoT Edition lets you create and test applications on Intel-based IoT platforms.<br>
- Store water detection data using Azure Redis Cache\* from Microsoft Azure\*, Redis Store\* from IBM Bluemix\*, or ElastiCache\* using Redis\* from Amazon Web Services\* (AWS), different cloud services for connecting IoT solutions including data analysis, machine learning, and a variety of productivity tools to simplify the process of connecting your sensors to the cloud and getting your IoT project up and running quickly.

## What it is

Using an Intel® Edison board, this project lets you create a storage unit flood detector that:<br>
- continuously checks the moisture sensor.<br>
- sounds an audible warning in case of a possible flooding.<br>
- stores a record of each time water is detected, using cloud-based data storage.

## How it works

This storage unit flood detector uses the moisture sensor to constantly ensure that your stored belongings are not destroyed by water damage.

If the moisture level exceeds a defined threshold, it makes a sound to indicate a warning.

Optionally, the monitor can store moisture data using the Intel® IoT Examples Datastore running in your own Microsoft Azure* account.

## Hardware requirements

Grove* Home Automation Kit containing:

1. Intel® Edison platform with an Arduino* breakout board
2. [Grove Moisture Sensor](http://iotdk.intel.com/docs/master/upm/node/classes/grovemoisture.html)
3. [Grove Speaker](http://iotdk.intel.com/docs/master/upm/node/classes/grovespeaker.html)


## Software requirements

1. Intel® System Studio IoT Edition
2. Microsoft Azure\*, IBM Bluemix\*, or AWS account (optional)

### How to set up

To begin, clone the **How-To Code Samples** repository with Git* on your computer as follows:

    $ git clone https://github.com/intel-iot-devkit/how-to-code-samples.git

To download a .zip file, in your web browser, go to <a href="https://github.com/intel-iot-devkit/how-to-code-samples">https://github.com/intel-iot-devkit/how-to-code-samples</a> and click the **Download ZIP** button at the lower right. Once the .zip file is downloaded, uncompress it, and then use the files in the directory for this example.

## Adding the program to Intel® System Studio IoT Edition

** The following screenshots are from the Alarm clock sample, however the technique for adding the program is the same, just with different source files and jars.

Open Intel® System Studio IoT Edition. It will start by asking for a workspace directory; choose one and then click OK.

In Intel® System Studio IoT Edition, select File -> new -> **Intel(R) IoT Java Project**:

![](./../../images/java/new_project.png)

Give the project the name "StorageUnitFloodDetector" and then click Next.

![](./../../images/java/project_name.png)

You now need to connect to your Intel® Edison board from your computer to send code to it.
Choose a name for the connection and enter the IP address of the Intel® Edison board in the "Target Name" field. You can also try to Search for it using the "Search Target" button. Click finish when you are done.

![](./../../images/java/Target_connection.png)

You have successfully created an empty project. You now need to copy the source files and the config file to the project.
Drag all of the files from your git repository's "src" folder into the new project's src folder in Intel® System Studio IoT Edition. Make sure previously auto-generated main class is overridden.

The project uses the following external jars: [gson-2.6.1](http://central.maven.org/maven2/com/google/code/gson/gson/2.6.1/gson-2.6.1.jar). These can be found in the Maven Central Repository. Create a "jars" folder in the project's root directory, and copy all needed jars in this folder.
In Intel® System Studio IoT Edition, select all jar files in "jars" folder and  right click -> Build path -> Add to build path

![](./../../images/java/add_to_build_path.png)

Now you need to add the UPM jar files relevant to this specific sample.
Right-click on the project's root -> Build path -> Configure build path. Java Build Path -> 'Libraries' tab -> click on "add external JARs..."

For this sample you will need the following jars:

1. upm_grovespeaker.jar
2. upm_grovemoisture.jar

The jars can be found at the IoT Devkit installation root path\iss-iot-win\devkit-x86\sysroots\i586-poky-linux\usr\lib\java

![](./../../images/java/add_external_jars_to_build_path.png)

### Connecting the Grove* sensors

![](./../../images/java/flood-detect.jpg)

You need to have a Grove* Shield connected to an Arduino*-compatible breakout board to plug all the Grove devices into the Grove Shield. Make sure you have the tiny VCC switch on the Grove Shield set to 5V.

1. Plug one end of a Grove cable into the Grove Moisture Sensor, and connect the other end to the A0 port on the Grove Shield.

2. Plug one end of a Grove cable into the Grove Speaker, and connect the other end to the D5 port on the Grove Shield.

### Data store server setup

Optionally, you can store the data generated by this sample program in a backend database deployed using Microsoft Azure\*, IBM Bluemix\*, or AWS\*, along with Node.js\*, and a Redis* data store.

For information on how to set up your own cloud data server, go to:

<a href="https://github.com/intel-iot-devkit/intel-iot-examples-datastore">https://github.com/intel-iot-devkit/intel-iot-examples-datastore</a>

## Configuring the example

To configure the example for the optional data store, change the `SERVER` and `AUTH_TOKEN` keys in the `config.properties` file to the server URL and authentication token that correspond to your own data store server setup. For example:

```
  SERVER=http://intel-examples.azurewebsites.net/logger/flood-detect
  AUTH_TOKEN=s3cr3t

```

## Preparing the Intel® Edison board before running the project

In order for the sample to run you will need to copy some files to the Intel® Edison board. This can be done using SCP through SSH.
Two sorts of files need to be copied from the sample repository:<br>
Jar files- external libraries in the project need to be copied to "/usr/lib/java"


## Running the program using Intel® System Studio IoT Edition

When you're ready to run the example, make sure you have saved all the files.

Click the **Run** icon on the toolbar of Intel® System Studio IoT Edition. This runs the code on the Intel® Edison board.

![](./../../images/java/run_project.png)

## Running the program from the command line

This can be easily achieved with basic Maven commands. For this to work you will need to have Maven installed, a guide can be found on the Maven website: <a href="https://maven.apache.org/install.html">https://maven.apache.org/install.html</a>

### Compiling on host machine and deploying to Intel® Edison board

If you want to compile the project on your local PC and then deploy it to the target you need to run `mvn package` at the location where the `pom.xml` file exists, or you can specify the file location using the `-f` parameter:

	$ mvn package -f <path_to_pom_file>

This will compile the source files and pack them in `.jar` archives. It will create a folder called `target` where you will find two jars, `StorageUnitFloodDetector-1.0-SNAPSHOT.jar` and `StorageUnitFloodDetector-1.0-SNAPSHOT-shaded.jar`. The first one contains only the classes from the current module, while the `shaded` version contains the classes from the current module and its dependencies, so running the program using the second jar will be easier since you don't have to worry about adding all the dependency jars to the classpath.

Next step is to copy the generated jar on the target using `scp`. The following command will copy the file to the `home` folder of user `root` on the Intel® Edison board:

	$ scp target/StorageUnitFloodDetector-1.0-SNAPSHOT-shaded.jar root@<target_ip>:

Then log in on the target using ssh:

	$ ssh root@<target_ip>

Next step is to run the program using `java`, providing the path to the copied jar file and the name of the `main` class:

	# java -cp StorageUnitFloodDetector-1.0-SNAPSHOT-shaded.jar howToCodeSamples.StorageUnitFloodDetector

### Running the program direcly on the Intel® Edison board

If you have copied the source files from Git directly on the board and already installed Maven, then you can compile and run the program direcly onto the target.
Log in to the board using ssh and navigate to the location of the `pom.xml` file.

First you will need to compile the source files:

	# mvn compile

Then you can execute the program. The following command will run the `main` file in a separate Java process:

	# mvn exec:exec 

### Determining the IP address of the Intel® Edison board

You can determine what IP address the Intel® Edison board is connected to by running the following command:

    ip addr show | grep wlan

You will see output similar to the following:

    3: wlan0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast qlen 1000
        inet 192.168.1.13/24 brd 192.168.1.255 scope global wlan0

The IP address is shown next to `inet`. In the example above, the IP address is `192.168.1.13`.


IMPORTANT NOTICE: This software is sample software. It is not designed or intended for use in any medical, life-saving or life-sustaining systems, transportation systems, nuclear systems, or for any other mission-critical application in which the failure of the system could lead to critical injury or death. The software may not be fully tested and may contain bugs or errors; it may not be intended or suitable for commercial release. No regulatory approvals for the software have been obtained, and therefore software may not be certified for use in certain countries or environments.
