
function searchProducts() {
    var productName = document.getElementById("productName").value;

    // Make an AJAX request to the backend
    fetch(`/product/productName/${productName}`)
        .then(response => response.json())
        .then(data => {
            displayProducts(data);
        })
        .catch(error => console.error('Error:', error));
}

function displayProducts(products) {
    var productListElement = document.getElementById("productList");
    productListElement.innerHTML = "";

    products.forEach(product => {
        var listItem = document.createElement("li");
        listItem.innerHTML = `
            <strong>ID:</strong> ${product.productId}<br>
            <strong>Name:</strong> ${product.productName}<br>
            <strong>Type:</strong> ${product.productType}<br>
            <strong>Description:</strong> ${product.productDescription}<br>
            <strong>Price:</strong> ${product.productPrice}<br>
            <strong>Available:</strong> ${product.productAvailable}<br>
            <hr>
        `;
        productListElement.appendChild(listItem);
    });
}