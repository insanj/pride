// pride webapp UX
// (c) 2019 julian weiss

var pride_namesSectionContents;
var pride_gallerySectionContents;

async function pride_runtime() {
    $("#pride-table-body").html("");

    const response = await pride_downloadPrideResponse();
    const responseJSON = pride_convertPrideResponseToJSON(response);
    const elements = pride_convertPrideResponseToTableBodyElements(responseJSON);
    const htmlContents = pride_generateTableBodyString(elements);
    $("#pride-table-body").append(htmlContents);

    pride_namesSectionContents = htmlContents;
    pride_gallerySectionContents = pride_generateGallerySection(htmlContents, function(areaName, filename) {
        return pride_syncGenerateImageHTMLStringForArea(filename);
    });

    pride_searchOnKeyUp(); // just to make sure any lingering input is used on load
    return;
}

function pride_searchOnKeyUp() {
    // Declare variables 
    var input, filter, table, tr, td, i, txtValue;
    input = document.getElementById("pride-search");
    filter = input.value.toUpperCase();
    table = document.getElementById("pride-table");
    tr = table.getElementsByTagName("tr");

    // Loop through all table rows, and hide those who don't match the search query
    for (i = 0; i < tr.length; i++) {
        td = tr[i].getElementsByTagName("th")[0];
        if (td) {
            txtValue = td.textContent || td.innerText;
            if (txtValue.toUpperCase().indexOf(filter) > -1) {
                tr[i].style.display = "";
            } else {
                tr[i].style.display = "none";
            }
        } 
    }
}

function prideSetupSections() {
    /*$(document).on("submit", "#collapse-form", function(event) { 
        const originalSource = $(".pride-artwork").attr('src');
        $(".pride-artwork").attr("src", originalSource + "?" + (new Date()).getTime()); // force reload images
    });*/

    // click listeners
    function prideToggleSection(sectionName) {
        if (sectionName === 'names') {
            $("#pride-names-button").removeClass("btn-outline-secondary"); 
            $("#pride-names-button").addClass("btn-secondary"); 
            $("#pride-names-button").prop("disabled", true); 

            $("#pride-gallery-button").removeClass("btn-secondary"); 
            $("#pride-gallery-button").addClass("btn-outline-secondary"); 
            $("#pride-gallery-button").prop("disabled", false); 

            $("#pride-table-body").html(pride_namesSectionContents);
        } else if (sectionName === 'gallery') {
            $("#pride-names-button").removeClass("btn-secondary"); 
            $("#pride-names-button").addClass("btn-outline-secondary"); 
            $("#pride-names-button").prop("disabled", false); 

            $("#pride-gallery-button").removeClass("btn-outline-secondary"); 
            $("#pride-gallery-button").addClass("btn-secondary"); 
            $("#pride-gallery-button").prop("disabled", true);
            
            $("#pride-table-body").html(pride_gallerySectionContents);
        }
    }

    $(document).on("click", "#pride-names-button", function(event) {
        prideToggleSection('names');
    });

    $(document).on("click", "#pride-gallery-button", function(event) {
        prideToggleSection('gallery');
    });

    $(document).on("change", 'input[type="file"]', function(event) {
        event.preventDefault();

        if (!this.files || this.files.length <= 0) {
            return;
        }

        const url = this.form.action;
        let promises = [];
        for (let file of this.files) {
            let data = new FormData($(this.form)[0]);
            promises.push(pride_uploadPrideArtwork(url, data));
        }

        Promise.all(promises).then(r => {
            pride_runtime().then(pr => {
                prideToggleSection('gallery');
            }).catch(e => {
                console.log(e);
            });
        }).catch(e => {
            console.log(e);
        });
    });
}