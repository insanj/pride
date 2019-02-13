
.PHONY: all
all: plugin server

.PHONY: plugin
plugin:
	-rm -r -f build
	mkdir build && mkdir build/bin
	cp -r plugin/plugin.yml build/bin/plugin.yml
	cp -r plugin/me build/bin/me
	jar -cvf build/Pride.jar -C build/bin .
	-rm -r -f server/plugins/Pride.jar
	cp -r build/Pride.jar server/plugins/Pride.jar
	#cd plugin && javac me/insanj/pride/*.java && jar -cvf Pride.jar -C bin .

.PHONY: server
server:
	cd server && java -Xms1G -Xmx1G -jar spigot-1.13.2.jar
