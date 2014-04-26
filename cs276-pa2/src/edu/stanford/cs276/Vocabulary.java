package edu.stanford.cs276;

import java.util.Set;

public interface Vocabulary {
    public boolean exists(String word);
    public Set<String> known(Set<String> candidates);
}
