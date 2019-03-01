<h1 align="center">pride</h1>
<h3 align="center">🦁  keep track of your playmates in minecraft</h3>


<p align="center">
  <a href="https://jdk.java.net/">
    <img src="https://img.shields.io/badge/java-10.0.2-red.svg" />
  </a>
  
  <a href="https://getbukkit.org/download/craftbukkit">
    <img src="https://img.shields.io/badge/bukkit-1.13.2-orange.svg" />
  </a>
  
  <a href="https://www.spigotmc.org/resources/pride.64859/">
    <img src="https://img.shields.io/badge/🚀-Download%20on%20spigotmc.org-blue.svg" />
  </a>
</p>

<p align="center">
    Pride is a suite of lightweight Minecraft tools for Spigot/Bukkit servers. The core mod, `pride.jar`, helps players keep track of each other without the use of any dependancies. Although relatively simple in its design, this plugin aims to be really useful and offer a nice supplement to the built-in navigation features of Minecraft.
</p>

In addition to `pride.jar`, it is highly recommended that server runners use the Pride webapp, which allows for easy management of all features of Pride, and some added bonuses -- such as adding multiple screenshots to create a blog-like website which can be easily saved and exported to external publishing.

Finally, some legacy users of Pride will need to convert from the old YAML string format to the new YAML dynamic format, a transition that occured in the first `0.4.0` version. This format is essential for powering the webapp and all `0.4.x` and beyond features. To convert from the old format to the new format, use 🐯 [tigon](#-use-tigon-to-convert-between-03x-and-04x-file-formats) by copy and pasting your `config.yml`.

## Playing

- `/areas <page_number>`
- `/settle <name>`
- `/abandon <name>`
- `/far <name>`
- `/here <optional:username>`
- `/between <name_1>, <name_2>`
- `/compass <optional:area>`
- `/north`

## Changelog

### 0.4.x Features

- New YAML file format


### 🐯 Use [tigon](https://github.com/insanj/tigon) to convert between 0.3.x and 0.4.x file formats!

<iframe src="https://insanj.github.io/tigon/" height="400" width="550"></iframe>

### 0.3.x Features

- Create "Pride areas", which represent the area where you are located (by default 50 blocks large, but can be configured in the `config.yml`)
- Detect when entering a "Pride area" and send a message to the server
- List, remove, and calculate distance from "Pride areas"

### 0.3.x config.yml
```YAML
initialized: true
distance: 50
worlds:
  75310a3a-6bb4-4e0c-8409-9e6648ac7f3e:
    Ocean Monument Entrance: -1287.35,29.56,-7453.77
    Birch & Brick: -593.27,83.00,-4818.50
    Cottage on the Cliff of the Icebergs: -910.49,74.00,-7357.35
    Chicken Farm: -705.96,67.00,-8065.75
```

## Webapp

Pride is most useful when paired with its simple webapp, designed to be ran locally on your computer.

If your server can be accessed with FTP, drop the `host`, `user`, and `password` in a `.env` file in the root `/webapp` directory. 

When launched using `npm start` (make sure to run `npm install` beforehand), the webapp will download the `/plugins/pride/config.yml` automatically and use it along with its own local JSON file to power the website.

![](webapp.png)


## Building

Pride was not built using Eclipse or any other IDE. Although you can use one, the instructions below are for building and running this plugin with only the command line and a few easy to understand tools.

### Requirements

- [Make](https://www.gnu.org/software/make/#download) to run the [makefile](https://github.com/insanj/pride/blob/master/makefile), although each command inside this file can be ran on its own
- [spigot](https://getbukkit.org/download/spigot)`.jar` or `bukkit.jar` version **1.13.2** or above, currently expected in a local `server/` directory
- [Java](https://www.oracle.com/technetwork/java/javase/downloads/index.html) 10 or above, required for `javac` and `jar` to run

> NOTE: Make sure the Java version on your server and the Java version on the machine that builds Pride are the same, otherwise Bukkit/Spitgot will not allow it to run.

### Commands

#### `make plugin`
- cleans `build/`
- compiles the `plugin/` directory
- builds a `.jar`
- moves it into `server/plugins/` for use in the Spigot/Bukkit server

#### `make server`
- launches the server 

## Authors

```
Julian Weiss & Anna Raykovska
me@insanj.com
github.com/insanj
```

## License

See [LICENSE](https://github.com/insanj/pride/blob/master/LICENSE). (c) 2019 Julian Weiss.

