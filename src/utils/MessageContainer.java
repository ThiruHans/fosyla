package utils;

import jade.core.AID;

import java.io.Serializable;
import java.util.List;

public class MessageContainer implements Serializable {

    private MapDataContainer mapDataContainer;
    private List<String> currentPath;
    private AID aid;
    private String position;

    public MapDataContainer getMap() {
        return this.mapDataContainer;
    }

    public List<String> getCurrentPath() {
        return this.currentPath;
    }

    public AID getAID() {
        return this.aid;
    }

    public void setMapDataContainer(MapDataContainer mapDataContainer) {
        this.mapDataContainer = mapDataContainer;
    }

    public void setCurrentPath(List<String> currentPath) {
        this.currentPath = currentPath;
    }

    public void setAid(AID aid) {
        this.aid = aid;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
