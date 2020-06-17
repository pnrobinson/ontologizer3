package org.jax.ontologizer.command;

import com.beust.jcommander.Parameters;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.annotations.obo.go.GoGeneAnnotationParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Parameters(commandDescription = "Create a population file with all annotated genes")
public class UniversePopulationCommand extends OntologizerCommand {

    private final String outputpath = Paths.get(this.dataDir, "population.txt").toAbsolutePath().toString();

    @Override
    public void run() {
       List<GoGaf21Annotation> annots = GoGeneAnnotationParser.loadAnnotations(this.goGafPath);
       Set<String> genes = new HashSet<>();
        for (GoGaf21Annotation ann : annots) {
            String termid = ann.getDb() + ":" + ann.getDbObjectId();
            genes.add(termid);
        }
       try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputpath))) {
            for (String gene : genes) {
               bw.write(gene + "\n");
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
