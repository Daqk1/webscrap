package com.example.webscrap;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;


public class test implements Runnable {
    private String setName;
    private String DATA_FILE = "card_data.json";
    private static int globalCardId = 1;  // This will keep track of the card ID
    private List<Card> fetchedCards;

    public static void main(String[] args) {
        try{
            getCardPrice("https://www.pricecharting.com/game/pokemon-gym-challenge/blaine%27s-charizard-2"); 
        }catch (IOException e) {
            System.out.println("Error while connecting to set URL for ");
        }    
    }
    
    public void run(){
        List<Card> cards = loadCardData();

        if (cards.isEmpty()) {
            System.out.println("No saved data found. Fetching from website...");
            Document setDoc = loadPage(changeURL(setName));
            fetchedCards = Collections.synchronizedList(new ArrayList<>());

            // Use multithreading to fetch data
            ExecutorService executor = Executors.newFixedThreadPool(10);
            ArrayList<String> allCardURLs = getAllCardsInASet(setDoc, setName);
            for (String url : allCardURLs) {
                executor.submit(() -> {
                    try {
                        String name = getAllTheNames(url,setName);
                        double price = getCardPrice(url);
                        String picture = getPicture(url);
                        int cardId = getNextCardId();  
                        fetchedCards.add(new Card(name, price, url,cardId, picture));
                        //System.out.println("Fetched: " + name + " | $" + price + " | " + cardId + " | " + picture);
                    } catch (Exception e) {
                        System.out.println("Error fetching card at: " + url);
                    }
                });
            }

            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            saveCardData(fetchedCards);
            cards = fetchedCards;
        } else {
            System.out.println("Loaded saved card data.");
        }

        double total = cards.stream().mapToDouble(Card::getPrice).sum();
        System.out.println("Total Price: $" + total);
    }
    public List<Card> cardList(){
        return fetchedCards;
    }
    public static String changeURL(String setName) {
        return "https://www.pricecharting.com/console/pokemon-" + setName + "?sort=model-number";
    }
    private synchronized int getNextCardId() {
        return globalCardId++;
    }
    public static ArrayList<String> getAllCardsInASet(Document doc, String setName) {
        ArrayList<String> cardURLs = new ArrayList<>();
        try {
            Element tbody = doc.select("tbody").first();
            Elements titles = tbody.getElementsByClass("title");    
            Elements links = titles.select("a");
            for (Element title : links) {
                String url = title.attr("href");
                cardURLs.add(constructCardURL(url));  
            }
        } catch (Exception e) {
            System.out.println("Error while connecting to set URL for " + setName);
        }
        return cardURLs;
    }
    public static String constructCardURL(String cardName) {
        return "https://www.pricecharting.com" + cardName;
    }

    public static String getAllTheNames(String setURL, String setName){
        String temp = "";
        try{
        Document setDoc = Jsoup.connect(setURL).get(); 
        Elements name = setDoc.getElementsByClass("chart_title");
        if (!name.isEmpty()) {
            for (Element chartTitle : name) {
                Element h1 = chartTitle.select("h1").first();
                if (h1 != null) {
                    Element a = h1.select("a").first();
                    if (a != null) {
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

    public static double getCardPrice(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element priceElement = doc.selectFirst(".price.js-price");
        if (priceElement != null) {
            String priceText = priceElement.text().replace("$", "").replace(",", "");
            return Double.parseDouble(priceText);
        }
        return 0.0;
    }
    public static String getPicture(String url) throws IOException{
        Document doc = Jsoup.connect(url).get();
        Element docName = doc.selectFirst(".chart_title");
        String name = docName.text();
        Element pictureElement = doc.selectFirst("img[alt='" + name + "']");
        String imageUrl = pictureElement.attr("src");
        return imageUrl;
    }
        public static Document loadPage(String url) {
        // Set path to chromedriver
        System.setProperty("webdriver.chrome.driver", "webscrap\\lib\\chromedriver-win64 (1)\\chromedriver-win64\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        Document doc = null; 

        try {
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
            WebElement order = driver.findElement(By.id("sortForm"));
            Select select = new Select(order);
            select.selectByVisibleText("Popularity");
            WebElement imageOrNot = driver.findElement(By.name("show-images"));
            Select noImage = new Select(imageOrNot);
            noImage.selectByVisibleText("Hide Images");
            boolean moreItemsLoaded = true;
            int i = 0;
            while (moreItemsLoaded) {
                i++;
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                Thread.sleep(100);  

                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("title")));

                if (i == 10) {
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

    public void saveCardData(List<Card> cards) {
        DATA_FILE = "webscrap/pokemon_data/"+ setName + ".json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            Gson gson = new Gson();
            gson.toJson(cards, writer);
            System.out.println("Card data saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Error saving data to file.");
            e.printStackTrace();
        }
    }

    public  List<Card> loadCardData() {
        try (Reader reader = new FileReader(DATA_FILE)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Card>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    public void setSetName(String set){
        setName = set;
    }
    static class Card {
        private String name;
        private double price;
        private String url;
        private String picture;
        private int id;
        public Card(String name, double price, String url,int id, String picture) {
            this.name = name;
            this.price = price;
            this.url = url;
            this.picture = picture;
            this.id = id;
        }
    
        public double getPrice() {
            return price;
        }
    
        @Override
        public String toString() {
            return "Card{" +
                    "name='" + name + '\'' +
                    ", price=" + price +
                    ", url='" + url + '\'' +
                    ", number=" + id +
                    ", picture='" + picture + '\'' +
                    '}';
        }
        public String getPicture(){
            return picture;
        }
        public int getNumber(){
            return id;
        }
        public String getUrl(){
            return url;
        }
    }
    
    
}

