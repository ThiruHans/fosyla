package mas.strategies;

import mas.agents.Agent;

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
        String dest = null;
        for(String node : this.agent.getOpenedNodes()){
            dest=node;
            break;
        }
        this.agent.setNewDestination(dest);
    }
}
