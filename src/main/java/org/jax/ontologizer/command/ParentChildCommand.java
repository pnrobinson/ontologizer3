package org.jax.ontologizer.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.ParentChildIntersectionPValueCalculation;
import org.monarchinitiative.phenol.stats.ParentChildPValuesCalculation;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Run one of the parent-child methods
 */
@Parameters(commandDescription = "Parent child analysis")
public class ParentChildCommand extends OverrepresentationCommand {

    public ParentChildCommand(){}

    @Override
    public void run() {
        init();
        MultipleTestingCorrection bonf = new Bonferroni();
        ParentChildPValuesCalculation pcPvalCalc = new ParentChildIntersectionPValueCalculation(gontology,
                associationContainer,
                populationSet,
                studySet,
                bonf);
        List<GoTerm2PValAndCounts> pvals = pcPvalCalc.calculatePVals();
        Collections.sort(pvals, COMPARATOR);
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
            Term term = gontology.getTermMap().get(tid);
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
        }
        System.out.println(String.format("PCI: %d of %d terms were significant at alpha %.7f", n_sig, pvals.size(), ALPHA));
    }


}
