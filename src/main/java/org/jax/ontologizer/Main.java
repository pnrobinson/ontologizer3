package org.jax.ontologizer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.jax.ontologizer.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    @Parameter(names = {"-h", "--help"}, help = true, description = "display this help message")
    private boolean usageHelpRequested;



    public static void main(String []args) {
        Main main = new Main();

        OntologizerCommand download = new DownloadCommand();
        OntologizerCommand population = new UniversePopulationCommand();
        OntologizerCommand convert = new ConvertToDbObjectIdCommand();
        OntologizerCommand parentchild = new ParentChildCommand();
        OntologizerCommand tft = new TermForTermCommand();
        OntologizerCommand mgsa = new MgsaCommand();
        OntologizerCommand matrix = new Go2MatrixCommand();
        JCommander jc = JCommander.newBuilder()
                .addObject(main)
                .addCommand("download", download)
                .addCommand("population", population)
                .addCommand("convert", convert)
                .addCommand("parentchild", parentchild)
                .addCommand("tft", tft)
                .addCommand("mgsa", mgsa)
                .addCommand("matrix", matrix)
                .build();
        try {
            jc.parse(args);
        } catch (ParameterException pe) {
            System.err.printf("[ERROR] Could not start ontologizer: %s\n", pe.getMessage());
            System.exit(1);
        }
        if (main.usageHelpRequested) {
            jc.usage();
            System.exit(0);
        }
        String command = jc.getParsedCommand();
        if (command == null) {
            System.err.println("[ERROR] no command passed");
            System.err.println(jc.toString());
            System.exit(1);
        }
        OntologizerCommand myCommand = null;
        switch (command) {
            case "download":
                myCommand = download;
                break;
            case "population":
                myCommand = population;
                break;
            case "convert":
                myCommand = convert;
                break;
            case "parentchild":
                myCommand = parentchild;
                break;
            case "tft":
                myCommand = tft;
                break;
            case "mgsa":
                myCommand = mgsa;
                break;
            case "matrix":
                myCommand = matrix;
                break;
            default:
                System.err.println("[ERROR] Did not recognize command: "+ command);
                jc.usage();
                System.exit(0);
        }
        myCommand.run();

    }

}
