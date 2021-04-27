import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PageRank {
    public Map<String, String> pr = null;

    public PageRank(String path) {
        this.pr = getMapFromCSV(path);
    }

    public static Map<String, String> getMapFromCSV(String filePath) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            Stream<String> lines = Files.lines(Paths.get(filePath));
            resultMap =
                    lines.map(line -> line.split(","))
                            .collect(Collectors.toMap(line -> line[0].replaceAll("_", " "), line -> line[1]));

            lines.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
