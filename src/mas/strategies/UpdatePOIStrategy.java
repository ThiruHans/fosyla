package mas.strategies;

import mas.agents.Agent;
import utils.PointOfInterest;

import java.util.Date;

public class UpdatePOIStrategy extends Strategy {

    public UpdatePOIStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public void computeNewGoal() {
        // find relevant poi
        String nextNode = null;
        Date minDate = new Date();
        for (PointOfInterest p : this.agent.getPoints()) {
            if (p.getDate().compareTo(minDate) < 0) {
                nextNode = p.getNode();
                minDate = p.getDate();
            }
        }
        // set as new destination
        assert(nextNode != null);
        this.agent.setNewDestination(nextNode);
    }
}
