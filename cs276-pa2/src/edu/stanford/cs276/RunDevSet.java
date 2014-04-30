package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

/**
 * Created by kavinyao on 4/29/14.
 */
public class RunDevSet {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        // command line arguments
        String dataDir = args[0];
        String modelChoice = args[1];
        String queryFilePath = args[2];
        String goldFilePath = args[3];

        if (dataDir.charAt(dataDir.length() - 1) == '/') {
            // trim trailing slash
            dataDir = dataDir.substring(0, dataDir.length() - 1);
        }

        String trainingCorpus = String.format("%s/corpus", dataDir);
        String editsFile = String.format("%s/edit1s.txt", dataDir);

        System.out.println("Training...");
        System.out.println("Building noisy channel model...");
        NoisyChannelModel ncm= NoisyChannelModel.create(editsFile);
        System.out.println("-- Completed!");
        System.out.println("Building language model...");
        LanguageModel languageModel = LanguageModel.create("interpolation", trainingCorpus);
        System.out.println("-- Completed!");

        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Training time: "+totalTime/1000+" seconds ");

        System.out.println();
        System.out.println("Testing...");
        startTime = System.currentTimeMillis();

        BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
        BufferedReader goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));

        ncm.setProbabilityType(modelChoice);

        // Load candidate generator
        CandidateGenerator cg = CandidateGenerator.get();

        int totalCount = 0;
        int yourCorrectCount = 0;
        String query = null;

        /*
         * Each line in the file represents one query.  We loop over each query and find
         * the most likely correction
         */
        double maxSoFar, prob;
        while ((query = queriesFileReader.readLine()) != null) {

            Set<String> candidates = cg.getCandidates(query, languageModel);
            String correctedQuery = query;
            maxSoFar = Double.NEGATIVE_INFINITY;
            for (String s : candidates) {
                prob = ncm.ecm_.editProbability(query, s, 1); // TODO: Calculate edit distance!
                prob += languageModel.computeProbability(s);
//                System.out.format("%s, %f\n", s, prob);
                if (prob > maxSoFar) {
                    maxSoFar = prob;
                    correctedQuery = s;
                }
            }

            // If a gold file was provided, compare our correction to the gold correction
            // and output the running accuracy
            if (goldFileReader != null) {
                String goldQuery = goldFileReader.readLine();
                if (goldQuery.equals(correctedQuery)) {
                    System.out.format("%d √: %s\n", totalCount, correctedQuery);
                    yourCorrectCount++;
                } else {
                    System.out.format("%d x: %s -> %s\n", totalCount, correctedQuery, goldQuery);
                }
                totalCount++;
            } else {
                System.out.format("%d: %s\n", totalCount, correctedQuery);
            }
        }
        queriesFileReader.close();
        endTime   = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println(yourCorrectCount);
        System.out.println(1.0 * yourCorrectCount / totalCount);
        System.out.println("Testing time: "+totalTime/1000+" seconds ");
    }
}
