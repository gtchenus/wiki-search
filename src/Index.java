import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class Index {
    // {term: [{docID: filedOccCount}]}
    public static Map<String, HashMap<Long, Record>> invertedIndex = new TreeMap<String, HashMap<Long, Record>>();

    /*
     * Constructor : Initialize index map per document
     */
    public Index() {
    }


    /*
     * Word handler for title and other fields
     */
    public void handlerLocal(String str, int type) {
        if (str.length() <= 1) return;
        if (type == Settings.ID)
            return;

        str = str.toLowerCase();
        if (Settings.stopWords.stopWords.contains(str))
            return;

        if (!str.matches(".*\\d+.*")) {

            Settings.stem.add(str.toCharArray(), str.length());

            if (Settings.stem.stem(str) != null) {
                str = Settings.stem.stem(str);
            }

            if (str.length() < 3 || Settings.stopWords.stopWords.contains(str)) {
                return;
            }
        }
        // index the word and increase the count
        Record r = null;
        // posting  = [{DocID: FieldOccurCount]
        HashMap<Long, Record> posting = null;
        if (invertedIndex.containsKey(str)) {
            posting = invertedIndex.get(str);
            if (posting.containsKey(Parser.currentPageID)) {
                r = posting.get(Parser.currentPageID);
            } else {
                r = new Record();
                r.setID(Parser.currentPageID);
            }
        } else {
            posting = new HashMap<Long, Record>();
            r = new Record();
            r.setID(Parser.currentPageID);
        }
        r.updateFieldCount(type);
        posting.put(Parser.currentPageID, r);
        invertedIndex.put(str, posting);
    }
}