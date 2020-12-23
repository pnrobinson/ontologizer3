package org.jax.ontologizer.command;

import picocli.CommandLine;

import java.util.concurrent.Callable;
import org.jax.ontologizer.io.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download a number of files needed for Ontologizer analysis. We download by default to a subdirectory called
 * {@code data}, which is created if necessary. We download the files TODO
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@CommandLine.Command(name = "download", aliases = {"D"},
        mixinStandardHelpOptions = true,
        description = "Download files for ontologizer3")
public class DownloadCommand implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(DownloadCommand.class);
    @CommandLine.Option(names={"-w","--overwrite"}, description = "overwrite previously downloaded files (default: ${DEFAULT-VALUE})")
    private boolean overwrite;
    @CommandLine.Option(names = {"-d", "--data"}, description = "path to data download file")
    protected String dataDir = "data";

    public DownloadCommand() {
        super();
    }


    @Override
    public Integer call() {
        logger.info(String.format("Download to %s", dataDir));
        Downloader downloader = new Downloader(dataDir, overwrite);
        downloader.download();
        return 0;
    }
}

