import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        try {
            // start time
            long startTime = System.currentTimeMillis();
            System.out.println("Start indexing ...");

            // init stopwords and stemmer
            Settings.stopWords = new StopWords();
            Settings.stem = new Stemmer();
            // SAX Parser
            File inputFile = new File("/Users/goddamnchen/Desktop/CS6200/Project/wiki-search/enwiki-20210301-sample-100m.xml");

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            FileHandler handler = new FileHandler();
            saxParser.parse(inputFile, handler);

            long stopTime = System.currentTimeMillis();
            long elapsed = stopTime - startTime;
            System.out.println("Complete indexing in " + elapsed + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}