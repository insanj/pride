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

// core vars
const app = express()
const port = 3000
const ftpConfig = {
    host: process.env.host, user: process.env.user, password: process.env.password
};

// routes
app.use(express.static('static'))

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
app.post('/upload', function(req, res) {
    let sampleFile;
    let uploadPath;
  
    if (Object.keys(req.files).length == 0) {
      res.status(400).send('No files were uploaded.');
      return;
    }
  
    console.log('req.files >>>', req.files); // eslint-disable-line
  
    sampleFile = req.files.sampleFile;
  
    uploadPath = __dirname + '/static/' + sampleFile.name;
  
    sampleFile.mv(uploadPath, function(err) {
      if (err) {
        return res.status(500).send(err);
      }
  
      res.send('File uploaded to ' + uploadPath);
    });
  });
// engage!
app.listen(port, () => console.log(`pride webapp live @ ${port} with config ${JSON.stringify(ftpConfig)}`))