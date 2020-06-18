package org.jax.ontologizer.analysis;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.annotations.obo.go.GoGeneAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PopulationExtractor {

    private final Set<TermId> populationTermIds;

    public PopulationExtractor(String goGafPath) {
        List<GoGaf21Annotation> annots = GoGeneAnnotationParser.loadAnnotations(goGafPath);
        Set<TermId> genes = new HashSet<>();
        for (GoGaf21Annotation ann : annots) {
            TermId termid = TermId.of(ann.getDb(), ann.getDbObjectId());
            genes.add(termid);
        }
        populationTermIds =  ImmutableSet.copyOf(genes);
    }

    public Set<TermId> getPopulationTermIds() {
        return populationTermIds;
    }
}
