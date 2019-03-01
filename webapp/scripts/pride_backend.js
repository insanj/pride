// pride webapp backend
// (c) 2019 julian weiss

function pride_convertPrideResponseToJSON(response) {
    return YAML.parse(response);
}

async function pride_getImagesForPrideArea(filename) {
    return await new Promise((resolve, reject) => {
        $.ajax({
            url: "/artwork/" + filename,
            success: resolve,
            error: reject,
            dataType: "json"
        });
    })
}

async function pride_downloadPrideResponse() {
    return await new Promise((resolve, reject) => {
        $.ajax({
            url: "/download",
            success: resolve,
            error: reject,
            dataType: "text"
        });
    })
}

function pride_syncGenerateImageHTMLStringForArea(filename) {
    const request = new XMLHttpRequest();
    request.open('GET',  "/artwork/" + filename, false);  // `false` makes the request synchronous
    request.send(null);
    const imageResponse = request.responseText;
    if (imageResponse == null || imageResponse.length <= 0) {
        return "";
    }
    
    let imageResponseJSON = JSON.parse(imageResponse);
    let allImagesString = "";
    for (let imageFilename of imageResponseJSON.imagePaths) {
        let imageString = "<img style='display: block;' class='pride-artwork rounded' src='" + imageFilename + "' height='320' width='auto' onerror='this.style.display=\"none\"' />";
        allImagesString += imageString;
    }

    return allImagesString;
}