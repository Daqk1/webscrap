
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.*;



public class back { 
    public static void main(String[] args) {
        String [] selectedPokemon = {"Giratina"};
        ArrayList<ArrayList<String>> eachSelectedPokemonURL = new ArrayList<ArrayList<String>>();
        String setName = "crown-zenith";
        Document setDoc = loadPageWithSelenium(changeURL(setName));
        if(selectedPokemon.length < 1 && setName.length() > 1){
                eachSelectedPokemonURL.add(getAllCardsInASet(setDoc, setName)); //each pokemon in the pokemon catagory
                HashMap<ArrayList<String>,ArrayList<Double>> bothPriceAndName = new HashMap<>(); 
                    for(int i = 0; i < eachSelectedPokemonURL.size(); i++){
                    ArrayList<String> innerList = eachSelectedPokemonURL.get(i);
                    ArrayList<Double> price = new ArrayList<>();
                    ArrayList<String> names = new ArrayList<>();
                    for(int a = 0; a < innerList.size(); a++){
                        price.add(getAllThePrices(innerList.get(a), setName));
                        names.add(getAllTheNames(innerList.get(a), setName));
                    } 
                  bothPriceAndName.put(names,price);
                }
                double total = 0.00;
        for (Map.Entry<ArrayList<String>, ArrayList<Double>> entry : bothPriceAndName.entrySet()) {
            ArrayList<String> names = entry.getKey();  // Get the list of names
            ArrayList<Double> prices = entry.getValue(); // Get the list of prices
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                Double price = prices.get(i);
                System.out.println("Pokémon: " + name + " | Price: $" + price);
                total += price;  
            }
        }
        System.out.println("Total: $" + total);
        }else{
            for(int i = 0; i < selectedPokemon.length; i++){
                eachSelectedPokemonURL.add(getPokemonURLARRAY(setDoc, selectedPokemon[i], setName)); //each pokemon in the pokemon catagory
            }
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
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                Double price = prices.get(i);
                System.out.println("Pokémon: " + name + " | Price: $" + price);
                total += price;  
            }
        }
        System.out.println("Total: $" + total);
        }       
        
    }

    public static double getPrice(String setURL, String pokemonName, String setName){ 
            // Fetch the HTML content from the URL
            double price = 0;
            try{
            Document pokemonDoc = Jsoup.connect(setURL).get(); 
            Elements prices = pokemonDoc.getElementsByClass("price js-price");
            // Select the price element
            Element firstPriceElement = prices.first(); 
            String firstPrice = firstPriceElement.text(); 
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
    public static double getAllThePrices(String setURL, String setName){
        // Fetch the HTML content from the URL
        double price = 0;
        try{
        Document pokemonDoc = Jsoup.connect(setURL).get(); 
        Elements prices = pokemonDoc.getElementsByClass("price js-price");
        // Select the price element
        Element firstPriceElement = prices.first(); 
        String firstPrice = firstPriceElement.text(); 
        if(firstPrice.equals("-")){
            System.out.println("Error " + setName);
            return 0;
        }else{
            firstPrice = firstPrice.replace("$", "").replace(",", "").trim();
            price = Double.parseDouble(firstPrice);
            System.out.println(price);
        }
        } catch (IOException e) {
            System.out.println("Connection failed while trying to retrieve price for " + setName);
        } catch (NumberFormatException e) {
            System.out.println("Price format issue for " + setName);
        }
        return price;
}

    public static void error(String pokemonName, String setNameString){
        System.out.println("Cannot Find Price of " + pokemonName + " in " + setNameString);
    }
    
    public static ArrayList<String> getPokemonURLARRAY(Document doc, String pokemonName, String setName){
        String getPriceURL = "";
        ArrayList<String> similarPokemonURL = new ArrayList<String>();
        try{
        Element tbody = doc.select("tbody").first();
        Elements name = tbody.getElementsByClass("title");
        for (Element pName : name) {
            String temp = pName.text(); // Extract text from the element //I NEED THIS SOMEWHERE!
        //System.out.println(temp);
            if(temp.contains(pokemonName)){
                getPriceURL = cPokemon(temp, setName);
                similarPokemonURL.add(getPriceURL);
            }
        }
    }catch (Exception e) {
        System.out.println("Error while connecting to set URL for " + pokemonName);
    }
    return similarPokemonURL;
    }

    public static ArrayList<String> getAllCardsInASet(Document doc, String setName){
        String getPriceURL = "";
        ArrayList<String> similarPokemonURL = new ArrayList<String>();
        try{
        Element tbody = doc.select("tbody").first();
        Elements name = tbody.getElementsByClass("title");
        for (Element pName : name) {
            String temp = pName.text(); 
            getPriceURL = cPokemon(temp, setName);
            similarPokemonURL.add(getPriceURL);
        }
    }catch (Exception e) {
        System.out.println("Error while connecting to set URL for " + setName);
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
                    
    }catch (IOException e) {
        System.out.println("Error while connecting to set URL for " + pokemonName);
    }
    return temp;
    }
    public static String getAllTheNames(String setURL, String setName){
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
                  
    }catch (IOException e) {
        System.out.println("Error while connecting to set URL for " + setName);
    }
    return temp;
    }
    public static Document loadPageWithSelenium(String url) {
        // Set path to chromedriver
        System.setProperty("webdriver.chrome.driver", "webscrap\\lib\\chromedriver-win64 (1)");

        WebDriver driver = new ChromeDriver();
        Document doc = null; // Document to return
         
        try {
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
            boolean moreItemsLoaded = true;
            int i = 0;
            while (moreItemsLoaded) {
                i++;
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                Thread.sleep(20);  // Adjust time depending on how long the page takes to load more content

                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("title")));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));

                if (i == 20) {
                    moreItemsLoaded = false;
                }
            }
            String pageSource = driver.getPageSource();
            doc = Jsoup.parse(pageSource); 

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
          
            driver.quit();
        }
        return doc; 
    }
}

