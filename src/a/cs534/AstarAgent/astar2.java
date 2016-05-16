package a.cs534.AstarAgent;

import ch.idsia.mario.environments.Environment;
import a.cs534.Sprites.*;
import ch.idsia.ai.agents.Agent;

public class astar2 implements Agent
{
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "AStar Agent";
    private astar2sim sim;
    private float lastX = 0;
    private float lastY = 0;
	int errCount = 0;
	astar2 errAgent;
    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        sim = new astar2sim();
    }
   

    public boolean[] getAction(Environment observation)
    {
   	
    	boolean[] ac = new boolean[5];
    	ac[Mario.KEY_RIGHT] = true;
    	ac[Mario.KEY_SPEED] = true;
    	
    	
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();

        sim.advanceStep(action);   

		float[] f = observation.getMarioFloatPos();
		if (sim.mainScene.mario.x != f[0] || sim.mainScene.mario.y != f[1])
		{
			if (f[0] == lastX && f[1] == lastY)
				return ac;
			sim.mainScene.mario.x = f[0];
			sim.mainScene.mario.xa = (f[0] - lastX) *0.89f;
			if (Math.abs(sim.mainScene.mario.y - f[1]) > 0.1f)
				sim.mainScene.mario.ya = (f[1] - lastY) * 0.85f + 3f; // Added the 3f 

			sim.mainScene.mario.y = f[1];
			
		}
		sim.setLevelPart(scene, enemies);
        
		lastX = f[0];
		lastY = f[1];
        action = sim.optimise(observation);

        return action;
    }

    public AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() 
    {        
    	return name;    
    }

    public void setName(String Name) 
    { 
    	this.name = Name;    
    }
}
