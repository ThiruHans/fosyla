package mas.behaviours;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.agents.AgentP;

import java.util.List;

public class WaitForStandby extends SimpleBehaviour {

	private static final long serialVersionUID = -8324034246585574116L;
	private int transitionId = 0;
	private int attempts = 0;

	public static final int T_SEND_DATA = 10;
	public static final int T_CHECK_VOICEMAIL = 11;

	public WaitForStandby(AgentP agentP) {
		super(agentP);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void action() {
		attempts += 1;
		AgentP agent = (AgentP) this.myAgent;
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		final ACLMessage msg = this.myAgent.receive(mt);
		
		if(msg != null) {
			agent.log("WaitForStandby: Ack received.");
			// add ack sender to recipients for SendData behavior
			((List<AID>)this.getDataStore().get("recipients_for_sharing")).add(msg.getSender());
			// Go to SendData
			this.transitionId = T_SEND_DATA;
			attempts += 1;
		} else {
			block(500);
			// No message received in the interval
			agent.log("WaitForStandby: No ack received, waiting for 500millis.");
			this.transitionId = T_CHECK_VOICEMAIL;
		}
	}

	@Override
	public boolean done() {
		return attempts > 1;
	}

	public int onEnd() {
		attempts = 0;
		return this.transitionId;
	}

}
