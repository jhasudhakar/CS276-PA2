package edu.stanford.cs276;

public class UniformCostModel implements EditCostModel {

    private static double UNIFORM_EDIT_COST = 0.1;

    @Override
    public double editProbability(String original, String R, int distance) {
        if (original.equals(R)) {
            return Math.log(1 - UNIFORM_EDIT_COST);
        } else {
            return distance * Math.log(UNIFORM_EDIT_COST);
        }
    }
}
