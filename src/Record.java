import java.util.HashMap;

interface RecordInterface {
    void set(String s, int y);

    int get(String c);

    long getID();

    void setID(long x);

    long getWeight();

    void setWeight(long x);

}


public class Record implements RecordInterface {
    long ID;
    long weight = 0;
    private HashMap<String, Integer> values = new HashMap<String, Integer>();

    @Override
    public void set(String s, int y) {
        // TODO Auto-generated method stub
        this.values.put(s, this.values.getOrDefault(s, 0) + y);
    }

    @Override
    public int get(String s) {
        // TODO Auto-generated method stub
        return this.values.getOrDefault(s, 0);
    }

    @Override
    public long getID() {
        return this.ID;
    }

    @Override
    public void setID(long x) {
        this.ID = x;
    }

    @Override
    public long getWeight() {
        // TODO Auto-generated method stub
        return this.weight;
    }

    @Override
    public void setWeight(long x) {
        // TODO Auto-generated method stub
        this.weight = x;
    }

    /*
     * Updating occur count for different fields
     */
    public void updateFieldCount(int type) {
        if (type == Settings.TITLE)
            this.set("T", 1);

        else if (type == Settings.BODY)
            this.set("B", 1);

        else if (type == Settings.INFOBOX)
            this.set("I", 1);

        else if (type == Settings.REFERENCE)
            this.set("R", 1);

        else if (type == Settings.EXTERNAL_REF)
            this.set("E", 1);

        else if (type == Settings.CATEGORY)
            this.set("C", 1);

        else if (type == Settings.GEOBOX)
            this.set("G", 1);
    }
}