package competition.cig.astar2;

import java.util.ArrayList;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import competition.cig.robinbaumgarten.astar.LevelScene;
import competition.cig.robinbaumgarten.astar.level.Level;

public class astar2sim {

	public static LevelScene levelScene;  		// current world state
    public LevelScene workScene;   		// world state used by the planner (some ticks in the future)
    public Environment observation;
    
    public Node bestPosition; 	// the current best position found by the planner
    public Node furthestPosition; // the furthest position found by the planner (sometimes different than best)
    
    float currentSearchStartingMarioXPos;
    
    public int timeBudget = 20; // ms
    public static final int visitedListPenalty = 1500; // penalty for being in the visited-states list
    int ticksBeforeReplanning = 0; 
    
    // A* Variables
    int depth = 0; // Depth Variable
    ArrayList<Node> posPool = new ArrayList<Node>(); // Open List
    ArrayList<int[]> visitedStates = new ArrayList<int[]>(); // Closed List
    private ArrayList<boolean[]> currentActionPlan = new ArrayList<boolean[]>(); // Path
    
    
    // ****************************************************
    // ****************************************************
    private class Node{
    	boolean[] action = null;
    	Node parent = null;
    	LevelScene scenesnapshot = null;
    	float cost = 0;
    	int depth = 0;
    	
    	public Node(boolean[] action, Node parent){
    		this.action = action;
    		this.parent = parent;
    	}
    	
    	public ArrayList<Node> generateChildren(){
    		ArrayList<Node> children = new ArrayList<Node>();
    		ArrayList<boolean[]> possible_actions = generate_possible_actions(this);
    		for(boolean[] action : possible_actions){
    			Node n = new Node(action, this);
    			n.depth = this.depth + 1;
    			children.add(n);
    		}
    		return children;
    	}
    	
    }
    // ****************************************************
    // ****************************************************
    
    public astar2sim(){
    	this.initialiseSimulator();
    }
    
    public void initialiseSimulator()
	{
		levelScene = new LevelScene();
		levelScene.init();	
		// increase max level length here if you want to run longer levels
		levelScene.level = new Level(1500,15);
		workScene = backupState();
	}
    
    public boolean[] optimise(Environment observation){
	    	this.observation = observation;
	    	
	    	// Check if this is our first time in optimise
	    	if(this.bestPosition == null){
	    		
	    		boolean[] init = new boolean[]{false, true, false, false, true};
	    		this.bestPosition = new Node(init, null);
	    		this.bestPosition.scenesnapshot = backupState();
	    	}
	    	
	    	// Add the current best action to action plan
	    	this.currentActionPlan.add(this.bestPosition.action);
	    	
	    	// Get children
	    	ArrayList<Node> children = this.bestPosition.generateChildren();	
	    		
	    	//Evaluate children
	    	this.evaluate_children(children);
	    	
	    	//Pick Best Child
	    	this.bestPosition = this.findBestChild(children);
	    	
	    	//Return Best Child
	    	return this.bestPosition.action;
	    	
    } // optimize() ends
    
    
    public Node search(){
    	// While there are still unexplored nodes from this depth
    		// Find node with the lowest cost
    		// increment its depth by one
    		// get the path to this node
    		// get this node
    		// put this node into the closed list
    		// get children of this node
    		// for each child in children
	    		// if one of the children is already in the closed list dont add
	    		// if one of the children is already in the open list dont add
	    		// Add child if one of the two above conditions have not been met
    	return null;
    }
    
    public Node findBestChild(ArrayList<Node> children){
    	float lowest_cost = -10000000;
    	Node lowest_Node = null;
    	
    	System.out.println("actions: ");
    	for(Node child : children){
    		if(child.cost > lowest_cost){
    			lowest_cost = child.cost;
    			lowest_Node = child;
    		}
    	}
    	
    	return lowest_Node;
    }
    
    public void evaluate_children(ArrayList<Node> children){
    	// Here we are giving every child it's cost after simulation
    	
    	System.out.println("*****");
    	
    	for(Node child : children){
    		
    		child.scenesnapshot = child.parent.scenesnapshot;
        	child.parent.scenesnapshot = backupState();
        	
        	int initialDamage = getMarioDamage();
        	int repeat_action = 2;
        	
        	workScene = backupState();
//        	System.out.println("Check :");
//        	System.out.println(workScene.level.width);
        	
        	float posMarioBeforeSimulation = workScene.mario.x;
        	int distance_travelled_before_simulation =  (int) estimateMaximumForwardMovement(levelScene.mario.xa, child.action, repeat_action)[0];
        	
        	for (int i = 0; i < repeat_action; i++)
        	{
        		advanceStep(child.action);
        	}
        	
        	float posMarioAfterSimulation = workScene.mario.x;
        	
        	byte[][] enemies = observation.getEnemiesObservation();
        	
        	int distance_travelled_after_simulation =  (int) estimateMaximumForwardMovement(levelScene.mario.xa, child.action, repeat_action)[0];
        	
        	// Cost according to the enemies
        	for(int i = 6; i < enemies.length - 5; i++) {
        		for(int j = 6; j < enemies[i].length - 5; j++) {
        			
        			// If enemy is closer, cost of action = 10000000
        			if(enemies[i][j] != 0) {
        				double euclideanDistance = Math.pow(((11-i)*(11-i) + (11-j)*(11-j)), 0.5);
        				if(euclideanDistance < 1.5) {
        					child.cost = 10000000;
        					System.out.println("DANGER !!!... ");
        				}
        			}
        		}
        	}
        	
        	if(distance_travelled_after_simulation < distance_travelled_before_simulation) {
        		child.cost = 10000;
        	}
        	
//        	System.out.println("Level Width: " + levelScene.level.isGap.length);
//        	System.out.println("MArio x: "+ observation.getMarioFloatPos()[0]);
        	int MarioPosX = (int) observation.getMarioFloatPos()[0];
        	
        	if(levelScene.level.isGap[MarioPosX + 2] == true) {
        		child.cost = 10000;
        	}
        	
        	System.out.print("GAP HEIGHT: ");
        	for(int i  = 0; i < levelScene.level.gapHeight.length; i++) {
        		System.out.print(levelScene.level.gapHeight[i] + "   ");
        	}
        	
        	System.out.println();
        	
        	levelScene.mario.damage = 0; 
        	
        	child.scenesnapshot = backupState();
        	
    	} // for loop ends
    	
    	//Given the current world state
    	//Simulate each child's action
    	//If it hits an enemy it should get a high cost
    	//If it falls in a gap it should get a high cost
    	//If it moves the player closer to the right it should get a lower cost
    
    } // evaluate_child ends
    
    
    public void setLevelPart(byte[][] levelPart, float[] enemies)
	{
    	levelScene.setLevelScene(levelPart);
    	levelScene.setEnemies(enemies);
	}
    
    public void advanceStep(boolean[] action)
	{
		workScene.mario.setKeys(action);
		if (workScene.verbose > 8) System.out.print("[" 
				+ (action[Mario.KEY_DOWN] ? "d" : "") 
				+ (action[Mario.KEY_RIGHT] ? "r" : "")
				+ (action[Mario.KEY_LEFT] ? "l" : "")
				+ (action[Mario.KEY_JUMP] ? "j" : "")
				+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		workScene.tick();
	} 
    
    public ArrayList<boolean[]> generate_possible_actions(Node currentPos){
        
		ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();

    	// run right
    	possibleActions.add(createAction(false, true, false, false, false));

    	// run left
    	possibleActions.add(createAction(true, false, false, false, false));

    	if(observation.mayMarioJump() || !observation.isMarioOnGround()){
			// jump
			possibleActions.add(createAction(false, false, false, true, false));
		
			//jump right
			possibleActions.add(createAction(false, true, false, true, false));
			
			//jump left
			possibleActions.add(createAction(true, false, false, true, false));
		}
    	return possibleActions;
	}
    
    private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed)
    {
    	boolean[] action = new boolean[5];
    	action[Mario.KEY_DOWN] = down;
    	action[Mario.KEY_JUMP] = jump;
    	action[Mario.KEY_LEFT] = left;
    	action[Mario.KEY_RIGHT] = right;
    	action[Mario.KEY_SPEED] = speed;
    	return action;
    }
    
    private int getMarioDamage()
    {
    	// early damage at gaps: Don't even fall 1 px into them.
    	if (levelScene.level.isGap[(int) (levelScene.mario.x/16)] &&
    			levelScene.mario.y > levelScene.level.gapHeight[(int) (levelScene.mario.x/16)]*16)
    	{
    		System.out.println("!!!--GAP--!!!");
     		levelScene.mario.damage+=5;
    	}
    	return levelScene.mario.damage;
    }
    
    public float[] estimateMaximumForwardMovement(float currentAccel, boolean[] action, int ticks)
    {
    	float dist = 0;
    	float runningSpeed =  action[Mario.KEY_SPEED] ? 1.2f : 0.6f;
    	int dir = 0;
    	if (action[Mario.KEY_LEFT]) dir = -1;
    	if (action[Mario.KEY_RIGHT]) dir = 1;
    	for (int i = 0; i < ticks; i++)
    	{
    		currentAccel += runningSpeed * dir;
    		dist += currentAccel;
    		// Slow down
    		currentAccel *= 0.89f;
    	}    	
    	float[] ret = new float[2];
    	ret[0] = dist;
    	ret[1] = currentAccel;
    	return ret;
    }
    
    public float calcRemainingTime(float marioX, float marioXA)
	{
	    float maxMarioSpeed = 10.9090909f;

		return (100000 - (maxForwardMovement(marioXA, 1000) + marioX)) 
			/ maxMarioSpeed - 1000;
	}
    
    private float maxForwardMovement(float initialSpeed, int ticks)
    {
    	float y = ticks;
    	float s0 = initialSpeed;
    	return (float) (99.17355373 * Math.pow(0.89,y+1)
    	  -9.090909091*s0*Math.pow(0.89,y+1)
    	  +10.90909091*y-88.26446282+9.090909091*s0);
    }
    
    public LevelScene backupState()
	{
		LevelScene sceneCopy = null;
		try
		{
			sceneCopy = (LevelScene) levelScene.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		
		return sceneCopy;
	}
    
}
