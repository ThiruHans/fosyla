package mas.behaviours;

import mas.agents.Agent;

import java.util.List;

public class MoveToTankerBehaviour extends ABehaviour{

    public MoveToTankerBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    public void action() {
        String position = this.agent.getCurrentPosition();
        this.agent.updateMap();

        if (this.getDataStore().get("tanker_position") == null) {
            System.out.println("==============================================================================================================");
            return;
        }

        String nextNode;
        if (position.equals(this.agent.getDestination()) || this.agent.getCurrentPlan() == null) {
            // compute new destination and set new plan
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
        return Agent.A_CHECK_VOICEMAIL;
    }
}
