package org.jax.ontologizer.analysis;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoMatrix {

    private Map<TermId, Integer> goTermCountMatrix;

    public GoMatrix(List<GoResultSet> results) {
        goTermCountMatrix = new HashMap<>();
        for (GoResultSet res : results) {
            for (TermId tid: res.getGoTerms()) {
                goTermCountMatrix.putIfAbsent(tid, 0);
                goTermCountMatrix.merge(tid, 1, Integer::sum); // increment by 1
            }
        }
    }

    public void dump() {
        int min = 5;
        for (Map.Entry<TermId, Integer> e: goTermCountMatrix.entrySet()) {
            if (e.getValue() > min) {
                System.out.println(e.getKey().getValue() + ": " + e.getValue());
            }
        }
    }
}
