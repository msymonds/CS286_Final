/*
 * Michael Symonds
 * CS286 FINAL
 * 
 * Z408 plaintext column permutation test
 * using jackobsen's algorithm and scoring
 * based on digraph analysis using simple
 * distance formula
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import smile.projection.PCA;

public class PCATest {

	static boolean debug = false; // toggle to get/hide additional output/status messages
	static boolean usePCA = true; // always keep this on true, will break otherwise
	static boolean usePCAWithRanking = false;
	static boolean useRankedDigraph = true;
	static boolean verbose = false;
  	static NumberFormat formatter = new DecimalFormat("#0.0000"); 

	//Z408 plaintext (0 thru 25, correct column order)
	int[][] z408Plain = {	{ 8, 11,  8, 10,  4, 10,  8, 11, 11,  8, 13,  6, 15,  4, 14, 15, 11}, 
			 				{ 4,  1,  4,  2,  0, 20, 18,  4,  8, 19,  8, 18, 18, 14, 12, 20,  2}, 
			 				{ 7,  5, 20, 13,  8, 19,  8, 18, 12, 14, 17,  4,  5, 20, 13, 19,  7}, 
			 				{ 0, 13, 10,  8, 11, 11,  8, 13,  6, 22,  8, 11,  3,  6,  0, 12,  4}, 
			 				{ 8, 13, 19,  7,  4,  5, 14, 17, 17,  4, 18, 19,  1,  4,  2,  0, 20}, 
			 				{18,  4, 12,  0, 13,  8, 18, 19,  7,  4, 12, 14, 18, 19,  3,  0, 13}, 
			 				{ 6,  4, 17, 14, 20, 18,  0, 13,  8, 12,  0, 11, 14,  5,  0, 11, 11}, 
			 				{19, 14, 10,  8, 11, 11, 18, 14, 12,  4, 19,  7,  8, 13,  6,  6,  8}, 
			 				{21,  4, 18, 12,  4, 19,  7,  4, 12, 14, 18, 19, 19,  7, 17,  8, 11}, 
			 				{11,  8, 13,  6,  4, 23, 15,  4, 17,  4, 13,  2,  4,  8, 19,  8, 18}, 
			 				{ 4, 21,  4, 13,  1,  4, 19, 19,  4, 17, 19,  7,  0, 13,  6,  4, 19}, 
			 				{19,  8, 13,  6, 24, 14, 20, 17, 17, 14,  2, 10, 18, 14,  5,  5, 22}, 
			 				{ 8, 19,  7,  0,  6,  8, 17, 11, 19,  7,  4,  1,  4, 18, 19, 15,  0}, 
			 				{17, 19, 14,  5,  8, 19,  8, 18, 19,  7,  0, 19, 22,  7,  4, 13,  8}, 
			 				{ 3,  8,  4,  8, 22,  8, 11, 11,  1,  4, 17,  4,  1, 14, 17, 13,  8}, 
			 				{13, 15,  0, 17,  0,  3,  8,  2,  4,  0, 13,  3,  0, 11, 11, 19,  7}, 
			 				{ 4,  8,  7,  0, 21,  4, 10,  8, 11, 11,  4,  3, 22,  8, 11, 11,  1}, 
			 				{ 4,  2, 14, 12,  4, 12, 24, 18, 11,  0, 21,  4, 18,  8, 22,  8, 11}, 
			 				{11, 13, 14, 19,  6,  8, 21,  4, 24, 14, 20, 12, 24, 13,  0, 12,  4}, 
			 				{ 1,  4,  2,  0, 20, 18,  4, 24, 14, 20, 22,  8, 11, 11, 19, 17, 24}, 
			 				{19, 14, 18, 11, 14, 22,  3, 14, 22, 13, 14, 17, 18, 19, 14, 15, 12}, 
			 				{24,  2, 14, 11, 11,  4,  2, 19,  8, 13,  6, 14,  5, 18, 11,  0, 21}, 
			 				{ 4, 18,  5, 14, 17, 12, 24,  0,  5, 19,  4, 17, 11,  8,  5,  4,  4}, 
			 				{ 1,  4, 14, 17,  8,  4, 19,  4, 12,  4, 19,  7,  7, 15,  8, 19,  8}};
	
	// holds an array of digraph vectors
	// based on brown corpus if using
	// PCA and a single digraph otherwise 
	double[][] englishDigraph;
	// digraph based on plain408 text
	double[][] z408PlainDigraph;
	// digraph-vector based on plain408 text
	double[] z408PlainDigraphVector;
	
	List<Integer[]> rankedDigraphPair;
	
	private class DigraphProb {
		public int i;
		public int j;
		public double prob;
		public DigraphProb(int i, int j, double prob) {
			this.i = i;
			this.j = j;
			this.prob = prob;
		}
	}
	
	public void runTest(){
		// initialize results file for printing results
		TextParse.initializeResultsFile();
		
		double[][] brownDigraph = generateDigraph();
		rankedDigraphPair = rankDigraph(brownDigraph);
		englishDigraph = generatePCADigraph();
		z408PlainDigraphVector = generateDigraphVector(z408Plain);
		
		// initial column permutation per data given by Stamp
		int[] startingOrder = {11,15,8,4,12,3,9,10,5,13,1,2,0,7,6,14,16};
		int[][] z408Permuted = orderByColumns(z408Plain, startingOrder);
		if(debug)
			printMatrix("column-Permuted z408:", z408Permuted, true);
		
		printMatrix("Original z408 plain-Text", z408Plain, true);
		
		File f = new File("results_pca.txt");
		try {
			FileWriter writer = new FileWriter(f, false);
			writer.write("");
			for (int i = 32; i < 33; i++) {
				int nEigvalues = i;
				double accuracy = executeJakobsenAlgorithm(englishDigraph, z408Permuted, nEigvalues);
				String message = "#eigvalues: " + nEigvalues + " accuracy: " + String.format("%.3f", accuracy);
				writer.write(message + System.getProperty( "line.separator" ));
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * systematically permute columns by first swapping adjacent
	 * then every 2, then every 3, etc.. until 1 swaps with 25
	 * If a swap generates a digraph that scores better (lower)
	 * than current score, keep new score/column order and start
	 * from beginning. In this version, we swap the text columns
	 * themselves, not the digraph. A new digraph is generated
	 * after a swap and then scored.
	 * See 9.4.1 (p245 in the book) for details of original version.
	 */
	private double executeJakobsenAlgorithm(double[][] E, int[][] C, int nEigvalues){
		int RANDOM_RESTARTS = 100;
		
		// preserve the original order of C for
		// random restarts and final ordering
		int[][] startingC = duplicateMatrix(C);
		
		int N = C[0].length;
		
		double[] D = generateDigraphVector(C);
		
		// initial column order for cipher text
		// plus working and final copies
		int[] order = new int[C[0].length];
		int[] bestOrder = new int[order.length];
		int[] bestOrderForAccuracy = new int[order.length];
		int[] testOrder = new int[order.length];
		for(int i = 0; i < order.length; i++){
			order[i] = i;
			bestOrder[i] = i;
			bestOrderForAccuracy[i] = i;
			testOrder[i] = i;
		}
		
		
		System.out.println("Training PCA model on Brown Corpus Digraph...");
		
		// From SMILE library. Documentation found here:
		// https://haifengl.github.io/smile/api/java/smile/projection/PCA.html
		
		PCA pca = new PCA(E);
		pca = pca.setProjection(nEigvalues);		
		double[][] scoreMatrix = pca.project(E); // 26 x n_eig
		if (usePCAWithRanking) { // overwrite PCA object for simplicity
			double[][] sortedE = sortForRowDescending2D(E);	
			pca = new PCA(sortedE);
			pca = pca.setProjection(nEigvalues);
			scoreMatrix = pca.project(sortedE); // 26 x n_eig
		}
		System.out.println("Done. Beginning column permutation analysis...");
		
		
		// track all starting and ending permutations from
		// each epoch(random restart) to make sure we don't
		// create a random starting order that matches one
		// we've already used
		ArrayList<int[]> finalOrders = new ArrayList<int[]>();
		finalOrders.add(order);
		
		double[] initialProjectedC = null;		
		if (usePCAWithRanking) {
	    		double[] sortedDPrime = sortForRowDescending1D(D);
	    		initialProjectedC = pca.project(sortedDPrime);
		} else {
			initialProjectedC = pca.project(D);
		}
		double score = scorePCA(scoreMatrix, initialProjectedC);
		double scorePrime = 0;
		double bestScore = score;
		
		String text = "";
		if (verbose) {
			printMatrix("\nStarting text: ", C, true);
			//TextParse.appendToFile("Starting score: " + score);
			TextParse.appendToFile("Starting order: ");
			for(int i = 0; i < order.length; i++){
				text += (order[i] + (i < (order.length-1) ? ", ":"\n"));
			}
			TextParse.appendToFile(text);			
		}
		
		double maxAccuracy = 0.0;
		for(int epoch = 0; epoch < RANDOM_RESTARTS; epoch++){
			// if after the first run, we need to
			// generate a new random column order
			// being careful not to generate an order
			// which matches one we know we are already
			// at the "top" of the hill for
			// 
			if(epoch > 0){
				if (verbose) {
					TextParse.appendToFile("generating random order...");	
				}
				
				order = getPermutation(startingC[0].length);
				finalOrders.add(order);
				C = orderByColumns(startingC, order);
				for(int i = 0; i < order.length; i++){
					testOrder[i] = order[i];
				}
				
				// score the intial random order
				D = generateDigraphVector(C);
				double[] projectedC = null;
	        		if (usePCAWithRanking) {
	            		double[] sortedDPrime = sortForRowDescending1D(D);
	            		projectedC = pca.project(sortedDPrime);
	        		} else {
	        			projectedC = pca.project(D);
	        		}
	        		
	            score = scorePCA(scoreMatrix, projectedC);
				
				//printMatrix("\nStarting epoch text: ", C, true);
	            if (verbose) {
	            		TextParse.appendToFile("Starting epoch score: " + score);
					TextParse.appendToFile("Starting epoch order: ");
					text = "";
					for(int i = 0; i < order.length; i++){
						text += (order[i] + (i < (order.length-1) ? ", ":"\n"));
					}
					TextParse.appendToFile(text);
	            }
				
			} // end epoch setup
			
			int a = 1;
			int b = 1;
			while (b < N-1){
				//System.out.println("a: " + a + ", b: " + b);
				
				// reset testOrder to match order
				for(int i = 0; i < order.length; i++){
					testOrder[i] = order[i];
				}
				int i = a-1;
				int j = a+b;
//				System.out.print("i: " + i + ", j: " + j + "  ");
//				if(iteration % 20 == 0)
//					System.out.println("");
				int[][] cPrime = swapColumns(C, i, j);
				testOrder = swapOrder(testOrder, i, j);
				
				double[] dPrime = generateDigraphVector(cPrime);

				double[] projectedC = null;
	        		if (usePCAWithRanking) {
	            		double[] sortedDPrime = sortForRowDescending1D(dPrime);
	            		projectedC = pca.project(sortedDPrime);
	        		} else {
	        			projectedC = pca.project(dPrime);
	        		}
	        		
                scorePrime = scorePCA(scoreMatrix, projectedC);
				
				if(scorePrime < score){
					score = scorePrime;
					D = dPrime;
					C = cPrime;
					order = swapOrder(order, i, j);
					a = b = 1;
					//System.out.println("\n");
				} else {
					a++;
					//System.out.println(a);
					if(a+b >= N){
						a = 1;
						b++;
						//System.out.println(a);
					}
				}
			}

			if (verbose) {
				TextParse.appendToFile("Epoch " + (epoch + 1) + " complete.");
				TextParse.appendToFile("Winning score: " + score);
				TextParse.appendToFile("Winning order: ");
				text = "";
				for(int i = 0; i < order.length; i++){
					text += (order[i] + (i < (order.length-1) ? ", ":"\n"));
				}
				TextParse.appendToFile(text);	
			}
			
			int[][] winText = orderByColumns(startingC, order);
			double acurracy = getPermutationAccuracy(winText);
			
			System.out.println("Epoch " + (epoch + 1) + " complete. Best accuracy: " + String.format("%.3f", maxAccuracy) + " " + String.format("%.3f", score));
			File f = new File("results_acc_score.txt");
			try {
				FileWriter writer = new FileWriter(f, true);
				String msg = String.format("%.4f", acurracy) + " " + String.format("%.4f", score);
				writer.write(msg + System.getProperty( "line.separator" ));
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (acurracy >= maxAccuracy) {
				maxAccuracy = acurracy;
				bestOrderForAccuracy = order.clone();
			}
			finalOrders.add(order);
			
			if(bestScore > score){
				bestScore = score;
				for(int i = 0; i < order.length; i++){
					bestOrder[i] = order[i];
				}
			}
			System.out.println("Epoch " + (epoch + 1) + " complete. Best accuracy: " + String.format("%.3f", maxAccuracy) + " " + String.format("%.3f", score));
		}
		
		
		System.out.println("\nJakobsen's algorithm completed");
		TextParse.appendToFile("\nJakobsen's algorithm completed");
		TextParse.appendToFile("Winning score: " + bestScore);
		TextParse.appendToFile("Winning order: ");
		TextParse.appendToFile("Best accuracy: " + maxAccuracy);
		text = "";
		for(int i = 0; i < bestOrder.length; i++){
			text += (bestOrder[i] + (i < (bestOrder.length-1) ? ", ":"\n"));
		}
		TextParse.appendToFile(text);
		
		int[][] finalText = orderByColumns(startingC, bestOrder);
		double accuracy = getPermutationAccuracy(finalText);
		printMatrix("\nText Result: ", finalText, true);
		
		int[][] finalTextForAccuracy = orderByColumns(startingC, bestOrderForAccuracy);
		printMatrix("\nText Result For Accuracy: ", finalTextForAccuracy, true);
		printPlainWithAscii("\nText Result For Accuracy: ", finalTextForAccuracy, true);
		
		return maxAccuracy;
	}
	
	/*
	 * returns the avg number of columns
	 * of the given text permutation are
	 * in the correct column position compared
	 * to the solved z408 text
	 */
	private double getPermutationAccuracy(int[][] m){
		double result = 0.0;
		int[][] mT = getTranspose(m);
		int[][] solution = getTranspose(z408Plain);
		int correct = 0;
		
		for(int i = 0; i < mT.length; i++){
			boolean same = true;
			for(int j = 0; j < mT[i].length/2; j++){
				if(mT[i][j] != solution[i][j])
					same = false;
			}
			if(same)
				correct++;
		}
		result = (double)(correct)/(double)solution.length;
		if (verbose)
			TextParse.appendToFile("Permutation accuracy: " + correct + "/" + solution.length + " = " +
					formatter.format(result));
		return result;
	}
	
	/*
	 * returns true if the given int[]
	 * is not among those we have collected
	 * and false otherwise
	 */
	private boolean orderNotTried(int[] order, ArrayList<int[]> finalOrders){
		Iterator<int[]> iter = finalOrders.iterator();
		int counter = 1;
		while(iter.hasNext()){
			int[] next = iter.next();
			boolean same = true;
			for(int i =0; i<next.length; i++){
				if(order[i] != next[i]){
					same = false;
					break;
				}
				
			}
			if(same){
				TextParse.appendToFile("(failed at iteration " + counter + ")...\n");
				return false;
			}
			counter++;
		}
		return true;
	}
	
	/*
	 * generates a random int array from
	 * 0 to the given n - 1
	 */
	private int[] getPermutation(int n){
		Random rand = new Random(System.currentTimeMillis());
		int[] result = new int[n];
		boolean[] taken = new boolean[n];
		int count = 0;
		do{
			int choice = (int)(rand.nextDouble() * n);
			while(taken[choice])
				choice = ((choice + 1) % n);
			result[count] = choice;
			taken[choice] = true;
			count++;
		} while(count < result.length);
		return result;
	}
	
	/*
	 * makes a deep copy of the given matrix
	 */
	private int[][] duplicateMatrix(int[][] C){
		int[][] result = new int[C.length][];
		for(int i =0; i < result.length; i++){
			result[i] = new int[C[i].length];
			for(int j = 0; j < result[i].length; j++){
				result[i][j] = C[i][j];
			}
		}
		return result;
	}
	
	/*
	 * get dT: (transpose the given matrix D)
	 * then swap row i with row j
	 * then return the transpose dT = D
	 */
	private double[][] swapColumns(double[][] D, int i, int j){
		double[][] result = getTranspose(D);
		double[] temp = result[i];
		result[i] = result[j];
		result[j] = temp;
		return getTranspose(result);
	}
	
	/*
	 * The int overload version of the method above
	 */
	private int[][] swapColumns(int[][] C, int i, int j){
		int[][] result = getTranspose(C);
		int[] temp = result[i];
		result[i] = result[j];
		result[j] = temp;
		return getTranspose(result);
	}
	
	/* 
	 * 1-dimensional version of the swap
	 * so we can track column position
	 * from original order
	 */
	private int[] swapOrder(int[] order, int i, int j){
		int temp = order[i];
		order[i] = order[j];
		order[j] = temp;
		return order;
	}
	
	/*
	 * Reads in Brown Corpus and creates an english language 
	 * digraph based on the first 1M characters read in from
	 * the Brown Corpus
	 */
	private double[][] generateDigraph(){
		double[][] result = null;
		int dGraphSize = 26;
		
		// read in text from file 
		// map each char to int values
		// (a = 0, b = 1, etc...)
		//
		char[] digraphInput = TextParse.getText("DigraphText.txt", 1000000, false);
		for(int i = 0; i < digraphInput.length; i++){
			digraphInput[i] = (char) (digraphInput[i] - 97);
		}
				
		// iterate through char array and
		// generate counts for letter pairings
		//
		result = new double[dGraphSize][];
		for(int i = 0; i < result.length; i++){
			result[i] = new double[dGraphSize];
		}
		int last = digraphInput[0];
		for(int i = 1; i < digraphInput.length; i++){
			result[last][digraphInput[i]]++;
			last = digraphInput[i];
					
		}
		
		
		// digraph calculation:
		// normalize each count to reflect
		// statistical probability of one
		// letter following another
		//
		for(int i = 0; i < result.length; i++){
			double rowSum = 0;
			// add 5 to each index (to eliminate zeros)
			// and sum the row
			for(int j = 0; j < result[i].length; j++){
				result[i][j] += 5;
				rowSum += result[i][j];
			}
			
			// normalize each index by its rowSum
			// (makes each row stochastic)
			for(int j = 0; j < result[i].length; j++){
				result[i][j] = result[i][j]/rowSum;
			}
		}

		return result;
	}
	
	/*
	 * Reads in 1M chars from the Brown Corpus
	 * divides into 100 chunks of 10k chars,
	 * generates a digraph from each chunk and,
	 * for each digraph, takes the rows and lines
	 * them up into one long vector. 
	 * Returns an array of these 100
	 * digraph-vectors
	 */
	private double[][] generatePCADigraph(){
		int dGraphSize = 26;
		int chunks = 100;
		double[][] result = new double[chunks][];
		
		// read in text from file and
		// map each char to int values
		// (a = 0, b = 1, etc...)
		//
		char[] digraphInput = TextParse.getText("DigraphText.txt", 1000001, false);
		for(int i = 0; i < digraphInput.length; i++){
			digraphInput[i] = (char) (digraphInput[i] - 97);
		}
		
		// break the 1M chars into 100 10k chunks
		// create a digraph from each chunk
		// line the digraph into a single vector
		// and add it to the array to return
		//
		int count = 0;
		for(int i = 0; i < chunks; i++){
			
			// initialize dGraph structure
			double[][] dGraph = new double[dGraphSize][];
			for(int j = 0; j < dGraph.length; j++){
				dGraph[j] = new double[dGraphSize];
			}
			
			// iterate through char array and
			// generate counts for letter pairings
			//
			System.out.println(count);
			int last = digraphInput[count];
			count++;
			for(int j = 0; j < (digraphInput.length/chunks); j++){
				dGraph[last][digraphInput[count]]++;
				last = digraphInput[count];
				count++;
			}
			count--;
			
			// digraph calculation:
			// normalize each count to reflect
			// statistical probability of one
			// letter following another
			//
			for(int j = 0; j < dGraph.length; j++){
				double rowSum = 0;
				// add 5 to each index (to eliminate zeros)
				// and sum the row
				for(int k = 0; k < dGraph[j].length; k++){
					dGraph[j][k] += 5;
					rowSum += dGraph[j][k];
				}
				
				// normalize each index by its rowSum
				// (makes each row stochastic)
				for(int k = 0; k < dGraph[j].length; k++){
					dGraph[j][k] = dGraph[j][k]/rowSum;
				}
			}
			
			double[] dGraphVector = null;
			if (!useRankedDigraph) {
				int dVecSize = (dGraphSize * dGraphSize);
				dGraphVector = new double[dVecSize];
				int dCounter = 0;
				for(int j = 0; j < dGraph.length; j++){
					for(int k = 0; k < dGraph[j].length; k++){
						dGraphVector[dCounter++] = dGraph[j][k];
					}
				}	
			} else {
				int dVecSize = rankedDigraphPair.size();
				dGraphVector = new double[dVecSize];
				for (int j = 0; j < rankedDigraphPair.size(); j++) {
					Integer[] pair = rankedDigraphPair.get(j);
					dGraphVector[j] = dGraph[pair[0]][pair[1]];
				}	
			}
			
			result[i] = dGraphVector;
		} // for each chunk
				
		return result;
	}
	
	/*
	 * generates a digraph from the given int[][],
	 * which is a matrix of values
	 * (a = 0, b = 1, c = 2, etc..)
	 * mapped from the cipherText
	 */
	private double[][] generateDigraph(int[][] digraphInput){
		double[][] result = null;
		int dGraphSize = 26;
		
		// initialize digraph structure as 26x26 matrix
		result = new double[dGraphSize][];
		for(int i = 0; i < result.length; i++){
			result[i] = new double[dGraphSize];
		}
		
		int last = digraphInput[0][0];
		for(int i = 0; i < digraphInput.length; i++){
			for(int j = 0; j < digraphInput[i].length; j++){
				if(i == 0 && j == 0){j++;} // skip very first element
				result[last][digraphInput[i][j]]++;
				last = digraphInput[i][j];
			}
		}

		for(int i = 0; i < result.length; i++){
			double rowSum = 0;
			for(int j = 0; j < result[i].length; j++){
				result[i][j] += 5;
				rowSum += result[i][j];
			}
			for(int j = 0; j < result[i].length; j++){
				result[i][j] = result[i][j]/rowSum;
			}
		}
	
		return result;
	}
	
	/*
	 * generates a digraph vector 
	 * from the given int[][],
	 * which is a matrix of values
	 * (a = 0, b = 1, c = 2, etc..)
	 * mapped from the cipherText
	 */
	private double[] generateDigraphVector(int[][] digraphInput){
		double[][] dGraph = null;
		int dGraphSize = 26;
		
		// initialize digraph structure as 26x26 matrix
		dGraph = new double[dGraphSize][];
		for(int i = 0; i < dGraph.length; i++){
			dGraph[i] = new double[dGraphSize];
		}
		
		int last = digraphInput[0][0];
		for(int i = 0; i < digraphInput.length; i++){
			for(int j = 0; j < digraphInput[i].length; j++){
				if(i == 0 && j == 0){j++;} // skip very first element
				dGraph[last][digraphInput[i][j]]++;
				last = digraphInput[i][j];
			}
		}

		for(int i = 0; i < dGraph.length; i++){
			double rowSum = 0;
			for(int j = 0; j < dGraph[i].length; j++){
				dGraph[i][j] += 5;
				rowSum += dGraph[i][j];
			}
			for(int j = 0; j < dGraph[i].length; j++){
				dGraph[i][j] = dGraph[i][j]/rowSum;
			}
		}
		
		double[] result = null;
		if (!useRankedDigraph) {
			int dVecSize = (dGraphSize * dGraphSize);
			result = new double[dVecSize];
			int dCounter = 0;
			for(int j = 0; j < dGraph.length; j++){
				for(int k = 0; k < dGraph[j].length; k++){
					result[dCounter++] = dGraph[j][k];
				}
			}	
		} else {
			int dVecSize = rankedDigraphPair.size();
			result = new double[dVecSize];
			for (int i = 0; i < rankedDigraphPair.size(); i++) {
				Integer[] pair = rankedDigraphPair.get(i);
				result[i] = dGraph[pair[0]][pair[1]];
			}	
		}
		
		return result;
	}

	/*
	 * generates a score based on the sum
	 * of the absolute values of the distance
	 * between corresponding digraph elements.
	 * Formula: Sum[i,j] |d[ij] - e[ij]|
	 * see equation 9.3 in book, page 247
	 */
	private double scoreDigraphs(double[][] d1, double[][] d2){
		double result = 0.0;
		for(int i = 0; i < d1.length; i++){
			for(int j = 0; j < d1[i].length; j++){
				result += Math.abs((d1[i][j] - d2[i][j]));
			}
		}
		return result;
	}
	
	private double scorePCA(double[][] scoringMatrix, double[] y) {
		double minDist = Double.MAX_VALUE;
		for (int row = 0; row < scoringMatrix.length; row++) {
			minDist = Math.min(minDist, getL2Distance(scoringMatrix[row], y));
		}	
		return minDist;
	}
	
	
	/*
	 * swaps the columns of the given matrix of cipher
	 * text m based on the column order of the given
	 * int[] order
	 */
	private int[][] orderByColumns(int[][] m, int[] order){
		int[][] mT = getTranspose(m);
		int[][] result = new int[mT.length][];
		for(int i = 0; i < order.length; i++){
			result[i] = new int[mT[order[i]].length];
			for(int j = 0; j < result[i].length; j++){
				result[i][j] = mT[order[i]][j];
			}
		}
		return getTranspose(result);
	}
	
	/*
	 * used when swapping or ordering text
	 * matrix by column. Changes the columns
	 * to rows which makes it easier to swap,
	 * Then gets the transpose again before 
	 * returning result in correct orientation
	 */
	private int[][] getTranspose(int[][] m){
		int[][] result = new int[m[0].length][];
		for(int i = 0; i < result.length; i++){
			result[i] = new int[m.length];
		}
		
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				result[j][i] = m[i][j];
			}
		}
		return result;
	}
	
	/*
	 * int overload version of the method above
	 */
	private double[][] getTranspose(double[][] m){
		double[][] result = new double[m[0].length][];
		for(int i = 0; i < result.length; i++){
			result[i] = new double[m.length];
		}
		
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				result[j][i] = m[i][j];
			}
		}
		return result;
	}
	
	/*
	 * prints the given double matrix with
	 * the given header s
	 */
	private void printMatrix(String s, double[][] m, boolean toFile){
		System.out.println(s);
		if(toFile)
			TextParse.appendToFile(s);
		for(int i = 0; i < m.length; i++){
			String line = "";
			for(int j = 0; j < m[i].length; j++){
				line += (formatter.format(m[i][j]) + (j == (m[i].length-1) ? "":", "));
			}
			//System.out.println(line);
			if(toFile)
				TextParse.appendToFile(line);
		}
	}
	
	/*
	 * prints the given int matrix with the
	 * given header s
	 */
	private void printMatrix(String s, int[][] m, boolean toFile){
		//System.out.println(s);
		if(toFile)
			TextParse.appendToFile(s);
		for(int i = 0; i < m.length; i++){
			String line = "";
			for(int j = 0; j < m[i].length; j++){
				line += (m[i][j] + (j == (m[i].length-1) ? "":", "));
			}
			//System.out.println(line);
			if(toFile)
				TextParse.appendToFile(line);
		}
	}
	
	private void printPlainWithAscii(String s, int[][] m, boolean toFile) {
		if (toFile) {
			TextParse.appendToFile(s);
			for (int i = 0; i < m.length; i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < m[i].length; j++) {
					char letter[] = Character.toChars(m[i][j] + (int)('A'));
					sb.append(String.valueOf(letter[0]) + " ");
				}
				TextParse.appendToFile(sb.toString());
			}	
		}
	}
	
	/*
 	 * compute euclidean distance 
 	 */
 	private double getL2Distance(double[] array1, double[] array2) {
         double sum = 0.0;
         for(int i = 0; i < array1.length; i++) {
            sum += Math.pow((array1[i] - array2[i]), 2.0);
         }
         return Math.sqrt(sum);
    }
	
 	/*
 	 * sort the row vector
 	 */
 	private double[][] sortForRowDescending2D(double[][] src) {
 		int height = src.length;
 		int width = src[0].length;
		double[][] dst = new double[height][width];
		for (int i = 0; i < height; i++)
			dst[i] = sortForRowDescending1D(dst[i]);
		
		return dst;
 	}
 	
 	private double[] sortForRowDescending1D(double[] src) {
 		Double[] tmp = new Double[src.length];
 		for (int i = 0; i < src.length; i++) tmp[i] = src[i];
 		Arrays.sort(tmp, Collections.reverseOrder());
 		
 		double[] dst = new double[src.length];
		for (int i = 0; i < src.length; i++) dst[i] = tmp[i];
		return dst;
 	}
 	
 	private List<Integer[]> rankDigraph(double[][] digraph) {
		int dGraphSize = 26;
		List<DigraphProb> digraphObjList = new ArrayList<>();
		for (int i = 0; i < dGraphSize; i++) {
			for (int j = 0; j < dGraphSize; j++) {
				digraphObjList.add(new DigraphProb(i, j, digraph[i][j]));
			}
		}
		
		Collections.sort(digraphObjList, new Comparator<DigraphProb>() {
			@Override
			public int compare(DigraphProb o1, DigraphProb o2) {
				if (o2.prob > o1.prob) return 1;
				else if (o2.prob < o1.prob) return -1;
				else return 0;
			}
		});
		
		List<Integer[]> sortedDigraph = new ArrayList<>();
		int nSelectedProbs = 100;
		for (int k = 0; k < nSelectedProbs; k++) {
			Integer[] pair = new Integer[2];
			pair[0] = digraphObjList.get(k).i;
			pair[1] = digraphObjList.get(k).j;
			sortedDigraph.add(pair);
		}
		return sortedDigraph;
	}
}
