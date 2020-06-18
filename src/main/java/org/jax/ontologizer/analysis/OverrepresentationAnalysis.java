package org.jax.ontologizer.analysis;

import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.util.Comparator;

public class OverrepresentationAnalysis {

    protected double ALPHA = 0.05;

    protected static final Comparator<GoTerm2PValAndCounts> COMPARATOR =
            Comparator.comparing(GoTerm2PValAndCounts::getAdjustedPValue);
}
