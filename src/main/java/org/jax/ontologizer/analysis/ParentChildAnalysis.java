package org.jax.ontologizer.analysis;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.ParentChildIntersectionPValueCalculation;
import org.monarchinitiative.phenol.stats.ParentChildPValuesCalculation;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParentChildAnalysis extends OverrepresentationAnalysis {

    private final List<GoTerm2PValAndCounts> pvals;

    private final Ontology ontology;
    AssociationContainer associationContainer;
    StudySet populationSet;
    StudySet studySet;
    MultipleTestingCorrection mtc;

    public ParentChildAnalysis(Ontology ontology,
                               AssociationContainer associationContainer,
                               StudySet populationSet,
                               StudySet studySet,
                               MultipleTestingCorrection bonf){
        this.ontology = ontology;
        this.associationContainer = associationContainer;
        this.populationSet = populationSet;
        this.studySet = studySet;
        this.mtc = bonf;
        ParentChildPValuesCalculation pcPvalCalc = new ParentChildIntersectionPValueCalculation(ontology,
                associationContainer,
                populationSet,
                studySet,
                this.mtc);
        pvals = pcPvalCalc.calculatePVals();
        Collections.sort(pvals, COMPARATOR);
    }


    public List<GoTerm2PValAndCounts> getPvals() {
        return pvals;
    }

    public List<GoTerm2PValAndCounts> getSignificantPvals() {
        return pvals.stream().filter(p -> p.getAdjustedPValue() <= ALPHA).collect(Collectors.toList());
    }

    public void dumpToShell() {
        System.err.println("Total number of retrieved p values: " + pvals.size());
        int n_sig = 0;
        int studysize = studySet.getAnnotatedItemCount();
        int popsize = populationSet.getAnnotatedItemCount();
        System.out.println(String.format("Study set: %d genes. Population set: %d genes",
                studysize, popsize));
        for (GoTerm2PValAndCounts item : pvals) {
            double pval = item.getRawPValue();
            double pval_adj = item.getAdjustedPValue();
            TermId tid = item.getItem();
            Term term = ontology.getTermMap().get(tid);
            if (term == null) {
                System.err.println("[ERROR] Could not retrieve term for " + tid.getValue());
                continue;
            }
            String label = term.getName();
            if (pval_adj > ALPHA) {
                continue;
            }
            n_sig++;
            double studypercentage = 100.0 * (double) item.getAnnotatedStudyGenes() / studysize;
            double poppercentage = 100.0 * (double) item.getAnnotatedPopulationGenes() / popsize;
            System.out.println(String.format("%s [%s]: %.2e (adjusted %.2e). Study: n=%d (%.1f%%); population: N=%d (%.1f%%)",
                    label, tid.getValue(), pval, pval_adj, item.getAnnotatedStudyGenes(), studypercentage,
                    item.getAnnotatedPopulationGenes(), poppercentage));
            System.out.println(String.format("PCI: %d of %d terms were significant at alpha %.7f", n_sig, pvals.size(), ALPHA));
        }
    }
}
