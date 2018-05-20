package mas.strategies;

import mas.agents.Agent;
import mas.agents.CollectorAgent;
import utils.PointOfInterest;

import java.util.Date;

public class CollectionStrategy extends Strategy {

    public CollectionStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public void computeNewGoal() {
        // find relevant poi
        PointOfInterest p = ((CollectorAgent) this.agent).findRelevantPoint();
        String nextNode = p.getNode();
        // set as new destination
        this.agent.setNewDestination(nextNode);
    }
}
