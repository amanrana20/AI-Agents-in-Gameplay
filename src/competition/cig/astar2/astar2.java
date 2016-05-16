package competition.cig.astar2;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class astar2 implements Agent {

	protected boolean action[] = new boolean[Environment.numberOfButtons];
	protected String name = "astar2";
	private astar2sim sim;
	private float lastX = 0;
	private float lastY = 0;
	    
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
		sim = new astar2sim();
	}

	public boolean[] getAction(Environment observation) {
		// get the environment and enemies from the Mario API
     	byte[][] scene = observation.getLevelSceneObservationZ(0);
     	float[] enemies = observation.getEnemiesFloatPos();
		float[] realMarioPos = observation.getMarioFloatPos();
		
//		sim.advanceStep(action); 
		
		// Update the internal world to the new information received
		sim.setLevelPart(scene, enemies);
		
		lastX = realMarioPos[0];
		lastY = realMarioPos[1];
//		
//		System.out.println("Mario Pos: ");
//		System.out.println(lastX + " " + lastY);
//		
		action = sim.optimise(observation);
		
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
