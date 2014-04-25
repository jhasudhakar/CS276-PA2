package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class LanguageModel implements Serializable {

	private static LanguageModel lm_;
	/* Feel free to add more members here.
	 * You need to implement more methods here as needed.
	 * 
	 * Your code here ...
	 */
	
	
	// Do not call constructor directly since this is a Singleton
	private LanguageModel(String corpusFilePath) throws Exception {
		constructDictionaries(corpusFilePath);
	}

    /**
     * Compute unigram probability of w in the training corpus.
     *
     * @param w
     * @return the probability, 0 if the world doesn't exist in corpus
     */
    public double unigramProbability(String w) {
        // TODO
        return 0.5;
    }

    /**
     * Compute bigram probability of w2 given w1 in the training corpus.
     *
     * @param w1
     * @param w2
     * @return the probability (with possible smoothing applied)
     */
    public double bigramProbability(String w1, String w2) {
        // TODO
        return 0.5;
    }

    /**
     * P(w1, w2, ..., wn) = uP(w1)bP(w2|w1)bP(w3|w2)...bP(wn|wn-1)
     * All words should exist in the training corpus.
     *
     * @param sentence
     * @return
     */
    public double computeProbability(String[] sentence) {
        double prob = Math.log(unigramProbability(sentence[0]));
        for (int i = 1; i < sentence.length; ++i)
            prob += Math.log(bigramProbability(sentence[i-1], sentence[i]));
        return prob;
    }

	public void constructDictionaries(String corpusFilePath)
			throws Exception {

		System.out.println("Constructing dictionaries...");
		File dir = new File(corpusFilePath);
		for (File file : dir.listFiles()) {
			if (".".equals(file.getName()) || "..".equals(file.getName())) {
				continue; // Ignore the self and parent aliases.
			}
			System.out.printf("Reading data file %s ...\n", file.getName());
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = input.readLine()) != null) {
				/*
				 * Your code here
				 */
			}
			input.close();
		}
		System.out.println("Done.");
	}
	
	// Loads the object (and all associated data) from disk
	public static LanguageModel load() throws Exception {
		try {
			if (lm_==null){
				FileInputStream fiA = new FileInputStream(Config.languageModelFile);
				ObjectInputStream oisA = new ObjectInputStream(fiA);
				lm_ = (LanguageModel) oisA.readObject();
			}
		} catch (Exception e){
			throw new Exception("Unable to load language model.  You may have not run build corrector");
		}
		return lm_;
	}
	
	// Saves the object (and all associated data) to disk
	public void save() throws Exception{
		FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
		ObjectOutputStream save = new ObjectOutputStream(saveFile);
		save.writeObject(this);
		save.close();
	}
	
	// Creates a new lm object from a corpus
	public static LanguageModel create(String corpusFilePath) throws Exception {
		if(lm_ == null ){
			lm_ = new LanguageModel(corpusFilePath);
		}
		return lm_;
	}
}
