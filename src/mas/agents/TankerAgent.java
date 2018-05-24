package mas.agents;

import jade.core.behaviours.DataStore;
import mas.behaviours.WaitForCollectorsBehaviour;
import mas.strategies.MoveToTankerPosTankerStrategy;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TankerAgent extends Agent {

    protected void setup() {
        super.setup();

        strategies.put(M_WAIT_FOR_COLLECTORS, new MoveToTankerPosTankerStrategy(this));

        WaitForCollectorsBehaviour waitForCollectorsBehaviour = new WaitForCollectorsBehaviour(this);
        waitForCollectorsBehaviour.setDataStore(this.dataStore);

        fsm.registerState(waitForCollectorsBehaviour, "WaitForCollectors");
        fsm.registerTransition("WaitForCollectors", "CheckVoiceMail", Agent.A_CHECK_VOICEMAIL);
        fsm.registerTransition("CheckVoiceMail", "WaitForCollectors", Agent.M_WAIT_FOR_COLLECTORS);
    }

    @Override
    public int getMovementBehaviour() {
        if ((boolean)this.dataStore.get("random_walk"))
            return M_RANDOM_WALK;

        if ((boolean) this.dataStore.get("avoiding_conflict"))
            return M_AVOID_CONFLICT;

        if ((boolean) this.dataStore.get("walk_to_random"))
            return M_WALK_TO_RANDOM;

        if (this.dataStore.get("tanker_position") == null)
            if (this.getOpenedNodes().isEmpty()) {
                this.dataStore.put("random_walk_max_steps", 1);
                return M_RANDOM_WALK;
            } else {
                return M_EXPLORE;
            }
        else
            return M_WAIT_FOR_COLLECTORS;
    }

    private String getMaxForBetweenness() {
        HashMap<String, Integer> degrees = new HashMap<>();
        for (String s : this.map.keySet()) {
            this.dijkstra.computeShortestPaths(s);
            for (String t : this.map.keySet()) {
                if (!s.equals(t)) {
                    List<String> path = this.dijkstra.getPath(s, t);
                    for (String v : path) {
                        degrees.put(v, degrees.getOrDefault(v, 0) + 1);
                    }
                }
            }
        }

        int max = -1;
        String center = null;
        for (Map.Entry<String, Integer> node : degrees.entrySet()) {
            if (node.getValue() > max) {
                max = node.getValue();
                center = node.getKey();
            }
        }
        return center;
    }

    @Override
    public void newDataReceived(DataStore data) {
        super.newDataReceived(data);

        // Determine tanker position
        // 1. find appropriate node
//        String tanker_position = null;
//        int max_deg = Integer.MIN_VALUE;
//        for (String node : this.map.keySet()) {
//            if (this.map.get(node).size() > max_deg) {
//                tanker_position = node;
//                max_deg = this.map.get(node).size();
//            }
//        }
//        if (tanker_position == null) tanker_position = this.getCurrentPosition();
        String tanker_position = getMaxForBetweenness();
        // 2. set as new tanker_position
        this.getDataStore().put("tanker_position", tanker_position);
        this.getDataStore().put("tanker_position_date", new Date());
        // 3. get rid of open nodes => end of exploration.
        this.openedNodes.clear();
        // misc
        this.dataStore.put("updater_name", data.get("updater_name"));
    }

    @Override
    public String getTypeSpecificInfo() {
        return "|map_s="+this.map.size()
                + "|opn="+this.openedNodes.size()
                +"|t_p="+this.dataStore.get("tanker_position")
                +"|dst="+this.destination;
    }
}
