import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import java.io.IOException;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        System.out.println("Prices: $" + getPrice("https://www.pricecharting.com/game/pokemon-evolving-skies/sylveon-vmax-212?q=sylveon+vmax+%23212"));
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
