
.PHONY: all
all: plugin server

.PHONY: plugin
plugin:
	# step 1 clean up / erase old version
	-rm -r -f build
	mkdir build && mkdir build/bin
	# step 1 compile the plugin
	javac -cp server/spigot-1.13.2.jar -d build/bin plugin/me/insanj/pride/*.java
	# step 3 copy config .yml to a new "build in progress" directory
	cp -r plugin/plugin.yml build/bin/plugin.yml
	# step 4 copy compiled code to the same place but in a folder called "bin"
	# cp -r plugin/me build/bin/me
	# step 5 create JAR file using the "build in progress" folder
	jar -cvf build/Pride.jar -C build/bin .
	# step 6 remove any existing plugin on the server in the server folder
	-rm -r -f server/plugins/Pride.jar
	# step 7 copy the JAR file into the server to run it!
	cp -r build/Pride.jar server/plugins/Pride.jar

.PHONY: server
server:
	# step 8 run the server!
	cd server && java -Xms1G -Xmx1G -jar spigot-1.13.2.jar
