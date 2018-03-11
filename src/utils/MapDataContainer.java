package utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class MapDataContainer implements Serializable {
	private static final long serialVersionUID = 7027481640243066857L;
	
	private HashMap<String, HashSet<String>> map;
	private List<String> openedNodes;

	
	public MapDataContainer(HashMap<String, HashSet<String>> map,
							List<String> openedNodes) {
		this.map = map;
		this.openedNodes = openedNodes;
	}


	public HashMap<String, HashSet<String>> getMap() {
		return map;
	}
	public List<String> getOpenedNodes() {
		return openedNodes;
	}
}
