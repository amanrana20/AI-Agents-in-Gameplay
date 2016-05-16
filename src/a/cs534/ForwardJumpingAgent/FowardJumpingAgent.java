package a.cs534.ForwardJumpingAgent;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.environments.Environment;

public class FowardJumpingAgent extends BasicAIAgent implements Agent{
	private boolean[] action;
		
	public FowardJumpingAgent() {
		super("Forward Jumping Agent");
		action = new boolean[Environment.numberOfButtons];
	}
	
    public boolean[] getAction(Environment observation){
    	// Randomly chooses which action mario should take
    	
    	// Jump    	
		if(observation.mayMarioJump() || !observation.isMarioOnGround()){
			action[0] = false;
			action[1] = true;
			action[2] = false;
			action[3] = true;
			action[4] = true;
		} else {
			action[0] = false;
			action[1] = true;
			action[2] = false;
			action[3] = true;
			action[4] = true;
		}
    
    	return action;
    }

}
