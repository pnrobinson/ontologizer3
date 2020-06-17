package org.jax.ontologizer.command;

import com.beust.jcommander.Parameter;

import java.io.File;

public abstract  class OntologizerCommand {
    @Parameter(names = {"-g", "--go"}, description = "path to go.obo file")
    protected String goOboPath = "data/go.obo";

    @Parameter(names = {"-a", "--gaf"},  description = "path to GAF file")
    protected String goGafPath = "data/goa_human.gaf";

    @Parameter(names = {"-d", "--data"}, description = "path to data download file")
    protected String dataDir = "data";

    protected OntologizerCommand() {
        if (goOboPath == null) {
            goOboPath = String.format("%s%s%s", this.dataDir, File.separator, "go.obo");
        }
        if (goGafPath == null) {
            goGafPath = String.format("%s%s%s", this.dataDir, File.separator, "goa_human.gaf");
        }
    }


    public abstract void run();


}
