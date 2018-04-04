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
	private int attempts = 0;

	public RcvData(ExplorationAgent explorationAgent) {
		super(explorationAgent);
	}

	@Override
	public void action() {
		attempts += 1;
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msg = this.myAgent.receive(mt);
		ExplorationAgent agent = ((ExplorationAgent)this.myAgent);
		
	 	if (msg != null) {
	 		agent.log("Data received, combining...");
	 		MessageContainer mc;
			try {
				mc = (MessageContainer) msg.getContentObject();
				MapDataContainer mapDataContainer = mc.getMap();
				HashMap<String, HashSet<String>> otherMap = mapDataContainer.getMap();
				HashMap<String, HashSet<String>> map = agent.getMap();
				List<String> openedNodes = agent.getOpenedNodes();
				
				for(String e : otherMap.keySet()) {
					if(!map.containsKey(e)) map.put(e, otherMap.get(e));
					openedNodes.remove(e);
				}

				// Reset goal after receiving new data.
				agent.getPlan().clear();
				// If openedNodes is not empty: compute new plan.

				// TODO: need to recompute next goal because after combining openedNodes has changed...
				// detect conflict :
				String otherPosition = mc.getPosition();
				List<String> otherPath = mc.getCurrentPath();
				String ownPosition = agent.getCurrentPosition();
				List<String> ownPath = agent.getPlan();

				if (otherPath.get(otherPath.size()-1).equals(ownPosition) &&
						ownPath.get(ownPath.size()-1).equals(otherPosition)) {
					agent.log("/!\\ CONFLICT DETECTED");
				}
			attempts += 1;
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
	 	} else {
	 		block(500);
			agent.log("No data received yet, waiting for one second.");
	 	}
	}

	@Override
	public boolean done() {
		return attempts > 1;
	}

	public int onEnd() {
		return 1;
	}

}
