package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.agents.ExplorationAgent;

public class RequestStandby extends SimpleBehaviour {

	private static final long serialVersionUID = -5079787101111370695L;
	public static final int T_WAIT_FOR_STANDBY = 10;

	public RequestStandby(ExplorationAgent agent) {
		super(agent);
	}

	@Override
	public void action() {
		ExplorationAgent agent = (ExplorationAgent)this.myAgent;
		String myPosition = agent.getCurrentPosition();
		agent.log("Requesting standby from nearby agents");
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(this.myAgent.getAID());
		
		// get potential receivers
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd  = new ServiceDescription();
//		sd.setType( "explorer" );
		dfd.addServices(sd);
		DFAgentDescription[] results;
		
		try {
			results = DFService.search(this.myAgent, dfd);
			if (results.length>0) {
				for(DFAgentDescription d : results) {
					if(d.getName().equals(agent.getAID())) continue;
					msg.addReceiver(d.getName());
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
			return;
		}
		
		msg.setContent(myPosition);
		agent.sendMessage(msg);
	}

	@Override
	public boolean done() {
		return true;
	}
	
	public int onEnd() {
		return T_WAIT_FOR_STANDBY;
	}

}
