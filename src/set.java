import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class set implements Runnable {
    private String setName;
    private String DATA_FILE = "card_data.json";
    private int globalCardId = 0; // This will keep track of the card ID
    private List<Card> fetchedCards;

    public static void main(String[] args) {
    }

    public void run() {
        List<Card> cards = loadCardData();

        if (cards.isEmpty()) {
            System.out.println("No saved data found. Fetching from website...");
            if (setName == "champion-27s-path") {
                setName = "champion%27s-path";
            }
            Document setDoc = loadPage(changeURL(setName));
            fetchedCards = Collections.synchronizedList(new ArrayList<>());

            // Use multithreading to fetch data
            ExecutorService executor = Executors.newFixedThreadPool(10);
            ArrayList<String> allCardURLs = getAllCardsInASet(setDoc, setName);
            for (String url : allCardURLs) {
                executor.submit(() -> {
                    try {
                        String name = getAllTheNames(url, setName);
                        double price = getCardPrice(url);
                        String picture = getPicture(url);
                        int cardId = globalCardId++;
                        Card card = new Card(name, price, url, cardId, picture, setName);
                        fetchedCards.add(card);
                        // System.out.println("Fetched: " + name + " | $" + price + " | " + cardId + " |
                        // " + picture);
                    } catch (Exception e) {
                        // System.out.println("Error fetching card at: " + url + " | " +
                        // e.getMessage());
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
        System.out.println("Total Price: $" + total + "Total Fetched: " + globalCardId);
    }

    public List<Card> cardList() {
        return fetchedCards;
    }

    public static String changeURL(String setName) {
        return "https://www.pricecharting.com/console/pokemon-" + setName + "?sort=model-number";
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

    public static String getAllTheNames(String setURL, String setName) {
        String temp = "";
        try {
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
        } catch (IOException e) {
            System.out.println("Error while connecting to set URL for " + setName);
        }
        return temp;
    }

    public static double getCardPrice(String url) throws IOException {
        Document doc = fetchWithRetry(url, 3);
        String priceText = "";
        if (doc != null) {
            Element priceElement = doc.selectFirst(".price.js-price");
            if (priceElement != null) {
                priceText = priceElement.text();
                if (priceText.equals("-")) {
                    return 0.00;
                } else {
                    priceText = priceText.replace("$", "").replace(",", "");
                    return Double.parseDouble(priceText);
                }
            }
        }
        return 0.0;
    }

    public static String getPicture(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element docName = doc.selectFirst(".chart_title");
        if (docName == null) {
            return "No title available";
        }

        String name = docName.text();

        // Look for an <img> tag with an 'alt' attribute that contains the card's name.
        Elements pictureElements = doc.select("img[alt]");
        for (Element pictureElement : pictureElements) {
            if (pictureElement.attr("alt").contains(name)) {
                return pictureElement.attr("src");
            }
        }

        return "No picture available";
    }

    public static Document fetchWithRetry(String url, int retryCount) throws IOException {
        int attempt = 0;
        IOException exception = null;
        while (attempt < retryCount) {
            try {
                // Add delay between retries to avoid being rate-limited
                if (attempt > 0) {
                    try {
                        Thread.sleep(2000); // Sleep for 2 seconds between attempts
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return Jsoup.connect(url).timeout(10000).get(); // Retry with timeout
            } catch (IOException e) {
                attempt++;
                exception = e;
                System.out.println("Attempt " + attempt + " failed, retrying...");
                if (attempt == retryCount) {
                    throw exception;
                }
            }
        }
        return null;
    }

    public static Document loadPage(String url) {
        // Set path to chromedriver
        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\wujus\\JavaCode\\web\\webscrap\\lib\\chromedriver-win64\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        Document doc = null;

        try {
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(500));
            WebElement order = driver.findElement(By.id("sortForm"));
            Select select = new Select(order);
            select.selectByVisibleText("Card Number");
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

                if (i == 100) {
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
        if (setName == "champion%27s-path") {
            setName = "champion-27s-path";
        }
        DATA_FILE = "webscrap/pokemon_data/" + setName + ".json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            Gson gson = new Gson();
            gson.toJson(cards, writer);
            System.out.println("Card data saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Error saving data to file: " + e.getMessage());
        }
    }

    public List<Card> loadCardData() {
        try (Reader reader = new FileReader(DATA_FILE)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Card>>() {
            }.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setSetName(String set) {
        setName = set;
    }

    static class Card {
        private String cardName;
        private double cardPrice;
        private String cardUrl;
        private String cardPicture;
        private String cardId;
        private int cardNumberOfCards;

        public Card(String name, double price, String url, int id, String picture, String setName) {
            this.cardName = name;
            this.cardPrice = price;
            this.cardUrl = url;
            this.cardPicture = picture;
            this.cardId = setName + "_" + Integer.toString(id);
            this.cardNumberOfCards = 0; // Default value, can be updated later if needed
        }

        public double getPrice() {
            return cardPrice;
        }

        @Override
        public String toString() {
            return "Card{" +
                    "cardName='" + cardName + '\'' +
                    ", cardPrice=" + cardPrice +
                    ", cardUrl='" + cardUrl + '\'' +
                    ", cardId=" + cardId +
                    ", cardPicture='" + cardPicture + '\'' +
                    ", cardNumberOfCards=" + cardNumberOfCards +
                    '}';
        }
    }
}
