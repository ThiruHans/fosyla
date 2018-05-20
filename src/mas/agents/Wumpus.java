//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package mas.agents;

import env.Attribute;
import env.Couple;
import env.Environment;
import jade.core.behaviours.TickerBehaviour;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mas.abstractAgent;

public class Wumpus extends abstractAgent {
    private static final long serialVersionUID = 2703609263614775545L;
    private static int MAXshiftDistance = 6;
    private static int MINshiftDistance = 1;

    public Wumpus() {
    }

    protected void setup() {
        super.setup();
        Object[] args = this.getArguments();
        if (args[0] != null) {
            this.deployWumpus((Environment)args[0]);
        } else {
            System.err.println("Malfunction during parameter's loading of agent" + this.getClass().getName());
            System.exit(-1);
        }

        this.addBehaviour(new Wumpus.RandomWalkBehaviour(this));
        System.out.println("the  agent " + this.getLocalName() + " is started");
    }

    protected void takeDown() {
    }

    public class RandomWalkBehaviour extends TickerBehaviour {
        private static final long serialVersionUID = 9088209402507795289L;
        private boolean finished = false;
        private int waitingTimeBeforeDropOff;
        private int currentWaitingTimeBeforeDromOff;
        private boolean grabbed;

        public RandomWalkBehaviour(abstractAgent myagent) {
            super(myagent, 300);
            Random r = new Random();
            this.waitingTimeBeforeDropOff = Wumpus.MINshiftDistance + r.nextInt(Wumpus.MAXshiftDistance);
            this.currentWaitingTimeBeforeDromOff = 0;
            this.grabbed = false;
        }

        public void onTick() {
            String myPosition = Wumpus.this.getCurrentPosition();
            if (myPosition != "") {
                List<Couple<String, List<Attribute>>> lobs = Wumpus.this.observe();
                System.out.println("lobs: " + lobs);
                if (this.grabbed && this.currentWaitingTimeBeforeDromOff == 0) {
                    Wumpus.this.dropOff();
                    this.grabbed = false;
                    this.currentWaitingTimeBeforeDromOff = this.waitingTimeBeforeDropOff;
                } else {
                    List<Attribute> lattribute = (List)((Couple)lobs.get(0)).getRight();
                    Iterator var4 = lattribute.iterator();

                    while(var4.hasNext()) {
                        Attribute a = (Attribute)var4.next();
                        switch(a) {
                            case TREASURE:
                                int valGrabbed = ((abstractAgent)this.myAgent).pick();
                                if (valGrabbed > 0 && !this.grabbed) {
                                    this.grabbed = true;
                                    this.currentWaitingTimeBeforeDromOff = this.waitingTimeBeforeDropOff;
                                }
                                break;
                            case DIAMONDS:
                                int valdiamGrabbed = ((abstractAgent)this.myAgent).pick();
                                if (valdiamGrabbed > 0 && !this.grabbed) {
                                    this.grabbed = true;
                                    this.currentWaitingTimeBeforeDromOff = this.waitingTimeBeforeDropOff;
                                }
                        }
                    }

                    if (this.grabbed) {
                        --this.currentWaitingTimeBeforeDromOff;
                    }
                }

                Random r = new Random();
                int moveId = 1 + r.nextInt(lobs.size() - 1);
                Wumpus.this.moveTo((String)((Couple)lobs.get(moveId)).getLeft());
            } else {
                System.err.println("Empty posit");
                System.exit(40);
            }

        }
    }
}
