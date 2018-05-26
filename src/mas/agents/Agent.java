package mas.agents;

import env.Attribute;
import env.Couple;
import env.EntityType;
import env.Environment;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import mas.abstractAgent;
import mas.behaviours.*;
import mas.strategies.*;
import utils.Dijkstra;
import utils.PointOfInterest;

import java.util.*;

public abstract class Agent extends abstractAgent {

    // Used as transition codes in the agent's state machine.
    public static final int M_EXPLORE = 20;
    public static final int M_RANDOM_WALK = 21;
    public static final int M_WAIT_FOR_COLLECTORS = 22;
    public static final int M_UPDATE_POI = 23;
    public static final int M_WALK_TO_RANDOM = 24;
    public static final int M_AVOID_CONFLICT = 25;
    public static final int M_COLLECT = 26;
    public static final int M_MOVE_TO_TANKER = 27;
    public static final int A_CHECK_VOICEMAIL = 10;
    public static final int A_SEND_DATA = 11;
    public static final int A_REQUEST_STANDBY = 12;
    public static final int A_WAIT_FOR_STANDBY = 13;
    public static final int A_RCV_DATA = 14;
    public static final int A_SEND_GOAL = 15;
    public static final int A_RCV_GOAL = 16;
    // Default time step between moves, in milliseconds.
    public static int TIME_STEP = 300;
    public static int WAIT_TIME = 400;
    // All agent data
    protected HashMap<String, HashSet<String>> map;
    protected HashSet<String> openedNodes;
    protected HashSet<String> closedNodes;
    protected List<String> currentPlan;
    protected List<PointOfInterest> points;
    protected HashMap<String, Couple<String, Integer>> collectors;
    public Dijkstra dijkstra;
    protected Random random;
    protected EntityType type;
    protected DataStore dataStore;
    protected FSMBehaviour fsm;
    protected String destination;
    protected HashMap<Integer, Strategy> strategies;

    protected void setup() {
        super.setup();

        // Get the parameters given into the object[].
        // In the current case, the environment where the agent will evolve
        final Object[] args = getArguments();
        if (args[0]!=null && args[1]!=null){
            deployAgent((Environment) args[0], (EntityType)args[1]);
            this.type = (EntityType) args[1];
        } else {
            System.err.println("Malfunction during parameter's loading of agent"
                    + this.getClass().getName());
            System.exit(-1);
        }

        // Init agent data
        this.map = new HashMap<>();
        this.openedNodes = new HashSet<>();
        this.closedNodes = new HashSet<>();
        this.points = new ArrayList<>();
        this.collectors = new HashMap<>();
        this.dijkstra = new Dijkstra(this.map);
        this.random = new Random();
        this.dataStore = new DataStore();
        dataStore.put("block_notification", false);
        dataStore.put("recipients_for_sharing", new ArrayList<>());
        dataStore.put("tanker_position", null);
        dataStore.put("tanker_position_date", new Date());
        dataStore.put("random_walk", false);
        dataStore.put("random_walk_max_steps", 0);
        dataStore.put("walk_to_random", false);
        dataStore.put("walk_to_random_max_steps", 0);
        dataStore.put("goal_exchanged", 0);
        dataStore.put("updater_name", this.getLocalName());
        dataStore.put("avoiding_conflict", false);
        dataStore.put("last_block_position", null);

        ExploreBehaviour exploreBehaviour = new ExploreBehaviour(this);
        exploreBehaviour.setDataStore(this.dataStore);
        CheckVoiceMail checkVoiceMail = new CheckVoiceMail(this);
        checkVoiceMail.setDataStore(this.dataStore);
        RequestStandbyBehaviour requestStandbyBehaviour = new RequestStandbyBehaviour(this);
        WaitForStandbyBehaviour waitForStandbyBehaviour = new WaitForStandbyBehaviour(this);
        waitForStandbyBehaviour.setDataStore(this.dataStore);
        SendDataBehaviour sendDataBehaviour = new SendDataBehaviour(this);
        sendDataBehaviour.setDataStore(this.dataStore);
        RcvDataBehaviour rcvDataBehaviour = new RcvDataBehaviour(this);
        rcvDataBehaviour.setDataStore(this.dataStore);
        SendGoalBehaviour sendGoalBehaviour = new SendGoalBehaviour(this);
        sendGoalBehaviour.setDataStore(this.dataStore);
        RcvGoalBehaviour rcvGoalBehaviour = new RcvGoalBehaviour(this);
        rcvGoalBehaviour.setDataStore(this.dataStore);
        RandomWalkBehaviour randomWalkBehaviour = new RandomWalkBehaviour(this);
        randomWalkBehaviour.setDataStore(this.dataStore);
        WalkToRandomBehaviour walkToRandomBehaviour = new WalkToRandomBehaviour(this);
        walkToRandomBehaviour.setDataStore(this.dataStore);
        AvoidConflictBehaviour avoidConflictBehaviour = new AvoidConflictBehaviour(this);
        avoidConflictBehaviour.setDataStore(this.dataStore);

        fsm = new FSMBehaviour();
        fsm.registerFirstState(exploreBehaviour, "Explore");
        fsm.registerState(checkVoiceMail, "CheckVoiceMail");
        fsm.registerState(requestStandbyBehaviour, "RequestStandby");
        fsm.registerState(waitForStandbyBehaviour, "WaitForStandby");
        fsm.registerState(sendDataBehaviour, "SendData");
        fsm.registerState(rcvDataBehaviour, "RcvData");
        fsm.registerState(sendGoalBehaviour, "SendGoal");
        fsm.registerState(rcvGoalBehaviour, "RcvGoal");
        fsm.registerState(randomWalkBehaviour, "RandomWalk");
        fsm.registerState(walkToRandomBehaviour, "WalkToRandom");
        fsm.registerState(avoidConflictBehaviour, "AvoidConflict");

        // Register all transitions.
        fsm.registerTransition("Explore", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("CheckVoiceMail", "Explore", M_EXPLORE);
        fsm.registerTransition("CheckVoiceMail", "RequestStandby", A_REQUEST_STANDBY);
        fsm.registerTransition("CheckVoiceMail", "SendData", A_SEND_DATA);
        fsm.registerTransition("CheckVoiceMail", "RandomWalk", M_RANDOM_WALK);
        fsm.registerTransition("RequestStandby", "WaitForStandby", A_WAIT_FOR_STANDBY);
        fsm.registerTransition("WaitForStandby", "SendData", A_SEND_DATA);
        fsm.registerTransition("WaitForStandby", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("SendData", "RcvData", A_RCV_DATA);
        fsm.registerTransition("RcvData", "SendGoal", A_SEND_GOAL);
        fsm.registerTransition("RcvData", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("SendGoal", "RcvGoal", A_RCV_GOAL);
        fsm.registerTransition("RcvGoal", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("RandomWalk", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("WalkToRandom", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("CheckVoiceMail", "WalkToRandom", M_WALK_TO_RANDOM);
        fsm.registerTransition("CheckVoiceMail", "AvoidConflict", M_AVOID_CONFLICT);
        fsm.registerTransition("AvoidConflict", "CheckVoiceMail", A_CHECK_VOICEMAIL);

        this.addBehaviour(fsm);

        // Set destination strategies
        strategies = new HashMap<>();
        strategies.put(M_EXPLORE, new ExploreStrategy(this));
        strategies.put(M_RANDOM_WALK, new RandomWalkStrategy(this));
        strategies.put(M_WALK_TO_RANDOM, new WalkToRandomStrategy(this));
        strategies.put(M_AVOID_CONFLICT, new AvoidConflictStrategy(this));

        // Register to DFService to enable communication with the other agents.
        // The AID is then available to all agents.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd  = new ServiceDescription();
        sd.setType(this.type.toString());
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd );
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void updateMap() {
        this.log("Updating map.");
        String position = this.getCurrentPosition();
        // List of observables from the agent's current position.
        List<Couple<String, List<Attribute>>> lobs = this.observe();
        this.log(lobs.toString());

        // Add current position to map if not known already.
        if (!this.map.containsKey(position)) {
            this.map.put(position, new HashSet<>());
        }
        // Remove current position from openedNodes.
        this.openedNodes.remove(position);
        this.closedNodes.add(position);

        // Update map edges and points of interest.
        Set<String> currentPositionNeighbors = this.map.get(position);
        for (Couple<String, List<Attribute>> c : lobs) {
            String node = c.getLeft();
            List<Attribute> attributes = c.getRight();

            // update openedNodes
            if (!this.closedNodes.contains(node)) {
                this.openedNodes.add(node);
            }

            // Map edges.
            HashSet<String> nodeNeighbors;
            if (!this.map.containsKey(node)) {
                nodeNeighbors = new HashSet<>();
                this.map.put(node, nodeNeighbors);
            } else {
                nodeNeighbors = this.map.get(node);
            }
            nodeNeighbors.add(position);
            currentPositionNeighbors.add(node);

            // Points of interest
            if (!attributes.isEmpty()) {
                // Filter `stench` attributes
                List<Attribute> filteredAttrs = new ArrayList<>();
                for (Attribute attr : attributes) {
                    if (attr.getName().equals("Diamonds") || attr.getName().equals("Treasure"))
                        filteredAttrs.add(attr);
                }
                if (!filteredAttrs.isEmpty()) {
                    boolean poiFound = false;
                    for (PointOfInterest p : this.points) {
                        if (p.getNode().equals(node)) {
                            poiFound = true;
                            p.update(filteredAttrs, this.getDate());
                        }
                    }
                    if (!poiFound)
                        this.points.add(new PointOfInterest(node, filteredAttrs, this.getDate()));
                } else {
                    if (node.equals(position)) {
                        // Check that there wasn't a previous POI on this node.
                        // Delete if an obsolete POI is found.
                        for (PointOfInterest p : this.points) {
                            if (p.getNode().equals(position)) {
                                this.log("POI obsolete " + p.getNode());
                                p.markObsolete(this.getDate());
                                break;
                            }
                        }
                    }
                }
            } else {
                if (node.equals(position)) {
                    // Check that there wasn't a previous POI on this node.
                    // Delete if an obsolete POI is found.
                    for (PointOfInterest p : this.points) {
                        if (p.getNode().equals(position)) {
                            this.log("POI obsolete " + p.getNode());
                            p.markObsolete(this.getDate());
                            break;
                        }
                    }
                }
            }
        }
        this.log("POIs: " + this.points);
    }

    public void setNewDestination(String dest) {
        if (dest == null) {
            this.currentPlan = null;
            this.log("Plan was set to null");
            return;
        }
        String position = this.getCurrentPosition();
        this.destination = dest;
        this.log("New destination set : " + dest);
        this.dijkstra.computeShortestPaths(position);
        this.currentPlan = this.dijkstra.getPath(position, dest);
        this.log("Path : " + this.currentPlan);
    }

    public List<String> computePlanTo(String start, String dest) {
        if (dest == null) return null;

        this.dijkstra.computeShortestPaths(start);
        return this.dijkstra.getPath(start, dest);
    }
    public void setPlan(List<String> plan) {
        this.log("New plan set to : " + plan.get(0) + " | " + plan);
        this.destination = plan.get(0);
        this.currentPlan = plan;
    }

    public Date getDate() {
        return new Date();
    }

    public HashMap<String, HashSet<String>> getMap() {
        return map;
    }

    public List<String> getCurrentPlan() {
        return currentPlan;
    }

    public List<PointOfInterest> getPoints() {
        return points;
    }

    public Random getRandom() {
        return random;
    }

    public EntityType getType() {
        return type;
    }

    public HashSet<String> getOpenedNodes() {
        return openedNodes;
    }

    public String getDestination() {
        return destination;
    }

    public HashMap<String, Couple<String, Integer>> getCollectors() {
        return collectors;
    }

    public Strategy getStrategy() {
        int code = this.getMovementBehaviour();
        return this.strategies.get(code);
    }
    public Strategy getStrategy(int code) {
        return this.strategies.get(code);
    }

    // This is dependent on the agent's function and its state.
    public abstract int getMovementBehaviour();

    @SuppressWarnings("unchecked")
    public void newDataReceived(DataStore data) {
        HashMap<String, HashSet<String>> otherMap = (HashMap) data.get("map");

        // update map
        for (String e : otherMap.keySet()) {
            if (map.containsKey(e)) map.get(e).addAll(otherMap.get(e));
            else map.put(e, otherMap.get(e));
//            openedNodes.remove(e);
        }

        // update collectors list
        HashMap<String, Couple<String, Integer>> newCollectors =
                ((HashMap<String, Couple<String, Integer>>)data.get("collectors"));
        this.collectors.putAll(newCollectors);

        // if other agent stops exploring, delegate exploration of opened nodes.
        HashSet<String> otherOpenedNodes = (HashSet<String>) data.get("opened_nodes");
        if (otherOpenedNodes != null) {
            openedNodes.addAll(otherOpenedNodes);
        }

        // update points of interest
        List<PointOfInterest> points = (List<PointOfInterest>) data.get("points");
        for (PointOfInterest pOther : points) {
            boolean poiFound = false;
            for (PointOfInterest pMe : this.points) {
                if (pOther.equals(pMe)) {
                    if (pOther.getDate().compareTo(pMe.getDate()) > 0) pMe.update(pOther);
                    poiFound = true;
                }
            }
            if (!poiFound) this.points.add(pOther);
        }
    }

    public void move(String nextNode) {
        String position = this.getCurrentPosition();
        // Move to the picked destination.
        if(!this.moveTo(nextNode)) {
            this.log("Moving to "+nextNode+" is impossible.");
            this.dataStore.put("block_notification", true);
            String lastBlockPosition = (String)this.dataStore.get("last_block_position");
            if (position.equals(lastBlockPosition)) {
                this.log("Agent was blocked more than once on the same node. Walk to random");
                this.dataStore.put("walk_to_random", true);
                this.dataStore.put("walk_to_random_max_steps", 1);
                this.setNewDestination(null);
                this.emptyMessages();
//                this.dataStore.put("random_walk", true);
//                this.dataStore.put("random_walk_max_steps", 5);
            }
            this.dataStore.put("last_block_position", position);
        }
    }

    public void emptyMessages() {
        this.log("Emptying all messages");
        while(this.receive() != null);
    }

    // Debug utils
    public void log(String s) {
        System.out.println(this + " " + s);
    }

    public String toString() {
        return "["+this.getLocalName()+"@"+this.getCurrentPosition()
                +this.getTypeSpecificInfo()+"|mvmt="+this.getMovementBehaviour()
                +"]";
    }

    public abstract String getTypeSpecificInfo();
    public DataStore getDataStore() {
        return this.dataStore;
    }
    public void delayCheckVoiceMail(int millis) {
        this.fsm.getState("CheckVoiceMail").block(millis);
    }
}
