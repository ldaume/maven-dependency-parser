package software.reinvent.dependency.parser.application;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.reinvent.dependency.parser.service.ArtifactDependencyGraph;
import software.reinvent.dependency.parser.service.CsvWriter;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Main method with a command line parser.
 * <p>
 * Created by leonard on 05.01.16.
 */
public class DependencyParser {
  private static final Logger logger = LoggerFactory.getLogger(DependencyParser.class);

  @Parameter(names = { "--rootDir", "-d" },
             description = "The root directory where to start the recursive scan of pom files (Required).",
             converter = FileConverter.class) private File rootDir;

  @Parameter(names = { "--resultDir", "-r" },
             description = "The dir where the CSV files will be written.",
             converter = FileConverter.class) private File resultDir = new File(System.getProperty("user.dir"));

  @Parameter(names = { "--filePrefix", "-p" },
             description = "Any prefix for the CSV files.") private String prefix = EMPTY;

  @Parameter(names = { "--csvSeparator", "-s" },
             description = "The separator used in the csv files.") private String csvSeparator = "\t";

  @Parameter(names = { "--groupId", "-g" },
             description = "The internal maven group id.") private String internalGroupId = EMPTY;

  @Parameter(names = { "--mavenUri", "-m" },
             description = "A specific maven repository URI.") private String
    mavenUri
    = "https://repository.sonatype.org/service/local/artifact/maven/redirect";
  @Parameter(names = { "--mavenRepository" },
             description = "Repository that the artifact is contained in.") private String
    mavenRepository
    = "central-proxy";
  @Parameter(names = { "--mavenUser" },
             description = "The maven repository username.") private String mavenUser = EMPTY;
  @Parameter(names = { "--mavenPassword" },
             description = "The maven repository password.") private String mavenPassword = EMPTY;

  public static void main(String args[]) throws IOException {
    try {
      DependencyParser parser = new DependencyParser();
      final JCommander jCommander = new JCommander(parser, args);
      if ( args.length == 0 ) {
        jCommander.setProgramName("dependency-parser");
        jCommander.usage();
      } else {
        parser.run();
      }
    } catch (IOException e) {
      logger.error("Some problem occured.", e);
    }
  }

  private void run() throws IOException {
    if ( rootDir == null ) {
      logger.error("The rootDir must be set but is {}.", rootDir);
      System.exit(1);
    }
    final ArtifactDependencyGraph artifactDependencyGraph = new ArtifactDependencyGraph(rootDir,
                                                                                        mavenUri,
                                                                                        mavenUser,
                                                                                        mavenPassword,
                                                                                        mavenRepository);
    final CsvWriter csvWriter = new CsvWriter(artifactDependencyGraph.getAllArtifacts(), csvSeparator);
    csvWriter.writeDependencies(internalGroupId, resultDir, prefix);
    System.exit(0);
  }
}
