let x = document.getElementById("soundNotification");

let newMessages = true;

function notifyMe() {
    if (!("Notification" in window)) {
        alert("This browser does not support desktop notification");
    }

    else if (Notification.permission === "granted") {
        let notification = new Notification("Hi there!");
    }

    else if (Notification.permission !== 'denied') {
        Notification.requestPermission(function (permission) {
            if (permission === "granted") {
                let notification = new Notification("Notification");
            }
        });
    }
}

if (newMessages){
    setTimeout(() => notifyMe(), 0);
}

const reloadPage = setInterval(() => location.reload(), 5000);
// const soundNotification = setTimeout(() => play(), 500);