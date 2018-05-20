package mas.behaviours;

import mas.agents.Agent;

import java.util.List;

public class WaitForCollectorsBehaviour extends ABehaviour {

    public WaitForCollectorsBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    public void action() {
        String position = this.agent.getCurrentPosition();

        if (!position.equals(this.getDataStore().get("tanker_position"))) {
            this.agent.updateMap();
            this.agent.getStrategy().computeNewGoal();

            String nextNode = this.agent.getCurrentPlan().get(
                    this.agent.getCurrentPlan().size()-1);
            this.agent.move(nextNode);
            block(Agent.TIME_STEP);
        } else {
            this.agent.log("Waiting for collectors");
            block(Agent.WAIT_TIME);
        }
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return Agent.A_CHECK_VOICEMAIL;
    }
}
