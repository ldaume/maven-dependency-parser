package software.reinvent.dependency.parser.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * The "Maven" Dependency containing:
 * <ul>
 * <li>group id</li>
 * <li>artifact id</li>
 * <li>all parsed versions</li>
 * <li>all parsed licenses</li>
 * <li>the parsed description</li>
 * </ul>
 *
 * @see <a href="https://maven.apache.org/glossary.html">Maven Glossary</a>
 * <br>
 * Created by Leonard Daume on 06.01.2016.
 */
public class ArtifactDependency {
  private final String groupId;
  private final String artifactId;
  private final Set<String> versions = Sets.newHashSet();
  private final Set<ArtifactLicense> artifactLicenses = Sets.newConcurrentHashSet();
  private String description = EMPTY;

  public ArtifactDependency(final String groupId, final String artifactId, final String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    Optional.ofNullable(stripToNull(version)).ifPresent(versions::add);
  }

  public ImmutableSet<String> getVersions() {
    return ImmutableSet.copyOf(versions);
  }

  public Set<ArtifactLicense> getArtifactLicenses() {
    return artifactLicenses;
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

    final ArtifactDependency that = (ArtifactDependency) o;

    if ( !getGroupId().equals(that.getGroupId()) )
      return false;
    return getArtifactId().equals(that.getArtifactId());
  }

  @Override public String toString() {
    return ImmutableList.of(groupId, artifactId, versions).toString();
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void addVersions(final Set<String> versions) {
    this.versions.addAll(versions);
  }

  public void addDescription(final String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}

