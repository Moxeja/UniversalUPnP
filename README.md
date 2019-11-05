# UniversalUPnP
 A client to allow users to create UPnP mappings manually Click [here](https://github.com/Moxeja/UniversalUPnP/wiki) to see how to use the
 software.
 
 Uses GSON, Cling and Seamless libraries from https://github.com/google/gson,
 https://github.com/4thline/cling and https://github.com/4thline/seamless respectively.

 Requires Java 8 or later.
 
# Building
 To build, use:
 ``` shell
  mvn clean package
 ```
 The built file will be in the UniversalUPnP/target folder.
 
 Alternatively, add the project as a Maven project to an IDE, and build with the "package" goal.

# Known Issues
* [SERIOUS] In command line mode, if the user closes the console before stopping the mappings, the ports will remain open.
	* To fix: start the program in command line mode again ``` java -jar <path-to-jar> -startall ``` and stop the mappings properly with the
	``` stop ``` command.
