import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;


public class data implements Runnable {
    private String setName;
    private String DATA_FILE = "card_data.json"; // File to save/load card data
    public static void main(String[] args) {
    }

    public void run(){
        List<Card> cards = loadCardData();

        if (cards.isEmpty()) { // If no data exists, fetch from the website
            System.out.println("No saved data found. Fetching from website...");
            Document setDoc = loadPage(changeURL(setName));

            // Use multithreading to fetch data
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Card> fetchedCards = Collections.synchronizedList(new ArrayList<>());

            ArrayList<String> allCardURLs = getAllCardsInASet(setDoc, setName);

            for (String url : allCardURLs) {
                executor.submit(() -> {
                    try {
                        String name = getAllTheNames(url,setName);
                        double price = getCardPrice(url);
                        fetchedCards.add(new Card(name, price, url));
                        System.out.println("Fetched: " + name + " | $" + price);
                    } catch (Exception e) {
                        System.out.println("Error fetching card at: " + url);
                    }
                });
            }

            // Shutdown and wait for tasks to finish
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Save fetched data to file
            saveCardData(fetchedCards);
            cards = fetchedCards;
        } else {
            System.out.println("Loaded saved card data.");
        }

        // Calculate and display the total price
        double total = cards.stream().mapToDouble(Card::getPrice).sum();
        System.out.println("Total Price: $" + total);
    }

    public static String changeURL(String setName) {
        return "https://www.pricecharting.com/console/pokemon-" + setName + "?sort=model-number";
    }

    public static ArrayList<String> getAllCardsInASet(Document doc, String setName) {
        ArrayList<String> cardURLs = new ArrayList<>();
        try {
            Element tbody = doc.select("tbody").first();
            Elements titles = tbody.getElementsByClass("title");
            for (Element title : titles) {
                String cardName = title.text();
                cardURLs.add(constructCardURL(cardName, setName));
            }
        } catch (Exception e) {
            System.out.println("Error while connecting to set URL for " + setName);
        }
        return cardURLs;
    }

    public static String constructCardURL(String cardName, String setName) {
        String baseURL = "https://www.pricecharting.com/game/pokemon-" + setName + "/";
        String cleanName = cardName.toLowerCase().replace(" ", "-").replaceAll("[\\[\\]#]", "");
        cleanName = cleanName.replaceAll("-+", "-").replaceAll("-$", "");
        return baseURL + cleanName;
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
                    // Now get the text of the <h1> without the <a> tag
    }catch (IOException e) {
        System.out.println("Error while connecting to set URL for " + setName);
    }
    return temp;
    }

    public static double getCardPrice(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element priceElement = doc.selectFirst(".price.js-price");
        if (priceElement != null) {
            String priceText = priceElement.text().replace("$", "").replace(",", "");
            return Double.parseDouble(priceText);
        }
        return 0.0;
    }

    public static Document loadPage(String url) {
        // Set path to chromedriver
        System.setProperty("webdriver.chrome.driver", "webscrap\\lib\\chromedriver-win64 (1)");

        WebDriver driver = new ChromeDriver();
        Document doc = null; // Document to return

        try {
            // Open the URL with Selenium
            driver.get(url);

            // Wait for the page to fully load (adjust condition if needed)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
            boolean moreItemsLoaded = true;
            int i = 0;
            while (moreItemsLoaded) {
                i++;
                // Scroll to the bottom of the page using JavaScript
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // Wait for new content to load
                Thread.sleep(100);  // Adjust time depending on how long the page takes to load more content

                // Check if new content has been loaded. This could depend on the website, but for now we'll assume the presence of a specific element.
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("title")));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));

                // Check if new items are loaded by comparing the previous state with the current state
                // If new items are not loaded, exit the loop
                if (i == 10) {
                    moreItemsLoaded = false;
                }
            }
            // Get page source and parse it using Jsoup
            String pageSource = driver.getPageSource();
            doc = Jsoup.parse(pageSource); // Convert page source into Jsoup Document

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }
        return doc; // Return the parsed Document
    }

    // Save card data to a JSON file
    public void saveCardData(List<Card> cards) {
        DATA_FILE = "pokemon_data/"+ setName + ".json";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            Gson gson = new Gson();
            gson.toJson(cards, writer);
            System.out.println("Card data saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Error saving data to file.");
            e.printStackTrace();
        }
    }

    // Load card data from a JSON file
    public  List<Card> loadCardData() {
        try (Reader reader = new FileReader(DATA_FILE)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Card>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // Card class to store card data
    static class Card {
        private String name;
        private double price;
        private String url;

        public Card(String name, double price, String url) {
            this.name = name;
            this.price = price;
            this.url = url;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name + " - $" + price;
        }
    }
    public void setSetName(String set){
        setName = set;
    }
}

