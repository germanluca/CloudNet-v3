/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.kyori.indra.git.IndraGitExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the
import java.net.HttpURLConnection
import java.net.URL

fun Project.applyJarMetadata(mainClass: String, module: String) {
  if ("jar" in tasks.names) {
    tasks.named<Jar>("jar") {
      manifest.attributes(
        "Main-Class" to mainClass,
        "Automatic-Module-Name" to module,
        "Implementation-Vendor" to "CloudNetService",
        "Implementation-Title" to Versions.cloudNetCodeName,
        "Implementation-Version" to project.version.toString() + "-${shortCommitHash()}")
      // apply git information to manifest
      git()?.applyVcsInformationToManifest(manifest)
    }
  }
}

fun Project.shortCommitHash(): String {
  return git()?.commit()?.name?.substring(0, 8) ?: "unknown"
}

fun Project.git(): IndraGitExtension? = rootProject.extensions.findByType()

fun Project.sourceSets(): SourceSetContainer = the<JavaPluginExtension>().sourceSets

fun ProjectDependency.sourceSets(): SourceSetContainer = dependencyProject.sourceSets()

fun Project.mavenRepositories(): Iterable<MavenArtifactRepository> = repositories.filterIsInstance<MavenArtifactRepository>()

fun Project.exportCnlFile(fileName: String) {
  val stringBuilder = StringBuilder("# CloudNet ${Versions.cloudNetCodeName} ${Versions.cloudNet}\n\n")
    .append("# repositories\n");
  // add all repositories
  mavenRepositories().forEach { repo ->
    stringBuilder.append("repo ${repo.name} ${repo.url.toString().dropLastWhile { it == '/' }}\n")
  }

  // add all dependencies
  stringBuilder.append("\n\n# dependencies\n")
  configurations.getByName("runtimeClasspath").resolvedConfiguration.resolvedArtifacts.forEach {
    resolveRepository(it.moduleVersion.id, mavenRepositories())?.run {
      stringBuilder
        .append("include $name ${it.moduleVersion.id.group} ${it.moduleVersion.id.name} ${it.moduleVersion.id.version} ${it.classifier ?: ""}\n")
    }
  }

  // write to the output file
  project.buildDir.resolve("libs").resolve(fileName).writeText(stringBuilder.toString())
}

private fun resolveRepository(
  id: ModuleVersionIdentifier,
  repositories: Iterable<MavenArtifactRepository>
): MavenArtifactRepository? {
  return repositories.firstOrNull {
    val url = URL(
      it.url.toURL(),
      "${id.group.replace('.', '/')}/${id.name}/${id.version}/${id.name}-${id.version}.jar"
    )
    with(url.openConnection() as HttpURLConnection) {
      useCaches = false
      readTimeout = 5000
      connectTimeout = 5000

      setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
      )

      connect()
      responseCode == 200
    }
  }
}