package competition.cig.astar_simple.code;

import java.util.ArrayList;

import competition.cig.robinbaumgarten.astar.LevelScene;
import competition.cig.robinbaumgarten.astar.level.Level;
import competition.cig.robinbaumgarten.astar.sprites.Mario;


public class AstarSimpleSim {
	public static final int visitedListPenalty = 1500; // penalty for being in the visited-states list
	
	// ****************************************************
	// ****************************************************
	// Node class that stores information about the nodes! 
	private class Node
	{
		
		private int timeElapsed = 0;			// How much ticks elapsed since start of search
		public float remainingTimeEstimated = 0; // Optimal (estimated) time to reach goal
		private float remainingTime = 0;		// Optimal time to reach goal AFTER simulating with the selected action

		public Node parentPos = null;		// Parent node
		public LevelScene sceneSnapshot = null; // World state of this node
		public boolean hasBeenHurt = false;
		public boolean isInVisitedList = false;
		
		boolean[] action;						// the action of this node
		int repetitions;
		
		public Node(boolean[] action, int repetitions, Node parent){
			this.parentPos = parent;
	    	if (parent != null)
	    	{
	    		this.remainingTimeEstimated = parent.estimateRemainingTimeChild(action, repetitions);
	    		timeElapsed = parent.timeElapsed + repetitions;
	    	}
	    	else
	    	{
	    		this.remainingTimeEstimated = calcRemainingTime(levelScene.mario.x, 0);
	    		timeElapsed = 0;
	    	}
	    	this.action = action;
	    	this.repetitions = repetitions;
		}
		
		public float calcRemainingTime(float marioX, float marioXA)
		{
		    float maxMarioSpeed = 10.9090909f;

			return (100000 - (maxForwardMovement(marioXA, 1000) + marioX)) 
				/ maxMarioSpeed - 1000;
		}
		
		public float getRemainingTime()
		{
			if (remainingTime > 0) 
				return remainingTime;
			else
				return remainingTimeEstimated;
		}
		
		public float estimateRemainingTimeChild(boolean[] action, int repetitions)
		{
			float[] childbehaviorDistanceAndSpeed = estimateMaximumForwardMovement(
					levelScene.mario.xa, action, repetitions);
			return calcRemainingTime(levelScene.mario.x + childbehaviorDistanceAndSpeed[0],
					childbehaviorDistanceAndSpeed[1]);			
		}
		
		public ArrayList<Node> generateChildren(){
			ArrayList<Node> list = new ArrayList<Node>();
			ArrayList<boolean[]> possibleActions = createPossibleActions(this);
			
			for (boolean[] action: possibleActions)
			{
				Node n = new Node(action, 1, this);
				list.add(n);
				n.simulatePos();
			}			
			return list;
		}
		
		// Simulate the world state after we applied the action of this node, using the parent world state
		public float simulatePos()
		{
	    	// set state to parents scene
			levelScene = parentPos.sceneSnapshot;
			parentPos.sceneSnapshot = backupState();
			
			int initialDamage = 0;
	    	for (int i = 0; i < repetitions; i++)
	    	{
	    		/* This is the graphical line output, it has been disabled for the competition
	    		if (debugPos < 1000)
	    		{
	    			GlobalOptions.Pos[debugPos][0] = (int) levelScene.mario.x;
	    			GlobalOptions.Pos[debugPos][1] = (int) levelScene.mario.y;
	    			debugPos++;
	    		}*/
	    		
	    		// Run the simulator
	    		advanceStep(action);
	    		
	    		/*if (debugPos < 1000)
	    		{
	    			GlobalOptions.Pos[debugPos][0] = (int) levelScene.mario.x;
	    			GlobalOptions.Pos[debugPos][1] = (int) levelScene.mario.y;
	    			debugPos++;
	    		}
	    		if (debugPos > 1000)
	    			debugPos = 0;
	    		*/
	    	}
	    	
	    	// set the remaining time after we've simulated the effects of our action,
	    	// penalising it if we've been hurt.
	    	remainingTime = calcRemainingTime(levelScene.mario.x, levelScene.mario.xa);
//	    	 	+ (getMarioDamage() - initialDamage) * (1000000 - 100 * timeElapsed);
	    	if (isInVisitedList)
	    		remainingTime += visitedListPenalty;
//	    	hasBeenHurt = (getMarioDamage() - initialDamage) != 0;
	    	sceneSnapshot = backupState();
	    			
	    	return remainingTime;			
		}
	}
	// ****************************************************
	// ****************************************************
	
	// Variables
	public LevelScene levelScene;  		// current world state
	public Node bestNode = null; 	   // the current best position found by the planner
	public Node currentNode = null;
		
	// Constructor for the Simple Astar
	public AstarSimpleSim(){
		initialiseSimulator();
	}
	
	public void initialiseSimulator(){
		levelScene = new LevelScene();
		levelScene.init();	
		// increase max level length here if you want to run longer levels
		levelScene.level = new Level(1500,15);
	}
	
	public boolean[] optimise(){
		
		// Given some start Node, create the children
		ArrayList<Node> children = new ArrayList<Node>();
		
		if (currentNode == null){
			boolean[] a = new boolean[]{false, true, false, false, true};
			currentNode = new Node(a, 1, null);
			children = currentNode.generateChildren();
		} else{
			children = currentNode.generateChildren();
		}
		
		//Evaluate the children, with simple cost function heuristic
		//Pick the best child node and return
		Node best = evaluate_children(children);
		
		return best.action;
	}
	
	public Node evaluate_children(ArrayList<Node> children){
		Node bestChild = null;
		float bestvalue= 1000000;
		
		for(Node child : children){
			
			float val = calculate_fn(child);

			if(val < bestvalue){
				bestvalue = val;
				bestChild = child;
			}
		}
		
		children.remove(bestChild);
		return bestChild; 
	}
	
	public float calculate_fn(Node child){
		float currentCost = child.getRemainingTime() + child.timeElapsed * 0.90f; 
//		
//		float t = child.simulatePos();
//		
//		return t;
		
		return currentCost;
	}
	
	private float maxForwardMovement(float initialSpeed, int ticks)
    {
    	float y = ticks;
    	float s0 = initialSpeed;
    	return (float) (99.17355373 * Math.pow(0.89,y+1)
    	  -9.090909091*s0*Math.pow(0.89,y+1)
    	  +10.90909091*y-88.26446282+9.090909091*s0);
    }
	
	public void setLevelPart(byte[][] levelPart, float[] enemies)
	{
    	levelScene.setLevelScene(levelPart);
    	levelScene.setEnemies(enemies);
	}
	
	
	private ArrayList<boolean[]> createPossibleActions(Node currentPos){
    	ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();

    	// jump
    	if (canJumpHigher(currentPos, true)) possibleActions.add(createAction(false, false, false, true, false));
    	if (canJumpHigher(currentPos, true)) possibleActions.add(createAction(false, false, false, true, true));
    	
    	// run right
    	possibleActions.add(createAction(false, true, false, false, true));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(false, true, false, true, true));
    	possibleActions.add(createAction(false, true, false, false, false));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(false, true, false, true, false));
 	
    	// run left
    	possibleActions.add(createAction(true, false, false, false, false));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(true, false, false, true, false));
    	possibleActions.add(createAction(true, false, false, false, true));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(true, false, false, true, true));
    	
    	return possibleActions;
	}
	
	public boolean canJumpHigher(Node currentPos, boolean checkParent)
    {
    	// This is a hack to allow jumping one tick longer than required 
    	// (because we're planning two steps ahead there might be some odd situations where we might need that)
    	if (currentPos.parentPos != null && checkParent
    			&& canJumpHigher(currentPos.parentPos, false))
    			return true;
    	return false;
//    	return currentPos.sceneSnapshot.mario.mayJump() || (currentPos.sceneSnapshot.mario.jumpTime > 0);
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
	  
	// make a clone of the current world state (copying marios state, all enemies, and some level information)
		public LevelScene backupState()
		{
			LevelScene sceneCopy = null;
//			try
//			{
//				sceneCopy = (LevelScene) levelScene.clone();
//			} catch (CloneNotSupportedException e)
//			{
//				e.printStackTrace();
//			}
//			
			return sceneCopy;
		}
		
		public void advanceStep(boolean[] action)
		{
			levelScene.mario.setKeys(action);
			if (levelScene.verbose > 8) System.out.print("[" 
					+ (action[Mario.KEY_DOWN] ? "d" : "") 
					+ (action[Mario.KEY_RIGHT] ? "r" : "")
					+ (action[Mario.KEY_LEFT] ? "l" : "")
					+ (action[Mario.KEY_JUMP] ? "j" : "")
					+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
			levelScene.tick();
		}
}
