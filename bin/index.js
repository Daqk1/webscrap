document.getElementById('pokemonForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the default form submission

    const pokemonName = document.getElementById('pokemonName').value;
    const pokemonSet = document.getElementById('pokemonSet').value;

    // Send data to the backend
    fetch('/submit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ pokemonName, pokemonSet })
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('response').innerText = data.message; // Show server response
    })
    .catch((error) => {
        console.error('Error:', error);
    });
});
