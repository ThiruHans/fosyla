package mas.behaviours;

import mas.agents.Agent;

import java.util.List;

public class AvoidConflictBehaviour extends ABehaviour {

    private boolean finished;

    public AvoidConflictBehaviour(Agent agent) {
        super(agent);
        this.finished = false;
    }

    @Override
    public void action() {
        String position = this.agent.getCurrentPosition();
        this.agent.updateMap();

        String nextNode;
        if (position.equals(this.agent.getDestination()) || this.agent.getCurrentPlan() == null) {
            // Wait for one tick and move on.
//            this.finished = true;
            this.agent.log("Arrived at escape node. Resuming normal activities");
            this.getDataStore().put("avoiding_conflict", false);
            this.agent.delayCheckVoiceMail(Agent.TIME_STEP*3);
        } else {
            // follow current plan
            List<String> plan = this.agent.getCurrentPlan();
            plan.remove(position);
            nextNode = plan.get(plan.size()-1);
            this.agent.log("Next node is : " + nextNode);
            // Move to the picked destination.
            this.agent.move(nextNode);
        }

        block(Agent.TIME_STEP);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        this.finished = false;
        return Agent.A_CHECK_VOICEMAIL;
    }
}
