package org.jax.ontologizer.command;

import com.beust.jcommander.Parameter;
import jdk.nashorn.internal.ir.Assignment;
import org.jax.ontologizer.analysis.*;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Go2MatrixCommand extends OntologizerCommand {
    @Parameter(names={"--directory"}, description = "directory that contains study set files", required = true)
    private String directory;

    @Parameter(names={"--pattern"}, description = "pattern with hich to filter the files in the directory")
    private String pattern = null;

    @Override
    public void run() {
        //sex_as_events_gene_set
        System.out.println(directory);
        File dir = new File(directory);
        File[] files;
        if (pattern != null) {
            files = dir.listFiles((d, name) -> name.contains(pattern));
        } else {
            files = dir.listFiles();
        }
        List<GoResultSet> resultList = new ArrayList<>();
        PopulationExtractor extractor = new PopulationExtractor(goGafPath);
        Set<TermId> populationTermIds = extractor.getPopulationTermIds();
        Ontology ontology = OntologyLoader.loadOntology(new File(goOboPath));
        AssociationContainer associationContainer = AssociationContainer.loadGoGafAssociationContainer(goGafPath);
        Map<TermId, DirectAndIndirectTermAnnotations> studyAssociations = associationContainer.getAssociationMap(populationTermIds, ontology);
        StudySet populationSet =  new StudySet(populationTermIds, "population", studyAssociations);
        for (File f : files) {
            String name = f.getName();
            try {
                Symbol2Id symbol2id = new Symbol2Id(f.getAbsolutePath(), goGafPath, ontology, name);
                StudySet studySet = symbol2id.getStudy();
                MultipleTestingCorrection bonf = new Bonferroni();
                ParentChildAnalysis pca = new ParentChildAnalysis(ontology,
                        associationContainer,
                        populationSet,
                        studySet,
                        bonf);
                List<GoTerm2PValAndCounts> sigVals = pca.getSignificantPvals();
                GoResultSet result = new GoResultSet(name, sigVals);
                resultList.add(result);
            } catch (PhenolRuntimeException pre) {
                System.err.println(pre.getMessage());
            }
        }
        GoMatrix matrix = new GoMatrix(resultList);
        matrix.dump();
    }
}
