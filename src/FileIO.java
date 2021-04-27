import java.io.BufferedWriter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class FileIO {

    private String localFile = "";

    public FileIO(String fileName) {
        this.localFile = fileName;
    }

    /*
     * Write writeIdTitleMapping
     */
    public static void writeId2Title(TreeMap<Long, String> idTitleMapping, int title_file) {
        try {
            File file = new File(Settings.indexDirec + "IdTitle" + title_file + ".txt");

            if (!file.exists())
                file.createNewFile();

            java.io.FileWriter fw = new java.io.FileWriter(file.getAbsoluteFile());
            BufferedWriter writer = new BufferedWriter(fw);

            // serialize the TreeMap of Indexer class
            for (Map.Entry<Long, String> e : idTitleMapping.entrySet()) {
                writer.write(Long.toString(e.getKey()) + ":" + e.getValue() + "\n");
            }
            writer.close();
        } catch (Exception e) {
            // do something
            e.printStackTrace();
        }
    }

    /*
     * Serialize posting list before writing
     */
    public static String serialize(HashMap<Long, Record> record) {
        StringBuilder res = new StringBuilder();
        for (Map.Entry<Long, Record> e : record.entrySet()) {
            Record r = e.getValue();
            res.append(r.getID());
            res.append("-");
            if (r.get("B") > 0) res.append("B" + r.get("B"));
            if (r.get("C") > 0) res.append("C" + r.get("C"));
            if (r.get("E") > 0) res.append("E" + r.get("E"));
            if (r.get("G") > 0) res.append("G" + r.get("G"));
            if (r.get("I") > 0) res.append("I" + r.get("I"));
            if (r.get("R") > 0) res.append("R" + r.get("R"));
            if (r.get("T") > 0) res.append("T" + r.get("T"));
            res.append(";");
        }
        return res.toString();
    }

    /*
     * Function to write index file of each document in a dump / temporary file
     */
    public void write() {

        try {
            File file = new File(localFile);
            System.out.println("Flushing partial index file to : " + localFile);
            if (!file.exists())
                file.createNewFile();

            java.io.FileWriter fw = new java.io.FileWriter(file.getAbsoluteFile());
            BufferedWriter writer = new BufferedWriter(fw);

            // serialize the TreeMap of Indexer class
            for (Map.Entry<String, HashMap<Long, Record>> e : Index.invertedIndex.entrySet()) {
                writer.write(e.getKey() + ":" + FileIO.serialize(e.getValue()) + "\n");
            }
            writer.close();
        } catch (Exception e) {
            // do something
            e.printStackTrace();
        }
    }

}
