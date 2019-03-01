// pride webapp backend
// (c) 2019 julian weiss

function pride_convertPrideResponseToJSON(response) {
    return YAML.parse(response);
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