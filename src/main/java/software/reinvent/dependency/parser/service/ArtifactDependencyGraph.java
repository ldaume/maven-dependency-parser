package software.reinvent.dependency.parser.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ning.http.client.AsyncHttpClientConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.libs.ws.ning.NingWSClient;
import software.reinvent.dependency.parser.model.Artifact;
import software.reinvent.dependency.parser.model.ArtifactDependency;
import software.reinvent.dependency.parser.model.ArtifactLicense;
import software.reinvent.dependency.parser.model.ArtifactParent;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * The "Maven" Repository like conglomerate of parsed
 * <ul>
 * <li>artifacts</li>
 * <li>artifact parents</li>
 * <li>artifact dependencies</li>
 * </ul>
 * and methods to parse pom files.
 * <p>
 * Created by Leonard Daume on 06.01.2016.
 */
public class ArtifactDependencyGraph {

  private final Set<Artifact> artifacts = Sets.newHashSet();
  private final Set<ArtifactParent> artifactParents = Sets.newHashSet();
  private final Set<ArtifactDependency> artifactDependencies = Sets.newHashSet();
  private final Logger logger = LoggerFactory.getLogger(ArtifactDependencyGraph.class);
  private final MavenXpp3Reader reader = new MavenXpp3Reader();
  private final WSClient wsClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build());
  private final String mavenUri;
  private final String mavenUser;
  private final String mavenPassword;
  private final String mavenRepository;

  /**
   * Starting point for the complete dependency graph. The poms will be parsed to create the graph.
   *
   * @param rootDir         the directory where to start the recursive scan of pom files
   * @param mavenUri        a specific maven repository URI to parse licenses and versions in remote pom files
   * @param mavenUser       the maven repository username
   * @param mavenPassword   the maven repository password
   * @param mavenRepository repository that the artifact is contained in like central
   */
  public ArtifactDependencyGraph(final File rootDir,
                                 final String mavenUri,
                                 final String mavenUser,
                                 final String mavenPassword,
                                 final String mavenRepository) {
    this.mavenUri = mavenUri;
    this.mavenUser = mavenUser;
    this.mavenPassword = mavenPassword;
    this.mavenRepository = mavenRepository;

    final List<File> pomFiles = findAllPomFiles(rootDir);
    parsePomFiles(pomFiles);
  }

  public Set<Artifact> getAllArtifacts() {
    return artifacts;
  }

  private ArrayList<File> findAllPomFiles(final File rootDir) {
    logger.info("Scanning pom files under {}.", rootDir.toString());
    return Lists.newArrayList(FileUtils.listFiles(rootDir,
                                                  FileFilterUtils.nameFileFilter("pom.xml"),
                                                  DirectoryFileFilter.DIRECTORY));
  }

  /**
   * Transforms pom files to
   * {@link Model}s and adds them to the dependency graph with all found licenses if the {@link #mavenUri} is
   * available.
   *
   * @param pomFiles all pom files to add
   */
  private void parsePomFiles(final List<File> pomFiles) {
    logger.info("Parsing {} pom files.", pomFiles.size());
    try {
      final List<Model> models = pomFiles.stream().map(file -> {
        try {
          final Model pom = reader.read(FileUtils.openInputStream(file));
          pom.setPomFile(file);
          return pom;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList());
      models.forEach(this::addPom);
      if ( isNoneBlank(mavenUri) ) {
        addLicensesAndDescriptions();
      }
    } catch (Exception e) {
      logger.error("Could not parse poms.", e);
    }
  }

  /**
   * Adds the licenses and descriptions by propagating all available versions as well as the LATEST and RELEASE
   * version.
   */
  private void addLicensesAndDescriptions() {
    logger.info("Adding licenses and descriptions from {} in the repository: {}.", mavenUri, mavenRepository);
    artifactDependencies.parallelStream().forEach(dependency -> {
      try {
        final List<String> versions = Lists.newArrayList("LATEST", "RELEASE");
        versions.addAll(dependency.getVersions());
        dependency.getVersions().forEach(version -> {
          try {
            addDependencyMetadata(dependency, version);
          } catch (Exception e) {
            logger.error("Could not load pom for {}.", dependency, e);
          }
        });
      } catch (Exception e) {
        logger.error("Could not add licenses.", e);
      }
    });
  }

  /**
   * Adds the license and description to a {@link ArtifactDependency} by downloading a single version of a pom file
   * from a remote maven repository.
   *
   * @param dependency the dependency where to add the data
   * @param version    the version of the artifact
   *
   * @throws IOException
   * @throws XmlPullParserException
   */
  private void addDependencyMetadata(final ArtifactDependency dependency, final String version)
    throws IOException, XmlPullParserException {
    final WSRequest wsRequest = wsClient.url(mavenUri)
                                        .setQueryParameter("p", "pom")
                                        .setQueryParameter("r", mavenRepository)
                                        .setQueryParameter("v", version)
                                        .setQueryParameter("g", dependency.getGroupId())
                                        .setQueryParameter("a", dependency.getArtifactId())
                                        .setFollowRedirects(true);
    if ( isNoneBlank(mavenUser, mavenPassword) ) {
      wsRequest.setAuth(mavenUser, mavenPassword);
    }
    final WSResponse wsResponse = wsRequest.get().get(3000);
    if ( wsResponse.getStatus() == 200 ) {
      final String body = IOUtils.toString(wsResponse.getBodyAsStream());
      if ( !containsIgnoreCase(body, "</html>") ) {
        final Model model = reader.read(IOUtils.toInputStream(body));
        dependency.addDescription(defaultIfBlank(model.getDescription(), dependency.getDescription()));
        dependency.getArtifactLicenses()
                  .addAll(model.getLicenses()
                               .stream()
                               .map(license -> new ArtifactLicense(license.getName(), license.getUrl()))
                               .collect(Collectors.toList()));
      }
    }
  }

  /**
   * Adds a {@link Model} as {@link ArtifactParent} or {@link Artifact}.
   *
   * @param model the model to add
   */
  private void addPom(Model model) {
    if ( StringUtils.equalsIgnoreCase(model.getPackaging(), "pom") ) {
      addParent(model);
    } else {
      addArtifact(model);
    }
  }

  /**
   * Adds a model as {@link ArtifactParent}. Parses especially the managed dependencies to make sure that the
   * dependencies which are child of the parent will contain the versions.
   *
   * @param parent the model to add
   */
  private void addParent(final Model parent) {
    final Properties properties = parent.getProperties();
    final List<Dependency> managedDependencies = parent.getDependencyManagement().getDependencies();
    setVersionToDepencies(parent, managedDependencies);
    addDependencies(managedDependencies);
    final ArtifactParent artifactParent = new ArtifactParent(parent.getGroupId(),
                                                             parent.getArtifactId(),
                                                             parent.getVersion(),
                                                             LocalDateTime.ofInstant(Instant.ofEpochMilli(parent
                                                                                                            .getPomFile()
                                                                                                                .lastModified()),
                                                                                     ZoneId.systemDefault()));
    if ( artifactParents.contains(artifactParent) ) {
      artifactParents.stream().filter(x -> x.equals(artifactParent)).findFirst().ifPresent(x -> {
        if ( x.getFileDate().isBefore(artifactParent.getFileDate()) ) {
          artifactParents.remove(artifactParent);
        }
      });
      artifactParents.add(artifactParent);
    } else {
      artifactParents.add(artifactParent);
    }
  }

  /**
   * Adds the version to a dependency if the version is behind a property by parsing {@link Model#getProperties()}.
   *
   * @param model        the model with the dependencies
   * @param dependencies the {@link Dependency}s to check.
   */
  private void setVersionToDepencies(final Model model, final List<Dependency> dependencies) {
    dependencies.forEach(dependency -> Optional.ofNullable(model.getProperties()
                                                                .getProperty(removeEnd(removeStart(defaultString(
                                                                  dependency.getVersion()), "${"), "}")))
                                               .ifPresent(dependency::setVersion));
  }

  /**
   * Adds a non parent {@link Model} as {@link Artifact}.
   *
   * @param model the model to parse
   */
  private void addArtifact(final Model model) {
    final Properties properties = model.getProperties();
    final List<Dependency> dependencies = model.getDependencies();
    setVersionToDepencies(model, dependencies);
    final Set<ArtifactDependency> artifactDependencies = addDependencies(dependencies);

    final String groupId = model.getGroupId() == null ? model.getParent().getGroupId() : model.getGroupId();

    final ArtifactParent artifactParent = model.getParent() == null ? null : new ArtifactParent(model.getParent());
    Artifact artifact = new Artifact(groupId,
                                     model.getArtifactId(),
                                     model.getVersion(),
                                     model.getPackaging(),
                                     LocalDateTime.ofInstant(Instant.ofEpochMilli(model.getPomFile().lastModified()),
                                                             ZoneId.systemDefault()),
                                     artifactParent);
    if ( artifacts.contains(artifact) ) {
      artifacts.stream().filter(x -> x.equals(artifact)).findFirst().ifPresent(x -> {
        if ( x.getFileDate().isBefore(artifact.getFileDate()) ) {
          artifacts.remove(artifact);
        } else {
          x.getDependencies().addAll(artifactDependencies);
        }
      });
      artifact.getDependencies().addAll(artifactDependencies);
      artifacts.add(artifact);
    } else {
      artifact.getDependencies().addAll(artifactDependencies);
      artifacts.add(artifact);
    }
  }

  /**
   * Transforms {@link Dependency}s to {@link ArtifactDependency}s and adds them to the {@link #artifactDependencies}.
   *
   * @param dependencies the dependencies to add
   *
   * @return all added {@link ArtifactDependency}s
   */
  private Set<ArtifactDependency> addDependencies(final List<Dependency> dependencies) {
    Set<ArtifactDependency> addedDependencies = Sets.newHashSet();
    dependencies.stream().forEach(dependency -> {
      final ArtifactDependency artifactDependencyToAdd = new ArtifactDependency(dependency.getGroupId(),
                                                                                dependency.getArtifactId(),

                                                                                dependency.getVersion());
      if ( artifactDependencies.contains(artifactDependencyToAdd) ) {
        artifactDependencies.stream()
                            .filter(artifactDependency -> artifactDependency.equals(artifactDependencyToAdd))
                            .findAny()
                            .ifPresent(optionalArtifactDependency -> {
                              optionalArtifactDependency.addVersions(artifactDependencyToAdd.getVersions());
                              addedDependencies.add(optionalArtifactDependency);
                            });
      } else {
        artifactDependencies.add(artifactDependencyToAdd);
        addedDependencies.add(artifactDependencyToAdd);
      }
    });
    return addedDependencies;
  }
}
