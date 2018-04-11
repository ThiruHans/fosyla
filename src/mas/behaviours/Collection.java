package mas.behaviours;

import mas.agents.CollectorAgent;
import jade.core.behaviours.SimpleBehaviour;

public class Collection extends SimpleBehaviour{

	public static final int T_CHECK_VOICEMAIL = 10;
	private CollectorAgent agent;
	
	public Collection(CollectorAgent a){
		super(a);
		agent = a;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public int onEnd(){
		return T_CHECK_VOICEMAIL;
	}

}
