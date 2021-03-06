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

import com.github.piasy.okbuck.helper.FileUtil
import com.github.piasy.okbuck.helper.StringUtil
import org.gradle.api.artifacts.ResolvedDependency

/**
 * maven dependency: jar/aar file.
 * */
public final class MavenDependency extends FileDependency {

    private final ResolvedDependency mResolvedDependency

    public MavenDependency(
            DependencyType dependencyType, File localFile, File projectRootDir,
            ResolvedDependency resolvedDependency
    ) {
        super(dependencyType, localFile, projectRootDir)
        mResolvedDependency = resolvedDependency
    }

    @Override
    boolean isDuplicate(Dependency dependency) {
        switch (dependency.type) {
            case DependencyType.MavenJarDependency:
            case DependencyType.MavenAarDependency:
                MavenDependency that = (MavenDependency) dependency
                // moduleGroup + moduleName相同才算一样
                return StringUtil.areEquals(this.mResolvedDependency.moduleGroup,
                        that.mResolvedDependency.moduleGroup) &&
                        StringUtil.areEquals(this.mResolvedDependency.moduleName,
                                that.mResolvedDependency.moduleName)
            default:
                return FileUtil.areDepFilesDuplicated(this.getDepFile(), dependency.getDepFile())
        }
    }

    @Override
    Dependency defensiveCopy() {
        return fromMavenDependency(mRootProjectDir, mLocalFile, mResolvedDependency)
    }
}