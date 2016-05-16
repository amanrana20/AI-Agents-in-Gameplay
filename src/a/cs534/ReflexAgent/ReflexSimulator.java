package a.cs534.ReflexAgent;

import java.util.ArrayList;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import a.cs534.AstarAgent.LevelScene;
import a.cs534.SceneDependencies.*;

public class ReflexSimulator {

	public static LevelScene levelScene;  		// current world state
    public LevelScene workScene;   		// world state used by the planner (some ticks in the future)
    public Environment observation;
    
    public Node bestPosition; 	// the current best position found by the planner
    public Node furthestPosition; // the furthest position found by the planner (sometimes different than best)
    
    float currentSearchStartingMarioXPos; 
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
    		for(boolean[] action1 : possible_actions){
				Node n = new Node(action1, this);
    			n.depth = this.depth + 1;
    			children.add(n);
    		}
    		return children;
    	}
    	
    }
    // ****************************************************
    // ****************************************************
    
    public ReflexSimulator(){
    	this.initialiseSimulator();
    }
    
    public void initialiseSimulator()
	{
		levelScene = new LevelScene();
		levelScene.init();	
		levelScene.level = new Level(1500,15);
		workScene = getScene();
	}
    
    public boolean[] optimise(Environment observation){
	    	this.observation = observation;
	    	
    		// Check if this is our first time in optimise
	    	if(this.bestPosition == null){
	    		boolean[] init = new boolean[]{false, true, false, false, true};
	    		this.bestPosition = new Node(init, null);
	    		this.bestPosition.scenesnapshot = getScene();
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
   }
    
    public Node findBestChild(ArrayList<Node> children){
    	
    	float lowest_cost = -10000;
    	Node lowest_Node = null;    	
    	
    	for(Node child : children){
    		if(child.cost > lowest_cost){
    			lowest_cost = child.cost;
    			lowest_Node = child;
    		}    	
    	}
    	return lowest_Node;
    }
    
    public void evaluate_children(ArrayList<Node> children){
    	
    	System.out.print("\n");
    	
    	boolean enemyFront = false;
		boolean enemyAbove = false;
		boolean isWall = false;
		boolean isGap = true; 
		
		byte[][] enemies = observation.getEnemiesObservation();
    	byte[][] squares = observation.getLevelSceneObservation();
    	
    	//Check for gaps
    	for(int i = 8; i<squares.length; i++){
    		for(int j = 12; j<13; j++){
    			if(squares[i][j] != 0){
    				isGap = false;
    			}
    		}
    	}
    	
    	for(Node child : children){   		
    		
    		//Starting cost for any action
    		child.cost = 10000;
    		
    		//We make standing in place a little bad
    		if (!child.action[0] && !child.action[1] && !child.action[2] && !child.action[3] && !child.action[4])
        	{
        		child.cost -=10;
        	}
        	
    		//Make going to the left a little worse
        	if(child.action[0])
        	{
        		child.cost -=20;
        	}
    		        	
    		//Wall to jump!
        	if(squares[11][12] != 0 || squares[11][13] != 0 || squares[11][14] != 0 || squares[11][15] != 0){ // || squares[11][13] != 0
        		System.out.print("W! ");
        		isWall = true;
        		if(squares[11][12] != 0){
        			if(child.action[3] && child.action[1]){        			
        				child.cost += 100;
        			}
        		}
        	}
        	
        	if(isGap){
        		if(child.action[3] && child.action[1]){
        			System.out.print("G! ");
        			child.cost += 1000;
        		} 
        	}
        	       	
        	//Enemy in front //enemies[10][10] != 0 ||  			 
        	if(enemies[10][11] != 0 || enemies[10][12] != 0 || enemies[10][13] != 0 || enemies[10][14] != 0
        			|| enemies[11][10] !=0 || enemies[11][11] !=0 || enemies[11][12] !=0 || enemies[11][13] != 0){
        		
        		System.out.print("F! ");
        		enemyFront = true;
        		
        		if(child.action[3] && child.action[1]){
        			child.cost += 1000;
        		}
        		
        		//if(!child.action[3] && child.action[1]){
        		//	child.cost -= 1000;
        		//}
        		
        		if (!child.action[0] && !child.action[1] && !child.action[2] && !child.action[3]){
            		child.cost -=500;
            	}
    		}
        	
        	//Enemy above //enemies[9][10] != 0 || 		//|| enemies[8][10] != 0 		|| enemies[7][10] != 0 
        	if(enemies[9][11] != 0 || enemies[9][12] != 0 || enemies[9][13] != 0 || enemies[9][14] != 0 || enemies[8][11] != 0 || enemies[8][12] != 0
        			|| enemies[8][13] != 0 || enemies[8][14] != 0 || enemies[7][11] != 0 || enemies[7][12] != 0 || enemies[7][13] != 0 || enemies[7][14] != 0){
        		
        		enemyAbove = true;
        		System.out.print("A! ");        		
        		
        		//Jumping to the right
        		if(child.action[3] && child.action[1]){
        			System.out.print("JR");
        			child.cost -= 1000;
        		}
        		
        		//Running to the right
        		//if(!child.action[3] && child.action[1]){
        		//	System.out.print("R ");
        		//	child.cost += 1000;
        		//}
        		
        		//Enemy above and enemy in front
        		if((enemies[11][14] != 0 || enemyFront) && child.action[0]){
        			System.out.print("L ");
        			child.cost +=1000;
        		}        		
        		
        		//Enemy above and wall
        		if(isWall && child.action[1]){
        			System.out.print("T ");
        			child.cost -=1030;
        		}
        		
        		if(!child.action[0] && !child.action[1] && !child.action[2] && !child.action[3] && !child.action[4]){       			
        			System.out.print("N ");
        			child.cost -= 150;
        		}
        	}
        	
        	//Enemy below		enemies[12][10] != 0 || 		|| enemies[13][10] != 0 
        	if(enemies[12][11] != 0 || enemies[12][12] != 0 || enemies[12][13] != 0 || enemies[12][14] != 0
        			|| enemies[13][11] != 0 || enemies[13][12] != 0 || enemies[13][13] != 0 || enemies[13][14] != 0){
        		System.out.print("B! ");
        		
        		//Jumping to the right
        		if(child.action[3] && child.action[1]){
        			if (!enemyAbove){
        				child.cost += 1000;;
        			}
        		}
        		
        		//
        		//if(enemyAbove && !child.action[3] && child.action[1]){
        		//	child.cost -= 1000;
        		//}
        	}      	
        	System.out.print(" " + child.cost + " ");
    	}
    }
    
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
		
		// do nothing
		possibleActions.add(createAction(false, false, false, false, false));
		
    	// run right
    	possibleActions.add(createAction(false, true, false, false, false));
    	
    	// run left
    	possibleActions.add(createAction(true, false, false, false, false));
    	
    	// just shoot
    	//possibleActions.add(createAction(false, false, false, false, true));
    	
    	if(observation.mayMarioJump() || !observation.isMarioOnGround()){
	
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
    
    public LevelScene getScene()
	{
		LevelScene sceneCopy = null;
		try{
			sceneCopy = (LevelScene) levelScene.clone();
		} catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		
		return sceneCopy;
	}
    
}
