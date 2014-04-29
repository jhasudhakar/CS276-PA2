package edu.stanford.cs276;

public class UniformCostModel implements EditCostModel {

    private static double UNIFORM_EDIT_COST = 0.01;

    @Override
    public double editProbability(String original, String R, int distance) {
        if (original.equals(R)) {
            return Math.log(0.9);
        } else {
            return distance * Math.log(UNIFORM_EDIT_COST);
        }
    }
}
