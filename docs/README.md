<h1 align="center">pride</h1>
<h3 align="center">🦁  keep track of your playmates in minecraft</h3>


![](https://img.shields.io/github/release/insanj/pride.svg)
![](https://img.shields.io/badge/bukkit-1.13.2-orange.svg)
![](https://img.shields.io/github/languages/code-size/insanj/pride.svg)
![](https://img.shields.io/github/license/insanj/pride.svg)

## Playing

- `/pride add <name>`

> adds a new area that is activated when anyone enters within 50 blocks

- `/pride where`

> shows the areas you are currently activating (aka inside of)

## Building

### Requirements

- GNU Make to run the [makefile](makefile), although each command inside this file can be ran on its own
- a `spigot.jar` or `bukkit.jar` file, currently expected in a local `server/` directory
- up-to-date jre, jdk, and other java deps that are required for `javac` and `jar` to run

### Building

- `make plugin`

> cleans `build/`, compiles the `plugin/` directory, builds a `.jar`, and moves it into `server/plugins/` for use in the Spigot/Bukkit server

- `make server`

> launches the server 


## Authors

```
Julian Weiss & Anna Raykovska
me@insanj.com
github.com/insanj
```

## License

See [LICENSE](LICENSE). (c) 2019 Julian Weiss.

