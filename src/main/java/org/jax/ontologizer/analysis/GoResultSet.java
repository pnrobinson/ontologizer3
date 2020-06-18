package org.jax.ontologizer.analysis;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.util.List;
import java.util.Set;

public class GoResultSet {
    private final String name;
    private final Set<TermId> goTerms;

    public GoResultSet(String name, List<GoTerm2PValAndCounts> pvals) {
        this.name = name;
        ImmutableSet.Builder<TermId> builder = new ImmutableSet.Builder<>();
        for (GoTerm2PValAndCounts p : pvals) {
            builder.add(p.getItem());
        }
        goTerms = builder.build();
    }

    public String getName() {
        return name;
    }

    public Set<TermId> getGoTerms() {
        return goTerms;
    }
}
