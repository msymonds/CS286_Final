/*
 * Michael Symonds
 * CS286 FINAL
 * 
 * A series of tests and experiements
 * to (attempt to) solve the zodiac340 cipher
 * 
 * Tier0Test examines the zodiac408 cipher
 * at an easy level using plaintext
 * comparing/analyzing digraphs generated
 * from the Brown corpus and the 408text
 * 
 * Will then continue analysis to 
 * include examination of digraph 
 * vectors using a PCA model trained
 * on Brown digraph vectors and tested
 * on z408 plaintext vectors
 */
public class Driver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// this test uses naive distance
		// measurement to score digraphs
		// uncomment to run
		Tier0Test t0 = new Tier0Test();
		//t0.runTest();
		
		// this test uses PCA scoring
		// uncomment to run
		//
		PCATest pcaT = new PCATest();
		pcaT.runTest();
	}

}
