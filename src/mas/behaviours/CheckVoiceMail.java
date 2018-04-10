package mas.behaviours;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.agents.ExplorationAgent;

import java.util.List;

public class CheckVoiceMail extends SimpleBehaviour {

	private static final long serialVersionUID = 8991919761504652388L;
	private int transitionId = 0;
	private ExplorationAgent explorationAgent;

	public static final int T_REQUEST_STANDBY = 10;
	public static final int T_SEND_DATA = 11;

	public CheckVoiceMail(ExplorationAgent explorationAgent) {
		super(explorationAgent);
		this.explorationAgent = explorationAgent;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void action() {
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		final ACLMessage msg = this.explorationAgent.receive(mt);

		if (msg != null) {
			// Add sender to agent recipients to share data in SendData behaviour.
			List<AID> recipientsList = (List<AID>)this.getDataStore().get("recipients_for_sharing");
			recipientsList.add(msg.getSender());

			explorationAgent.log("Message received in voice mail from: "+ msg.getSender());
			// Send acknowledgement to sender
			ACLMessage ack = new ACLMessage(ACLMessage.CONFIRM);
			ack.addReceiver(msg.getSender());
			ack.setSender(explorationAgent.getAID());
			explorationAgent.sendMessage(ack);

			// Switch to SendData behaviour
			this.transitionId = T_SEND_DATA;
		} else {
			// If no message was received and the agent checked the voicemail because of collision:
			// RequestStandby to nearby agents.
			Boolean blockedState = (boolean)this.getDataStore().get("exploration_blocked_notification");
			if (blockedState) {
				explorationAgent.log("No message was received but the agent was blocked.");
				this.transitionId = T_REQUEST_STANDBY;
				this.getDataStore().put("exploration_blocked_notification", false);
				return;
			}
			// Otherwise, just go back to previous movement behaviour
			this.transitionId = (int) this.getDataStore().get("movement_behaviour");
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
