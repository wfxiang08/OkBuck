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

package com.github.piasy.okbuck.rules.base

import static com.github.piasy.okbuck.helper.CheckUtil.checkStringNotEmpty
import static com.github.piasy.okbuck.helper.CheckUtil.checkNotNull

/**
 * General presentation for BUCK build rule with name and visibility.
 * */
public abstract class BuckRule extends AbstractBuckRule {
    private final String mRuleType
    private final String mName
    private final List<String> mVisibility

    protected BuckRule(String ruleType, String name, List<String> visibility) {
        checkStringNotEmpty(ruleType, "BuckRule rule type can't be empty.")
        mRuleType = ruleType
        checkStringNotEmpty(name, "BuckRule name can't be empty.")
        mName = name
        checkNotNull(visibility, "BuckRule visibility must be non-null.")
        mVisibility = visibility
    }

    /**
     * Print this rule into the printer.
     * */
    @Override
    public final void print(PrintStream printer) {
        // 打印类型和name
        printer.println("${mRuleType}(")
        printer.println("\tname = '${mName}',")

        // 打印Detail
        printDetail(printer)

        // 打印: visibility
        if (!mVisibility.empty) {
            printer.println("\tvisibility = [")
            for (String visibility : mVisibility) {
                printer.println("\t\t'${visibility}',")
            }
            printer.println("\t],")
        }
        printer.println(")")
        printer.println()
    }

    /**
     * Print other details.
     * */
    protected abstract void printDetail(PrintStream printer)
}