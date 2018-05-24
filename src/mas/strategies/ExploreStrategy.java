package mas.strategies;

import mas.agents.Agent;

import java.util.List;

public class ExploreStrategy extends Strategy {

    public ExploreStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public void computeNewGoal() {
        if (this.agent.getOpenedNodes().isEmpty()) {
            this.agent.setNewDestination(null);
            return;
        }
        this.agent.log("Opened nodes: " + this.agent.getOpenedNodes());
        String position = this.agent.getCurrentPosition();
        this.agent.dijkstra.computeShortestPaths(position);

        int minLength = Integer.MAX_VALUE;
        List<String> bestPlan = null;
        // find closest opened node.
        for(String node : this.agent.getOpenedNodes()){
            List<String> plan = this.agent.dijkstra.getPath(position, node);
            if(plan.size() < minLength) {
                bestPlan = plan;
                minLength = plan.size();
            }
        }
        this.agent.setPlan(bestPlan);
    }
}
