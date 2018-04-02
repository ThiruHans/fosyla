package graph;

import java.util.*;

public class Dijkstra {

    private Map<String, HashSet<String>> map;
    private Map<String, Integer> distance;
    private Map<String, String> predecessors;
    private Set<String> openedNodes;
    private Set<String> closedNodes;

    public Dijkstra(HashMap<String, HashSet<String>> map) {
        this.map = map;
    }

    public void computeShortestPaths(String source) {

        openedNodes = new HashSet<>();
        closedNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();

        distance.put(source, 0);
        openedNodes.add(source);

        while (openedNodes.size() > 0) {
            String node = getMinimumNode(openedNodes);
            closedNodes.add(node);
            openedNodes.remove(node);
            findMinimalDistance(node);
        }
    }

    private String getMinimumNode(Set<String> nodes) {
        String minNode = null;
        for (String node : nodes) {
            if (minNode == null) minNode = node;
            else {
                int minDistance = distance.get(minNode);
                int nodeDistance = distance.getOrDefault(node, Integer.MAX_VALUE-1);
                if (minDistance > nodeDistance) minNode = node;
            }
        }
        return minNode;
    }

    private void findMinimalDistance(String node) {
        Set<String> neighbors = map.get(node);
        for (String neighbor : neighbors) {
            if (closedNodes.contains(neighbor)) continue;

            int minNodeDistance = distance.getOrDefault(node, Integer.MAX_VALUE-1);
            int minNeighborDistance = distance.getOrDefault(neighbor, Integer.MAX_VALUE-1);
            if (minNeighborDistance > minNodeDistance + 1) {
                distance.put(neighbor, minNodeDistance + 1);
                predecessors.put(neighbor, node);
                openedNodes.add(neighbor);
            }

        }
    }

    public List<String> getPath(String source, String dest) {
        if (predecessors.get(dest) == null) return null;

        String step = dest;
        List<String> path = new ArrayList<>();
        path.add(step);
        while(!(step = predecessors.get(step)).equals(source)) {
            path.add(step);
        }

//        Collections.reverse(path);
        return path;
    }
}
