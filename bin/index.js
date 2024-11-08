function displaySelection(event) {
    event.preventDefault();
    const pokemonNameInput = document.getElementById("pokemonName");
    const pokemonName = pokemonNameInput.value;
    const pokemonSetName = document.getElementById("pokemonSet");
    const pokemonSet = pokemonSetName.options[pokemonSetName.selectedIndex].text;
    console.log(`Selected Pokémon: ${pokemonName} from ${pokemonSet}`);
}
