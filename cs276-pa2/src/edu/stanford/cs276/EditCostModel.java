package edu.stanford.cs276;

import java.io.Serializable;

public interface EditCostModel extends Serializable {

    /**
     * P(R|Q) - see PA2 description for notation.
     *
     * @param Q - the intended query
     * @param R - the actual but possibly corrupted query
     * @param distance
     * @return
     */
    public double editProbability(String Q, String R, int distance);
}
