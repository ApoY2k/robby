
document.querySelectorAll(".field").forEach(field => {
    field.addEventListener("click", (event) => {
        console.log(event.target.attributes["data-id"].value);
    });
});
