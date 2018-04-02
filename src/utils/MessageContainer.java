package utils;

import java.io.Serializable;
import java.util.List;

public class MessageContainer implements Serializable {

    private MapDataContainer mapDataContainer;
    private List<String> currentPath;

    public MessageContainer(MapDataContainer map, List<String> path) {
        this.mapDataContainer = map;
        this.currentPath = path;
    }

    public MapDataContainer getMap() {
        return this.mapDataContainer;
    }

    public List<String> getCurrentPath() {
        return this.currentPath;
    }
}
