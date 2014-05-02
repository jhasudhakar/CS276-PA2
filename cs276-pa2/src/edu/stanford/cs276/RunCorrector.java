package edu.stanford.cs276;

import edu.stanford.cs276.edit.EditDistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class RunCorrector {

    public static LanguageModel languageModel;
    public static NoisyChannelModel nsm;
    public static CandidateGenerator cg;

    private static double mu = 1;


    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        // Parse input arguments
        String uniformOrEmpirical = null;
        String queryFilePath = null;
        String goldFilePath = null;
        String extra = null;
        BufferedReader goldFileReader = null;
        if (args.length == 2) {
            // Run without extra and comparing to gold
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
        }
        else if (args.length == 3) {
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
            if (args[2].equals("extra")) {
                extra = args[2];
            } else {
                goldFilePath = args[2];
            }
        }
        else if (args.length == 4) {
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
            extra = args[2];
            goldFilePath = args[3];
        }
        else {
            System.err.println(
                    "Invalid arguments.  Argument count must be 2, 3 or 4" +
                    "./runcorrector <uniform | empirical> <query file> \n" +
                    "./runcorrector <uniform | empirical> <query file> <gold file> \n" +
                    "./runcorrector <uniform | empirical> <query file> <extra> \n" +
                    "./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt extra \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
            return;
        }

        if (goldFilePath != null ){
            goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
        }

        if (uniformOrEmpirical.equals("empirical")) {
            mu = 1.0;
        } else {
            mu = 0.5;
        }

        // Load models from disk
//        System.out.println("Loading language model...");
        languageModel = LanguageModel.load();
//        System.out.println("-- Completed!");
//        System.out.println("Loading noisy channel model...");
        nsm = NoisyChannelModel.load();
//        System.out.println("-- Completed!");

        BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
        nsm.setProbabilityType(uniformOrEmpirical);

        // Load candidate generator
        cg = CandidateGenerator.get();

        int totalCount = 0;
        int yourCorrectCount = 0;
        String query = null;

        /*
         * Each line in the file represents one query.  We loop over each query and find
         * the most likely correction
         */
        double maxSoFar, prob;
        while ((query = queriesFileReader.readLine()) != null) {

            String correctedQuery = query;

            if ("extra".equals(extra)) {
                /*
                 * If you are going to implement something regarding to running the corrector,
                 * you can add code here. Feel free to move this code block to wherever
                 * you think is appropriate. But make sure if you add "extra" parameter,
                 * it will run code for your extra credit and it will run you basic
                 * implementations without the "extra" parameter.
                 */
                correctedQuery = viterbi(query);
            } else {
                Set<String> candidates = cg.getCandidates(query, languageModel);
                maxSoFar = Double.NEGATIVE_INFINITY;
                for (String s : candidates) {
                    if (uniformOrEmpirical.equals("empirical")) {
                        prob = nsm.ecm_.editProbability(query, s, 1);
                    } else {
                        prob = nsm.ecm_.editProbability(query, s, EditDistance.editDistance(query, s));
                    }
                    prob += languageModel.computeProbability(s) * mu;
//                System.out.format("%s, %f\n", s, prob);
                    if (prob > maxSoFar) {
                        maxSoFar = prob;
                        correctedQuery = s;
                    }
                }
            }


            // If a gold file was provided, compare our correction to the gold correction
            // and output the running accuracy
            if (goldFileReader != null) {
                String goldQuery = goldFileReader.readLine();
                if (goldQuery.equals(correctedQuery)) {
//                    System.out.format("%d √: %s\n", totalCount, correctedQuery);
                    yourCorrectCount++;
                } else {
//                    System.out.format("%d x: %s -> %s -> %s\n", totalCount, query, correctedQuery, goldQuery);
                }
            } else {
//                System.out.format("%d: %s\n", totalCount, correctedQuery);
            }
            System.out.println(correctedQuery);
            totalCount++;
        }
        queriesFileReader.close();
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
//        System.out.println(yourCorrectCount);
//        System.out.println(1.0 * yourCorrectCount / totalCount);
//        System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");
    }

    private static String viterbi(String query) throws Exception {
        List<TreeSet<Node>> nodes = new ArrayList<TreeSet<Node>>();
        TreeSet<Node> start = new TreeSet<Node>();
        start.add(new Node("", 0, ""));
        nodes.add(start);
        String[] tokens = query.split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Set<String> candidates = cg.getCandidatesForToken(token, languageModel);
            nodes.add(new TreeSet<Node>());
            /* Candidates have no space */
            for (String cand : candidates) {
                double score_channel = nsm.ecm_.editProbability(token, cand, EditDistance.editDistance(token, cand));
                double score_language;
                String best_path = "";
                double best_so_far = Double.NEGATIVE_INFINITY;
                for (Node prev : nodes.get(i)) {
                    if (i == 0) {
                        score_language = Math.log(languageModel.unigramProbability(cand)) * mu;
                    } else {
                        score_language = Math.log(languageModel.bigramProbability(prev.token, cand)) * mu;
                    }
                    if (prev.score + score_language + score_channel > best_so_far) {
                        best_so_far = prev.score + score_language + score_channel;
                        best_path = prev.path + " " + cand;
                    }
                }
                nodes.get(i + 1).add(new Node(cand, best_so_far, best_path));
            }

            /* Consider splits: split "singledays" -> "single" "days" */
            candidates = cg.getCandidatesForSplits(token, languageModel);
            for (String cand : candidates) {
                double score_channel = nsm.ecm_.editProbability(token, cand, EditDistance.editDistance(token, cand));
                double score_language;
                String best_path = "";
                double best_so_far = Double.NEGATIVE_INFINITY;
                String[] bigram = cand.split("\\s+");
                for (Node prev : nodes.get(i)) {
                    if (i == 0) {
                        score_language = Math.log(languageModel.unigramProbability(bigram[0])) * mu;
                        score_language += Math.log(languageModel.bigramProbability(bigram[0], bigram[1])) * mu;
                    } else {
                        score_language = Math.log(languageModel.bigramProbability(prev.token, bigram[0])) * mu;
                        score_language += Math.log(languageModel.bigramProbability(bigram[0], bigram[1])) * mu;
                    }
                    if (prev.score + score_language + score_channel > best_so_far) {
                        best_so_far = prev.score + score_language + score_channel;
                        best_path = prev.path + " " + cand;
                    }
                }
                nodes.get(i + 1).add(new Node(bigram[1], best_so_far, best_path));
            }

            /* Consider combine: "some thing" -> "something" */
            if (i >= 1) {
                candidates = cg.getCandidatesForToken(tokens[i - 1] + tokens[i], languageModel);
                for (String cand : candidates) {
                    double score_channel = nsm.ecm_.editProbability(tokens[i - 1] + " " + tokens[i], cand, EditDistance.editDistance(tokens[i - 1] + " " + tokens[i], cand));
                    double score_language;
                    String best_path = "";
                    double best_so_far = Double.NEGATIVE_INFINITY;
                    for (Node prev : nodes.get(i - 1)) {
                        if (i - 1 == 0) {
                            score_language = Math.log(languageModel.unigramProbability(cand)) * mu;
                        } else {
                            score_language = Math.log(languageModel.bigramProbability(prev.token, cand)) * mu;
                        }
                        if (prev.score + score_language + score_channel > best_so_far) {
                            best_so_far = prev.score + score_language + score_channel;
                            best_path = prev.path + " " + cand;
                        }
                    }
                    nodes.get(i + 1).add(new Node(cand, best_so_far, best_path));
                }
            }

            /* Reduce the size of nodes */
            while (nodes.get(i + 1).size() > 15) {
                nodes.get(i + 1).remove(nodes.get(i + 1).last());
            }
        }

        return nodes.get(nodes.size() - 1).first().path.trim();
    }

    private static class Node implements Comparable<Node> {
        public String token;
        public double score;
        public String path;
        public Node(String token, double score, String path) {
            this.token = token;
            this.score = score;
            this.path = path;
        }

        @Override
        public int compareTo(Node o) {
            return this.score == o.score ? 0 : (this.score > o.score ? -1 : 1);
        }
    }

}
