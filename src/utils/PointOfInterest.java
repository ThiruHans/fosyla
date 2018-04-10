package utils;

import env.Attribute;

import java.io.Serializable;
import java.util.List;

public class PointOfInterest implements Serializable {

    private String node;
    private List<Attribute> attrs;
    private int lastUpdatedDate;

    public PointOfInterest(String node, List<Attribute> attrs, int date) {
        this.node = node;
        this.attrs = attrs;
        this.lastUpdatedDate = date;
    }

    public List<Attribute> getAttrs() {
        return attrs;
    }

    public int getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    public String getNode() {
        return this.node;
    }

}
