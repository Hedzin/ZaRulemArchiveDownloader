package zrdownloader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class ZRDownloader {

	public static void main(String[] args) throws IOException {

		Document doc = Jsoup.connect("https://www.zr.ru/archive/zr/1961/04/po-sliedam-nieopublikova..").get();

		Elements pages = doc.getElementsByAttributeValueContaining("href", "javascript:loadViewer");
		int pageCount = pages.size();
		List<String> pagesUrl = new ArrayList<String>();
		for (Element page : pages) {
			pagesUrl.add(page.attr("href").replace("javascript:loadViewer('", "").replace("')", ""));
		}

		Element link4file = doc.getElementsByClass("active").select("a[href]").last();
		;
		String filename = link4file.attr("abs:href").replace("http://www.zr.ru/archive/", "").replace("/", "_");

		Element container = doc.getElementsByClass("container app-content").first();
		Element script = container.getElementsByTag("script").last();
		String scripttest = script.html();
		String[] lines = scripttest.split("\n");
		String MY_TILESIZE = "";
		String MY_WIDTH = "";
		String MY_HEIGHT = "";
		// String URL = "";
		for (String line : lines) {
			if (line.contains("var MY_TILESIZE =")) {
				MY_TILESIZE = line.replace("var MY_TILESIZE = ", "").replace(";", "").trim();
				continue;
			}
			if (line.contains("createViewer( viewer1")) {

				String[] values = line.replace("createViewer( viewer1, 'viewer1', url, ", "").replace(" );", "").trim()
						.split(", ");
				MY_WIDTH = values[0];
				MY_HEIGHT = values[1];
				continue;
			}
		}
		int titleSize = Integer.parseInt(MY_TILESIZE);
		int width = Integer.parseInt(MY_WIDTH);
		int height = Integer.parseInt(MY_HEIGHT);
		System.out.println("titleSize: " + titleSize);
		System.out.println("width: " + width);
		System.out.println("height: " + height);
		int widthCount = (int) Math.ceil((double) width / titleSize);
		int heightCount = (int) Math.ceil((double) height / titleSize);
		System.out.println("widthCount: " + widthCount);
		System.out.println("heightCount: " + heightCount);

		String linkStart = "https://www.zr.ru";

		int pageIndex = 0;
		for (String URL : pagesUrl) {
			pageIndex++;
			System.out.println("URL: " + URL);
			List<String> links = new ArrayList<String>();
			for (int i = 0; i < heightCount; i++) {
				for (int j = 0; j < widthCount; j++) {
					links.add(linkStart + URL + "000/" + String.format("%03d", j) + "/" + String.format("%03d", i));
				}
			}
			for (String string : links) {
				System.out.println(string);
			}
			int rows = heightCount; // we assume the no. of rows and cols are known and each chunk has equal width
									// and height
			int cols = widthCount;
			int chunks = rows * cols;

			int chunkWidth, chunkHeight;
			int type;
			// fetching image files
			File[] imgFiles = new File[chunks];
			for (int i = 0; i < chunks; i++) {
				imgFiles[i] = new File("archi" + i + ".jpg");
			}

			// creating a bufferd image array from image files
			BufferedImage[] buffImages = new BufferedImage[chunks];
			for (int i = 0; i < chunks; i++) {
				buffImages[i] = ImageIO.read(new URL(links.get(i)));
			}
			type = buffImages[0].getType();
			chunkWidth = buffImages[0].getWidth();
			chunkHeight = buffImages[0].getHeight();

			// Initializing the final image
			BufferedImage finalImg = new BufferedImage(width, height, type);
			String appendix = pageCount > 1 ? "_" + pageIndex : "";
			int num = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
					num++;
				}
			}
			System.out.println("Image concatenated.....");
			ImageIO.write(finalImg, "jpeg", new File(filename + appendix + ".jpg"));
		}
	}
}