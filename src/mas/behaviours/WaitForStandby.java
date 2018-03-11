package mas.behaviours;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.agents.ExplorationAgent;

import java.util.List;

public class WaitForStandby extends SimpleBehaviour {

	private static final long serialVersionUID = -8324034246585574116L;
	private int transitionId = 0;

	public WaitForStandby(ExplorationAgent explorationAgent) {
		super(explorationAgent);
	}

	@Override
	public void action() {
		ExplorationAgent agent = (ExplorationAgent)this.myAgent;
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		final ACLMessage msg = this.myAgent.receive(mt);
		
		if(msg != null) {
			agent.log("WaitForStandby: Ack received.");
			// add ack sender to recipients for SendData behaviour
			((List<AID>)this.getDataStore().get("recipients_for_sharing")).add(msg.getSender());
			// Go to SendData
			this.transitionId = 1;
		} else {
			block(500);
			// No message received in the interval
			agent.log("WaitForStandby: No ack received, waiting for 500millis.");
			this.transitionId = 2;
		}
	}

	@Override
	public boolean done() {
		return this.transitionId != 0;
	}

	public int onEnd() {
		return this.transitionId;
	}

}
