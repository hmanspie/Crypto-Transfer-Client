function openMenu(id) {
    // Declare all variables
    let blinks;

    // Get all elements with class="blinks" and remove the class "active"
    blinks = document.getElementsByClassName("blinks");
    for (let i = 0; i < blinks.length; i++) {
        blinks[i].className = blinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the link that opened the tab
    id.currentTarget.className += " active";
}