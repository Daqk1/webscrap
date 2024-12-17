function displayCards() {
    const setName = document.getElementById("pokemonSet").value; // Set selected by the user
    const pokemonName = document.getElementById("pokemonName").value.toLowerCase(); // Name filter (case-insensitive)
    const cardsOutput = document.getElementById("cardsOutput");
    const errorOutput = document.getElementById("error");

    cardsOutput.innerHTML = ""; // Clear previous cards
    errorOutput.innerHTML = ""; // Clear error messages

    // Check if setName is provided
    if (!setName) {
        errorOutput.innerHTML = '<p style="color: red; font-size: 12px;">Please select a Pokémon set.</p>';
        return;
    }

    // Fetch data for the selected set
    fetch(`../pokemon_data/${setName}.json`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            if (data && Array.isArray(data)) {
                cardsOutput.innerHTML = ""; // Clear the previous results

                // If no name is provided, show all cards from the selected set
                if (!pokemonName) {
                    // Show all cards from the set
                    data.forEach(card => {
                        const cardDiv = document.createElement("div");
                        cardDiv.classList.add("card");

                        cardDiv.innerHTML = `
                            <h3>${card.name}</h3>
                            <img src="${card.picture}" alt="${card.name}" style="width: 150px; height: auto;">
                            <p>Price: $${card.price.toFixed(2)}</p>
                            <a href="${card.url}" target="_blank">View Card</a>
                            <hr>
                        `;

                        cardsOutput.appendChild(cardDiv); // Append the card to the output container
                    });
                } else {
                    // If a name is provided, show matching cards from the selected set
                    const filteredCards = data.filter(card => 
                        card.name.toLowerCase().includes(pokemonName)
                    );

                    // Check if any cards matched the filter
                    if (filteredCards.length > 0) {
                        filteredCards.forEach(card => {
                            const cardDiv = document.createElement("div");
                            cardDiv.classList.add("card");

                            cardDiv.innerHTML = `
                                <h3>${card.name})</h3>
                                <img src="${card.picture}" alt="${card.name}" style="width: 150px; height: auto;">
                                <p>Price: $${card.price.toFixed(2)}</p>
                                <a href="${card.url}" target="_blank">View Card</a>
                                <hr>
                            `;

                            cardsOutput.appendChild(cardDiv); // Append the card to the output container
                        });
                    } else {
                        errorOutput.innerHTML = '<p style="color: red; font-size: 12px;">No cards found with the given name in the selected set.</p>';
                    }
                }
            } else {
                throw new Error("Invalid data format.");
            }
        })
        .catch(error => {
            console.error('Error:', error);
            cardsOutput.innerHTML = `<p style="color: red;">Error loading cards: ${error.message}</p>`;
        });
}
