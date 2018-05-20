package mas.behaviours;

import mas.agents.Agent;

import java.util.List;

public class WalkToRandomBehaviour extends ABehaviour {

    private int step;

    public WalkToRandomBehaviour(Agent agent) {
        super(agent);
        this.step = 0;
    }

    @Override
    public void action() {
        String position = this.agent.getCurrentPosition();
        this.agent.updateMap();

        String nextNode;
        if (position.equals(this.agent.getDestination()) || this.agent.getCurrentPlan() == null) {
            // compute new destination and set new plan
            this.agent.log("Walk to random: step=" + this.step
                    + ", max_steps="+this.getDataStore().get("walk_to_random_max_steps"));
            if (this.step >= (int)this.getDataStore().get("walk_to_random_max_steps")) {
                this.getDataStore().put("walk_to_random", false);
                this.getDataStore().put("walk_to_random_max_steps", 0);
                return;
            }
            this.step++;
            this.agent.getStrategy().computeNewGoal();
            List<String> plan = this.agent.getCurrentPlan();
            nextNode = plan.get(plan.size()-1);
        } else {
            // follow current plan
            List<String> plan = this.agent.getCurrentPlan();
            plan.remove(position);
            nextNode = plan.get(plan.size()-1);
        }

        this.agent.log("Next node is : " + nextNode);
        block(Agent.TIME_STEP);

        // Move to the picked destination.
        this.agent.move(nextNode);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        this.step = 0;
        return Agent.A_CHECK_VOICEMAIL;
    }
}
