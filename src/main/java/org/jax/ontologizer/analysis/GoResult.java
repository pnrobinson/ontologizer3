package org.jax.ontologizer.analysis;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.util.ArrayList;
import java.util.Arrays;
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
     * Header for table with TFT.
     * In contrast to Ontologizer, do not show p.min
     */
    private final static List<String> tftHeaders = Arrays.asList("ID", "Pop.total", "Pop.term", "Study.total", "Study.term", "p", "p.adjusted", "name");

    public static List<String> ontologizerHeader() {
        return tftHeaders;
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

    public static List<String> extendedHeader() {
        return extendedHeaderFields;
    }
    private static final List<String> extendedHeaderFields =
            Arrays.asList("name", "ID", "p", "p.adjusted", "study", "pop");


    public List<String> getExtendedRow() {
        List<String> row = new ArrayList<>();
        double studypercentage = 100.0 * (double) this.annotatedStudyGenes / this.totalStudyGenes;
        double poppercentage = 100.0 * (double) this.annotatedPopulationGenes / this.totalPopulationGenes;
        row.add(this.goLabel);
        row.add(this.goId);
        row.add(String.valueOf(this.raw_p));
        row.add(String.valueOf(this.adj_p));
        row.add(String.format("n=%d (%.1f%%)",this.annotatedStudyGenes, studypercentage));
        row.add(String.format("n=%d (%.1f%%)", this.annotatedPopulationGenes , poppercentage));
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
        return Double.compare(that.raw_p, this.raw_p);
    }
}
