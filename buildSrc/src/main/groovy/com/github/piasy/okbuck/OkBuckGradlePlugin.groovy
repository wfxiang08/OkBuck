/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.piasy.okbuck

import com.github.piasy.okbuck.configs.BUCKFile
import com.github.piasy.okbuck.configs.GenManifestPyFile
import com.github.piasy.okbuck.configs.ScriptBUCKFile
import com.github.piasy.okbuck.dependency.DependencyAnalyzer
import com.github.piasy.okbuck.dependency.DependencyExtractor
import com.github.piasy.okbuck.dependency.DependencyProcessor
import com.github.piasy.okbuck.generator.BuckFileGenerator
import com.github.piasy.okbuck.generator.DotBuckConfigGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * task added: okbuck, okbuckDebug, okbuckRelease, and okbuck is the shortcut for okbuckRelease
 * */
class OkBuckGradlePlugin implements Plugin<Project> {

    // 如何定制一个 Gradle Plugin呢?
    void apply(Project project) {

        // 定义: okbuck需要哪些属性
        project.extensions.create("okbuck", OkBuckExtension)

        // 添加 task
        Task okBuckClean = project.task('okbuckClean')
        okBuckClean << {
            if (project.okbuck.overwrite) {
                File okBuckDir = new File("${project.projectDir.absolutePath}/.okbuck")
                okBuckDir.deleteDir()
                File dotBuckdDir = new File("${project.projectDir.absolutePath}/.buckd")
                dotBuckdDir.deleteDir()
                File buckOutDir = new File("${project.projectDir.absolutePath}/buck-out")
                buckOutDir.deleteDir()
                File okBuckScriptsDir = new File("${project.projectDir.absolutePath}/okbuck-scripts")
                okBuckScriptsDir.deleteDir()
                File dotBuckConfig = new File("${project.projectDir.absolutePath}/.buckconfig")
                dotBuckConfig.delete()

                // Subprojects如何定义的呢?
                project.subprojects { prj ->
                    File buck = new File("${prj.projectDir.absolutePath}/BUCK")
                    buck.delete()
                }
            }
        }

        // 调用 clean时，则一并清理 buck
        project.getTasksByName("clean", true).each { task ->
            task.dependsOn(okBuckClean)
        }

        // 添加 okbuck的任务
        Task okBuck = project.task('okbuck')

        // okBuck依赖: bundleRelease & jar
        dependsOnBuild(okBuck, project)

        // 每次: okbuck的代价也不小
        okBuck.dependsOn(okBuckClean)
        okBuck << {
            applyWithoutBuildVariant(project)
        }
    }

    private static dependsOnBuild(Task task, Project project) {
        project.getTasksByName("bundleRelease", true).each { bundleRelease ->
            task.dependsOn(bundleRelease)
        }
        project.getTasksByName("jar", true).each { jar ->
            task.dependsOn(jar)
        }
    }

    private static applyWithoutBuildVariant(Project project) {
        // 参考文档: http://blog.piasy.com/OkBuck/
        boolean overwrite = project.okbuck.overwrite
        if (overwrite) {
            println "==========>> overwrite mode is toggle on <<=========="
        }

        // step 1: create .buckconfig
        File dotBuckConfig = new File("${project.projectDir.absolutePath}/.buckconfig")
        if (dotBuckConfig.exists() && !overwrite) {
            throw new IllegalStateException(
                    ".buckconfig already exist, set overwrite property to true to overwrite existing file.")
        } else {
            PrintStream printer = new PrintStream(dotBuckConfig)

            // .buckconfig 保存什么文件呢?
//            okbuck {
//                buildToolVersion "23.0.1"
//                target "android-23"
//                overwrite true
//                checkDepConflict true
//            flavorFilter is a map, used for controlling OkBuck only generate BUCK files for flavors you need, default is empty, which will generate all flavors BUCK file
            new DotBuckConfigGenerator(project, (String) project.okbuck.buildToolVersion,
                    (String) project.okbuck.target,
                    (Map<String, List<String>>) project.okbuck.flavorFilter)
                    .generate()
                    .print(printer)
            printer.close()
        }

        // step 2: generate script files
        File scriptsDir = new File("${project.projectDir.absolutePath}/okbuck-scripts")
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs()
        }
        File manifestPyFile = new File("${scriptsDir.absolutePath}/manifest.py")
        PrintStream printer = new PrintStream(manifestPyFile)
        new GenManifestPyFile().print(printer)
        printer.close()

        // 这个BUCK文件作用?
        File scriptBUCKFile = new File("${scriptsDir.absolutePath}/BUCK")
        printer = new PrintStream(scriptBUCKFile)
        new ScriptBUCKFile().print(printer)
        printer.close()

        // step 3: analyse dependencies
        File okBuckDir = new File("${project.projectDir.absolutePath}/.okbuck")
        if (okBuckDir.exists() && !overwrite) {
            throw new IllegalStateException(
                    ".okbuck dir already exist, set overwrite property to true to overwrite existing file.")
        } else {

            // 如何做依赖分析呢?
            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(project, okBuckDir,
                    (boolean) project.okbuck.checkDepConflict, new DependencyExtractor(project))
            dependencyAnalyzer.analyse()
            new DependencyProcessor(dependencyAnalyzer).process()

            // step 4: generate BUCK file for each sub project
            Map<Project, BUCKFile> buckFiles = new BuckFileGenerator(project, dependencyAnalyzer,
                    okBuckDir, (Map<String, String>) project.okbuck.resPackages,
                    (Map<String, Integer>) project.okbuck.linearAllocHardLimit,
                    (Map<String, List<String>>) project.okbuck.primaryDexPatterns,
                    (Map<String, Boolean>) project.okbuck.exopackage,
                    (Map<String, String>) project.okbuck.appClassSource,
                    (Map<String, List<String>>) project.okbuck.appLibDependencies,
                    (Map<String, List<String>>) project.okbuck.flavorFilter,
                    (Map<String, List<String>>) project.okbuck.cpuFilters)
                    .generate()

            // 修改每一个subProject的BUCK文件
            for (Project subProject : buckFiles.keySet()) {
                File buckFile = new File("${subProject.projectDir.absolutePath}/BUCK")
                printer = new PrintStream(buckFile)
                buckFiles.get(subProject).print(printer)
                printer.close()
            }
        }
    }
}
