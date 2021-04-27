public class Settings {
    public final static int ID = 1;
    public final static int TITLE = 2;
    public final static int BODY = 4;
    public final static int EXTERNAL_REF = 8;
    public final static int INFOBOX = 16;
    public final static int REFERENCE = 32;
    public final static int CATEGORY = 64;
    public final static int GEOBOX = 128;

    public final static int TITLE_WEIGHT = 1000;
    public final static int BODY_WEIGHT = 2;
    public final static int EXTERNAL_REF_WEIGHT = 1;
    public final static int INFOBOX_WEIGHT = 25;
    public final static int REFERENCE_WEIGHT = 1;
    public final static int CATEGORY_WEIGHT = 20;
    public final static int GEOBOX_WEIGHT = 1;

    public final static int MAX_DOC_SIZE = 5000;
    public final static int MAX_TITLE_SIZE = 10000;
    public final static int K = 30;

    public static StopWords stopWords = null;
    public static Stemmer stem = null;
    public static PageRank pageRank = null;
    // intermediate result
    public static String fileDirec = "/Users/goddamnchen/Desktop/CS6200/Project/wiki-search/intermediate/";
    // final result after merging
    public static String indexDirec = "/Users/goddamnchen/Desktop/CS6200/Project/wiki-search/index/";
}
