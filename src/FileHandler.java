import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.TreeMap;

public class FileHandler extends DefaultHandler {
    int id = 0;
    boolean inPage = false;
    boolean inPageId = false;
    boolean inTitle = false;
    boolean inText = false;
    StringBuilder str = new StringBuilder();
    TreeMap<Long, String> Id2Title = new TreeMap<Long, String>();        // flush when reach MAX_TITLE_SIZE
    String title = "";
    int docs = 0;            // docs count
    int files = 1;           // partial index file count
    int title_files = 1;    // partial docID-title file count
    private Parser parser = null;

    public FileHandler() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("page")) {
            inPage = true;
            id = 0;

        } else if (qName.equals("id") && inPage && id == 0) {
            inPageId = true;
            parser = new Parser();
            str.setLength(0);
            id++;

        } else if (qName.equals("title") && inPage) {
            inTitle = true;
            str.setLength(0);

        } else if (qName.equals("text") && inPage) {
            inText = true;
            str.setLength(0);
            docs++;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("id") && inPageId && id == 1) {
            inPageId = false;
            this.parser.processAsPageID(str.toString());
            this.parser.processAsPageTitle(title.toString());
            Id2Title.put(Long.parseLong(str.toString()), title.toString());
            if (Id2Title.size() % Settings.MAX_TITLE_SIZE == 0) {
                FileIO.writeId2Title(Id2Title, title_files);
                Id2Title.clear();
                title_files++;
            }
            this.title = "";
        } else if (qName.equals("title") && inTitle) {
            inTitle = false;
            this.title = str.toString();
        } else if (qName.equals("text") && inText) {
            inText = false;
            this.parser.processAsPageText(str.toString());
            if (docs % Settings.MAX_DOC_SIZE == 0) {
                // write to file
                FileIO io = new FileIO(Settings.fileDirec + "file" + files + ".txt");
                io.write();
                Index.invertedIndex.clear();
                files++;
            }

        } else if (qName.equals("page")) {
            inPage = false;
        }

    }


    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        str.append(s);
    }


    @Override
    public void endDocument() throws SAXException {
        //this.parser.writeInvertedIndex();
        if (!Index.invertedIndex.isEmpty()) {
            //System.out.println("write in file : "+ Settings.fileDirec +  "file" +files+".txt");
            FileIO io = new FileIO(Settings.fileDirec + "file" + files + ".txt");
            io.write();
            Index.invertedIndex.clear();
        }

        KMerger.mergeFiles(files);

        if (!Id2Title.isEmpty()) {
            FileIO.writeId2Title(Id2Title, title_files);
            Id2Title.clear();
        }

        KMerger.mergeTitle(title_files);
    }
}
