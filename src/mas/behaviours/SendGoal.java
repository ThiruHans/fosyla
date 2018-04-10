package mas.behaviours;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.agents.ExplorationAgent;
import utils.MessageContainer;

import java.io.IOException;
import java.util.List;

public class SendGoal extends SimpleBehaviour {

    public static final int T_RCV_GOAL = 10;

    public SendGoal(ExplorationAgent explorationAgent) {
        super(explorationAgent);
    }

    @Override
    public void action() {
        ExplorationAgent agent = (ExplorationAgent)this.myAgent;
        ACLMessage dataMessage = new ACLMessage(ACLMessage.PROPOSE);
        dataMessage.setSender(agent.getAID());
        dataMessage.addReceiver((AID)this.getDataStore().get("aid_for_goal"));

        // compute new goal
        agent.computePlan(agent.getCurrentPosition());
        List<String> plan = agent.getPlan();

        try {

            MessageContainer messageContainer = new MessageContainer();
            messageContainer.setAid(agent.getAID());
            messageContainer.setPosition(agent.getCurrentPosition());
            messageContainer.setCurrentPath(plan);

            dataMessage.setContentObject(messageContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        agent.log("Sending new goal");
        agent.sendMessage(dataMessage);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return T_RCV_GOAL;
    }
}
