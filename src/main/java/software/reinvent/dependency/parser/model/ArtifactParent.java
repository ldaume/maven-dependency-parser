package software.reinvent.dependency.parser.model;

import org.apache.maven.model.Parent;

import java.time.LocalDateTime;

/**
 * A parent pom containing:
 * <ul>
 * <li>group id</li>
 * <li>artifact id</li>
 * <li>version</li>
 * </ul>
 *
 * @see <a href="https://maven.apache.org/glossary.html">Maven Glossary</a>
 * <br>
 * Created by Leonard Daume on 06.01.2016.
 */
public class ArtifactParent {
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final LocalDateTime fileDate;

  public ArtifactParent(final String groupId,
                        final String artifactId,
                        final String version,
                        final LocalDateTime fileDate) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.fileDate = fileDate;
  }

  public ArtifactParent(final Parent parent) {
    this.groupId = parent.getGroupId();
    this.artifactId = parent.getArtifactId();
    this.version = parent.getVersion();
    this.fileDate = null;
  }

  public LocalDateTime getFileDate() {
    return fileDate;
  }

  @Override public int hashCode() {
    int result = getGroupId().hashCode();
    result = 31 * result + getArtifactId().hashCode();
    result = 31 * result + getVersion().hashCode();
    return result;
  }

  @Override public boolean equals(final Object o) {
    if ( this == o )
      return true;
    if ( o == null || getClass() != o.getClass() )
      return false;

    final ArtifactParent artifactParent = (ArtifactParent) o;

    if ( !getGroupId().equals(artifactParent.getGroupId()) )
      return false;
    if ( !getArtifactId().equals(artifactParent.getArtifactId()) )
      return false;
    return getVersion().equals(artifactParent.getVersion());
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }
}
