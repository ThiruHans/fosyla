package graph;

import java.util.*;

public class Dijkstra {

    private Map<String, List<String>> map;
    private Map<String, Integer> distance;
    private Map<String, String> predecessors;
    private Set<String> openedNodes;
    private Set<String> closedNodes;

    public Dijkstra(Map<String, List<String>> map) {
        this.map = map;
    }

    public List<String> computeShortestPaths(String source) {

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

        return null;
    }

    private String getMinimumNode(Set<String> nodes) {
        String minNode = null;
        for (String node : nodes) {
            if (minNode == null) minNode = node;
            else {
                int minDistance = distance.get(minNode);
                int nodeDistance = distance.getOrDefault(node, Integer.MAX_VALUE);
                if (minDistance > nodeDistance) minNode = node;
            }
        }
        return minNode;
    }

    private void findMinimalDistance(String node) {
        List<String> neighbors = map.get(node);
        for (String neighbor : neighbors) {
            if (closedNodes.contains(neighbor)) continue;

            int minNodeDistance = distance.getOrDefault(node, Integer.MAX_VALUE);
            int minNeighborDistance = distance.getOrDefault(neighbor, Integer.MAX_VALUE);
            if (minNeighborDistance + 1 < minNodeDistance) {
                distance.put(node, minNeighborDistance + 1);
                predecessors.put(node, neighbor);
                openedNodes.add(neighbor);
            }

        }
    }

    public List<String> getPath(String dest) {
        if (predecessors.get(dest) == null) return null;

        String step = dest;
        List<String> path = new ArrayList<>();
        path.add(step);
        while((step = predecessors.get(step)) != null) {
            path.add(step);
        }

        Collections.reverse(path);
        return path;
    }
}
