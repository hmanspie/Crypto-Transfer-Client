function copyToClipboard(elementId){
    let copyText = document.getElementById(elementId);
    // Select the text field
    copyText.select();
    copyText.setSelectionRange(0, 99999); // For mobile devices
    // Copy the text inside the text field
    navigator.clipboard.writeText(copyText.value);
}

$(document).ready(function () {
    $('[data-bs-toggle="tooltip"]').tooltip();
})