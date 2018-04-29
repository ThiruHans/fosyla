package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import env.Attribute;
import env.Couple;

public class PointOfInterest implements Serializable {

    private String node;
    private List<Couple<String,Integer>> attrs;
    private int lastUpdatedDate;

    public PointOfInterest(String node, List<Attribute> attrs, int date) {
        this.node = node;
        this.attrs = new ArrayList<>();
        for(Attribute attr:attrs){
        	Couple<String,Integer> c = new Couple<>(attr.getName(),(Integer)attr.getValue());
        	this.attrs.add(c);
        }
        this.lastUpdatedDate = date;
    }

    public List<Couple<String,Integer>> getAttrs() {
        return attrs;
    }

    public int getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    public String getNode() {
        return this.node;
    }
    
    public void log(String s){
    	System.out.print("["+ this.getNode() +" : "+ this.getAttrs() +"|"+this.lastUpdatedDate+" ]" + s);
    }

    public void update(List<Attribute> attrs, int date) {
    	this.attrs.clear();
    	for(Attribute attr:attrs){
        	Couple<String,Integer> c = new Couple<>(attr.getName(),(Integer)attr.getValue());
        	this.attrs.add(c);
        }
    	this.lastUpdatedDate = date;
    }
    
    public String toString() {
    	return "["+this.node+":"+this.attrs+"|"+this.lastUpdatedDate+"]";
    }
}
