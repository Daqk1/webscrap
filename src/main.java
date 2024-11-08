import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.IOException;
import java.util.*;
import java.time.Duration;  // Import Duration

public class main {
    public static void main(String[] args) {
        //collect the user input
        String [] selectedPokemon = {"Eldegoss"};
        //user input
        ArrayList<ArrayList<String>> eachSelectedPokemonURL = new ArrayList<ArrayList<String>>();
        String setName = "evolving-skies";
        for(int i = 0; i < selectedPokemon.length; i++){
            eachSelectedPokemonURL.add(getPokemonURLARRAY(changeURL(setName), selectedPokemon[i], setName)); //each pokemon in the pokemon catagory
        }
        //this would be the string selectedPokemon
        HashMap<ArrayList<String>,ArrayList<Double>> bothPriceAndName = new HashMap<>(); 
        
        for(int i = 0; i < eachSelectedPokemonURL.size(); i++){
            ArrayList<String> innerList = eachSelectedPokemonURL.get(i);
            ArrayList<Double> price = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            for(int a = 0; a < innerList.size(); a++){
                price.add(getPrice(innerList.get(a), selectedPokemon[i], setName));
                names.add(getTheNames(innerList.get(a), selectedPokemon[i], setName));
            }
            bothPriceAndName.put(names,price);
        }
        double total = 0.00;
        for (Map.Entry<ArrayList<String>, ArrayList<Double>> entry : bothPriceAndName.entrySet()) {
            ArrayList<String> names = entry.getKey();  // Get the list of names
            ArrayList<Double> prices = entry.getValue(); // Get the list of prices
            // Print the names and corresponding prices
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                Double price = prices.get(i);
                System.out.println("Pokémon: " + name + " | Price: $" + price);
                total += price;  // Add price to total
            }
        }
        System.out.println("Total: $" + total);
    }

    public static double getPrice(String setURL, String pokemonName, String setName){ //need it to load, so use Selenium . IDK HOW
            // Fetch the HTML content from the URL
            double price = 0;
            Element firstPriceElement; // Get the first price element
            String firstPrice; // Extract the text (price)
            try{
            Document pokemonDoc = Jsoup.connect(setURL).get(); 
            Elements prices = pokemonDoc.getElementsByClass("price js-price");
            // Select the price element
            firstPriceElement = prices.first(); // Get the first price element
            firstPrice = firstPriceElement.text(); // Extract the text (price)
            if(firstPrice.equals("-")){
                error(pokemonName, setName);
                return 0;
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
    
    public static ArrayList<String> getPokemonURLARRAY(String setURL, String pokemonName, String setName){
        String getPriceURL = "";
        ArrayList<String> similarPokemonURL = new ArrayList<String>();
        try{
        Document setDoc = Jsoup.connect(setURL).get(); //some error here maybe
        Element tbody = setDoc.select("tbody").first();
        Elements name = tbody.getElementsByClass("title");
        for (Element pName : name) {
            String temp = pName.text(); // Extract text from the element //I NEED THIS SOMEWHERE!
            if(temp.contains(pokemonName)){
            getPriceURL = cPokemon(temp, setName);
            similarPokemonURL.add(getPriceURL);
            }
        }
    }catch (IOException e) {
        System.out.println("Error while connecting to set URL for " + pokemonName);
    }
    return similarPokemonURL;
    }



    public static String changeURL(String setName){
        return "https://www.pricecharting.com/console/pokemon-"+ setName + "?sort=model-number&model-number=&exclude-variants=false&show-images=true&in-collection=";
    }

    public static String cPokemon(String grabbedPokemonName, String setName){
        String ogURL = "https://www.pricecharting.com/game/pokemon-"+ setName + "/";
        String makingitRight = grabbedPokemonName.toLowerCase().replace(" ", "-");
        makingitRight = makingitRight.replace("[", "-")
                             .replace("]", "-")
                             .replace("#", "-");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < makingitRight.length(); i++) {
            // If the current character is a dash, check if it's not part of a consecutive dash sequence
            if (!(makingitRight.charAt(i) == '-' && i > 0 && makingitRight.charAt(i - 1) == '-')) {
                result.append(makingitRight.charAt(i));
            }
        }
        if (result.charAt(result.length() - 1) == '-') {
            result.deleteCharAt(result.length() - 1);
        }
        makingitRight = result.toString();
        return ogURL + makingitRight;

    }
    public static String getTheNames(String setURL, String pokemonName, String setName){
        String temp = "";
        try{
        Document setDoc = Jsoup.connect(setURL).get(); //some error here maybe
        Elements name = setDoc.getElementsByClass("chart_title");
        if (!name.isEmpty()) {
            // Loop through all elements with class "chart_title" (if there are multiple)
            for (Element chartTitle : name) {
                // Get the <h1> tag
                Element h1 = chartTitle.select("h1").first();
                if (h1 != null) {
                    // Select the <a> tag inside the <h1>
                    Element a = h1.select("a").first();
                    // If there's an <a> tag, remove it from the <h1> content
                    if (a != null) {
                        // Remove the <a> tag from the <h1> content
                        a.remove();
                    }
                    temp = h1.text();
                }
            }
        }
                    // Now get the text of the <h1> without the <a> tag
    }catch (IOException e) {
        System.out.println("Error while connecting to set URL for " + pokemonName);
    }
    return temp;
    }
}

//ISSUE, THIS SHIT AINT LOADING