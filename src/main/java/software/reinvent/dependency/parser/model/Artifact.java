package software.reinvent.dependency.parser.model;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * The "Maven" Artifact containing:
 * <ul>
 * <li>group id</li>
 * <li>artifact id</li>
 * <li>packaging type</li>
 * <li>all parsed versions</li>
 * <li>the parsed artifact parent</li>
 * </ul>
 *
 * @see <a href="https://maven.apache.org/glossary.html">Maven Glossary</a>
 * <br>
 * Created by Leonard Daume on 06.01.2016.
 */
public class Artifact {

  private final String groupId;
  private final String artifactId;
  private final Set<String> versions = Sets.newHashSet();
  private final String packaging;
  private final LocalDateTime fileDate;

  private final Set<ArtifactDependency> dependencies = Sets.newHashSet();

  public Artifact(final String groupId,
                  final String artifactId,
                  final String version,
                  final String packaging,
                  final LocalDateTime fileDate,
                  final ArtifactParent artifactParent) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.packaging = packaging;
    this.fileDate = fileDate;
    Optional.ofNullable(StringUtils.stripToNull(version)).ifPresent(versions::add);
  }

  public Set<String> getVersions() {
    return versions;
  }

  public String getPackaging() {
    return packaging;
  }

  public LocalDateTime getFileDate() {
    return fileDate;
  }

  public Set<ArtifactDependency> getDependencies() {
    return dependencies;
  }

  @Override public int hashCode() {
    int result = getGroupId().hashCode();
    result = 31 * result + getArtifactId().hashCode();
    return result;
  }

  @Override public boolean equals(final Object o) {
    if ( this == o )
      return true;
    if ( o == null || getClass() != o.getClass() )
      return false;

    final Artifact artifact = (Artifact) o;

    if ( !getGroupId().equals(artifact.getGroupId()) )
      return false;
    return getArtifactId().equals(artifact.getArtifactId());
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }
}
