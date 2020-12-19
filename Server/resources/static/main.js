// -------------------------------- Static shared types
// Keep in sync with SharedTypes.kt

const ATTR_ACTION = "data-action";
const ATTR_BIND = "data-bind";

// -------------------------------- DOM manipulation

// Replace the inner HTML of an element with the inner HTML of the body of an HTML string
const replaceDom = (element, text) => {
    // Disregard dismounted elements due to replacing of components higher up in the DOM
    if (element.parentNode === null) {
        return;
    }

    const dom = new DOMParser().parseFromString(text, "text/html");
    const newElement = dom.querySelector("body").firstElementChild;
    addActionEventListeners(newElement);
    element.parentNode.replaceChild(newElement, element);
    reloadStyles();
};

// -------------------------------- WebSocket management

const socket = new WebSocket("ws://" + window.location.host + "/ws");
socket.addEventListener("message", (event) => {
    const view = event.data;
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
        .catch(console.error);
    });
});

// Event listener for any element that has actions associated with it
const actionEventListener = (element) => (event) => {
    event.stopPropagation();
    event.preventDefault();

    const action = new URLSearchParams(element.attributes[ATTR_ACTION].value);
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
    rootElement.querySelectorAll("[" + ATTR_ACTION + "]").forEach((element) => {
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
    $('[data-toggle="tooltip"]').tooltip();
};

// Initialize action event listeners upon document load
$(function () {
    addActionEventListeners(document);
    reloadStyles();
});
