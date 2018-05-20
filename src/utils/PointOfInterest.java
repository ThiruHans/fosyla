package utils;

import env.Attribute;
import env.Couple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PointOfInterest implements Serializable {

    private String node;
    private List<Couple<String, Integer>> attributes;
    private Date date;

    public PointOfInterest(String node, List<Attribute> attributes, Date date) {
        this.node = node;
        this.date = date;
        this.attributes = new ArrayList<>();
        this.updateAttributes(attributes);
    }

    public void update(List<Attribute> attributes, Date date) {
        this.attributes.clear();
        this.updateAttributes(attributes);
        this.date = date;
    }

    public void update(PointOfInterest p) {
        this.attributes = p.getAttributes();
        this.date = p.getDate();
    }

    public String getNode() {
        return this.node;
    }

    public List<Couple<String, Integer>> getAttributes() {
        return attributes;
    }

    public Date getDate() {
        return date;
    }

    private void updateAttributes(List<Attribute> attributes) {
        for (Attribute attr : attributes) {
            this.attributes.add(
                    new Couple<>(attr.getName(), (int)attr.getValue()));
        }
    }

    public boolean equals(PointOfInterest other) {
        return this.getNode().equals(other.getNode());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(this.node);
        builder.append("::");
        for (int i = 0; i < this.attributes.size(); i++) {
            Couple<String, Integer> attr = this.attributes.get(i);
            builder.append(attr.getLeft());
            builder.append(":");
            builder.append(attr.getRight());
            if (i != this.attributes.size()-1) builder.append(", ");
        }
        builder.append("|");
        builder.append(this.date.getTime());
        builder.append(")");
        return builder.toString();
    }

    public void markObsolete(Date date) {
        this.attributes.clear();
        this.date = date;
    }
}
