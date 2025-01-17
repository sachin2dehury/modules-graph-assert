package com.jraska.module.graph.assertion

import com.jraska.module.graph.DependencyGraph
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.Project

object GradleDependencyGraphFactory {

  fun create(project: Project, configurationsToLook: Set<String>): DependencyGraph {
    val dependencies = project.listAllDependencyPairs(configurationsToLook)

    if (dependencies.isEmpty()) {
      return DependencyGraph.createSingular(project.moduleDisplayName())
    }

    val fullDependencyGraph = DependencyGraph.create(dependencies)

    return if (project == project.rootProject) {
      fullDependencyGraph
    } else {
      fullDependencyGraph.subTree(project.moduleDisplayName())
    }
  }

  private fun Project.listAllDependencyPairs(configurationsToLook: Set<String>): List<Pair<String, String>> {
    return rootProject.subprojects
      .flatMap { project ->
        project.configurations
          .filter { configurationsToLook.contains(it.name) }
          .flatMap { configuration ->
            configuration.dependencies.filterIsInstance(ProjectDependency::class.java)
              .map { it.dependencyProject }
          }
          .map { project.moduleDisplayName() to it.moduleDisplayName() }
      }
  }
}
