package mas.agents;

import env.Environment;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import mas.abstractAgent;
import mas.behaviours.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ExplorationAgent extends abstractAgent {
	/**
	 * Exploration Agent:
	 * Explores the graph it is dropped into. If another agent is met, current knowledge of the map is shared
	 * and coordination for the remaining exploration is computed like so:
	 * - nodes scheduled to be explored and known by the two agents are distributed between them evenly.
	 * - the rest of the nodes scheduled for exploration are handled by the discoverer of said nodes.
	 */

	private static final long serialVersionUID = -1784844593772918359L;

	// Map of the environment known by the agent, stored as a list of neighbors for each node.
	private HashMap<String, HashSet<String>> map;
	// Currently opened nodes: scheduled for exploration.
	private List<String> openedNodes;
	// For each explored node, the parent node used to access it is stored. It is used by the exploration
	// behaviour to find paths to the next goal.
	private HashMap<String, String> exploredNodes;

	protected void setup() {

		super.setup();

		//get the parameters given into the object[]. In the current case, the environment where the agent will evolve
		final Object[] args = getArguments();
		if(args[0]!=null){
			deployAgent((Environment) args[0]);
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}

		// Initialise agent data
		this.map = new HashMap<>();
		this.exploredNodes = new HashMap<>();
		this.openedNodes = new ArrayList<>();
		
		// Register to DFService to enable communication with the other agents. The AID is
		// then available to all agents.
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd  = new ServiceDescription();
		sd.setType("explorer");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd );
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		
		// Init state
		// The exploration agent provides a `dataStore` to some behaviours to allow the passing of data between state
		// in the FSM.
		DataStore dataStore = new DataStore();
		dataStore.put("exploration_blocked_notification", false);
		dataStore.put("recipients_for_sharing", new ArrayList<>());

		// Initialize behaviors
		Exploration explorationBehaviour = new Exploration(this);
		CheckVoiceMail checkVoiceMailBehaviour = new CheckVoiceMail(this);
		SendData sendDataBehaviour = new SendData(this);
		RcvData rcvDataBehaviour = new RcvData(this);
		RequestStandby requestStandbyBehaviour = new RequestStandby(this);
		WaitForStandby waitForStandbyBehaviour = new WaitForStandby(this);

		// Common data store.
		explorationBehaviour.setDataStore(dataStore);
		checkVoiceMailBehaviour.setDataStore(dataStore);
		sendDataBehaviour.setDataStore(dataStore);
		waitForStandbyBehaviour.setDataStore(dataStore);

		// Add the behaviors to the Finite State Machine.
		FSMBehaviour fsm = new FSMBehaviour();
		fsm.registerFirstState(explorationBehaviour, "Explore");
		fsm.registerState(checkVoiceMailBehaviour, "CheckVoiceMail");
		fsm.registerState(requestStandbyBehaviour, "RequestStandby");
		fsm.registerState(waitForStandbyBehaviour, "WaitForStandby");
		fsm.registerState(sendDataBehaviour, "SendData");
		fsm.registerState(rcvDataBehaviour, "RcvData");

		// Register all transitions.
		fsm.registerTransition("Explore", "CheckVoiceMail", 1);
		fsm.registerTransition("CheckVoiceMail", "Explore", 1);
		fsm.registerTransition("CheckVoiceMail", "SendData", 2);
		fsm.registerTransition("CheckVoiceMail", "RequestStandby", 3);
		fsm.registerTransition("SendData", "RcvData", 1);
		fsm.registerTransition("RcvData", "Explore", 1);
		fsm.registerTransition("RequestStandby", "WaitForStandby", 1);
		fsm.registerTransition("WaitForStandby", "SendData", 1);
		fsm.registerTransition("WaitForStandby", "CheckVoiceMail", 2);

		addBehaviour(fsm);
		System.out.println("the agent "+this.getLocalName()+ " is started");
	}
	
	public HashMap<String, HashSet<String>> getMap() {
		return this.map;
	}
	
	public List<String> getOpenedNodes() {
		return openedNodes;
	}

	public HashMap<String, String> getExploredNodes() {
		return exploredNodes;
	}

	public void log(String s) {
		String myPosition = this.getCurrentPosition();
		System.out.println("["+this.getLocalName()+"@"+myPosition+"] " + s);
	}
	
}
