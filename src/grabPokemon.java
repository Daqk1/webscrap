import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class grabPokemon {
    public static void main(String[] args) {
        String[] pokemonSets = {
            //  "base-set",
            //  "jungle",
            //  "fossil",
            //  "base-set-2",
            //  "team-rocket",
            //  "gym-heroes",
            //  "gym-challenge",
            //  "neo-genesis",
            //  "neo-discovery",
            //  "neo-revelation",
            //  "neo-destiny",
            //  "legendary-collection",
            //  "expedition",
            //  "aquapolis",
            // "skyridge",
            //  "ruby-&-sapphire",
            //  "sandstorm",
            //  "dragon",
            //  "team-magma-&-team-aqua",
            //  "hidden-legends",
            //  "fire-red-&-leaf-green",
            //  "team-rocket-returns",
            //  "deoxys",
            //  "emerald",
            //  "unseen-forces",
            //  "delta-species",
            //  "legend-maker",
            //  "holon-phantoms",
            //  "crystal-guardians",
            //  "dragon-frontiers",
            //  "power-keepers",
            //  "diamond-&-pearl",
            //  "mysterious-treasures",
            //  "secret-wonders",
            //  "great-encounters",
            //  "majestic-dawn",
            //  "legends-awakened",
            //  "stormfront",
            //  "platinum",
            //  "rising-rivals",
            //  "supreme-victors",
            //  "arceus",
              "heartgold-&-soulsilver",
            //  "unleashed",
            // "undaunted",
            //  "triumphant",
            //  "call-of-legends",
            //  "black-&-white",
            //  "emerging-powers",
            //  "noble-victories",
            //  "next-destinies",
            //  "dark-explorers",
            //  "dragons-exalted",
            //  "boundaries-crossed",
            //  "plasma-storm",
            //  "plasma-freeze",
            //  "plasma-blast",
            //  "legendary-treasures",
            //  "xy",
            //  "flashfire",
            //  "furious-fists",
            //  "phantom-forces",
            //  "primal-clash",
            //  "roaring-skies",
            //  "ancient-origins",
            //  "breakthrough",
            //  "breakpoint",
            //  "generations",
            //  "fates-collide",
            //  "steam-siege",
            // "evolutions",
            // "sun-&-moon",
            // "guardians-rising",
            // "burning-shadows",
            // "shining-legends",
            // "crimson-invasion",
            // "ultra-prism",
            // "forbidden-light",
            // "celestial-storm",
            // "dragon-majesty",
            // "lost-thunder",
            // "team-up",
            // "detective-pikachu",
            // "unbroken-bonds",
            // "unified-minds",
            // "hidden-fates",
            // "cosmic-eclipse",
            // "sword-&-shield",
            // "rebel-clash",
            // "darkness-ablaze",
            // "champion%27s-path",
            // "vivid-voltage",
            // "shining-fates",
            // "battle-styles",
            // "chilling-reign",
            // "evolving-skies",
            // "celebrations",
            // "fusion-strike",
            // "brilliant-stars",
            // "astral-radiance",
            "go",
            "lost-origin",
            "silver-tempest",
            "crown-zenith",
            "scarlet-&-violet",
            "paldea-evolved",
            "obsidian-flames",
            "paradox-rift"
        };
        ExecutorService executor = Executors.newFixedThreadPool(3); 

        for (int i = 0; i < pokemonSets.length; i++) {        
            data set = new data(); 
            set.setSetName(pokemonSets[i]);
            executor.execute(set); 
        }

        executor.shutdown();

    }
}