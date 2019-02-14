SOURCE_PATH=plugin
BUILD_PATH=build
SERVER_PATH=server
BUKKIT_JAR_FILENAME=spigot.jar

.PHONY: all
all: plugin server

.PHONY: plugin
plugin:
	# step 1 clean up / erase old version
	-rm -r -f $(BUILD_PATH)
	mkdir $(BUILD_PATH) && mkdir $(BUILD_PATH)/bin
	# step 2 compile the plugin into the bin dir
	javac -cp $(SERVER_PATH)/$(BUKKIT_JAR_FILENAME) -d $(BUILD_PATH)/bin $(SOURCE_PATH)/me/insanj/pride/*.java
	# step 3 copy config .yml to a new "build in progress" directory
	cp -r $(SOURCE_PATH)/plugin.yml $(BUILD_PATH)/bin/plugin.yml
	# step 4 create JAR file using the "build in progress" folder
	jar -cvf $(BUILD_PATH)/Pride.jar -C $(BUILD_PATH)/bin .
	# step 5 remove any existing plugin on the server in the server folder
	-rm -r -f $(SERVER_PATH)/plugins/Pride.jar
	# step 6 copy the JAR file into the server to run it!
	cp -r $(BUILD_PATH)/Pride.jar $(SERVER_PATH)/plugins/Pride.jar

.PHONY: server
server:
	# step 7 run the server!
	cd $(SERVER_PATH) && java -Xms1G -Xmx1G -jar -DIReallyKnowWhatIAmDoingISwear $(BUKKIT_JAR_FILENAME)
