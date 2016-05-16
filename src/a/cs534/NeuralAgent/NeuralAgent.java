package a.cs534.NeuralAgent;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.MLP;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.environments.Environment;


public class NeuralAgent extends BasicAIAgent implements Agent, Evolvable {
	// Class Variables
	static private String name = "NeuralAgent";
	public double fitness;
	private NeuralNetwork nn;
    
	// One for each action mario can perform
    final int numberOfOutputs = 6;
    
    // One for each input we are considering
    final int numberOfInputs = 33;
	
    // Constructor for our Neural Agent
	public NeuralAgent(){
		super(name);
		// Creates Neural Network given number of inputs, hidden layers, output
        this.nn = new NeuralNetwork (numberOfInputs, 10, numberOfOutputs);
	}
	
	public NeuralAgent(NeuralNetwork nn){
		super(name);
		// Creates Neural Network given a NN agent
		this.nn = nn;
	}
	
	// Evaluates the Neural Network given some input and returns desired action
	public boolean[] getAction(Environment observation)
    {
		// Given the current world scene
	  	byte[][] scene = observation.getLevelSceneObservation();
        byte[][] enemies = observation.getEnemiesObservation();
     
        // Set inputs to Neural Network as follows
		double[] inputs = new double[numberOfInputs];
		
		// Check Scene if Mario is near blocks
		inputs[0] = check_near_mario(-2, -1, scene);		
		inputs[1] = check_near_mario(-1, -1, scene);
		inputs[2] = check_near_mario(0, -1, scene);
		inputs[3] = check_near_mario(1, -1, scene);
		inputs[4] = check_near_mario(2, -1, scene);
		
		inputs[5] = check_near_mario(-2, 0, scene);		
		inputs[6] = check_near_mario(-1, 0, scene);
		inputs[7] = check_near_mario(0, 0, scene);
		inputs[8] = check_near_mario(1, 0, scene);
		inputs[9] = check_near_mario(2, 0, scene);
		
		inputs[10] = check_near_mario(-2, 1, scene);		
		inputs[11] = check_near_mario(-1, 1, scene);
		inputs[12] = check_near_mario(0, 1, scene);
		inputs[13] = check_near_mario(1, 1, scene);
		inputs[14] = check_near_mario(2, 1, scene);
		
		// Check if Mario is near enemies
		inputs[15] = check_near_mario(-2, -1, enemies);		
		inputs[16] = check_near_mario(-1, -1, enemies);
		inputs[17] = check_near_mario(0, -1, enemies);
		inputs[18] = check_near_mario(1, -1, enemies);
		inputs[19] = check_near_mario(2, -1, enemies);
		
		inputs[20] = check_near_mario(-2, 0, enemies);		
		inputs[21] = check_near_mario(-1, 0, enemies);
		inputs[22] = check_near_mario(0, 0, enemies);
		inputs[23] = check_near_mario(1, 0, enemies);
		inputs[24] = check_near_mario(2, 0, enemies);
		
		inputs[25] = check_near_mario(-2, 1, enemies);		
		inputs[26] = check_near_mario(-1, 1, enemies);
		inputs[27] = check_near_mario(0, 1, enemies);
		inputs[28] = check_near_mario(1, 1, enemies);
		inputs[29] = check_near_mario(2, 1, enemies);
		
		// Check if Mario can Jump
		inputs[30] = observation.isMarioOnGround() ? 1 : 0;
		inputs[31] = observation.mayMarioJump() ? 1 : 0;
		
		// Check Mario State
		inputs[32] = observation.getMarioMode();

		// Get Neural Network Outputs given our inputs
		double[] outputs = nn.propagate(inputs);
		
		// Transform outputs into valid inputs
		boolean[] action = new boolean[numberOfOutputs];
        for (int i = 0; i < action.length; i++) {
            action[i] = outputs[i] > 0;
        }
        return action;
    }
	
	// Return Weights of Neural Network
	public double[] getNeuralNetworkWeights(){
		double[] weights = this.nn.getWeightsArray();
		return weights;
	}
	
	// Set Weights of Neural Network
	public void setNeuralNetworkWeights(double[] weights){
		this.nn.setWeightsArray(weights);
	}
	
	// Generates a new instance of this NeuralAgent
	public Evolvable getNewInstance() {
		return new NeuralAgent(this.nn);
	}
	
	// Mutates this neural agent
	public void mutate() {
		nn.mutate();
	}
	
	// Get Agent Type
	public Agent.AGENT_TYPE getType() {
	   return Agent.AGENT_TYPE.AI;
	}

	// Get Agent Name
    public String getName() {
        return name;
    }
	
    // Get this Neural Agent's Fitness
	public double getFitness(){
		return fitness;
	}

	// Set this Neural Agent's Fitness
	public void setFitness(double d) {
		this.fitness = d; 
	}
	
	// Given a 2D array check near mario's position
    private double check_near_mario (int posX, int posY, byte[][] scene) {
        int realX = posX + 11;
        int realY = posY + 11;
        return (scene[realX][realY] != 0) ? 1 : 0;
    }

    // Needed for Evolvables
	public Evolvable copy() {
		return null;
	}
	
}
