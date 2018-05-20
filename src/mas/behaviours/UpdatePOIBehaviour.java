package mas.behaviours;

import mas.agents.Agent;

import java.util.List;

public class UpdatePOIBehaviour extends ABehaviour {

    private int numberOfUpdates;

    public UpdatePOIBehaviour(Agent agent) {
        super(agent);
        this.numberOfUpdates = 0;
    }

    // TODO:
    // - Pb si un seul POI
    // - circuit fermé
    @Override
    public void action() {

        String position = this.agent.getCurrentPosition();
        this.agent.updateMap();

        String nextNode;
        if (position.equals(this.agent.getDestination()) || this.agent.getCurrentPlan() == null) {
            if (this.agent.getPoints().size() == 1) {
                this.getDataStore().put("walk_to_random", true);
                this.getDataStore().put("walk_to_random_max_steps", 1);
                block(Agent.TIME_STEP);
                return;
            }
            // compute new destination and set new plan
            this.agent.getStrategy().computeNewGoal();
            List<String> plan = this.agent.getCurrentPlan();
            nextNode = plan.get(plan.size()-1);
            this.numberOfUpdates++;
        } else {
            // follow current plan
            List<String> plan = this.agent.getCurrentPlan();
            plan.remove(position);
            nextNode = plan.get(plan.size()-1);
        }
        this.agent.log("Next node is : " + nextNode);
        block(Agent.TIME_STEP);

        if (this.numberOfUpdates == this.agent.getPoints().size()) {
            this.getDataStore().put("walk_to_random", true);
            this.getDataStore().put("walk_to_random_max_steps", 1);
        }

        // Move to the picked destination.
        this.agent.move(nextNode);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return Agent.A_CHECK_VOICEMAIL;
    }
}
