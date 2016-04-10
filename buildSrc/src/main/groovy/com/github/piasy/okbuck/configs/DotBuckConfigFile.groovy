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

package com.github.piasy.okbuck.configs

import static com.github.piasy.okbuck.helper.CheckUtil.checkStringNotEmpty
import static com.github.piasy.okbuck.helper.CheckUtil.checkNotNull

/**
 * .buckconfig file.
 *
 * TODO full buck support
 * */
public final class DotBuckConfigFile extends BuckConfigFile {
    private final Map<String, String> mAlias
    private final String mBuildToolVersion
    private final String mTarget
    private final List<String> mIgnore

    public DotBuckConfigFile(Map<String, String> alias, String buildToolVersion, String target,
            List<String> ignore) {
        checkNotNull(alias, "DotBuckConfigFile alias must be non-null.")
        mAlias = alias
        checkStringNotEmpty(buildToolVersion, "DotBuckConfigFile buildToolVersion can't be empty.")
        mBuildToolVersion = buildToolVersion
        checkStringNotEmpty(target, "DotBuckConfigFile target can't be empty.")
        mTarget = target
        checkNotNull(ignore, "DotBuckConfigFile ignore must be non-null.")
        mIgnore = ignore
    }

    @Override
    public final void print(PrintStream printer) {

// .buckconfig的定义
//        [alias]
//        appDevDebug = //app:bin_dev_debug
//        appDevRelease = //app:bin_dev_release
//        appProdDebug = //app:bin_prod_debug
//        appProdRelease = //app:bin_prod_release     // 定义了Flavor
//        anotherappDebug = //anotherapp:bin_debug    // 没有Flavor
//        anotherappRelease = //anotherapp:bin_release
//
//        [android]
//        build_tools_version = 23.0.1
//        target = android-23
//
//        [project]
//        ignore = .git, **/.svn


        printer.println("[alias]")
        for (String alias : mAlias.keySet()) {
            printer.println("\t${alias} = ${mAlias.get(alias)}")
        }
        printer.println()

        printer.println("[android]")
        printer.println("\tbuild_tools_version = ${mBuildToolVersion}")
        printer.println("\ttarget = ${mTarget}")
        printer.println()

        printer.println("[project]")
        printer.print("\tignore =")
        for (int i = 0; i < mIgnore.size(); i++) {
            if (i != mIgnore.size() - 1) {
                printer.print(" ${mIgnore.get(i)},")
            } else {
                printer.print(" ${mIgnore.get(i)}")
            }
        }
        printer.println()
    }
}