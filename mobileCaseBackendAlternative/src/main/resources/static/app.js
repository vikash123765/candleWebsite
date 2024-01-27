function markOrderAsSent() {
    const orderNumber = document.getElementById('orderNumber').value;
    const trackingId = document.getElementById('trackingId').value;

    const adminEmail = "andreas@gmail.com";  // Replace with your admin email
    const tokenValue = "58ee7a5b-2a24-4edc-8974-812cbe693a99";  // Replace with the actual token value

    fetch(`http://your-api-base-url/order/sent/${orderNumber}/${trackingId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'email': adminEmail,
            'x-auth-token': tokenValue
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('result').innerText = data;
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('result').innerText = 'An error occurred. Please try again.';
    });
}
