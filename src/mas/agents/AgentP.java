package mas.agents;

import env.Attribute;
import env.Couple;
import env.EntityType;
import env.Environment;
import graph.Dijkstra;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import mas.abstractAgent;
import utils.PointOfInterest;

import java.util.*;

public class AgentP extends abstractAgent {

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

    protected void setup() {
        super.setup();

        //get the parameters given into the object[]. In the current case, the environment where the agent will evolve
        final Object[] args = getArguments();
        if(args[0]!=null && args[1]!=null){
            deployAgent((Environment) args[0], (EntityType)args[1]);
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

        // Register to DFService to enable communication with the other agents. The AID is then available to all agents.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd  = new ServiceDescription();
//		sd.setType("explorer");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd );
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

    }

//    private String findOldestPOI() {
//		PointOfInterest max_p;
//		int max_date = Integer.MAX_VALUE;
//		for (PointOfInterest p : this.pois.values()) {
//			if (p.getLastUpdatedDate() < max_date) {
//				max_p = p;
//			}
//		}
//		return max_p.getNode();
//	}

    @SuppressWarnings("unchecked")
    public void updateMap() {
        String myPosition = this.getCurrentPosition();
        //List of observable from the agent's current position
        List<Couple<String,List<Attribute>>> lobs = this.observe();

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

        // update points of interests
        for(Couple observable : lobs) {
            List<Attribute> attrs = (List<Attribute>) observable.getRight();
            String node = (String) observable.getLeft();
            if (!attrs.isEmpty()) {
                pois.put(node, new PointOfInterest(node, attrs, this.getTick()));
            }
            if (!this.map.containsKey(node)) {
                this.openedNodes.add(node);
            }
        }
    }

    public void computePlan(String position) {
        String goal = "";
        if (this.getOpenedNodes().isEmpty()) {
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

    public Random getRandomGenerator() {
        return random;
    }
    public HashMap<String, HashSet<String>> getMap() {
        return this.map;
    }
    public HashMap<String, PointOfInterest> getPois() { return this.pois; }
    public List<String> getOpenedNodes() {
        return openedNodes;
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
}
