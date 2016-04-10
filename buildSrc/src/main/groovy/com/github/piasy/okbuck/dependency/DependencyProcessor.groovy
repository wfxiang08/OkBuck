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

package com.github.piasy.okbuck.dependency

import com.android.builder.model.BuildType
import com.android.builder.model.SigningConfig
import com.github.piasy.okbuck.configs.ThirdPartyDependencyBUCKFile
import com.github.piasy.okbuck.helper.IOHelper
import com.github.piasy.okbuck.helper.ProjectHelper
import com.github.piasy.okbuck.rules.KeystoreRule
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

public final class DependencyProcessor {
    private final DependencyAnalyzer mDependencyAnalyzer

    public DependencyProcessor(DependencyAnalyzer analyzer) {
        mDependencyAnalyzer = analyzer
    }

    /**
     * copy dependencies into .okbuck dir, and generate BUCK file for them
     * */
    public void process() {
        processCompileDependencies()

        processAptDependencies()
    }

    private void processAptDependencies() {
        for (Project project : mDependencyAnalyzer.aptDependencies.keySet()) {
            for (Dependency dependency : mDependencyAnalyzer.aptDependencies.get(project)) {
                createBuckFileIfNeed(dependency, true)
                copyDependencyIfNeed(dependency)
            }
        }
    }

    private void processCompileDependencies() {
        for (Project project : mDependencyAnalyzer.finalDependencies.keySet()) {
            // 如果是Android Project
            if (ProjectHelper.getSubProjectType(project) == ProjectHelper.ProjectType.AndroidAppProject) {
                // 根目录下保存这些Key的信息
                File keystoreDir = new File("${project.rootProject.projectDir.absolutePath}/" + ".okbuck/${project.name}_keystore")

                // 定义了: key store的 keystore, properties等
                KeystoreRule debugKeystoreRule = createKeystoreRule(project, keystoreDir, "debug")
                KeystoreRule releaseKeystoreRule = createKeystoreRule(project, keystoreDir, "release")
                PrintStream printer = new PrintStream("${keystoreDir.absolutePath}/BUCK")
                debugKeystoreRule.print(printer)
                releaseKeystoreRule.print(printer)
                printer.close()
            }

            // 获取每个项目的依赖
            for (String flavor : mDependencyAnalyzer.finalDependencies.get(project).keySet()) {
                // 对于每一个Flavor
                for (Dependency dependency : mDependencyAnalyzer.finalDependencies.get(project).get(flavor)) {
                    createBuckFileIfNeed(dependency, false)
                    copyDependencyIfNeed(dependency)
                }
            }
        }
    }

    private static void createBuckFileIfNeed(Dependency dependency, boolean includeShortCut) {
        if (dependency.shouldCopy()) {
            if (!dependency.dstDir.exists()) {
                dependency.dstDir.mkdirs()

                // 为同一个目录下的第三方文件创建一个BUCK(Buck不包含具体的信息)
                PrintStream printer = new PrintStream(new File("${dependency.dstDir.absolutePath}/BUCK"))
                new ThirdPartyDependencyBUCKFile(includeShortCut).print(printer)
                printer.close()
            }
        }
    }

    private static void copyDependencyIfNeed(Dependency dependency) {
        if (dependency.shouldCopy()) {
            dependency.copyTo()
        }
    }

    private static KeystoreRule createKeystoreRule(Project project, File dir, String variant) {
        SigningConfig config = getSigningConfig(project, variant)
        if (config == null) {
            throw new IllegalArgumentException(
                    "You must specify signing config for ${project.name} ${variant} build type")
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        IOHelper.copy(new FileInputStream(config.getStoreFile()),
                new FileOutputStream(new File("${dir.absolutePath}/" +
                        "${project.name}_${variant}.keystore")))

        PrintWriter writer = new PrintWriter(new FileOutputStream(new File(
                "${dir.absolutePath}${File.separator}${project.name}_${variant}.keystore.properties")))
        writer.println("key.store=${project.name}_${variant}.keystore")
        writer.println("key.alias=${config.getKeyAlias()}")
        writer.println("key.store.password=${config.getStorePassword()}")
        writer.println("key.alias.password=${config.getKeyPassword()}")
        writer.close()

        return new KeystoreRule("key_store_${variant}", Arrays.asList("PUBLIC"),
                "${project.name}_${variant}.keystore",
                "${project.name}_${variant}.keystore.properties")
    }

    private static SigningConfig getSigningConfig(Project project, String variant) {
        try {
            for (PropertyValue prop : project.extensions.getByName("android").metaPropertyValues) {
                if ("buildTypes".equals(prop.name)) {
                    for (BuildType buildType :
                            ((NamedDomainObjectContainer<BuildType>) prop.value).asList()) {
                        if (buildType.name.equals(variant)) {
                            return buildType.signingConfig
                        }
                    }
                }
            }
        } catch (Exception e) {
            //
        }
        return null
    }
}