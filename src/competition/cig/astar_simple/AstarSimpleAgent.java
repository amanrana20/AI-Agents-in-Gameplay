package competition.cig.astar_simple;

import competition.cig.astar_simple.code.AstarSimpleSim;
import competition.cig.robinbaumgarten.astar.AStarSimulator;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class AstarSimpleAgent implements Agent {
	
	protected boolean action[] ;
	private AstarSimpleSim sim;

	public void reset() {
		 action = new boolean[Environment.numberOfButtons];
	     sim = new AstarSimpleSim();
	}

	public boolean[] getAction(Environment observation) {
		
		// Get the environment and enemies from the Mario API
     	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
		float[] realMarioPos = observation.getMarioFloatPos();
		
		// Pass world scene variables into simulator
		sim.setLevelPart(scene, enemies);
		
		// This is the call to the simulator (where all the planning work takes place)
        action = sim.optimise();
		
		return action;
	}
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public AGENT_TYPE getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

}
