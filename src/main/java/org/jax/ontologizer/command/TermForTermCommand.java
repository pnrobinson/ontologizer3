package org.jax.ontologizer.command;

import com.beust.jcommander.Parameter;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;

import java.util.List;

public class TermForTermCommand extends OverrepresentationCommand {


    @Override
    public void run() {
        init();
        MultipleTestingCorrection bonf = new Bonferroni();
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(gontology,
                associationContainer,
                populationSet,
                studySet,
                bonf);
        List<GoTerm2PValAndCounts> pvals = tftpvalcal.calculatePVals();
        System.out.println("[INFO] Total number of retrieved p values: " + pvals.size());
        int n_sig = 0;
        int studysize = studySet.getAnnotatedItemCount();
        int popsize = populationSet.getAnnotatedItemCount();
        System.out.println(String.format("[INFO] Study set: %d genes. Population set: %d genes",
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
        System.out.println(String.format("TFT: %d of %d terms were significant at alpha %.7f", n_sig, pvals.size(), ALPHA));
    }
}
