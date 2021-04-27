import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

public class Query {
    private static int port = 9000;
    private static long N = 0;  // doc_count
    private static ArrayList<TitleIndex> titleSecIndx = null;

    public static void main(String args[]) {

        // load stop words and init stemmer
        Settings.stopWords = new StopWords();
        Settings.stem = new Stemmer();
        // init pagerank
        System.out.print("Loading page rank into memory ...");
        Settings.pageRank = new PageRank("/Users/goddamnchen/Desktop/CS6200/Project/wiki-search/pageranks.csv");
        System.out.println("ok");
        try {
            // load indexed document count
            BufferedReader dcReader = new BufferedReader(new FileReader(Settings.indexDirec + "DocCount.txt"));
            Query.N = Long.parseLong(dcReader.readLine());
            dcReader.close();

            // load the tertiary term position index
            // & secondary id-title position index into memory
            titleSecIndx = loadIdTitleSecIndex();
            ArrayList<TermIndex> termTerIndxList = loadTermTertiaryIndex();

            System.out.print("Starting a Http client... ");
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            // Http root page handler
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    String response = "<h1>Wiki Search - CS6200 Project Spring21</h1>" +
                            "<form method=\"POST\" enctype=\"text/plain\"  action=\"result\">\n" +
                            " <input type=\"text\" name=\"qtext\" size=\"50\">" +
                            " <input type=\"submit\" value=\"Submit\"></form>" +
                            "<pr> Note: Specify a keyword/phrase query to find wiki pages! </pr> <br>" +
                            "<pr> Author: Guanting Chen </pr>";
                    httpExchange.sendResponseHeaders(200, response.length());
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            // Http result handler
            server.createContext("/result", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    Headers h = httpExchange.getResponseHeaders();
                    h.add("Content-Type", "text/html");
                    httpExchange.sendResponseHeaders(200, 0);
                    InputStream is = httpExchange.getRequestBody();
                    Scanner scanner = new Scanner(is);
                    String input = scanner.useDelimiter("\\A").next();
                    String query = input.replace("qtext=", "");
                    query = query.replaceAll("!@#$%+^&;*'.><", "");
                    query = query.trim();
                    long startTime = System.currentTimeMillis();
                    Comparator<TermIndex> termComparator = new Comparator<TermIndex>() {
                        public int compare(TermIndex u1, TermIndex u2) {
                            return u1.term.compareTo(u2.term);
                        }
                    };
                    StringTokenizer tokenizer = new StringTokenizer(query, " ");
                    // candidate docs with scores
                    HashMap<String, Double> DocScore = new HashMap<String, Double>();
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken().toLowerCase();
                        // pre-process with stopword and stemming
                        if (Settings.stopWords.stopWords.contains(token)) {
                            continue;
                        }
                        Settings.stem.add(token.toCharArray(), token.length());
                        if (Settings.stem.stem(token) != null) {
                            token = Settings.stem.stem(token);
                        }

                        // binary search the tertiary/secondary term position index
                        // retrieve the secondary/primary position offsets
                        long secIndxOffset = search(termTerIndxList, token, termComparator);
                        long priIndxOffset = searchTermSecondaryIndex(secIndxOffset, token, termComparator);
                        // binary search the primary index and retrieve the postlist if exist
                        String postingList = searchTermPrimaryIndex(priIndxOffset, token);
                        if (postingList != null) {

                            DocScore = wordDocScoring(DocScore, postingList);
                        }
                    }

                    // retriving top-k
                    Set<Entry<String, Double>> set = DocScore.entrySet();
                    List<Entry<String, Double>> list = new ArrayList<>(set);
                    Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                        public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                            if (o2.getValue() > o1.getValue()) return 1;
                            else if (o2.getValue() < o1.getValue()) return -1;
                            else return 0;
                        }
                    });
                    String stats = "<h2>Statistics</h2>" +
                            "<pr> Indexed Document: " + Query.N + "</pr> <br>" +
                            "<pr> Query Latency: " + (System.currentTimeMillis() - startTime) + " ms</pr> <br>";
                    String header = "<h2>Query</h2>" + "<pr> " + query + "</pr>" +
                            "<h2>Top 30 Relevant Wikipages </h2>";
                    String result = "";
                    for (int i = 0; i < Settings.K && i < list.size(); i++) {
                        String wiki = list.get(i).getKey();
                        result += "<pr> &nbsp &nbsp[" + (i + 1) + "]" +
//                                "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" +
                                "<a href=https://en.wikipedia.org/wiki/" + wiki.replaceAll(" ", "_") + ">" + wiki + "</a></pr> <br>";
                    }
                    OutputStream os = httpExchange.getResponseBody();
                    is.close();
                    os.write((stats + header + result).getBytes());
                    os.flush();
                    os.close();
                    httpExchange.close();
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("localhost:" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<TermIndex> loadTermTertiaryIndex() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Settings.indexDirec + "TerIndex.txt"));
        ArrayList<TermIndex> indxList = new ArrayList<TermIndex>();
        String line = reader.readLine();
        while (line != null) {
            String w = line.substring(0, line.indexOf(":"));
            long o = Long.parseLong(line.substring(line.indexOf(":") + 1));
            TermIndex t = new TermIndex(w, o);
            indxList.add(t);
            line = reader.readLine();
        }
        reader.close();
        return indxList;
    }

    private static ArrayList<TitleIndex> loadIdTitleSecIndex() throws IOException {
        // read secondary id-title ampping into idTitle2ArrayList
        BufferedReader reader = new BufferedReader(new FileReader(Settings.indexDirec + "SecTitleIndex.txt"));
        ArrayList<TitleIndex> titleSecIndxList = new ArrayList<TitleIndex>();
        String line = reader.readLine();
        while (line != null) {
            long id = Long.parseLong(line.substring(0, line.indexOf(":")));
            long o = Long.parseLong(line.substring(line.indexOf(":") + 1));
            TitleIndex t = new TitleIndex(id, o);
            titleSecIndxList.add(t);
            line = reader.readLine();
        }
        reader.close();
        return titleSecIndxList;
    }

    private static long searchTermSecondaryIndex(long startIndex, String term, Comparator<TermIndex> termComp) throws IOException {
        RandomAccessFile file = new RandomAccessFile(Settings.indexDirec + "SecIndex.txt", "r");
        file.seek(startIndex);
        ArrayList<TermIndex> indxList = new ArrayList<TermIndex>();
        String line = file.readLine();
        for (int i = 1; i <= 200 && line != null; i++) {
            String w = line.substring(0, line.indexOf(":"));
            long o = Long.parseLong(line.substring(line.indexOf(":") + 1));
            TermIndex t = new TermIndex(w, o);
            indxList.add(t);
            line = file.readLine();

        }
        long offset = search(indxList, term, termComp);
        indxList.clear();
        file.close();
        return offset;
    }

    private static String searchTermPrimaryIndex(long startIndex, String term) throws IOException {
        RandomAccessFile file = new RandomAccessFile(Settings.indexDirec + "Index.txt", "r");
        file.seek(startIndex);
        String line = file.readLine();
        String postings = null;
        for (int i = 1; i <= 200 && line != null; i++) {
            String w = line.substring(0, line.indexOf(":"));
            if (w.equals(term)) {
                postings = line.substring(line.indexOf(":") + 1);
                break;
            }
            line = file.readLine();
        }
        file.close();
        return postings;
    }

    private static String docId2Title(Long id) throws Exception {
        /*
         * Search in primary title file and retrieving title for given ID = key
         */
        Comparator<TitleIndex> titleComparator = new Comparator<TitleIndex>() {
            public int compare(TitleIndex u1, TitleIndex u2) {
                return (int) ((int) u1.id - u2.id);
            }
        };
        Long offset = search(titleSecIndx, id, titleComparator);
        RandomAccessFile file = new RandomAccessFile(Settings.indexDirec + "IdTitle.txt", "r");
        file.seek(offset);
        String line = file.readLine();
        String title = "";
        for (int i = 1; i <= 200 && line != null; i++) {
            line = file.readLine();
            Long w = Long.parseLong(line.substring(0, line.indexOf(":")));
            if (w.longValue() == id.longValue()) {
                title = line.substring(line.indexOf(":") + 1);
                break;
            }
        }
        file.close();
        return title;
    }

    private static long search(ArrayList<TitleIndex> list, Long tok, Comparator<TitleIndex> comp) {
        long tokIndex = Collections.binarySearch(list, new TitleIndex(tok, 0), comp);
        long startIndex;
        if (tokIndex < 0) {
            tokIndex *= -1;
            if (tokIndex > 2)
                startIndex = list.get((int) (tokIndex - 2)).offset;
            else
                startIndex = 0;
        } else if (tokIndex > 3)
            startIndex = list.get((int) (tokIndex - 3)).offset;
        else
            startIndex = 0;
        return startIndex;
    }

    private static long search(ArrayList<TermIndex> list, String tok, Comparator<TermIndex> comp) {
        long tokIndex = Collections.binarySearch(list, new TermIndex(tok, 0), comp);
        long startIndex;
        if (tokIndex < 0) {
            tokIndex *= -1;
            if (tokIndex > 2)
                startIndex = list.get((int) (tokIndex - 2)).offset;
            else
                startIndex = 0;
        } else if (tokIndex > 3)
            startIndex = list.get((int) (tokIndex - 3)).offset;
        else
            startIndex = 0;
        return startIndex;
    }

    private static HashMap<String, Double> wordDocScoring(HashMap<String, Double> DocScore, String postingList) {
        if (postingList == null || postingList.length() == 0) return DocScore;
        StringTokenizer tokens = new StringTokenizer(postingList, ";");

        while (tokens.hasMoreTokens()) {
            String r = tokens.nextToken();

            long tf = 0;
            String title = "";
            Long ID = Long.parseLong(r.split("-")[0]);        // docID
            try {
                title = docId2Title(ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            double pr = Double.parseDouble(Settings.pageRank.pr.getOrDefault(title, "0.0"));

            r = r.split("-")[1];        // term Freq in Fields
            // parse term frequency by field
            int bFreq = 0, cFreq = 0, eFreq = 0, gFreq = 0, iFreq = 0, tFreq = 0, rFreq = 0;
            int j = 0;
            while (j < r.length() && r.length() < 500) {
                char field = r.charAt(j);
                j++;
                if (field == 'B') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        bFreq = bFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                } else if (field == 'C') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        cFreq = cFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                } else if (field == 'E') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        eFreq = eFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                } else if (field == 'G') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        gFreq = gFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                } else if (field == 'I') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        iFreq = iFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                } else if (field == 'R') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        rFreq = rFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                } else if (field == 'T') {
                    while (j < r.length() && r.charAt(j) >= '0' && r.charAt(j) <= '9') {
                        tFreq = tFreq * 10 + (r.charAt(j) - '0');
                        j++;
                    }
                }

            }
            tf = (bFreq * Settings.BODY_WEIGHT) + (cFreq * Settings.CATEGORY_WEIGHT) + (iFreq * Settings.INFOBOX_WEIGHT) +
                    (eFreq * Settings.EXTERNAL_REF_WEIGHT) + (gFreq * Settings.GEOBOX_WEIGHT) +
                    (tFreq * Settings.TITLE_WEIGHT) + (rFreq * Settings.REFERENCE_WEIGHT);
            tf = (long) Math.log10(tf);
            tf *= Math.log10(Query.N / postingList.split(";").length);
            double score = (tf != 0) ? 0.7 * tf + 0.3 * pr : 0.7 * tf;
            DocScore.put(title, DocScore.getOrDefault(ID, (double) 0) + score);
        }
        return DocScore;
    }
}


class TermIndex {
    String term;
    long offset;

    TermIndex(String term, long offset) {
        this.offset = offset;
        this.term = term;
    }
}

class TitleIndex {
    long id;
    long offset;

    TitleIndex(long id, long o) {
        this.offset = o;
        this.id = id;
    }

}

