package software.reinvent.dependency.parser.service;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import software.reinvent.dependency.parser.TestBase;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by leonard on 25.01.16.
 */
public class CsvWriterTest extends TestBase {

  @Test public void testWriteDependencies() throws Exception {
    final CsvWriter csvWriter = new CsvWriter(artifactDependencyGraph.getAllArtifacts(), "\t");
    final File resultDir = tempFolder.getRoot();
    csvWriter.writeDependencies(internalGroupId, resultDir, StringUtils.EMPTY);
    final String now = LocalDate.now().toString();
    assertThat(resultDir.listFiles()).hasSize(3);
    final File artifacts = new File(resultDir, "Artifacts_" + now + ".csv");
    final File external = new File(resultDir, "External_" + now + ".csv");
    final File internal = new File(resultDir, "Internal_" + now + ".csv");
    assertThat(resultDir.listFiles()).contains(artifacts, external, internal);

    final List<String[]> artifactsCsv = new CSVReader(new FileReader(artifacts)).readAll();
    assertThat(artifactsCsv).hasSize(3);

    final List<String[]> externalCsv = new CSVReader(new FileReader(external)).readAll();
    assertThat(externalCsv).hasSize(7);

    final List<String[]> internalCsv = new CSVReader(new FileReader(internal)).readAll();
    assertThat(internalCsv).hasSize(2);
  }
}
