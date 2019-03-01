// pride webapp frontend
// (c) 2019 julian weiss

// NOTE: no deps required for this file

// expects param of format: [[column value 1, column value 2], [column value 1], ...]
function pride_generateTableBodyString(elements) {
    let colString = '';
    for (let ele of elements) {
        let rowString = '<tr>';
        let i = 0;
        for (let sub of ele) {
            if (i == 0) {
                let filename = ele[0].replace(/[^a-z0-9]/gi, '_').toLowerCase();
                
                rowString += '<th id="' + filename + '" scope="row">' + sub + '</th>';
            } else {
                rowString += '<td>' + sub + '</td>';
            }
            i++;
        }

        rowString += '</tr>';
        colString += rowString;
    }

    return colString;
}

function pride_generateGallerySection(htmlContents, imageCallback) {
    var regex = /<th id="([^"]+)" scope="row">([^"]+)<\/th>/g;
    return htmlContents.replace(regex, function($0, $1, $2, $3) {
        let filename = $1;
        let areaName = $2;
        let uploadFormString = "<form class='' id='collapse-form' ref='uploadForm' id='pride-upload' action='/upload/" + filename + "' method='post' encType='multipart/form-data'> <input type='file' name='sampleFile' /> <input type='submit' value='Upload' /></form>";

        let artworkString = imageCallback(areaName, filename);
        let replacement = '<th id="' + filename + '" scope="row">' + areaName + artworkString + uploadFormString + '</th>';
        return replacement;
    });
}

function pride_convertPrideResponseToTableBodyElements(response) {
    let elements = [];
    let worlds = response.worlds;
    for (let worldUID in worlds) {
        let worldDict = worlds[worldUID];
        for (let areaName in worldDict) {
            let areaData = worldDict[areaName];
            elements.push([areaName, areaData.x, areaData.y, areaData.z]);
        }
    }
    return elements.sort((a, b) => {
        return a[0].toUpperCase().localeCompare(b[0].toUpperCase());
    });
}

