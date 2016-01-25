package software.reinvent.dependency.parser.service;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.opencsv.CSVWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.reinvent.dependency.parser.model.Artifact;
import software.reinvent.dependency.parser.model.ArtifactDependency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Writes artifacts and their dependencies to csv files.
 * Created by leonard on 25.01.16.
 */
public class CsvWriter {

  private final Logger logger = LoggerFactory.getLogger(CsvWriter.class);
  private final Set<Artifact> artifacts;
  private final char separator;

  public CsvWriter(final Set<Artifact> artifacts, final String separator) {
    this.artifacts = artifacts;
    this.separator = separator.charAt(0);
  }

  /**
   * Creates the csv files
   * <ul>
   * <li>Internal_{date}.csv</li>
   * <li>External_{date}.csv</li>
   * <li>Artifacts_{date}.csv</li>
   * </ul>
   * with all important information's about the {@link Artifact}s and their {@link ArtifactDependency}'s.
   *
   * @param internalGroupId the internal maven group id
   * @param resultDir       the dir where the CSV files will be written
   * @param prefix          any optional prefix for the CSV files
   *
   * @throws IOException
   */
  public void writeDependencies(final String internalGroupId, final File resultDir, final String prefix)
    throws IOException {
    final Set<ArtifactDependency> allDependencies = artifacts.stream()
                                                             .map(Artifact::getDependencies)
                                                             .flatMap(Collection::stream)
                                                             .collect(Collectors.toSet());
    final Set<ArtifactDependency> internalDependencies = allDependencies.stream()
                                                                        .filter(isInternalPredicate(internalGroupId))
                                                                        .sorted(Comparator.comparing
                                                                          (ArtifactDependency::getGroupId))
                                                                        .collect(toSet());
    final Set<ArtifactDependency> externalDependencies = Sets.newHashSet(CollectionUtils.subtract(allDependencies,
                                                                                                  internalDependencies));

    final Multimap<ArtifactDependency, Artifact> dependencyToArtifact = HashMultimap.create();
    allDependencies.forEach(dependency -> artifacts.stream()
                                                   .filter(artifact -> artifact.getDependencies().contains(dependency))
                                                   .forEach(x -> dependencyToArtifact.put(dependency, x)));

    CSVWriter internalWriter = null;
    CSVWriter externalWriter = null;
    CSVWriter artifactWriter = null;
    try {
      resultDir.mkdirs();
      final File internalResultFile = new File(resultDir, prefix + "Internal_" +
                                                          LocalDate.now().toString() + ".csv");
      final File externalResultFile = new File(resultDir, prefix + "External_" +
                                                          LocalDate.now().toString() + ".csv");
      final File artifactResultFile = new File(resultDir, prefix + "Artifacts_" +
                                                          LocalDate.now().toString() + ".csv");
      logger.info("Will write results to {} and {}.", internalResultFile, externalResultFile);
      internalWriter = new CSVWriter(new FileWriter(internalResultFile), separator);
      writeDependencyHeader(internalWriter);
      externalWriter = new CSVWriter(new FileWriter(externalResultFile), separator);
      writeDependencyHeader(externalWriter);
      artifactWriter = new CSVWriter(new FileWriter(artifactResultFile), separator);
      artifactWriter.writeNext(( "groupId#artifactId#version#package#internalDependencies"
                                 + "#externalDependencies" ).split("#"));
      final CSVWriter finalInternalWriter = internalWriter;
      final CSVWriter finalExternalWriter = externalWriter;
      dependencyToArtifact.keySet()
                          .stream()
                          .sorted(Comparator.comparing(ArtifactDependency::getGroupId)
                                            .thenComparing(ArtifactDependency::getArtifactId))
                          .forEach(dependency -> {
                            final List<String> dependentArtifacts = dependencyToArtifact.get(dependency)
                                                                                        .stream()
                                                                                        .map(Artifact::getArtifactId)
                                                                                        .sorted()
                                                                                        .collect(toList());
                            final String artifactLicenses = defaultIfBlank(Joiner.on("\n")
                                                                                 .join(dependency.getArtifactLicenses
                                                                                   ()),
                                                                           "n/a in pom");

                            final ArrayList<String> newLine = Lists.newArrayList(dependency.getGroupId(),
                                                                                 dependency.getArtifactId(),
                                                                                 Joiner.on("\n")
                                                                                       .join(dependency.getVersions()),
                                                                                 artifactLicenses,
                                                                                 dependency.getDescription(),
                                                                                 Joiner.on("\n")
                                                                                       .join(dependentArtifacts));
                            final String[] csvLine = Iterables.toArray(newLine, String.class);
                            if ( isInternal(internalGroupId, dependency) ) {
                              finalInternalWriter.writeNext(csvLine);
                            } else {
                              finalExternalWriter.writeNext(csvLine);
                            }
                          });
      final CSVWriter finalArtifactWriter = artifactWriter;
      artifacts.stream()
               .sorted(Comparator.comparing(Artifact::getGroupId).thenComparing(Artifact::getArtifactId))
               .forEachOrdered(artifact -> {
                 final String intDependencies = getDependencyColumn(artifact,
                                                                    internalDependencies,
                                                                    ArtifactDependency::getArtifactId);
                 final String extDependencies = getDependencyColumn(artifact,
                                                                    externalDependencies,
                                                                    ArtifactDependency::toString);
                 final ArrayList<String> newLine = Lists.newArrayList(artifact.getGroupId(),
                                                                      artifact.getArtifactId(),
                                                                      Joiner.on(",").join(artifact.getVersions()),
                                                                      defaultString(artifact.getPackaging()),

                                                                      intDependencies,
                                                                      extDependencies);
                 final String[] csvLine = Iterables.toArray(newLine, String.class);
                 finalArtifactWriter.writeNext(csvLine);
               });
    } catch (IOException e)

    {
      logger.error("Could not write csv.", e);
    } finally

    {
      if ( internalWriter != null ) {
        internalWriter.close();
      }
      if ( externalWriter != null ) {
        externalWriter.close();
      }
      if ( artifactWriter != null ) {
        artifactWriter.close();
      }
    }

    logger.info("Found {} dependencies. {} internal and {} external",
                allDependencies.size(),
                internalDependencies.size(),
                externalDependencies.size());
  }

  /**
   * @see #isInternal(String, ArtifactDependency)
   */
  private Predicate<ArtifactDependency> isInternalPredicate(final String internalGroupId) {
    return dependency -> isInternal(internalGroupId, dependency);
  }

  private void writeDependencyHeader(final CSVWriter csvWriter) {
    csvWriter.writeNext(( "groupId#artifactId#versions#licenses#description#dependentArtifacts" + "" ).split("#"));
  }

  /**
   * Checks if the group id of a dependency is equal to the internal group id.
   *
   * @param internalGroupId the internal (company) group id
   * @param dependency      the dependency to check
   *
   * @return true, if it is an internal dependency
   */
  private boolean isInternal(final String internalGroupId, final ArtifactDependency dependency) {
    return equalsIgnoreCase(dependency.getGroupId(), internalGroupId);
  }

  /**
   * Transforms {@link ArtifactDependency} information's to a ","-separated string.
   *
   * @param artifact                 the artifact to write
   * @param dependenciesToFilter     the dependencies to filter - like internal or external dependencies
   * @param dependencyStringFunction the string transform function to apply
   *
   * @return the ","-separated string
   */
  private String getDependencyColumn(final Artifact artifact,
                                     final Set<ArtifactDependency> dependenciesToFilter,
                                     final Function<ArtifactDependency, String> dependencyStringFunction) {
    return Joiner.on(",")
                 .join(artifact.getDependencies()
                               .stream()
                               .filter(Optional.ofNullable(dependenciesToFilter).orElse(Sets.newHashSet())::contains)
                               .sorted(Comparator.comparing(ArtifactDependency::getGroupId)
                                                 .thenComparing(ArtifactDependency::getArtifactId))
                               .map(dependencyStringFunction)
                               .collect(Collectors.toList()));
  }
}
