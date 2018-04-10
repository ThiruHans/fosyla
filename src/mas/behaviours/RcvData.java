package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mas.agents.ExplorationAgent;
import utils.MapDataContainer;
import utils.MessageContainer;

public class RcvData extends SimpleBehaviour {
	private static final long serialVersionUID = -1268869791618955047L;
	private int attempts;
	private int transitionId;

	public static final int T_SEND_GOAL = 10;
	public static final int T_CHECK_VOICEMAIL = 11;

	public RcvData(ExplorationAgent explorationAgent) {
		super(explorationAgent);
		this.attempts = 0;
		this.transitionId = T_CHECK_VOICEMAIL;
	}

	@Override
	public void action() {
		attempts += 1;
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msg = this.myAgent.receive(mt);
		ExplorationAgent agent = ((ExplorationAgent)this.myAgent);
		
	 	if (msg != null) {
	 		agent.log("Data received, processing...");
	 		MessageContainer mc;
			try {
				mc = (MessageContainer) msg.getContentObject();
				MapDataContainer mapDataContainer = mc.getMap();
				HashMap<String, HashSet<String>> otherMap = mapDataContainer.getMap();
				HashMap<String, HashSet<String>> map = agent.getMap();
				List<String> openedNodes = agent.getOpenedNodes();

//				agent.log("++++++++++++++++ map before");
//				agent.log(map.toString());
//				agent.log(otherMap.toString());
				for(String e : otherMap.keySet()) {
					if (map.containsKey(e)) map.get(e).addAll(otherMap.get(e));
					else map.put(e, otherMap.get(e));
					openedNodes.remove(e);
				}
//				agent.log("map after");
//				agent.log(map.toString());
//				agent.log("++++++++++++++++");

				this.getDataStore().put("aid_for_goal", mc.getAID());
				this.transitionId = T_SEND_GOAL;
			attempts += 1;
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
	 	} else {
	 		block(500);
			agent.log("No data received yet, waiting for 500ms.");
	 	}
	}

	@Override
	public boolean done() {
		return attempts > 1;
	}

	public int onEnd() {
		this.attempts = 0;
		return this.transitionId;
	}

}
