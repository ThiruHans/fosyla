package mas.agents;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class MessageContainer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7027481640243066857L;
	
	private HashMap<String, HashSet<String>> map;
	private List<String> openedNodes;
	private HashMap<String, String> exploredNodes;

	
	public MessageContainer(HashMap<String, HashSet<String>> map, 
			List<String> openedNodes, HashMap<String, String> exploredNodes) {
		this.map = map;
		this.openedNodes = openedNodes;
		this.exploredNodes = exploredNodes;
	}


	public HashMap<String, HashSet<String>> getMap() {
		return map;
	}


	public List<String> getOpenedNodes() {
		return openedNodes;
	}


	public HashMap<String, String> getExploredNodes() {
		return exploredNodes;
	}

	
}
