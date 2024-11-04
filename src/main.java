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
        //collect the user input
        String [] selectedPokemon = {"Sylveon"};
        //user input
        double [] priceOfSelectedPokemon = new double[selectedPokemon.length];
        String setName = "brilliant-stars";
        for(int i = 0; i < selectedPokemon.length; i++){
            priceOfSelectedPokemon[i] = getPrice(changeURL(setName), selectedPokemon[i], setName); 
        }
        //this would be the string selectedPokemon
        double total = 0.00;
        for(int i = 0; i < priceOfSelectedPokemon.length; i++){
            total += priceOfSelectedPokemon[i];
            System.out.println(selectedPokemon[i] + " Price: $" + priceOfSelectedPokemon[i]);
        }
        System.out.println("Total: $" + total);
    }

    public static double getPrice(String setURL, String pokemonName, String setName){
            // Fetch the HTML content from the URL
            double price = 0;
            Element firstPriceElement; // Get the first price element
            String firstPrice; // Extract the text (price)
            try{
            String getPriceURL = getPokemonURL(setURL, pokemonName, setName);
            Document pokemonDoc = Jsoup.connect(getPriceURL).get(); //some error here maybe?
            Elements prices = pokemonDoc.getElementsByClass("price js-price");
            // Select the price element
            firstPriceElement = prices.first(); // Get the first price element
            firstPrice = firstPriceElement.text(); // Extract the text (price)
            if(firstPrice.equals("-")){
                price = 0;
                error(pokemonName, setName);
            }else{
                firstPrice = firstPrice.replace("$", "").replace(",", "").trim();
                price = Double.parseDouble(firstPrice);
            }
            } catch (IOException e) {
                System.out.println("Connection failed while trying to retrieve price for " + pokemonName);
            } catch (NumberFormatException e) {
                System.out.println("Price format issue for " + pokemonName);
            }
            return price;
    }

    public static void error(String pokemonName, String setNameString){
        System.out.println("Cannot Find Price of " + pokemonName + " in " + setNameString);
    }
    public static String getPokemonURL(String setURL, String pokemonName, String setName){
        String getPriceURL = "";
        try{
        Document setDoc = Jsoup.connect(setURL).get(); //some error here maybe
        Elements name = setDoc.getElementsByClass("title");
        for (Element pName : name) {
            String temp = pName.text(); // Extract text from the element
            if(temp.contains(pokemonName)){
                getPriceURL = cPokemon(temp, setName);
                break;
            }
        }
    }catch (IOException e) {
        System.out.println("Error while connecting to set URL for " + pokemonName);
    }
    return getPriceURL;
    }
    public static String changeURL(String setName){
        return "https://www.pricecharting.com/console/pokemon-"+ setName + "?sort=model-number&model-number=&exclude-variants=false&show-images=true&in-collection=";
    }

    public static String cPokemon(String grabbedPokemonName, String setName){
        String ogURL = "https://www.pricecharting.com/game/pokemon-"+ setName + "/";
        return ogURL + grabbedPokemonName.toLowerCase().replace(" ", "-");

    }
}