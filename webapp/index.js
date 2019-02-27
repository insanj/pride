// (c) 2019 julian weiss
// pride webapp

// deps
const express = require('express')
const path = require('path')
const YAML = require('yamljs')

// core vars
const app = express()
const port = 3000

// routes
app.use(express.static('static'))

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname + '/index.html'));
});

app.get("/download", (req, res) => {
    let configYAML = YAML.load(__dirname + '/config.yml');
    res.status(200).send(configYAML);
});

// engage!
app.listen(port, () => console.log(`pride webapp live @ ${port}`))