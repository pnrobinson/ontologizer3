package org.jax.ontologizer.command;

import org.monarchinitiative.phenol.analysis.mgsa.MgsaCalculation;
import org.monarchinitiative.phenol.analysis.mgsa.MgsaGOTermsResultContainer;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.util.Comparator;

public class MgsaCommand extends OverrepresentationCommand {
    @Override
    public void run() {
        init();
        System.out.println();
        System.out.println("[INFO] Demo: MGSA analysis");
        System.out.println();
        int mcmcSteps = 2000000;
        MgsaCalculation mgsa = new MgsaCalculation(this.gontology, this.associationContainer, mcmcSteps);
        MgsaGOTermsResultContainer result = mgsa.calculateStudySet(studySet);
        result.dumpToShell();
    }
}
