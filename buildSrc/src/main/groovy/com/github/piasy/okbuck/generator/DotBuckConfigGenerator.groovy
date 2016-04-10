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

package com.github.piasy.okbuck.generator

import com.android.build.gradle.internal.dsl.ProductFlavor
import com.github.piasy.okbuck.configs.DotBuckConfigFile
import com.github.piasy.okbuck.helper.FileUtil
import com.github.piasy.okbuck.helper.ProjectHelper
import org.gradle.api.Project

/**
 * Created by Piasy{github.com/Piasy} on 15/10/6.
 *
 * used to generate .buckconfig file content.
 */
public final class DotBuckConfigGenerator {
    private final Project mRootProject
    private final String mBuildToolVersion
    private final String mTarget
    private final Map<String, List<String>> mFlavorFilter

    /**
     * Create generator.
     */
    public DotBuckConfigGenerator(Project rootProject, String buildToolVersion, String target,
            Map<String, List<String>> flavorFilter) {
        mRootProject = rootProject
        mBuildToolVersion = buildToolVersion
        mTarget = target
        mFlavorFilter = flavorFilter
    }

    /**
     * generate {@code DotBuckConfigFile}
     */
    public DotBuckConfigFile generate() {
        Map<String, String> alias = new HashMap<>()
        for (Project project : mRootProject.subprojects) {
            // 子项目的类型:
            // AndroidAppProject, AndroidLibProject, JavaLibProject, 其他
            if (ProjectHelper.getSubProjectType(project) == ProjectHelper.ProjectType.AndroidAppProject) {
                // 如何处理Flavor?
                if (ProjectHelper.exportFlavor(project)) {
                    //
                    Map<String, ProductFlavor> flavorMap = getFilteredFlavors(project)
                    for (String flavor : flavorMap.keySet()) {
                        // 每个flavor对应Debug/Release
                        alias.put(project.name + flavor.capitalize() + "Debug",   "/${FileUtil.getProjectPathDiff(mRootProject, project)}:bin_${flavor}_debug")
                        alias.put(project.name + flavor.capitalize() + "Release", "/${FileUtil.getProjectPathDiff(mRootProject, project)}:bin_${flavor}_release")
                    }
                } else {
                    // 处理默认的
                    alias.put(project.name + "Debug", "/${FileUtil.getProjectPathDiff(mRootProject, project)}:bin_debug")
                    alias.put(project.name + "Release", "/${FileUtil.getProjectPathDiff(mRootProject, project)}:bin_release")
                }
            }
        }
        return new DotBuckConfigFile(alias, mBuildToolVersion, mTarget, Arrays.asList(".git", "**/.svn"))
    }

    private Map<String, ProductFlavor> getFilteredFlavors(Project project) {
        Map<String, ProductFlavor> flavorMap = ProjectHelper.getProductFlavors(project)
        List<String> filter = mFlavorFilter.get(project.name)
        if (filter == null || filter.empty) {
            return flavorMap
        } else {
            // 从 flavorMap 上读取指定的 Flavor
            Map<String, ProductFlavor> filtered = new HashMap<>()
            for (String flavor : filter) {
                if (flavorMap.containsKey(flavor)) {
                    filtered.put(flavor, flavorMap.get(flavor))
                } else {
                    throw new IllegalArgumentException("`${project.name}` doesn't have flavor " +
                            "named `${flavor}`, please correct your root project build.gradle file")
                }
            }
            return filtered
        }
    }
}
