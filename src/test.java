import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class test {
    public static void main(String[] args)  {
        try{
            String pictureUrl = getPicture("https://www.pricecharting.com/game/pokemon-evolving-skies/rayquaza-vmax-218");
            System.out.print(pictureUrl);
        }catch (Exception e) {
            System.out.println("Error fetching card at: " + "https://www.pricecharting.com/game/pokemon-evolving-skies/rayquaza-vmax-218");
        }
       
    }
    public static String getPicture(String url) throws IOException{
        Document doc = Jsoup.connect(url).get();
        Element docName = doc.selectFirst(".chart_title");
        String name = docName.text();
        Element pictureElement = doc.selectFirst("img[alt='" + name + "']");
        String imageUrl = pictureElement.attr("src");
        return imageUrl;
    }
}
