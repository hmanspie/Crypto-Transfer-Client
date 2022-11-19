let countOfFields = 1;
let maxFieldLimit = 6;

let id = 1;

function removeField(input) {
    let inputTo = document.getElementById(input);
    inputTo.remove();
    countOfFields--;
}

function addField() {
    if (countOfFields >= maxFieldLimit) {
        return;
    }
    countOfFields++;
    let div = document.createElement("div");
    div.className = "username-input-container";
    div.id = "name" + id;
    id++;

    div.innerHTML =
        "            <input class=\"base-input-style\" placeholder=\"Username\" autocomplete=\"off\" name=\"username\"" +
        "                       type=\"text\" th:field=\"*{user_to}\" required>";

    let iconMinus = document.createElement("span");

    iconMinus.className = "";
    iconMinus.id = "btn-minus"
    iconMinus.onclick = () => removeField(div.id)
    iconMinus.innerHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-dash-circle\" viewBox=\"0 0 16 16\">\n" +
        "<path d=\"M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z\"/>\n" +
        "<path d=\"M4 8a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7A.5.5 0 0 1 4 8z\"/>\n" +
        "</svg>";

    div.appendChild(iconMinus);

    let inputTo = document.getElementById("destination");
    document.getElementById("write-form").insertBefore(div, inputTo);
}