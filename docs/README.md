<h1 align="center">pride</h1>
<h3 align="center">🦁  keep track of your playmates in minecraft</h3>


## Usage

### Requirements

- **GNU Make** and a [Makefile](Makefile) which is completely cross-platform
- a **`spigot-1.13.2.jar` file which exists in a local `server/` directory**, although easy to configure in the `Makefile`
- up-to-date jre, jdk, and other java deps that are required for `javac` and `jar` to run

### Building

- `make plugin`, cleans `build/`, compiles the `plugin/` directory, builds a `.jar`, and moves it into `server/plugins/` for use in the Spigot/Bukkit server
- `make server`, launches the server 

### Playing

- `pride add <name>`, adds a new area that is activated when anyone enters within 50 blocks

## Authors

```
Julian Weiss & Anna Raykovska
me@insanj.com
github.com/insanj
```

## License

See [LICENSE](LICENSE). (c) 2019 Julian Weiss.

