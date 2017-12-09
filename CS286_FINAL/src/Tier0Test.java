/*
 * Michael Symonds
 * CS286 FINAL
 * 
 * Z408 plaintext column permutation test
 * using jackobsen's algorithm and scoring
 * based on digraph analysis using simple
 * distance formula
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class Tier0Test {
	static boolean debug = true; // toggle to get/hide additional output/status messages
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
	
	//digraph based on brown corpus
	double[][] englishDigraph = generateDigraph();
	// digraph based on plain408 text
	double[][] z408PlainDigraph = generateDigraph(z408Plain);
	
	
	public void runTest(){
		if(debug){
			printMatrix("English Language Digraph:", englishDigraph);
			printMatrix("\n\nZodiac Plain Text Digraph:", z408PlainDigraph);
		}
		
		double score = scoreDigraphs(englishDigraph, z408PlainDigraph);
		System.out.println("\n\n english -> zodiacPlain - Comparative score: " + 
				formatter.format(score));
		
		// initial column permutation per data given by Stamp
		int[] startingOrder = {11,15,8,4,12,3,9,10,5,13,1,2,0,7,6,14,16};
		int[][] z408Permuted = orderByColumns(z408Plain, startingOrder);
		if(debug)
			printMatrix("column-Permuted z408:", z408Permuted);
		
		double[][] z408PermutedDigraph = generateDigraph(z408Permuted);
		if(debug)
			printMatrix("column-Permuted z408 Digraph:", z408PermutedDigraph);
		
		score = scoreDigraphs(englishDigraph, z408PermutedDigraph);
		System.out.println("\n\n english -> zodiacPermuted - Comparative score: " + formatter.format(score));
		
		score = scoreDigraphs(z408PlainDigraph, z408PermutedDigraph);
		System.out.println("\n\n zodiacPlain -> zodiacPermuted - Comparative score: " + formatter.format(score));
		
		executeJakobsenAlgorithm(englishDigraph, z408Permuted);
		
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
	private void executeJakobsenAlgorithm(double[][] E, int[][] C){
		printMatrix("\nStarting text: ", C);
		int N = C[0].length;
		// no K needed, ciphertext is already decoded to plaintext
		double[][] D = generateDigraph(C);
		// initial column order for cipher text
		int[] order = new int[D[0].length];
		for(int i = 0; i < order.length; i++){
			order[i] = i;
		}
		double score = scoreDigraphs(E, D);
		int a = 1;
		int b = 1;
		
		while (b < N-1){
			//System.out.println("a: " + a + ", b: " + b);
			int i = a;
			int j = a+b;
			int[][] cPrime = swapColumns(C, i, j);
			double[][] dPrime = generateDigraph(cPrime);
			double scorePrime = scoreDigraphs(E, dPrime);
			
			if(scorePrime < score){
				score = scorePrime;
				D = dPrime;
				C = cPrime;
				order = swapOrder(order, i, j);
				a = b = 1;
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
		
		System.out.println("\nJakobsen's algorithm completed");
		System.out.println("Winning order: ");
		int colCount = 0;
		for(int i = 0; i < order.length; i++){
			System.out.print(order[i] + (i < (order.length-1) ? ", ":""));
			if(order[i] == i)
				colCount++;
		}
		System.out.println("\nAccuracy: " + colCount + "/" + 
				order.length + " = " + ((double)colCount/(double)order.length));
		
		//int[][] cResult = orderByColumns(C, order);
		printMatrix("\nText Result: ", C);
		
		
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
	private void printMatrix(String s, double[][] m){
		System.out.println(s);
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				System.out.print(formatter.format(m[i][j]) + (j == (m[i].length-1) ? "":", "));
			}
			System.out.println("");
		}
	}
	
	/*
	 * prints the given int matrix with the
	 * given header s
	 */
	private void printMatrix(String s, int[][] m){
		System.out.println(s);
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				System.out.print(m[i][j] + (j == (m[i].length-1) ? "":", "));
			}
			System.out.println("");
		}
	}
	
	
}
