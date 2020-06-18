package org.jax.ontologizer.analysis;

import com.google.common.collect.ImmutableMap;
import org.jax.ontologizer.command.OntologizerCommand;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.annotations.obo.go.GoGeneAnnotationParser;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * For GO analysis, we require the genes to be represented as TermIds. However, we want to allow the user to
 * use files with gene symbols. This class takes an input file representing a population or a study set and
 * returns a {@link org.monarchinitiative.phenol.analysis.StudySet} object based on the corresponding TermIds.
 * It uses the information in the GoGaf file to translate from gene symbols to database object ids.
 * If the input file already has TermIds, then we use them directly.
 * @author Peter Robinson
 */
public class Symbol2Id {

    private final Map<String, TermId> symbolToTermIdMap;
    private final String studySetName;
    private final AssociationContainer associationContainer;
    private final Ontology ontology;
    private final StudySet study;

    public Symbol2Id(String path, String goGafPath, Ontology ontology, String name) {
        List<GoGaf21Annotation> annots = GoGeneAnnotationParser.loadAnnotations(goGafPath);
        this.associationContainer = AssociationContainer.loadGoGafAssociationContainer(goGafPath);
        this.ontology = ontology;
        this.studySetName = name;
        symbolToTermIdMap = new HashMap<>();
        for (GoGaf21Annotation a : annots) {
            TermId tid = TermId.of(a.getDb(), a.getDbObjectId());
            String symbol = a.getDbObjectSymbol();
            symbolToTermIdMap.put(symbol, tid);
        }
        boolean usesTermId = fileHasTermIds(path);
        if (usesTermId) {
            study = parseTermIdFile(path);
        } else {
            study = parseGeneSymbolFile(path);
        }
    }

    public StudySet getStudy() {
        return study;
    }

    private StudySet parseTermIdFile(String path) {
        Set<TermId> termIds = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                TermId uniprotId = TermId.of(fields[0]);
                termIds.add(uniprotId);
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not load study set: " + e.getMessage());
        }
        Map<TermId, DirectAndIndirectTermAnnotations> studyAssociations = associationContainer.getAssociationMap(termIds, ontology);
        return new StudySet(termIds, this.studySetName, studyAssociations);
    }



    private boolean fileHasTermIds(String path) {
        int symbol = 0;
        int termid = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.startsWith("%") || line.startsWith("!")) {
                    continue; // skip all kinds of header
                }
                if (line.isEmpty()) {
                    continue;
                }
                // assume line has one or possible multiple tab separated fields with the first being the gene symbol or id
                String [] fields = line.split("\t");
                String item = fields[0];
                if (item.contains(":")) {
                    termid++;
                } else {
                    symbol++;
                }
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not open file: " + e.getMessage());
        }
        if (symbol == 0 && termid>0) {
            return true;
        } else if (symbol > 0 && termid == 0) {
            return false;
        } else if (symbol == 0 && termid == 0) {
            throw new PhenolRuntimeException("No genes found");
        } else {
            System.out.printf("Symbols: %d termids: %d", symbol, termid);
            System.out.printf("Bad file:" + path);
            throw new PhenolRuntimeException("Mixed gene symbols/term ids not allowed! ");
        }
    }


    private StudySet parseGeneSymbolFile(String path) {
        Set<TermId> termIds = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                String symbol = fields[0];
                if (! this.symbolToTermIdMap.containsKey(symbol)) {
                    System.err.println("[WARNING] Could not find symbol " + symbol);
                    continue;
                }
                TermId uniprotId = this.symbolToTermIdMap.get(symbol);
                termIds.add(uniprotId);
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not load study set: " + e.getMessage());
        }
        Map<TermId, DirectAndIndirectTermAnnotations> studyAssociations = associationContainer.getAssociationMap(termIds, ontology);
        return new StudySet(termIds, this.studySetName, studyAssociations);
    }

}
