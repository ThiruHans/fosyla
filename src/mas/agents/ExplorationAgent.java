package mas.agents;

import env.Attribute;
import env.Couple;
import env.Environment;
import graph.Dijkstra;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import mas.abstractAgent;
import mas.behaviours.*;
import utils.PointOfInterest;

import java.util.*;

public class ExplorationAgent extends abstractAgent {
	/**
	 * Exploration Agent:
	 * Explores the graph it is dropped into. If another agent is met, current knowledge of the map is shared
	 * and coordination for the remaining exploration is computed.
	 */

	private static final long serialVersionUID = -1784844593772918359L;
	public static final int T_EXPLORE = 20;
	public static final int RANDOM_WALK = 21;
	public static final int UPDATE_POIS = 22;

	// Map of the environment known by the agent, stored as a list of neighbors for each node.
	private HashMap<String, HashSet<String>> map;
	// Currently opened nodes: scheduled for exploration.
	private HashMap<String, PointOfInterest> pois;
	private List<String> openedNodes;
	// Current plan consists of the path to the next goal node.
	private List<String> currentPlan;
	// Current tick: used for logging to differentiate steps
	private int tick;
	private Dijkstra dijkstra;
	private Random random;

	public Random getRandomGenerator() {
		return random;
	}

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
		this.openedNodes = new ArrayList<>();
		this.pois = new HashMap<>();
		this.tick = 0;
		this.dijkstra = new Dijkstra(this.map);
		this.random = new Random();
		
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
		dataStore.put("movement_behaviour", ExplorationAgent.T_EXPLORE);
		dataStore.put("default_movement_behaviour", ExplorationAgent.T_EXPLORE);

		// Initialize behaviors
		Exploration explorationBehaviour = new Exploration(this);
		CheckVoiceMail checkVoiceMailBehaviour = new CheckVoiceMail(this);
		SendData sendDataBehaviour = new SendData(this);
		RcvData rcvDataBehaviour = new RcvData(this);
		RequestStandby requestStandbyBehaviour = new RequestStandby(this);
		WaitForStandby waitForStandbyBehaviour = new WaitForStandby(this);
		RandomWalk randomWalkBehaviour = new RandomWalk(this);
		SendGoal sendGoalBehaviour = new SendGoal(this);
		RcvGoal rcvGoalBehaviour = new RcvGoal(this);

		// Common data store.
		explorationBehaviour.setDataStore(dataStore);
		checkVoiceMailBehaviour.setDataStore(dataStore);
		sendDataBehaviour.setDataStore(dataStore);
		waitForStandbyBehaviour.setDataStore(dataStore);
		rcvDataBehaviour.setDataStore(dataStore);
		sendGoalBehaviour.setDataStore(dataStore);
		rcvGoalBehaviour.setDataStore(dataStore);
		randomWalkBehaviour.setDataStore(dataStore);

		// Add the behaviors to the Finite State Machine.
		FSMBehaviour fsm = new FSMBehaviour();
		fsm.registerFirstState(explorationBehaviour, "Explore");
		fsm.registerState(checkVoiceMailBehaviour, "CheckVoiceMail");
		fsm.registerState(requestStandbyBehaviour, "RequestStandby");
		fsm.registerState(waitForStandbyBehaviour, "WaitForStandby");
		fsm.registerState(sendDataBehaviour, "SendData");
		fsm.registerState(rcvDataBehaviour, "RcvData");
		fsm.registerState(randomWalkBehaviour, "RandomWalk");
		fsm.registerState(sendGoalBehaviour, "SendGoal");
		fsm.registerState(rcvGoalBehaviour, "RcvGoal");

		// Register all transitions.
		fsm.registerTransition("Explore", "CheckVoiceMail", Exploration.T_CHECK_VOICEMAIL);
		fsm.registerTransition("CheckVoiceMail", "Explore", ExplorationAgent.T_EXPLORE);
		fsm.registerTransition("CheckVoiceMail", "RandomWalk", ExplorationAgent.RANDOM_WALK)
		;
		fsm.registerTransition("CheckVoiceMail", "SendData", CheckVoiceMail.T_SEND_DATA);
		fsm.registerTransition("CheckVoiceMail", "RequestStandby", CheckVoiceMail.T_REQUEST_STANDBY);
		fsm.registerTransition("SendData", "RcvData", SendData.T_RCV_DATA);
		fsm.registerTransition("RcvData", "SendGoal", RcvData.T_SEND_GOAL);
		fsm.registerTransition("RcvData", "CheckVoiceMail", RcvData.T_CHECK_VOICEMAIL);
		fsm.registerTransition("SendGoal", "RcvGoal", SendGoal.T_RCV_GOAL);
		fsm.registerTransition("RcvGoal", "CheckVoiceMail", RcvGoal.T_CHECK_VOICEMAIL);

		fsm.registerTransition("RequestStandby", "WaitForStandby", RequestStandby.T_WAIT_FOR_STANDBY);
		fsm.registerTransition("WaitForStandby", "SendData", WaitForStandby.T_SEND_DATA);
		fsm.registerTransition("WaitForStandby", "CheckVoiceMail", WaitForStandby.T_CHECK_VOICEMAIL);

		fsm.registerTransition("RandomWalk", "CheckVoiceMail", RandomWalk.T_CHECK_VOICEMAIL);

		addBehaviour(fsm);
		System.out.println("the agent "+this.getLocalName()+" is started");
	}
	
	public HashMap<String, HashSet<String>> getMap() {
		return this.map;
	}
	public HashMap<String, PointOfInterest> getPois() { return this.pois; }
	public List<String> getOpenedNodes() {
		return openedNodes;
	}

	public void computePlan(String position) {
		String goal = "";
		if (openedNodes.isEmpty()) {
//			this.kill();
//			goal = findOldestPOI();
		} else {
			goal = openedNodes.get(openedNodes.size()-1);
		}

		// Next goal is next unexplored node in `openedNodes`.
		// Compute shortest path to goal with Dijkstra:
		this.dijkstra.computeShortestPaths(position);
		this.log("New goal : " + goal + ". Currently opened:" + openedNodes + ". NDiscovered="+map.size());
		this.currentPlan = this.dijkstra.getPath(position, goal);
		this.log("Path : " + this.currentPlan);
//		this.log("MAP:" + map);
	}

	@SuppressWarnings("unchecked")
	public void updateMap() {
		String myPosition = this.getCurrentPosition();
		//List of observable from the agent's current position
		List<Couple<String,List<Attribute>>> lobs = this.observe();
		// update points of interests
		for(Couple observable : lobs) {
			List<Attribute> attrs = (List<Attribute>) observable.getRight();
			if (!attrs.isEmpty()) {
				String node = (String) observable.getLeft();
				pois.put(node, new PointOfInterest(node, attrs, this.getTick()));
			}
		}

		// Add current position to map if not contained already.
		if(!map.containsKey(myPosition)) {
			map.put(myPosition, new HashSet<>());
		}
		// Remove current position from openedNodes
		openedNodes.remove(myPosition);

		HashSet<String> currentPositionNeighbors = map.get(myPosition);
		// For each discovered node
		for(int i = 1; i < lobs.size(); i++) {
			Couple<String,List<Attribute>> c = lobs.get(i);
			String nodeId = c.getLeft();

			HashSet<String> nodeNeighbors;
			if(!map.containsKey(nodeId)) {
				// If discovered for the first time, add unexplored node to map.
				nodeNeighbors = new HashSet<>();
				map.put(nodeId, nodeNeighbors);
				// if discovered for the first time, add to opened nodes.
				openedNodes.add(nodeId);
			} else {
				nodeNeighbors = map.get(nodeId);
			}
			nodeNeighbors.add(myPosition);
			currentPositionNeighbors.add(nodeId);
		}
	}

	public void log(String s) {
		String myPosition = this.getCurrentPosition();
		System.out.println("["+this.getLocalName()+" @ "+myPosition+ " /" +this.tick +"] " + s);
	}

	public List<String> getPlan() {
		return this.currentPlan;
	}

	public void tick() {
		this.tick += 1;
	}
	private int getTick() {return this.tick;}
	public void kill() { this.takeDown();}

//	private String findOldestPOI() {
//		PointOfInterest max_p;
//		int max_date = Integer.MAX_VALUE;
//		for (PointOfInterest p : this.pois.values()) {
//			if (p.getLastUpdatedDate() < max_date) {
//				max_p = p;
//			}
//		}
//		return max_p.getNode();
//	}
}
