package org.jax.ontologizer.command;

import com.beust.jcommander.Parameter;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.annotations.obo.go.GoGeneAnnotationParser;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ConvertToDbObjectIdCommand extends OntologizerCommand {
    private static final Logger logger = LoggerFactory.getLogger(ConvertToDbObjectIdCommand.class);

    @Parameter(names={"-i","--input"}, description = "input study set (with gene symbols)", required = true)
    private String input;

    @Parameter(names={"-o","--output"}, description = "output study set (with accession numbers from goa file)", required = true)
    private String output;

    @Override
    public void run() {
        List<GoGaf21Annotation> annots = GoGeneAnnotationParser.loadAnnotations(this.goGafPath);
        Map<String, String> sym2idMap = new HashMap<>();
        for (GoGaf21Annotation ann : annots) {
            String termid = ann.getDb() + ":" + ann.getDbObjectId();
            String sym = ann.getDbObjectSymbol();
            sym2idMap.put(sym, termid);
        }
        // get gene ids from study set
        Set<String> genes = getGeneIds();
        // output corresponding study set with UniProt ids
        int missing = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.output))) {
            for (String g : genes) {
                if (!sym2idMap.containsKey(g)) {
                    missing++;
                    continue;
                }
                String accession = sym2idMap.get(g);
                bw.write(accession + "\n");
            }
        } catch (IOException e ){
            e.printStackTrace();
        }
    }


    private Set<String> getGeneIds() {
        Set<String> genes = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.input))) {
            String line;
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                genes.add(fields[0]);
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not load study set: " + e.getMessage());
        }
        return genes;
    }
}
