package mas.strategies;

import mas.agents.Agent;

public class WalkToRandomStrategy extends Strategy {

    public WalkToRandomStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public void computeNewGoal() {
        // do nothing if destination is not reached
        String position = this.agent.getCurrentPosition();
        if(position.equals(this.agent.getDestination()) || this.agent.getCurrentPlan() == null) {
            // select new random node in map.
            String nextNode = position;
            while (nextNode.equals(position)) {
                int nextNodeIdx = this.agent.getRandom().nextInt(this.agent.getMap().size());
                int i = 0;
                for (String node : this.agent.getMap().keySet()) {
                    nextNode = node;
                    if (i == nextNodeIdx) break;
                    i++;
                }
            }

            this.agent.setNewDestination(nextNode);
        }
    }
}
