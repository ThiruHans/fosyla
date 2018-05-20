package mas.behaviours;

import mas.agents.Agent;

import java.util.HashSet;
import java.util.List;

public class ExploreBehaviour extends ABehaviour {

    public ExploreBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    public void action() {
        String position = this.agent.getCurrentPosition();
        HashSet<String> openedNodes = this.agent.getOpenedNodes();
        // On startup, position may not be defined right away...
        if (position.equals("")) return;

        this.agent.updateMap();

        if (openedNodes.isEmpty()) {
            // No more nodes to explore in map.
            this.agent.log("Exploration finished. Switching to agent's default function.");
            return;
        }

        String nextNode;
        if (position.equals(this.agent.getDestination()) || this.agent.getCurrentPlan() == null) {
            // compute new destination and set new plan
            this.agent.getStrategy(Agent.M_EXPLORE).computeNewGoal();
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
