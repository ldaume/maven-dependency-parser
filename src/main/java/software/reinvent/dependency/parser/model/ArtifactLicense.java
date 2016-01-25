package software.reinvent.dependency.parser.model;

/**
 * Parsed description and url of a license for a project.
 *
 * @see <a href="https://maven.apache.org/glossary.html">Maven Glossary</a>
 * <br>
 * Created by Leonard Daume on 07.01.2016.
 */
public class ArtifactLicense {
  private final String license;
  private final String url;

  public ArtifactLicense(final String license, final String url) {
    this.license = license;
    this.url = url;
  }

  @Override public int hashCode() {
    int result = license != null ? license.hashCode() : 0;
    result = 31 * result + ( url != null ? url.hashCode() : 0 );
    return result;
  }

  @Override public boolean equals(final Object o) {
    if ( this == o )
      return true;
    if ( o == null || getClass() != o.getClass() )
      return false;

    final ArtifactLicense that = (ArtifactLicense) o;

    if ( license != null ? !license.equals(that.license) : that.license != null )
      return false;
    return url != null ? url.equals(that.url) : that.url == null;
  }

  @Override public String toString() {
    return license + " ; " + url;
  }
}
