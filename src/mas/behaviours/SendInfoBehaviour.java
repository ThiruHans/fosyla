package mas.behaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import mas.agents.ExploAgent;
import mas.agents.MessageContainer;

public class SendInfoBehaviour extends TickerBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public SendInfoBehaviour (final Agent myagent) {
		super(myagent, 500);
		//super(myagent);
	}

	@Override
	public void onTick() {
//		if (!((ExploAgent)this.myAgent).getHasMetSomeone()) return;
		
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		ACLMessage msg=new ACLMessage(7);
		msg.setSender(this.myAgent.getAID());

		if (myPosition != ""){
			System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			
			ExploAgent agent = ((ExploAgent)this.myAgent);
			MessageContainer mc = new MessageContainer(agent.getMap(), agent.getOpenedNodes(), agent.getExploredNodes());
			try {
				msg.setContentObject(mc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!myAgent.getLocalName().equals("Agent1")){
				msg.addReceiver(new AID("Agent1",AID.ISLOCALNAME));
			}else{
				msg.addReceiver(new AID("Agent2",AID.ISLOCALNAME));
			}

			((mas.abstractAgent)this.myAgent).sendMessage(msg);

		}

	}

}