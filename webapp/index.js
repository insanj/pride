// (c) 2019 julian weiss
// pride webapp

// deps
const express = require('express')
const path = require('path')
// const YAML = require('yamljs')
const PromiseFtp = require('promise-ftp')
const fs = require('fs')
require('dotenv').config()
const fileUpload = require('express-fileupload');
const JsonDB = require('node-json-db');

// core vars
const app = express()
const db = new JsonDB("pride-webapp-artwork", true, false);
const port = 3000
const ftpConfig = {
    host: process.env.host, user: process.env.user, password: process.env.password
};

// routes
app.use("/external", express.static(__dirname + "/external"))
app.use("/static", express.static(__dirname + "/static"))
app.use("/scripts", express.static(__dirname + "/scripts"))
app.use("/uploads", express.static(__dirname + "/uploads"))

// -- homepage
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname + '/index.html'));
});

// -- download API
app.get("/download", (req, res) => {
    // let configYAML = YAML.load(__dirname + '/config.yml');
    var ftp = new PromiseFtp();
    ftp.connect(ftpConfig).then(function (serverMessage) {
        return ftp.get('/plugins/pride/config.yml');
    }).then(function (stream) {
        return new Promise(function (resolve, reject) {
            stream.once('close', resolve);
            stream.once('error', reject);
            stream.pipe(res);
        });
    }).then(function () {
        return ftp.end();
    });
});

// -- upload API
// default options
app.use(fileUpload());

app.post('/upload/:areaName', function(req, res) { 
    let sampleFile;
    let uploadPath;

    if (Object.keys(req.files).length == 0) {
      res.status(400).send('No files were uploaded.');
      return;
    }
    
    sampleFile = req.files.sampleFile;
  
    uploadPath = __dirname + '/uploads/' + sampleFile.name;

    let entryName = "/" + req.params.areaName + "/" + new Date().toISOString();
    console.log("Adding screenshot entry with name " + entryName);
    db.push(entryName, sampleFile.name);

    sampleFile.mv(uploadPath, function(err) {
      if (err) {
        return res.status(500).send(err);
      }
  
      res.sendFile(path.join(__dirname + '/index.html')); // reload homepage when uploading file
    });
});

app.get('/artwork/:areaName', async function(req, res) { 
    try {
        const response = db.getData("/" + req.params.areaName);
        let imagePaths = [];

        for (var timestamp in response) {
            let imagePath = "/uploads/" + response[timestamp];
            imagePaths.push(imagePath);
        }

        if (response != null) {
            res.status(200).send({
                imagePaths
            });
            return;
        }
    } catch (e) {
        // console.log(e);
    }
    
    res.status(200).send("");
});

// engage!
app.listen(port, () => console.log(`pride webapp live @ ${port} with config ${JSON.stringify(ftpConfig)}`))