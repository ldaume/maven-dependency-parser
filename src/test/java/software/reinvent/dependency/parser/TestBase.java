package software.reinvent.dependency.parser;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import software.reinvent.dependency.parser.service.ArtifactDependencyGraph;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by leonard on 25.01.16.
 */
public class TestBase {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  protected static ArtifactDependencyGraph artifactDependencyGraph = null;

  protected String internalGroupId = "software.reinvent.test";

  @BeforeClass public static void setUp() throws Exception {
    if ( artifactDependencyGraph == null ) {
      ClassLoader classLoader = TestBase.class.getClassLoader();
      File rootDir = new File(classLoader.getResource("poms").getFile());

      artifactDependencyGraph = new ArtifactDependencyGraph(rootDir,
                                                            "https://repository.sonatype"
                                                            + ".org/service/local/artifact/maven/redirect",
                                                            EMPTY,
                                                            EMPTY,
                                                            "central-proxy");
    }
  }
}
