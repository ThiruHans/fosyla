package mas.behaviours;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import mas.agents.Agent;

public class RequestStandbyBehaviour extends ABehaviour {

    public RequestStandbyBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    public void action() {
        String position = this.agent.getCurrentPosition();
        this.agent.log("Requesting standby from nearby agents");

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setSender(this.agent.getAID());

        DFAgentDescription dfd = new DFAgentDescription();
        DFAgentDescription[] results;
        try {
            results = DFService.search(this.agent, dfd);
            if (results.length > 0) {
                for (DFAgentDescription d : results) {
                    if (d.getName().equals(this.agent.getAID())) continue;
                    msg.addReceiver(d.getName());
                }
            }
        } catch (FIPAException e) {
            e.printStackTrace();
            return;
        }

        msg.setContent(position);
        this.agent.sendMessage(msg);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return Agent.A_WAIT_FOR_STANDBY;
    }
}
