function displayCards() {
    const setName = document.getElementById("pokemonSet").value; 
    const pokemonName = document.getElementById("pokemonName").value.toLowerCase(); 
    const cardsOutput = document.getElementById("cardsOutput");
    const errorOutput = document.getElementById("error");

    cardsOutput.innerHTML = "";
    errorOutput.innerHTML = ""; 

    const sets = [
        "base-set", "jungle", "fossil", "base-set-2", "team-rocket", 
        "gym-heroes", "gym-challenge", "neo-genesis", "neo-discovery",
        "neo-revelation", "neo-destiny", "legendary-collection", 
        "expedition", "aquapolis", "skyridge", "ruby-&-sapphire",
        "sandstorm", "dragon", "team-magma-&-team-aqua", "hidden-legends", 
        "fire-red-&-leaf-green", "team-rocket-returns", "deoxys", "emerald", 
        "unseen-forces", "delta-species", "legend-maker", "holon-phantoms", 
        "crystal-guardians", "dragon-frontiers", "power-keepers", 
        "diamond-&-pearl", "mysterious-treasures", "secret-wonders", 
        "great-encounters", "majestic-dawn", "legends-awakened", "stormfront", 
        "platinum", "rising-rivals", "supreme-victors", "arceus", 
        "heartgold-&-soulsilver", "unleashed", "undaunted", "triumphant", 
        "call-of-legends", "black-&-white", "emerging-powers", 
        "noble-victories", "next-destinies", "dark-explorers", 
        "dragons-exalted", "boundaries-crossed", "plasma-storm", 
        "plasma-freeze", "plasma-blast", "legendary-treasures", "xy", 
        "flashfire", "furious-fists", "phantom-forces", "primal-clash", 
        "roaring-skies", "ancient-origins", "breakthrough", "breakpoint", 
        "generations", "fates-collide", "steam-siege", "evolutions", 
        "sun-&-moon", "guardians-rising", "burning-shadows", 
        "shining-legends", "crimson-invasion", "ultra-prism", 
        "forbidden-light", "celestial-storm", "dragon-majesty", 
        "lost-thunder", "team-up", "detective-pikachu", "unbroken-bonds", 
        "unified-minds", "hidden-fates", "cosmic-eclipse", "sword-&-shield", 
        "rebel-clash", "darkness-ablaze","champion-27s-path", 
        "vivid-voltage", "shining-fates", "battle-styles", "chilling-reign", 
        "evolving-skies", "celebrations", "fusion-strike", "brilliant-stars", 
        "astral-radiance", "go", "lost-origin", "silver-tempest", 
        "crown-zenith", "scarlet-&-violet", "paldea-evolved", 
        "obsidian-flames", "paradox-rift"
    ];

    let fetchPromises = [];

    if (setName) {
        // Fetch only the selected set
        fetchPromises.push(fetchSetData(setName));
    } else {
        // Fetch all sets if no setName is provided
        sets.forEach(set => fetchPromises.push(fetchSetData(set)));
    }

    Promise.all(fetchPromises)
        .then(results => {
            let allCards = results.flat(); // Combine all card arrays into one
            let filteredCards = allCards.filter(card =>
                card.name.toLowerCase().includes(pokemonName)
            );

            if (filteredCards.length > 0) {
                cardsOutput.innerHTML = "";
                filteredCards.forEach(card => {
                    const cardDiv = document.createElement("div");
                    cardDiv.classList.add("card");
                    const number = 0;
                    cardDiv.innerHTML = `
                        <h3 class = "${card.name}">${card.name}</h3>
                        <img src="${card.picture}" alt="${card.name}" style="width: 200px; height: auto;">
                        <p>Price: $${card.price.toFixed(2)}</p>
                        <div class="b">
                            <button class="minusButton" onclick="buttonCallMinus(${card.number}, '${card.setName}', '${card.name}')">-</button>
                            <p id="binderNumber">${number}</p>
                            <button class="addButton" id="add" onclick="buttonCallAdd(${card.number}, '${card.setName}', '${card.name}')">+</button>
                        </div>
                        <hr>
                    `;

                    cardsOutput.appendChild(cardDiv);
                });
            } else {
                errorOutput.innerHTML = '<p style="color: red; font-size: 12px;">No cards found with the given name.</p>';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            cardsOutput.innerHTML = `<p style="color: red;">Error loading cards: ${error.message}</p>`;
        });
}

// Helper function to fetch data for a specific set
function fetchSetData(setName) {
    return fetch(`../pokemon_data/${setName}.json`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch data for set: ${setName}`);
            }
            return response.json();
        })
        .then(data => {
            if (!Array.isArray(data)) {
                throw new Error(`Invalid data format for set: ${setName}`);
            }
            // Add setName to each card for reference
            return data.map(card => ({ ...card, setName }));
        });
}


function buttonCallAdd(cardNumber, cardSet, cardName) {
    const cards = document.querySelectorAll(".card");

    cards.forEach(card => {
        const title = card.querySelector("h3");
        if (title && title.textContent.trim() === cardName) {
            const binderNumberElement = card.querySelector("#binderNumber");

            if (binderNumberElement) {
                let currentCount = parseInt(binderNumberElement.textContent, 10) || 0;

                binderNumberElement.textContent = currentCount + 1;
            }
        }
    });
    updateJsonFile();
}


function buttonCallMinus(cardNumber, cardSet, cardName) {
    const cards = document.querySelectorAll(".card");

    cards.forEach(card => {
        const title = card.querySelector("h3");
        if (title && title.textContent.trim() === cardName) {
            const binderNumberElement = card.querySelector("#binderNumber");

            if (binderNumberElement) {
                let currentCount = parseInt(binderNumberElement.textContent, 10) || 0;
                if(currentCount == 0){
                    stop();
                }else{
                    binderNumberElement.textContent = currentCount - 1;

                }
            }
        }
    });
    updateJsonFile();
}

function updateJsonFile() {
    fetch('http://localhost:3000/update-json', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ cardCount: 10 }) // Modify this data as needed
    })
    .then(response => response.json())
    .then(data => console.log('Updated JSON:', data))
    .catch(error => console.error('Error updating JSON:', error));
}

// Call the function to update JSON
updateJsonFile();
