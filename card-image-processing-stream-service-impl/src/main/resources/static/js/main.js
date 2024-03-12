'use strict';

var singleUploadForm = document.querySelector('#singleUploadForm');
var singleFileUploadInput = document.querySelector('#singleFileUploadInput');
var singleFileUploadError = document.querySelector('#singleFileUploadError');
var singleFileUploadSuccess = document.querySelector('#singleFileUploadSuccess');

function uploadSingleFile(file) {
    var formData = new FormData();
    formData.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadFile");

    xhr.onload = function () {
        console.log(xhr.responseText);
        var jsonArr = xhr.responseText;
        var arrFileDesc = JSON.parse(jsonArr);

        if (xhr.status == 200) {

            singleFileUploadError.style.display = "none";
            var innerHtmlResult = "";
            for (const fileDescription of arrFileDesc) {
                var descriptionFileName = fileDescription.fileName;
                var descriptionFileDownloadUrl = fileDescription.fileDownloadUrl;
                var descriptionFileParseUrl = fileDescription.fileParseUrl;
                innerHtmlResult += "<p>File Uploaded Successfully.</p>" +
                    "<p>Download : <a href='" + descriptionFileDownloadUrl + "' target='_blank'>" + descriptionFileName + "</a></p>" +
                    "<p>Parse : <a href='" + descriptionFileParseUrl + "' target='_blank'>" + descriptionFileName + "</a></p>"
            }

            singleFileUploadSuccess.innerHTML = innerHtmlResult;
            singleFileUploadSuccess.style.display = "block";


        } else {
            singleFileUploadSuccess.style.display = "none";
            singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }

    xhr.send(formData);
}


singleUploadForm.addEventListener('submit', function (event) {
    var files = singleFileUploadInput.files;
    if (files.length === 0) {
        singleFileUploadError.innerHTML = "Please select a file";
        singleFileUploadError.style.display = "block";
    }
    uploadSingleFile(files[0]);
    event.preventDefault();
}, true);
