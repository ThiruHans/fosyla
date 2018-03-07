package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.agents.ExploAgent;

public class WaitForStandby extends SimpleBehaviour {

	private static final long serialVersionUID = -8324034246585574116L;
	private int transitionId = 0;

	@Override
	public void action() {
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		final ACLMessage msg = this.myAgent.receive(mt);
		
		if(msg != null) {
			// add ack sender to recipients for SendData behaviour
			((ExploAgent)this.myAgent).getRecipients().add(msg.getSender());
			
			this.transitionId = 1;
		} else {
			block(500);
			this.transitionId = 2;
		}
	}

	@Override
	public boolean done() {
		return this.transitionId != 0;
	}

}
