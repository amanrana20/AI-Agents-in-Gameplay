package a.cs534.NeuralAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wox.serial.Easy;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.ea.ES;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

// Genetic Evolver class perform evolution of agents
// Written by : MarioAi team! 
public class Genetic_Evolver {
	
    final static int populationSize = 300;
    final static int geneticCycles = 2500;
    private static ArrayList<NeuralAgent> population = new ArrayList<NeuralAgent>();
    
    // Runs Genetic Evolution for the Population of Neural Agents
    public static void main(String[] args) {
    	// Initialize Population to train
    	initializePopulation(); 
    	
    	// Set up Simulation
    	EvaluationOptions options = new CmdLineOptions(args);
    	options.setLevelType(0);
        options.setNumberOfTrials(1);
        // This line enables or disables enemies
        // options.setPauseWorld(true);
        
        // Set difficulty setting
        for (int difficulty = 0; difficulty <= 11; difficulty++){
        	System.out.println("Current Difficulty : " + difficulty);
        	
        	// Set up Simulation Difficulty
        	options.setLevelDifficulty(difficulty);
            options.setMaxFPS(true);
            options.setVisualization(false);
            
            // Sets up the task which runs agent through level
            Task task = new ProgressTask(options);
            int bf = -1;
            int cycle = 0;
        	// Run this level for a number of generations
            while (bf < 4000 && cycle < geneticCycles){
        		// Run each member of the population through the level
        		for(int member = 0; member < population.size(); member++){
        			// Set current agent we're looking at
        			Evolvable initial = population.get(member);
                    options.setAgent((Agent)initial);
                    
                    // Shows the level being run by every 50th member
                    options.setVisualization(member % 50 == 0);
                    options.setMaxFPS(true);
                    
                    // Evaluate this agent
                    evaluate((NeuralAgent)initial, task, 1);
            	}
        		
        		// Increment cycle
        		cycle++;
        		
        		// Set BF to best agent fitness        		
            	NeuralAgent best = getStrongestAgent();
            	bf = (int) best.fitness;
        		
        		// Remove weak agents from the genetic pool
            	removeWeakAgents();
            
            	// Creates next generation from surviving parents
            	createNextGeneration();	
        	}
        	
            // Evolution For this Round Completed Print Results
        	System.out.println("Best Agent from Round: " + difficulty);
        	NeuralAgent best = getStrongestAgent();
        	bf = (int) best.fitness;
        	System.out.println("Best Fitness: " + best.fitness);
        	System.out.println("BF: " + bf);
        	System.out.println("Cylce: " + cycle);
        	
        	double[] best_weights = best.getNeuralNetworkWeights();
            String w = "";
            for(int i = 0; i < best_weights.length; i++){
            	w = w + best_weights[i] + " ";
            }
            
            System.out.println("Best Agent Weights for Round : " + w);
        	
            // Run the best agent once for us to see :)
        	Evolvable best_round = best;
            options.setAgent((Agent)best_round);
            options.setVisualization(true);
            options.setMaxFPS(true);
            evaluate((NeuralAgent)best_round, task, 1);
        }
        
        // Overall Evolution Complete
        NeuralAgent best = getStrongestAgent();
        double[] best_weights = best.getNeuralNetworkWeights();
        String w = "";
        for(int i = 0; i < best_weights.length; i++){
        	w = w + best_weights[i] + " ";
        }
        
        System.out.println("Evolution complete: ");
        System.out.println("Best Agent Fitness : " + best.getFitness());
        System.out.println("Best Agent Weights : " + w);
        
        
        // Not sure if needed, but doesnt hurt
        System.exit(0);
    }
    
    // Initializes a random population
   	public static void initializePopulation(){
   		for(int i = 0; i< populationSize; i++){
   			NeuralAgent agent = new NeuralAgent();
   			population.add(agent);
   		}
   	}
   	
   	// Evaluate agent for given level
    private static void evaluate(NeuralAgent n_agent, Task task, int repetitions) {
    	int fitness = 0;
        for (int i = 0; i < repetitions; i++) {
        	fitness += task.evaluate((Agent) n_agent)[0];
        }
        double fitness_average = fitness / repetitions;
        n_agent.setFitness(fitness_average);
    }
    
    // Removes only the weakest half of the population
    public static void removeWeakestAgents(){
    	int currentGenerationSize = population.size();
    	// Remove weakest half of population
    	for(int i = 0 ; i < currentGenerationSize/2; i++){
    		int index_to_remove = 0;
    		double min = Double.POSITIVE_INFINITY;
    		for (int j = 0; j < population.size(); j++){
    			if(population.get(j).fitness < min){
    				index_to_remove = j;
    				min = population.get(j).fitness;
    			}
    		}
    		
    		population.remove(index_to_remove);
    	}
    }
    
    //Removes weakest agents from population Tournament Style
  	public static void removeWeakAgents(){
  		//Create a list of the location of weakest agents
  		ArrayList<Integer> indexOfWeakAgents = new ArrayList<Integer>();
  		int currentGenerationSize = population.size();
  		//Set up tournament style battle to the death
  		for(int i =0; i< currentGenerationSize; i+=2){
  			//If there is an opponent
  			if (i+1 < currentGenerationSize){
  				//If the first agent was weaker than the second agent
  				if(population.get(i).getFitness() < population.get(i+1).getFitness()){
  					//Add the first agents position
  					indexOfWeakAgents.add(i);
  				} else {
  					indexOfWeakAgents.add(i+1);
  				}
  			}
  		}
  		
  		//Have to eliminate agents starting from the bottom of the list
  		// due to the nature of the remove method
  		Collections.sort(indexOfWeakAgents);
  		for(int i = indexOfWeakAgents.size()-1; i >= 0; i--){
  			int index = indexOfWeakAgents.get(i);
  			population.remove(index);
  		}
  	}
  	
    // Creates the next Generation of children based on surviving parents
 	public static void createNextGeneration(){
 		int parentGenerationSize = population.size();
 		for(int i =0; i< parentGenerationSize; i+=2){
 			// If there is a pair of parents
 			if(i+1 < parentGenerationSize){
 				// Generate Children
 				NeuralAgent childAgent = generateChild(population.get(i), population.get(i+1), true);
 				NeuralAgent childAgent2 = generateChild(population.get(i), population.get(i+1), false);
 				// Add children to end of population list
 				population.add(childAgent);
 				population.add(childAgent2);
 			}
 		}
 	}
 	
 	// Generates new child from parent children
 	public static NeuralAgent generateChild(NeuralAgent parentA, NeuralAgent parentB, boolean flag){
		//TODO Add mutation function for making children
		NeuralAgent child = new NeuralAgent();
		
		double[] weightA = parentA.getNeuralNetworkWeights();
		double[] weightB = parentB.getNeuralNetworkWeights();
		double[] weight_child = new double[weightA.length];
		
		if(flag){
			for(int i = 0; i < weightA.length/2; i++){
				weight_child[i] = weightA[i];
				weight_child[weight_child.length - i-1] = weightB[weightB.length - i-1]; 
			}
		} else {
			for(int i = 0; i < weightB.length/2; i++){
				weight_child[i] = weightB[i];
				weight_child[weight_child.length - i-1] = weightA[weightA.length - i-1]; 
			}
		}
		
	
		child.mutate();
		return child;
	}
 	
 	//Gets the strongest agent from the population
	public static NeuralAgent getStrongestAgent(){
		//Set initial conditions such that anything is greater than max
		int max_index = -1;
		double max = Double.NEGATIVE_INFINITY; 
		// Loop through the population to search for max
		for(int i = 0; i<population.size(); i++){
			// If the current agent has a fitness greater than the max
			if(population.get(i).getFitness() > max){
				// Then set that agent's fitness to the max and save its location
				max_index = i;
				max = population.get(i).getFitness();
			}
		}
		
		return population.get(max_index);
	}
}
