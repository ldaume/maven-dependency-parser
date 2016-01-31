Maven Dependency Parser
=====================

# Abstract
Generates CSV files of internal and external artifacts and their dependencies by parsing pom files recursively. The application will download pom files of external dependencies from a maven repository to provide versions, licenses and descriptions in the result.
# Usage
## Linux & Windows Binary
One can get usage information's by executing `bin/dependency-parser` in the latest [binary](https://github.com/ldaume/maven-dependency-parser/releases).
### Parameter
#### Required
The only required parameter is `--rootDir, -d` which is the root directory where to start the recursive scan of pom files.
