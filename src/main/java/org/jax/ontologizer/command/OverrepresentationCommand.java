package org.jax.ontologizer.command;


import org.jax.ontologizer.analysis.GoResult;
import org.jax.ontologizer.exception.Ontologizer3RuntimeException;
import org.jax.ontologizer.io.TabularOutput;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.analysis.mgsa.MgsaCalculation;
import org.monarchinitiative.phenol.analysis.mgsa.MgsaGOTermsResultContainer;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.PValueCalculation;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public  class OverrepresentationCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-g", "--go"}, description = "path to go.obo file")
    protected String goOboPath = "data/go.obo";

    @CommandLine.Option(names = {"-a", "--gaf"}, description = "path to GAF file")
    protected String goGafPath = "data/goa_human.gaf";

    @CommandLine.Option(names = {"-d", "--data"}, description = "path to data download file")
    protected String dataDir = "data";

    @CommandLine.Option(names = {"-s", "--study"}, description = "study set", required = true)
    protected String study;

    @CommandLine.Option(names = {"-p", "--population"}, description = "population set")
    protected String population = "data/population.txt";

    @CommandLine.Option(names = {"-c", "--calculation"}, description = "Method of overrepresentation calculation")
    public String calculation = "Term-for-Term";

    protected double ALPHA = 0.05;

    protected static final Comparator<GoTerm2PValAndCounts> COMPARATOR =
            Comparator.comparing(GoTerm2PValAndCounts::getAdjustedPValue);
    protected GoAssociationContainer associationContainer;

    protected Ontology gontology;

    protected StudySet studySet;
    protected StudySet populationSet;




    protected void init() {
        gontology = OntologyLoader.loadOntology(new File(this.goOboPath), "GO");
        int n_terms = gontology.countAllTerms();
        System.out.println("[INFO] parsed " + n_terms + " GO terms.");
        System.out.println("[INFO] parsing  " + goGafPath);
        this.associationContainer = GoAssociationContainer.loadGoGafAssociationContainer(new File(this.goGafPath),gontology);
        studySet = getStudySet(this.study, "study");
        populationSet = getStudySet(this.population, "population");
    }


    /**
     * Check if the study set uses CURIEs, e.g., Uniprot ids to represent genes.
     * If every non-comment line has a semicolon,then we assume the file has CURIEs
     * @param lines lines of study/population set file
     * @return true if the file has gene ids as CURIEs rather than symbols
     */
    private boolean studySetUsesCuries(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith("#") || line.startsWith("!") || line.startsWith("//")) {
                continue; // skip comments
            }
            String[] fields = line.split("\t");
            String item = fields[0];
            if (! item.contains(":")) {
                return false;
            }
        }
        // if all items have a ':', then we will assume they are termids/curies
        return true;
    }

    protected StudySet getStudySet(String path, String name) {
        File f = new File(path);
        if (! f.exists()) {
            System.err.printf("[ERR] Could not find file %s\n", path);
            System.exit(1);
        }
       try {
           List<String> lines = Files.readAllLines(Paths.get(path),
                   Charset.defaultCharset());
           if (studySetUsesCuries(lines)) {
               Set<TermId> genes = new HashSet<>();
               for (String line : lines) {
                   String[] fields = line.split("\t");
                   TermId geneId = TermId.of(fields[0]);
                   genes.add(geneId);
               }
               return associationContainer.fromGeneIds(genes, name);
           } else {
               // i.e., study set file has gene symbols
                Set<String> symbols = new HashSet<>();
               for (String line : lines) {
                   String[] fields = line.split("\t");
                   symbols.add(fields[0]);
               }
               return associationContainer.fromGeneSymbols(symbols, name);
           }
       } catch (IOException e) {
           throw new Ontologizer3RuntimeException(e.getMessage());
       }
    }

    /**
     * Header for table with TFT.
     * In contrast to Ontologizer, do not show p.min
     */
    private final List<String> tftHeaders =  Arrays.asList("ID", "Pop.total", "Pop.term","Study.total","Study.term","p", "p.adjusted","name");

    private int termForTerm() {
        // List<GoTerm2PValAndCounts>
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.gontology,
                    this.populationSet,
                    this.studySet,
                    new Bonferroni());
        List<GoTerm2PValAndCounts> pvals =  tftpvalcal.calculatePVals();
//                    .stream()
//                    .filter(item -> item.passesThreshold(ALPHA))
//                    .collect(Collectors.toList());
        TabularOutput tab = new TabularOutput(tftHeaders, GoResult.fromGoTerm2PValAndCounts(pvals, gontology));
        tab.dump();
        return 0;
        }

    public void dumpToShell(List<GoTerm2PValAndCounts> pvals) {
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
            Term term = this.gontology.getTermMap().get(tid);
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

    private void mgsa() {
            System.out.println();
            System.out.println("[INFO] Demo: MGSA analysis");
            System.out.println();
            int mcmcSteps = 2000000;
//            MgsaCalculation mgsa = new MgsaCalculation(this.gontology, this.associationContainer, mcmcSteps);
//            MgsaGOTermsResultContainer result = mgsa.calculateStudySet(studySet);
//            result.dumpToShell();

    }

    @Override
    public Integer call() {
        init();
        switch (calculation.toLowerCase()) {
            case "term-for-term":
            default:
                return termForTerm();
        }
    }

}
