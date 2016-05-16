package a.cs534.NeuralAgent;

import java.util.Random;

public class NeuralNetwork {
	// Store layer of nodes for neural network
	private double[][] firstLayer;
    private double[][] secondLayer;
    private double[] hiddenLayer;
    private double[] outputs;
    private double[] inputs;
  
    // Magnitude of mutation and random varaible
    public double mutation_mag = 0.1;
    public static final Random random = new Random();
    
    // Values for Gaussian
    public static double mu = 0.0f;       
    public static double sigma = 0.1f; 

    // Neural Network Constructor
    public NeuralNetwork(int num_inputs, int num_hidden, int num_outputs){
    	firstLayer = new double[num_inputs][num_hidden];
        secondLayer = new double[num_hidden][num_outputs];
        hiddenLayer = new double[num_hidden];
        outputs = new double[num_outputs];
        inputs = new double[num_inputs];
        
        // Build first two layers 
        initialize(firstLayer);
        initialize(secondLayer);
    }
    
    // Method initializes layers
    protected void initialize(double[][] layer) {
        for (int i = 0; i < layer.length; i++) {
            for (int j = 0; j < layer[i].length; j++) {
                layer[i][j] = (random.nextGaussian() * sigma + mu);
            }
        }
    }
    
    // Method causes mutation to first and second layer
    public void mutate() {
        mutate(firstLayer);
        mutate(secondLayer);
    }
    
    // Mutates a given layer by adding a gaussian term
    private void mutate(double[][] array) {
        for (double[] anArray : array) {
        	for (int i = 0; i < anArray.length; i++) {
                anArray[i] += random.nextGaussian() * mutation_mag;
            }
        }
    }
    
    // Propogates inputs through the neural network
    public double[] propagate(double[] inputIn) {
        if (inputs != inputIn) {
            System.arraycopy(inputIn, 0, this.inputs, 0, inputIn.length);
        }
        
        propagateForward(inputs, hiddenLayer, firstLayer);
        for (int i = 0; i < hiddenLayer.length; i++) {
        	hiddenLayer[i] = Math.tanh(hiddenLayer[i]);
        }
        
        propagateForward(hiddenLayer, outputs, secondLayer);
        for (int i = 0; i < hiddenLayer.length; i++) {
        	hiddenLayer[i] = Math.tanh(hiddenLayer[i]);
        }

        return outputs;
    }
   
    // Propogates one step into layer
    private void propagateForward(double[] fromLayer, double[] toLayer, double[][] connections) {
    	// Clear Layer Values
        for (int i = 0; i < toLayer.length; i++) {
        	toLayer[i] = 0;
        }

        for (int from = 0; from < fromLayer.length; from++) {
            for (int to = 0; to < toLayer.length; to++) {
                toLayer[to] += fromLayer[from] * connections[from][to];
            }
        }
    }
    
    // Get Current Weights for Neural Network
    public double[] getWeightsArray() {
        double[] weights = new double[inputs.length * hiddenLayer.length + hiddenLayer.length * outputs.length];
        int k = 0;
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenLayer.length; j++) {
                weights[k] = firstLayer[i][j];
                k++;
            }
        }
        for (int i = 0; i < hiddenLayer.length; i++) {
            for (int j = 0; j < outputs.length; j++) {
                weights[k] = secondLayer[i][j];
                k++;
            }
        }
        return weights;
    }
    
    // Sets weights for Neural Network
    public void setWeightsArray(double[] weights) {
        int k = 0;
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenLayer.length; j++) {
                firstLayer[i][j] = weights[k];
                k++;
            }
        }
        for (int i = 0; i < hiddenLayer.length; i++) {
            for (int j = 0; j < outputs.length; j++) {
                secondLayer[i][j] = weights[k];
                k++;
            }
        }
    }
    
}
