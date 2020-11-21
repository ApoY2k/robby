// -------------------------------- Static shared types
// Keep in sync with SharedTypes.kt

const ATTR_ACTION = "data-action";
const ATTR_BIND = "data-bind";

const COMMANDS = {
    SWITCH_FIELD: "SWITCH_FIELD",
    REFRESH_BOARD: "REFRESH_BOARD",
    RESET_BOARD: "RESET_BOARD",
};

const VIEW_BOARD = "/views/board";

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
                throw new Error("Could not refresh view [" + view + "]");
            }
            return response;
        })
        .then((response) => response.text())
        .then((text) => replaceDom(element, text))
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
        case COMMANDS.REFRESH_BOARD:
            updateView(VIEW_BOARD);
            break;

        default:
            console.error("Command [" + message + "] is unknown");
    }
});

// Add the action event listener to all action-elements, starting from a given root element
const addActionEventListeners = (rootElement) => {
    rootElement.querySelectorAll("[" + ATTR_ACTION + "]").forEach((element) => {
        element.addEventListener("click", (event) => {
            event.stopPropagation();
            event.preventDefault();
            socket.send(element.attributes[ATTR_ACTION].value);
        });
    });
};

// Initialize action event listeners upon document load
document.addEventListener("DOMContentLoaded", () => {
    addActionEventListeners(document);
});
