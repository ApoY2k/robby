// -------------------------------- WebSocket management

const socket = new WebSocket("ws://" + window.location.host + "/ws");
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
    $("[data-toggle='tooltip']").tooltip();
    $("ul.tabs").on("click", (e) => {
        e.preventDefault();
        $(this).tab("show");
    });
};

// Initialize action event listeners upon document load
$(function () {
    addActionEventListeners(document);
    reloadStyles();
});

const runAdminCommand = (command, args) => {
    const params = new URLSearchParams(args);

    fetch("/console/" + command + "?" + params, {
        credentials: "same-origin",
    })
    .then((response) => {
        if (!response.ok) {
            return response.text().then((text) => {
                throw new Error("Could not run admin command: " + text);
            });
        } else {
            return response.text();
        }
    })
    .then((text) => {
        if (text.startsWith("[") || text.startsWith("(")) {
            text = JSON.parse(text);
        }
        console.log(text);
    })
    .catch((err) => console.error(err));
};

// Initialize admin console functions
window.robby = {
    Players: {
        List: () => runAdminCommand("players/list")
    },
    Robots: {
        SetDamage: (id, value) => runAdminCommand("robots/setdamage", { id, value })
    }
};
