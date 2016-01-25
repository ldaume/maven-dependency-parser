package software.reinvent.dependency.parser.service;

import org.junit.Test;
import software.reinvent.dependency.parser.TestBase;
import software.reinvent.dependency.parser.model.Artifact;
import software.reinvent.dependency.parser.model.ArtifactDependency;
import software.reinvent.dependency.parser.model.ArtifactLicense;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by leonard on 25.01.16.
 */
public class ArtifactDependencyGraphTest extends TestBase {

  @Test public void testGetAllArtifacts() throws Exception {
    assertThat(artifactDependencyGraph).isNotNull();
    final Set<Artifact> allArtifacts = artifactDependencyGraph.getAllArtifacts();
    assertThat(allArtifacts).hasSize(2);
    assertThat(allArtifacts.stream().map(Artifact::getArtifactId).collect(toSet())).containsOnly("first", "second");
    final Optional<Artifact> first = allArtifacts.stream()
                                                 .filter(artifact -> artifact.getArtifactId().equals("first"))
                                                 .findAny();

    assertThat(first).isPresent();

    final Optional<ArtifactDependency> junit = first.get()
                                                    .getDependencies()
                                                    .stream()
                                                    .filter(dependency -> dependency.getArtifactId().equals("junit"))
                                                    .findAny();
    assertThat(junit).isPresent();
    assertThat(junit.get().getVersions()).containsExactly("4.12");
    assertThat(junit.get().getArtifactLicenses()).containsExactly(new ArtifactLicense("Eclipse Public License 1.0",
                                                                                      "http://www.eclipse"
                                                                                      + ".org/legal/epl-v10"
                                                                                      + ".html"));
    final Optional<Artifact> second = allArtifacts.stream()
                                                  .filter(artifact -> artifact.getArtifactId().equals("second"))
                                                  .findAny();
    assertThat(second).isPresent();
    assertThat(second.get()
                     .getDependencies()
                     .stream()
                     .filter(artifactDependency -> artifactDependency.getArtifactId().equals("first"))
                     .findAny()).isPresent();
    final Optional<ArtifactDependency> wicketCore = second.get()
                                                          .getDependencies()
                                                          .stream()
                                                          .filter(artifactDependency -> artifactDependency
                                                            .getArtifactId()
                                                                                                          .equals(
                                                                                                            "wicket-core"))
                                                          .findAny();
    assertThat(wicketCore).isPresent();
    assertThat(wicketCore.get().getDescription()).isEqualTo("Wicket is a Java web application "
                                                            + "framework that takes simplicity, \n"
                                                            + "\t\tseparation of concerns and ease of"
                                                            + " development to a whole new level. \n"
                                                            + "\t\tWicket pages can be mocked up, "
                                                            + "previewed and later revised using \n"
                                                            + "\t\tstandard WYSIWYG HTML design tools"
                                                            + ". Dynamic content processing and \n"
                                                            + "\t\tform handling is all handled in "
                                                            + "Java code using a first-class \n"
                                                            + "\t\tcomponent model backed by POJO "
                                                            + "data beans that can easily be \n"
                                                            + "\t\tpersisted using your favorite "
                                                            + "technology.");
    assertThat(wicketCore.get().getVersions()).containsExactly("7.1.0");
  }
}
