SHELL:=/bin/bash
SOURCE_PATH=plugin
BUILD_PATH=build
SERVER_PATH=server
BUKKIT_JAR_FILENAME=spigot-1.16.5.jar
GIT_TAG:=$(shell git describe --tags)
OUTPUT_NAME=pride-$(GIT_TAG)

.PHONY: all
all: plugin server

.PHONY: plugin
plugin:
	# step 1 clean up / erase old version
	-rm -r -f $(BUILD_PATH)
	mkdir $(BUILD_PATH) && mkdir $(BUILD_PATH)/bin
	# step 1.5 move all external files to same dir
	# cp $(SERVER_PATH)/$(BUKKIT_JAR_FILENAME) $(EXTERNAL_PATH)/
	# step 2 compile the plugin into the bin dir
	javac -cp $(SERVER_PATH)/$(BUKKIT_JAR_FILENAME) -d $(BUILD_PATH)/bin $(SOURCE_PATH)/me/insanj/pride/*.java
	# step 3 copy config .yml to a new "build in progress" directory
	cp -r $(SOURCE_PATH)/*.yml $(BUILD_PATH)/bin/
	# step 4 create JAR file using the "build in progress" folder
	jar -cvf $(BUILD_PATH)/$(OUTPUT_NAME).jar -C $(BUILD_PATH)/bin .

.PHONY: server
server:
	# step 5 remove any existing plugin on the server in the server folder
	-rm -r -f $(SERVER_PATH)/plugins/$(OUTPUT_NAME).jar
	# step 6 copy the JAR file into the server to run it!
	-rm -r -f $(SERVER_PATH)/plugins/pride*.jar
	-cp -r $(BUILD_PATH)/$(OUTPUT_NAME).jar $(SERVER_PATH)/plugins/$(OUTPUT_NAME).jar
	# step 7 run the server!
	cd $(SERVER_PATH) && java -Xms1G -Xmx1G -jar -DIReallyKnowWhatIAmDoingISwear $(BUKKIT_JAR_FILENAME)

.PHONY: webapp
webapp:
	cd webapp && npm start