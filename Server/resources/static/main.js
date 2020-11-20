const socket = new WebSocket("ws://" + window.location.host + "/ws");

socket.addEventListener("open", () => {
    console.log("WebSocket connected!");
    socket.send("ping");
});

socket.addEventListener("message", (event) => {
    const message = event.data;

    if (message.startsWith("refresh:")) {

    }
});

document.querySelectorAll("[data-socket-action]").forEach(actionTag => {
    actionTag.addEventListener("click", (event) => {
        event.stopPropagation();
        event.preventDefault();
        socket.send("action:" + actionTag.attributes["data-socket-action"].value);
    });
});
