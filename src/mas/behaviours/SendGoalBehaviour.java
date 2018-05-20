package mas.behaviours;

import jade.core.AID;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import mas.agents.Agent;

import java.io.IOException;
import java.util.List;

public class SendGoalBehaviour extends ABehaviour {

    public SendGoalBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setSender(this.agent.getAID());
        msg.addReceiver((AID) this.getDataStore().get("aid_for_goal"));

        // compute new goal
        this.agent.getStrategy().computeNewGoal();
        List<String> plan = this.agent.getCurrentPlan();

        try {
            DataStore ds = new DataStore();
            ds.put("position", this.agent.getCurrentPosition());
            ds.put("current_plan", plan);
            ds.put("tanker_position", this.getDataStore().get("tanker_position"));
            ds.put("tanker_position_date", this.getDataStore().get("tanker_position_date"));
            ds.put("avoiding_conflict", this.getDataStore().get("avoiding_conflict"));
            msg.setContentObject(ds);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.agent.log("Sending new goal to " + ((AID)this.getDataStore().get("aid_for_goal")).getLocalName());
        this.agent.sendMessage(msg);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return Agent.A_RCV_GOAL;
    }
}
