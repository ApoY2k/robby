// -------------------------------- Static shared types
// Keep in sync with SharedTypes.kt

const ATTR_ACTION = "data-action";
const ATTR_BIND = "data-bind";

const COMMANDS = {
    REFRESH_VIEW: "REFRESH_VIEW",
};

// -------------------------------- DOM manipulation

// Parse a command string and split it into the command itself and its parameters
const parseCommand = (command) => {
    const parts = command.split(":");
    return {
        command: parts[0],
        params: parts[1].split(";"),
    };
};

// Update a view component if it exists in the current DOM
const updateView = (view) => {
    const elements = document.querySelectorAll("[" + ATTR_BIND + "='" + view + "']");

    if (elements.length === 0) {
        return;
    }

    elements.forEach((element) => {
        fetch(view, {
            credentials: "same-origin",
        })
        .then((response) => {
            if (!response.ok) {
                return response.text().then((text) => {
                    throw new Error("Could not refresh view [" + view + "]: " + text);
                });
            } else {
                return response.text().then((text) => replaceDom(element, text));
            }
        })
        .catch((err) => console.log(err));
    });
};

// Replace the inner HTML of an element with the inner HTML of the body of an HTML string
const replaceDom = (element, text) => {
    const dom = new DOMParser().parseFromString(text, "text/html");
    const newElement = dom.querySelector("body").firstElementChild;
    addActionEventListeners(newElement);
    element.parentNode.replaceChild(newElement, element);
};

// -------------------------------- WebSocket management

const socket = new WebSocket("ws://" + window.location.host + "/ws");
socket.addEventListener("message", (event) => {
    const message = event.data;
    const command = parseCommand(message);
    switch (command.command) {
        case COMMANDS.REFRESH_VIEW:
            updateView(command.params[0]);
            break;

        default:
            console.error("Command [" + message + "] is unknown");
    }
});

// Event listener for any element that has actions associated with it
const actionEventListener = (element) => (event) => {
    event.stopPropagation();
    event.preventDefault();
    socket.send(element.attributes[ATTR_ACTION].value);
};

// Add the action event listener to all action-elements, starting from a given root element
const addActionEventListeners = (rootElement) => {
    rootElement.querySelectorAll("[" + ATTR_ACTION + "]").forEach((element) => {
        if (element.nodeName === "A" || element.nodeName === "BUTTON") {
            element.addEventListener("click", actionEventListener(element));
        } else if (element.nodeName === "FORM") {
            element.addEventListener("submit", actionEventListener(element));
        }
    });
};

// Initialize action event listeners upon document load
document.addEventListener("DOMContentLoaded", () => {
    addActionEventListeners(document);
});
