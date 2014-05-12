window.addEventListener("message", receiveMessage, false);

function receiveMessage(event) {
    var data = event.data;
    onreceive(null, data);
}
