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

package com.github.piasy.okbuck.rules

import com.github.piasy.okbuck.helper.StringUtil
import com.github.piasy.okbuck.rules.base.BuckRuleWithDeps

import static com.github.piasy.okbuck.helper.CheckUtil.checkNotNull
import static com.github.piasy.okbuck.helper.CheckUtil.checkSetNotEmpty

/**
 * android_library()
 *
 * TODO full buck support
 * */
public final class AndroidLibraryRule extends BuckRuleWithDeps {
    private final Set<String> mSrcSet
    private final String mManifest
    private final List<String> mAnnotationProcessors
    private final List<String> mAnnotationProcessorDeps
    private final boolean mExcludeAppClass
    private final String mAidlRuleName

    public AndroidLibraryRule(
            String name, List<String> visibility, List<String> deps, Set<String> srcSet,
            String manifest, List<String> annotationProcessors,
            List<String> annotationProcessorDeps, boolean excludeAppClass, String aidlRuleName
    ) {
        super("android_library", name, visibility, deps)

        checkSetNotEmpty(srcSet, "AndroidLibraryRule srcs must be non-null.")
        mSrcSet = srcSet
        mManifest = manifest
        checkNotNull(annotationProcessors,
                "AndroidLibraryRule annotation_processors must be non-null.")
        mAnnotationProcessors = annotationProcessors
        checkNotNull(annotationProcessorDeps,
                "AndroidLibraryRule annotation_processor_deps must be non-null.")
        mAnnotationProcessorDeps = annotationProcessorDeps
        mExcludeAppClass = excludeAppClass
        mAidlRuleName = aidlRuleName

        if (!StringUtil.isEmpty(mAidlRuleName)) {
            deps.add(":" + mAidlRuleName)
        }
    }

    @Override
    protected final void printSpecificPart(PrintStream printer) {
        printer.println("\tsrcs = glob([")
        for (String src : mSrcSet) {
            printer.println("\t\t'${src}',")
        }
        if (mExcludeAppClass) {
            printer.println("\t], excludes = [APP_CLASS_SOURCE]),")
        } else {
            printer.println("\t]),")
        }

        if (!StringUtil.isEmpty(mManifest)) {
            printer.println("\tmanifest = '${mManifest}',")
        }

        printer.println("\tannotation_processors = [")
        for (String processor : mAnnotationProcessors) {
            printer.println("\t\t'${processor}',")
        }
        printer.println("\t],")

        printer.println("\tannotation_processor_deps = [")
        for (String dep : mAnnotationProcessorDeps) {
            printer.println("\t\t'${dep}',")
        }
        printer.println("\t],")

        if (!StringUtil.isEmpty(mAidlRuleName)) {
            printer.println("\texported_deps = [")
            printer.println("\t\t':${mAidlRuleName}',")
            printer.println("\t],")
        }
    }
}