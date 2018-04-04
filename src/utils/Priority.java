package utils;

import mas.agents.ExplorationAgent;

import java.util.List;

public class Priority {

    public static final int HIGH = 1;
    public static final int LOW = -1;

    public static int compute(ExplorationAgent agent, MessageContainer mc) {
        List<String> ownPath = agent.getPlan();
        List<String> otherPath = mc.getCurrentPath();

        if (ownPath.size() < otherPath.size()) return HIGH;
        if (ownPath.size() > otherPath.size()) return LOW;

        if (agent.getAID().compareTo(mc.getAID()) > 0) return HIGH;
        else return LOW;
    }
}
