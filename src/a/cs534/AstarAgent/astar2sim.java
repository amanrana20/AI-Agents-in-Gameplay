package a.cs534.AstarAgent; 

import java.util.ArrayList;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Mario.MODE;
import ch.idsia.mario.environments.Environment;
import a.cs534.Sprites.*;
import a.cs534.SceneDependencies.*;

public class astar2sim 
{
	public LevelScene aux;
    public LevelScene mainScene;
    public LevelScene workScene;
    public SearchNode bestPosition;
    public SearchNode goalPosition;
    float marioStartPosition;
    ArrayList<SearchNode> posPool;
    ArrayList<int[]> visitedStates = new ArrayList<int[]>();
    private boolean requireReplanning = false;
    
    public int timeBudget = 20; // in milliseconds; required by the API

    
    private ArrayList<boolean[]> currentActionPlan;
    int ticksBeforeReplanning = 0;
    
	
	private class SearchNode
	{
		private int timePassed = 0;
		private float distanceLeft = 0;
		public SearchNode positionOfParent = null;
		public LevelScene sceneSnapshot = null;
		public boolean isInVisitedList = false;
		
		boolean[] action;
		int repetitions = 2; 
	
		public SearchNode(boolean[] action, int repetitions, SearchNode parent)
		{
	    	this.positionOfParent = parent;
	    	this.action = action;
	    	this.repetitions = repetitions;
	    	
	    	if (parent != null)
	    		timePassed = parent.timePassed + repetitions;
	    	else
	    		timePassed = 0;
		}
		
		//************************************************************************************
		
		public float simulationOfPosition()
		{
	    	// set state to parents scene
			if (positionOfParent.sceneSnapshot == null)
			{
				System.out.println("Snapshot does not exist");
			}
			mainScene = positionOfParent.sceneSnapshot;
			positionOfParent.sceneSnapshot = backupState();

			
			int initialDamage = findMarioDamage();

	    	for (int i = 0; i < repetitions; i++)
	    	{
	    		advanceStep(action);
	    	}
	    	
    	distanceLeft = (1000000-mainScene.mario.xa) + ((findMarioDamage() - initialDamage) * (1000000 ));
	    	//System.out.print(timeLeft+"\n");
	    	if (isInVisitedList)
	    	{
	    		posPool.remove(this);
	    	}
	    	
	    	sceneSnapshot = backupState();
	    			
	    	return distanceLeft;			
		}
		
		//************************************************************************************
		

		public ArrayList<SearchNode> generateChildren()
		{
			ArrayList<SearchNode> list = new ArrayList<SearchNode>();
			ArrayList<boolean[]> possibleActions = createPossibleActions(this);
			
			for (boolean[] action: possibleActions)
			{
				list.add(new SearchNode(action, repetitions, this));
			}			
			return list;
		}
		
	}
	
    
    public astar2sim()
    {
    	initialiseSimulator();
    }
    
    private ArrayList<boolean[]> createPossibleActions(SearchNode currentPos)
    {
    	ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();

    	//---------------- jump---------------------------------
    	if (currentPos.sceneSnapshot.mario.mayJump() || (!currentPos.sceneSnapshot.mario.isOnGround()))
    	{
    		//possibleActions.add(createAction(false, false, false, true, false));
    		possibleActions.add(createAction(false, false, false, true, true));
    	}
    	
    	//--------------- run right-----------------------------
    	   	possibleActions.add(createAction(false, true, false, false, true));//right plus speed
    	   //possibleActions.add(createAction(false, true, false, false, false));//only right
    	
    	//--------------- run left-------------------------------
    	
        possibleActions.add(createAction(true, false, false, false, true));//left plus speed
        //possibleActions.add(createAction(true, false, false, false, false));//only left
    	
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
        
    private int findMarioDamage()
    { 
    	if (mainScene.level.isGap[(int) (mainScene.mario.x/16)] &&
    			mainScene.mario.y > mainScene.level.gapHeight[(int) (mainScene.mario.x/16)]*16)
    	{
    		mainScene.mario.damage+=5;
    	}
    	return mainScene.mario.damage;	
    }
    

    
    private void search(long startTime)
    {
    	SearchNode current = bestPosition;
    	boolean currentGood = false;
    	while(posPool.size() != 0 
    		&& (System.currentTimeMillis() - startTime < Math.min(200,timeBudget/2))
    		&& (bestPosition.sceneSnapshot.mario.x - marioStartPosition < 176) || !currentGood)
    		    		
    	{
    		current = pickBestPos(posPool);
    		
//    		if((!current.isInVisitedList 
//    			&& isInVisited((int) current.sceneSnapshot.mario.x, (int) current.sceneSnapshot.mario.y, current.timePassed)))
//    		{
//    			current.isInVisitedList = true;
//    			current.distanceLeft +=1500;
//    			//posPool.add(current);
//    		}
//    		else
//    		
//    		{
        		currentGood = true;
        		visited((int) current.sceneSnapshot.mario.x, (int) current.sceneSnapshot.mario.y, current.timePassed);
        		posPool.addAll(current.generateChildren());    			

//    		}
    			
    		
    		if (currentGood) 
    		{
    			bestPosition = current;
    		}
    	}
    		mainScene = current.sceneSnapshot;
    }
    
    private void startSearch(int repetitions)
    {    	
    	SearchNode startingPosition = new SearchNode(null, repetitions, null);
    	startingPosition.sceneSnapshot = backupState();
    	
    	posPool = new ArrayList<SearchNode>();
    	visitedStates.clear();
    	posPool.addAll(startingPosition.generateChildren());
    	marioStartPosition = mainScene.mario.x;
    	bestPosition = startingPosition;
   }
    
    private ArrayList<boolean[]> extractPlan()
    {
    	ArrayList<boolean[]> actions = new ArrayList<boolean[]>();
    	
    	// just move forward if no best position exists
    	if (bestPosition == null)
    	{
    		for (int i = 0; i < 1 ; i++)
    		{
    			actions.add(createAction(false, true, false, false, true));        		
    		}
    		return actions; 
    	}
    	SearchNode current = bestPosition;
    	while (current.positionOfParent != null)
    	{
    		for (int i = 0; i < current.repetitions; i++)
    			actions.add(0, current.action);
    		
    		current = current.positionOfParent;
    	}
		return actions;
    }
    
    public String printAction(boolean[] action)
    {
    	String s = "";
    	if (action[Mario.KEY_RIGHT]) s+= "Forward ";
    	if (action[Mario.KEY_LEFT]) s+= "Backward ";
    	if (action[Mario.KEY_SPEED]) s+= "Speed ";
    	if (action[Mario.KEY_JUMP]) s+= "Jump ";
    	if (action[Mario.KEY_DOWN]) s+= "Duck";
    	return s;
    }
    
    private SearchNode pickBestPos(ArrayList<SearchNode> posPool)
    {
    	float auxTime=0;
    	aux = backupState();
    	SearchNode bestPos = null;
    	float bestPosCost = 10000000;
    	
    	for (SearchNode current: posPool)
    	//Iterator<SearchNode> iter = posPool.iterator();
    	//while(iter.hasNext())
    	{
    		//SearchNode current = iter.next();
    		//for ( int i=0;i< 2;i++)
    		//{
    			 auxTime = current.simulationOfPosition();
    	//	}
    		
    		float currentCost = auxTime;
    		if (currentCost < bestPosCost)
    		{
    			bestPos = current;
    			bestPosCost = currentCost;
    		}
    	}
    	posPool.remove(bestPos);
    	restoreState(aux);
    	return bestPos;
    }
        
	public void initialiseSimulator()
	{
		mainScene = new LevelScene();
		mainScene.init();	
		mainScene.level = new Level(1500,15);
		Mario.setMode(MODE.MODE_LARGE);
	} 
	
	public void setLevelPart(byte[][] levelPart, float[] enemies)// used to update observation world with MARIO world stuff and enemies
	{
    	if (mainScene.setLevelScene(levelPart)) // return true is there is a gap 
    	{
    		requireReplanning = true;
    	}
    	requireReplanning = mainScene.setEnemies(enemies);//return true if there is an enemy in curent screen
	}
	
	public LevelScene backupState()
	{
		LevelScene sceneCopy = null;
		try
		{
			sceneCopy = (LevelScene) mainScene.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		
		return sceneCopy;
	}
	
	
	
	public void restoreState(LevelScene l)
	{
		mainScene = l;
	}
	
	public void advanceStep(boolean[] action)
	{
		mainScene.mario.setKeys(action);
		if (mainScene.verbose > 8) System.out.print("[" 
				+ (action[Mario.KEY_DOWN] ? "d" : "") 
				+ (action[Mario.KEY_RIGHT] ? "r" : "")
				+ (action[Mario.KEY_LEFT] ? "l" : "")
				+ (action[Mario.KEY_JUMP] ? "j" : "")
				+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		mainScene.tick();
	}

	public boolean[] optimise(Environment observation)
	{
		
		LevelScene currentState;
        // do stuff
		long startTime = System.currentTimeMillis();
          currentState = backupState();
        if (workScene == null)
        	workScene = mainScene;
        
        int planAhead = 2; 
        int stepsPerSearch = 1;
        
        ticksBeforeReplanning--; 
        requireReplanning = false;//
        if (ticksBeforeReplanning <= 0 || currentActionPlan.size() == 0 || requireReplanning)
        {
        	currentActionPlan = extractPlan(); 
        	if (currentActionPlan.size() < planAhead)
        	{
        		System.out.println("Warning!! currentActionPlan smaller than planAhead! plansize: "+currentActionPlan.size());
        		planAhead = currentActionPlan.size();
        	}
        	
        	// simulate ahead to predicted future state, and then plan for this future state 
        	for (int i = 0; i < planAhead; i++)
        	{
        		advanceStep(currentActionPlan.get(i));        		
        	}
        	
        	workScene = backupState();
        	startSearch(stepsPerSearch);
        	ticksBeforeReplanning = planAhead;
        }
        restoreState(workScene);
		search(startTime);
    	workScene = backupState();
        System.out.print(currentActionPlan.size()+"\n");        
		boolean[] action = new boolean[5];
        if (currentActionPlan.size() > 0)
        	action = currentActionPlan.remove(0);

		restoreState(currentState);       
        return action;
	}
	
	private void visited(int x, int y, int t)
	{
		visitedStates.add(new int[]{x,y,t});
	}
	
	private boolean isInVisited(int x, int y, int t)
	{
		int timeDiff = 5;
		int xDiff = 2;
		int yDiff = 2;
		for(int[] v: visitedStates)
		{
			if (Math.abs(v[0] - x) < xDiff
					&& Math.abs(v[1] - y) < yDiff
					&& Math.abs(v[2] - t) < timeDiff
					&& t >= v[2])
			{
				return true;
			}
		}
		return false;	
		//return visitedStates.contains(new int[]{x,y,t});
	}
	
	
}