# UniversalUPnP
 A client to allow users to create UPnP mappings manually, useful for software/games that don't make mappings themselves automatically. Click [here](https://github.com/Moxeja/UniversalUPnP/wiki) to see how to use the
 software.
 
 Uses GSON, Cling and Seamless libraries from https://github.com/google/gson,
 https://github.com/4thline/cling and https://github.com/4thline/seamless respectively.

 Requires Java Runtime 8 or later.
 
# Building
### Requirements
* Maven
* Java Development Kit 8 or higher

### Using Maven
 To build, use the following command in the UniversalUPnP directory:
 ``` shell
  mvn clean package
 ```
 The built file will be in the UniversalUPnP/target folder.
 
 Alternatively, add the project as a Maven project to an IDE, and build with the "package" goal.

# Known Issues
* [SERIOUS] In command line mode, if the user closes the console before stopping the mappings, the ports will remain open.
	* To fix: start the program in command line mode again ``` java -jar <path-to-jar> -startall ``` and stop the mappings properly with the
	``` stop ``` command.
