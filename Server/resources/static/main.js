// Connect to a game's websocket and attach the appropriate event listeners
const connect = () => {
    const socket = new WebSocket("ws://" + window.location.host + window.location.pathname + "/ws");
    socket.addEventListener("message", (event) => {

        // Replace the inner HTML of an element with the inner HTML of the body of an HTML string
        const dom = new DOMParser().parseFromString(event.data, "text/html");
        const newElement = dom.querySelector("body").firstElementChild;

        addActionEventListeners(newElement);

        const gameView = document.querySelector("div#gameview")
        gameView.parentNode.replaceChild(newElement, gameView);

        reloadStyles();
    });

    // Event listener for any element that has actions associated with it
    const actionEventListener = (element) => (event) => {
        event.stopPropagation();
        event.preventDefault();

        const action = new URLSearchParams(element.attributes["data-action"].value);
        const actionForm = new FormData();

        action.forEach((value, key) => actionForm.append(key, value));

        if (element.nodeName === "FORM") {
            const dynamicForm = new FormData(element);
            dynamicForm.forEach((value, key) => actionForm.append(key, value));
            element.reset();
        }

        socket.send(new URLSearchParams(actionForm).toString());
    };

    // Add the action event listener to all action-elements, starting from a given root element
    const addActionEventListeners = (rootElement) => {
        rootElement.querySelectorAll("[data-action]").forEach((element) => {
            if (["A", "DIV", "BUTTON"].includes(element.nodeName)) {
                element.addEventListener("click", actionEventListener(element));
            }

            if (element.nodeName === "FORM") {
                element.addEventListener("submit", actionEventListener(element));
            }
        });
    };

    // Apply DOM specific options
    const reloadStyles = () => {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
        const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
          return new bootstrap.Tooltip(tooltipTriggerEl)
        });
    };

    // Initialize action event listeners upon document load
    document.addEventListener("DOMContentLoaded", () => {
        addActionEventListeners(document);
        reloadStyles();
    });
}

if (gameId) {
    connect();
}
