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

import com.github.piasy.okbuck.rules.base.BuckRuleWithDeps

import static com.github.piasy.okbuck.helper.CheckUtil.checkNotNull
import static com.github.piasy.okbuck.helper.CheckUtil.checkStringNotEmpty

/**
 * android_binary()
 *
 * TODO full buck support
 * */
public final class AndroidBinaryRule extends BuckRuleWithDeps {
    private final String mManifest
    private final String mKeystore
    private final boolean mEnableMultiDex
    private final int mLinearAllocHardLimit
    private final List<String> mPrimaryDexPatterns
    private final boolean mExopackage
    private final List<String> mCpuFilters
    private final Map<String, String> mCupFiltersMap

    public AndroidBinaryRule(
            String name, List<String> visibility, List<String> deps, String manifest,
            String keystore, boolean exopackage, List<String> cpuFilters
    ) {
        super("android_binary", name, visibility, deps)

        checkStringNotEmpty(manifest, "AndroidBinaryRule manifest must be non-null.")
        mManifest = manifest
        checkStringNotEmpty(keystore, "AndroidBinaryRule keystore must be non-null.")
        mKeystore = keystore
        mEnableMultiDex = false
        mLinearAllocHardLimit = 0
        mPrimaryDexPatterns = null
        mExopackage = exopackage
        mCpuFilters = cpuFilters
        mCupFiltersMap = new HashMap<>()
        mCupFiltersMap.put("armeabi", "ARM")
        mCupFiltersMap.put("armeabi-v7a", "ARMV7")
        mCupFiltersMap.put("x86", "X86")
        mCupFiltersMap.put("x86_64", "X86_64")
        mCupFiltersMap.put("mips", "MIPS")
    }

    public AndroidBinaryRule(
            String name, List<String> visibility, List<String> deps, String manifest,
            String keystore, int linearAllocHardLimit, List<String> primaryDexPatterns,
            boolean exopackage, List<String> cpuFilters
    ) {
        super("android_binary", name, visibility, deps)

        checkStringNotEmpty(manifest, "AndroidBinaryRule manifest must be non-null.")
        mManifest = manifest  // https://buckbuild.com/rule/android_binary.html
        // Relative path to the Android manifest for the APK. The common case is that the manifest will be in the same directory as the rule, in which case this will simply be 'AndroidManifest.xml', but it can also reference an android_manifest rule.

        checkStringNotEmpty(keystore, "AndroidBinaryRule keystore must be non-null.")
        mKeystore = keystore
        mEnableMultiDex = true
        mLinearAllocHardLimit = linearAllocHardLimit
        checkNotNull(primaryDexPatterns, "AndroidBinaryRule primaryDexPatterns must be non-null.")
        mPrimaryDexPatterns = primaryDexPatterns
        mExopackage = exopackage
        mCpuFilters = cpuFilters

        mCupFiltersMap = new HashMap<>()
        mCupFiltersMap.put("armeabi", "ARM")
        mCupFiltersMap.put("armeabi-v7a", "ARMV7")
        mCupFiltersMap.put("x86", "X86")
        mCupFiltersMap.put("x86_64", "X86_64")
        mCupFiltersMap.put("mips", "MIPS")
    }

    @Override
    protected final void printSpecificPart(PrintStream printer) {
        printer.println("\tmanifest = '${mManifest}',")
        printer.println("\tkeystore = '${mKeystore}',")
        if (mExopackage) {
            printer.println("\texopackage_modes = ['secondary_dex'],")
        }
        if (mEnableMultiDex && mPrimaryDexPatterns != null) {
            printer.println("\tuse_split_dex = True,")
            printer.println("\tlinear_alloc_hard_limit = ${mLinearAllocHardLimit},")
            if (!mPrimaryDexPatterns.empty) {
                printer.println("\tprimary_dex_patterns = [")
                for (String pattern : mPrimaryDexPatterns) {
                    printer.println("\t\t'${pattern}',")
                }
                printer.println("\t],")
            }
        }
        if (mCpuFilters != null && !mCpuFilters.empty) {
            printer.println("\tcpu_filters = [")
            for (String filter : mCpuFilters) {
                if (mCupFiltersMap.containsKey(filter)) {
                    printer.println("\t\t'${mCupFiltersMap.get(filter)}',")
                }
            }
            printer.println("\t],")
        }
    }
}