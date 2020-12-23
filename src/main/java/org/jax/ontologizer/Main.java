package org.jax.ontologizer;



import org.jax.ontologizer.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "ontologizer3", mixinStandardHelpOptions = true, version = "0.0.2",
        description = "Java 14 version of Ontologizer.")
public class Main implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);




    public static void main(String []args) {
        Main main = new Main();
        CommandLine cline = new CommandLine(new Main())
                .addSubcommand("download", new DownloadCommand())
                .addSubcommand("overrep", new OverrepresentationCommand());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);

    }

    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }

}
