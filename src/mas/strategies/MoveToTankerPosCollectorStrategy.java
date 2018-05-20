package mas.strategies;

import mas.agents.CollectorAgent;

public class MoveToTankerPosCollectorStrategy extends Strategy {

    public MoveToTankerPosCollectorStrategy(CollectorAgent collectorAgent) {
        super(collectorAgent);
    }

    @Override
    public void computeNewGoal() {
        String tankerPos = (String)this.agent.getDataStore().get("tanker_position");
        this.agent.setNewDestination(tankerPos);
    }
}
