package org.jax.ontologizer.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jax.ontologizer.io.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download a number of files needed for the org.jax.hc2go.analysis. We download by default to a subdirectory called
 * {@code data}, which is created if necessary. We download the files {@code hp.obo}, {@code phenotype.hpoa},
 * {@code Homo_sapiencs_gene_info.gz}, and {@code mim2gene_medgen}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@Parameters(commandDescription = "Download files for HC2GO")
public class DownloadCommand extends OntologizerCommand {
    private static final Logger logger = LoggerFactory.getLogger(DownloadCommand.class);
    @Parameter(names={"-w","--overwrite"}, description = "overwrite prevously downloaded files, if any")
    private boolean overwrite;

    public DownloadCommand() {
        super();
    }


    @Override
    public void run() {
        logger.info(String.format("Download to %s", dataDir));
        Downloader downloader = new Downloader(dataDir, overwrite);
        downloader.download();
    }
}

