import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.HashMap;
import java.util.*;

public class main {
    public static void main(String[] args) {
        HashMap<String, Double> pokemonPrice = new HashMap<String, Double>();
        //collect the user input
        String selectedPokemon = "Sylveon";
        //user input
        double priceOfSelectedPokemon = getPrice("https://www.pricecharting.com/game/pokemon-evolving-skies/sylveon-vmax-212?q=sylveon+vmax+%23212"); 
        //this would be the string selectedPokemon
        pokemonPrice.put(selectedPokemon, priceOfSelectedPokemon);
        double total = 0;
        for(double price: pokemonPrice.values()){
            total += price;
        }
        System.out.println("Total: $" + total);
    }

    public static double getPrice(String pokemonName){
            // Fetch the HTML content from the URL
            double price = 0;
            Element firstPriceElement; // Get the first price element
            String firstPrice; // Extract the text (price)
            try{
            Document doc = Jsoup.connect(pokemonName).get();

            // Select the price element
            Elements prices = doc.getElementsByClass("price js-price");

            firstPriceElement = prices.first(); // Get the first price element
            firstPrice = firstPriceElement.text(); // Extract the text (price)
            firstPrice = firstPrice.replace("$", "").replace(",", "").trim();
            price = Double.parseDouble(firstPrice);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return price;
    }
    
}
