package org.jax.ontologizer.command;


import com.beust.jcommander.Parameter;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public abstract  class OverrepresentationCommand extends OntologizerCommand {
    @Parameter(names={"-s","--study"}, description = "study set", required = true)
    protected String study;

    @Parameter(names={"-p", "--population"}, description = "population set")
    protected String population = "data/population.txt";

    protected AssociationContainer associationContainer;

    protected Ontology gontology;

    protected double ALPHA = 0.05;

    protected StudySet studySet;
    protected StudySet populationSet;

    protected static final Comparator<GoTerm2PValAndCounts> COMPARATOR =
            Comparator.comparing(GoTerm2PValAndCounts::getAdjustedPValue);


    protected void init() {
        gontology = OntologyLoader.loadOntology(new File(this.goOboPath), "GO");
        int n_terms = gontology.countAllTerms();
        System.out.println("[INFO] parsed " + n_terms + " GO terms.");
        System.out.println("[INFO] parsing  " + goGafPath);
        this.associationContainer = AssociationContainer.loadGoGafAssociationContainer(this.goGafPath);
        List<TermAnnotation> goAnnots = associationContainer.getRawAssociations();
        studySet = getStudySet(this.study, "study");
        populationSet = getStudySet(this.population, "population");
    }


    protected StudySet getStudySet(String path, String name) {
        Set<TermId> genes = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                TermId uniprotId = TermId.of(fields[0]);
                genes.add(uniprotId);
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not load study set: " + e.getMessage());
        }
        Map<TermId, DirectAndIndirectTermAnnotations> studyAssociations = associationContainer.getAssociationMap(genes, gontology);
        return new StudySet(genes, name, studyAssociations);
    }

}
