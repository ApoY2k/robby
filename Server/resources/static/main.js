const socket = new WebSocket("ws://" + window.location.host + "/ws");

socket.addEventListener("open", () => {
    console.log("WebSocket connected!");
    socket.send("ping");
});

socket.addEventListener("message", (event) => {
    console.log("Message received", event.data);
});

document.querySelectorAll(".field").forEach(field => {
    field.addEventListener("click", (event) => {
        console.log(event.target.attributes["data-id"].value);
    });
});
