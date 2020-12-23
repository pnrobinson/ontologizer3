package org.jax.ontologizer.command;


import org.jax.ontologizer.analysis.GoResult;
import org.jax.ontologizer.exception.Ontologizer3RuntimeException;
import org.jax.ontologizer.io.TabularOutput;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.analysis.mgsa.MgsaCalculation;
import org.monarchinitiative.phenol.analysis.mgsa.MgsaGOTermsResultContainer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.*;
import org.monarchinitiative.phenol.stats.mtc.*;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

import static org.jax.ontologizer.analysis.GoResult.ontologizerHeader;

public class OverrepresentationCommand implements Callable<Integer> {
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

    @CommandLine.Option(names={"-m", "--mtc"}, description = "Specifies the MTC method to use. Possible values are:\n" +
            " \"Benjamini-Hochberg\", \"Benjamini-Yekutieli\", \"Bonferroni\" (default),\n" +
            " \"Bonferroni-Holm\", \"None\" (default), \"Westfall-Young-Single-Step\",\n" +
            " \"Westfall-Young-Step-Down\"")
    public String mtcMethod = "Bonferroni";

    private MultipleTestingCorrection multipleTestingCorrection = new Bonferroni();

    protected double ALPHA = 0.05;

    protected static final Comparator<GoTerm2PValAndCounts> COMPARATOR =
            Comparator.comparing(GoTerm2PValAndCounts::getRawPValue);
    /**
     * An object that represents the set of gene ontology annotations.
     */
    protected GoAssociationContainer associationContainer;

    protected Ontology gontology;
    /**
     * Set of overexpressed or otherwise special genes from an experiment.
     */
    protected StudySet studySet;
    /**
     * Set of all genes investigated in the experiment.
     */
    protected StudySet populationSet;


    private void init() {
        gontology = OntologyLoader.loadOntology(new File(this.goOboPath), "GO");
        int n_terms = gontology.countAllTerms();
        System.out.println("[INFO] parsed " + n_terms + " GO terms.");
        System.out.println("[INFO] parsing  " + goGafPath);
        this.associationContainer = GoAssociationContainer.loadGoGafAssociationContainer(new File(this.goGafPath), gontology);
        studySet = getStudySet(this.study, "study");
        populationSet = getStudySet(this.population, "population");
    }


    /**
     * Check if the study set uses CURIEs, e.g., Uniprot ids to represent genes.
     * If every non-comment line has a semicolon,then we assume the file has CURIEs
     *
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
            if (!item.contains(":")) {
                return false;
            }
        }
        // if all items have a ':', then we will assume they are termids/curies
        return true;
    }

    protected StudySet getStudySet(String path, String name) {
        File f = new File(path);
        if (!f.exists()) {
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


    private PValueCalculation termForTerm() {
        return new TermForTermPValueCalculation(this.gontology,
                this.populationSet,
                this.studySet,
                this.multipleTestingCorrection);
    }

    private PValueCalculation parentChildIntersection() {
        return new ParentChildIntersectionPValueCalculation(this.gontology,
                this.populationSet,
                this.studySet,
                this.multipleTestingCorrection);
    }


    private PValueCalculation parentChildUnion() {
        return new ParentChildUnionPValueCalculation(this.gontology,
                this.populationSet,
                this.studySet,
                this.multipleTestingCorrection);
    }


    private int mgsa() {
        System.out.println("[INFO] MGSA analysis");
        int mcmcSteps = 2000000;
        System.err.println("[NOT IMPLEMENTED YET");
//            MgsaCalculation mgsa = new MgsaCalculation(this.gontology, this.associationContainer, mcmcSteps);
//            MgsaGOTermsResultContainer result = mgsa.calculateStudySet(studySet);
//            result.dumpToShell();
        return 0;
    }

    @Override
    public Integer call() {
        init();
        PValueCalculation pValueCalculation;
        setMtc();
        switch (calculation.toLowerCase()) {
            case "mgsa":
                // special case, handle in its own function
                return mgsa();
            case "term-for-term":
                pValueCalculation = termForTerm();
            case "parent-child-intersection":
                pValueCalculation = parentChildIntersection();
            case "parent-child-union":
                pValueCalculation = parentChildUnion();
            default:
                pValueCalculation = parentChildUnion();
        }
        List<GoTerm2PValAndCounts> pvals = pValueCalculation.calculatePVals();
        pvals.sort(COMPARATOR);
        TabularOutput tab = new TabularOutput(ontologizerHeader(), GoResult.fromGoTerm2PValAndCounts(pvals, gontology));
        tab.dump();
        return 0;
    }

    private void setMtc() {
        switch (this.mtcMethod.toLowerCase()) {
            case "bonferroni":
            case "bonferoni": // spelling error but be tolerant
                this.multipleTestingCorrection = new Bonferroni();
                break;
            case "benjamini-hochberg":
            case "bh":
                this.multipleTestingCorrection = new BenjaminiHochberg();
                break;
            case "benjamini-yekutieli":
            case "by":
                this.multipleTestingCorrection = new BenjaminiYekutieli();
            case "bonferroni-holm":
            case "holm":
                this.multipleTestingCorrection = new BonferroniHolm();
                break;
            case "sidak":
                this.multipleTestingCorrection = new Sidak();
                break;
            case "none":
                this.multipleTestingCorrection = new NoMultipleTestingCorrection();
                break;
            case "Westfall-Young-Single-Step":
            case "Westfall-Young-Step-Down":
                throw new UnsupportedOperationException("Not implemented yet");
        }

    }

}
