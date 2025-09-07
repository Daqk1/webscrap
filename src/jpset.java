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

public class jpset implements Runnable {
    private String setName;
    private String DATA_FILE = "card_data.json";
    private int globalCardId = 0; // This will keep track of the card ID
    private List<Card> fetchedCards;

    public static void main(String[] args) {
    }

    public void run() {
        // Always fetch fresh data from website
        System.out.println("Fetching fresh data from website...");

        Document setDoc = loadPage(changeURL(setName));
        fetchedCards = Collections.synchronizedList(new ArrayList<>());

        // Use multithreading to fetch data while maintaining order
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ArrayList<String> allCardURLs = getAllCardsInASet(setDoc, setName);
        System.out.println(setName + " | " + allCardURLs.size() + " | " + allCardURLs.getLast());

        // Create a list to store results in order
        List<CompletableFuture<Card>> futures = new ArrayList<>();

        for (int i = 0; i < allCardURLs.size(); i++) {
            final String url = allCardURLs.get(i);

            CompletableFuture<Card> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Fetch all data in a single request instead of multiple separate calls
                    CardData cardData = fetchCardData(url, setName);
                    if (cardData != null) {
                        int cardId = globalCardId++;
                        Card card = new Card(cardData.name, cardData.price, url, cardId, cardData.picture, setName);
                        System.out.println("Fetched: " + cardData.name + " | $" + cardData.price + " | " + cardId
                                + " | " + cardData.picture);
                        return card;
                    }
                    return null;
                } catch (Exception e) {
                    System.out.println("Error fetching card at: " + url + " | " + e.getMessage());
                    return null;
                }
            }, executor);

            futures.add(future);
        }

        // Wait for all futures to complete and collect results in order
        for (CompletableFuture<Card> future : futures) {
            try {
                Card card = future.get(30, TimeUnit.SECONDS); // 30 second timeout per card
                if (card != null) {
                    fetchedCards.add(card);
                }
            } catch (Exception e) {
                System.out.println("Error waiting for card completion: " + e.getMessage());
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                System.out.println("Some tasks did not complete within timeout, forcing shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Executor was interrupted, forcing shutdown...");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Always save the fresh data
        saveCardData(fetchedCards);
        List<Card> cards = fetchedCards;

        double total = cards.stream().mapToDouble(Card::getPrice).sum();
        System.out.println("Total Price: $" + total + " | Total Fetched: " + globalCardId);
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
            if (tbody == null) {
                System.out.println("Warning: No tbody found in document");
                return cardURLs;
            }

            Elements titles = tbody.getElementsByClass("title");
            Elements links = titles.select("a");
            System.out.println("Found " + links.size() + " card links in the document");

            for (Element title : links) {
                String url = title.attr("href");
                cardURLs.add(constructCardURL(url));
            }

            System.out.println("Successfully extracted " + cardURLs.size() + " card URLs");
        } catch (Exception e) {
            System.out.println("Error while connecting to set URL for " + setName + ": " + e.getMessage());
        }
        return cardURLs;
    }

    public static String constructCardURL(String cardName) {
        return "https://www.pricecharting.com" + cardName;
    }

    // Optimized method to fetch all card data in a single request
    public static CardData fetchCardData(String url, String setName) {
        try {
            Document doc = fetchWithRetry(url, 2);
            if (doc == null) {
                return null;
            }

            // Extract name
            String name = "";
            Elements nameElements = doc.getElementsByClass("chart_title");
            if (!nameElements.isEmpty()) {
                for (Element chartTitle : nameElements) {
                    Element h1 = chartTitle.select("h1").first();
                    if (h1 != null) {
                        Element a = h1.select("a").first();
                        if (a != null) {
                            a.remove();
                        }
                        name = h1.text();
                    }
                }
            }

            // Extract price
            double price = 0.00;
            Element priceElement = doc.selectFirst(".price.js-price");
            if (priceElement != null) {
                String priceText = priceElement.text();
                if (!priceText.equals("-")) {
                    priceText = priceText.replace("$", "").replace(",", "");
                    price = Double.parseDouble(priceText);
                }
            }

            // Extract picture
            String picture = "No picture available";
            Elements pictureElements = doc.select("img[alt]");
            for (Element pictureElement : pictureElements) {
                if (pictureElement.attr("alt").contains(name)) {
                    picture = pictureElement.attr("src");
                    break;
                }
            }

            return new CardData(name, price, picture);
        } catch (Exception e) {
            System.out.println("Error fetching card data from: " + url);
            return null;
        }
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
        Document doc = fetchWithRetry(url, 2); // Reduced retry count
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
        return 0.00;
    }

    public static String getPicture(String url) throws IOException {
        Document doc = fetchWithRetry(url, 2); // Use retry logic and reuse connection
        if (doc == null) {
            return "No picture available";
        }

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
                        Thread.sleep(1000); // Reduced sleep time to 1 second
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return Jsoup.connect(url)
                        .timeout(5000) // Reduced timeout to 5 seconds
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .get();
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
                "lib\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        // Configure Chrome options for better performance
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for better performance
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-images"); // Disable image loading for faster page loads
        options.addArguments("--disable-javascript"); // Disable JS if not needed for data extraction

        WebDriver driver = new ChromeDriver(options);
        Document doc = null;

        try {
            driver.get(url);

            // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); //
            // Reduced wait time
            WebElement order = driver.findElement(By.id("sortForm"));
            Select select = new Select(order);
            select.selectByVisibleText("Card Number");

            // Advanced scrolling algorithm for dynamic content loading
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            int lastItemCount = 0;
            int stableCount = 0;
            int maxIterations = 300; // Increased for large sets
            int iteration = 0;

            System.out.println("Starting dynamic content loading...");

            // First, get initial count
            lastItemCount = getCurrentItemCount(driver);
            System.out.println("Initial item count: " + lastItemCount);

            while (stableCount < 10 && iteration < maxIterations) { // Need 10 stable iterations
                iteration++;

                // Scroll to bottom with multiple scroll attempts
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(200);
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(200);
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // Wait for potential new content to load
                Thread.sleep(1500); // Increased wait time for content to load

                // Try to wait for new content to appear
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));
                } catch (Exception e) {
                    // Continue if wait times out
                }

                // Count current items in the table
                int currentItemCount = getCurrentItemCount(driver);

                if (currentItemCount > lastItemCount) {
                    // New items loaded
                    stableCount = 0;
                    int newItems = currentItemCount - lastItemCount;
                    lastItemCount = currentItemCount;
                    System.out.println("Iteration " + iteration + ": Found " + currentItemCount + " items (loaded "
                            + newItems + " new items)");
                } else {
                    // No new items loaded
                    stableCount++;
                    System.out.println(
                            "Iteration " + iteration + ": No new items loaded (stable count: " + stableCount + "/10)");
                }

                // Additional wait to ensure all content is loaded
                Thread.sleep(500);
            }

            if (stableCount >= 10) {
                System.out.println("Scrolling completed! Final item count: " + lastItemCount);
            } else {
                System.out.println(
                        "Reached maximum iterations (" + maxIterations + "). Final item count: " + lastItemCount);
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

    // Helper method to count current items in the table
    private static int getCurrentItemCount(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // Count the number of rows in the tbody
            Long count = (Long) js.executeScript(
                    "var tbody = document.querySelector('tbody');" +
                            "if (tbody) {" +
                            "  return tbody.querySelectorAll('tr').length;" +
                            "} else {" +
                            "  return 0;" +
                            "}");
            return count.intValue();
        } catch (Exception e) {
            System.out.println("Error counting items: " + e.getMessage());
            return 0;
        }
    }

    public void saveCardData(List<Card> cards) {
        // Clean the set name for file naming
        String cleanSetName = cleanSetNameForFile(setName);
        DATA_FILE = "jppokemon_data/" + cleanSetName + ".json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            Gson gson = new Gson();
            gson.toJson(cards, writer);
            System.out.println("Card data saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Error saving data to file: " + e.getMessage());
        }
    }

    // Helper method to clean set name for file naming
    private String cleanSetNameForFile(String setName) {
        if (setName == null) {
            return "unknown-set";
        }

        String cleaned = setName;

        cleaned = cleaned.replace("&", "and");

        return cleaned;
    }

    public List<Card> loadCardData() {
        String cleanSetName = cleanSetNameForFile(setName);
        String dataFile = "jppokemon_data/" + cleanSetName + ".json";

        try (Reader reader = new FileReader(dataFile)) {
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

    // Helper class to hold card data from a single request
    static class CardData {
        public final String name;
        public final double price;
        public final String picture;

        public CardData(String name, double price, String picture) {
            this.name = name;
            this.price = price;
            this.picture = picture;
        }
    }
}