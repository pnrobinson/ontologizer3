package org.jax.ontologizer.analysis;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.PValueCalculation;

import java.util.ArrayList;
import java.util.List;

public class GoResult implements Comparable<GoResult> {

    private final double raw_p;

    private final double adj_p;

    private final int annotatedStudyGenes;

    private final int totalStudyGenes;

    private final int annotatedPopulationGenes;

    private final int totalPopulationGenes;

    private final String goId;

    private final String goLabel;

    public GoResult(GoTerm2PValAndCounts pval, String label) {
      //  pval.
        this.raw_p = pval.getRawPValue();
        this.adj_p = pval.getAdjustedPValue();
        this.annotatedStudyGenes = pval.getAnnotatedStudyGenes();
        this.annotatedPopulationGenes = pval.getAnnotatedPopulationGenes();
        this.goId = pval.getItem().getValue();
        this.goLabel = label;
        this.totalStudyGenes = pval.getTotalStudyGenes();
        this.totalPopulationGenes = pval.getTotalPopulationGenes();
    }

    /**
     * Get row similar to Ontologizer
     * ID	Pop.total	Pop.term	Study.total	Study.term	p	p.adjusted	p.min	name
     * GO:0044419	3460	556	60	25	1.6603329636585245E-6	0.0049179062383565494	0.0	"biological process involved in interspecies interaction between organisms"
     */
    public List<String> getOntologizerRow() {
        List<String> row = new ArrayList<>();
        row.add(this.goId);
        row.add(String.valueOf(this.totalPopulationGenes));
        row.add(String.valueOf(this.annotatedPopulationGenes));
        row.add(String.valueOf(this.totalStudyGenes));
        row.add(String.valueOf(this.annotatedStudyGenes));
        row.add(String.valueOf(this.raw_p));
        row.add(String.valueOf(this.adj_p));
        row.add(this.goLabel);
        return row;
    }

    public static List<GoResult> fromGoTerm2PValAndCounts(List<GoTerm2PValAndCounts> pvals, Ontology ontology) {
        List<GoResult> results = new ArrayList<>();
        for (GoTerm2PValAndCounts pval : pvals) {
            TermId go = pval.getItem();
            if (ontology.getTermMap().containsKey(go)) {
                String label = ontology.getTermMap().get(go).getName();
                GoResult gr = new GoResult(pval, label);
                results.add(gr);
            } else {
                GoResult gr = new GoResult(pval, "n/a");
                results.add(gr);
            }
        }
        return results;
    }

    @Override
    public int compareTo(GoResult that) {
        if (this.raw_p < that.raw_p) return 1;
        else if (that.raw_p < this.raw_p) return -1;
        else return 0;
    }
}
