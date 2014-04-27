package edu.stanford.cs276;

public class UniformCostModel implements EditCostModel {

    @Override
    public double editProbability(String original, String R, int distance) {
        if (original.equals(R)) {
            return 0.9;
        } else {
            return 0.1;
        }
    }
}
