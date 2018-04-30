/*
 * Copyright 2016 The AppAuth for Android Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openid.appauth.browser;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * A blacklist of browsers. This will reject a match for any browser on the list, and permit
 * all others. Examples:
 *
 * ```java
 * // blacklist Chrome, whether using a custom tab or not
 * new BrowserBlacklist(
 *     VersionedBrowserMatcher.CHROME_BROWSER,
 *     VersionedBrowserMatcher.CHROME_CUSTOM_TAB);
 *
 * // blacklist Firefox
 * new BrowserBlacklist(
 *     VersionedBrowserMatcher.FIREFOX_BROWSER);
 *
 * // blacklist Dolphin Browser
 * new BrowserBlacklist(
 *     new VersionedBrowserMatcher(
 *         "mobi.mgeek.TunnyBrowser",
 *         "<DOLPHIN_SIGNATURE>",
 *         false,
 *         VersionRange.ANY_VERSION));
 * }
 * ```
 */
public class BrowserBlacklist implements BrowserMatcher {

    private List<BrowserMatcher> mBrowserMatchers;

    /**
     * Creates a blacklist from the provided set of matchers.
     */
    public BrowserBlacklist(BrowserMatcher... matchers) {
        mBrowserMatchers = Arrays.asList(matchers);
    }

    @Override
    public boolean matches(@NonNull BrowserDescriptor descriptor) {
        for (BrowserMatcher matcher : mBrowserMatchers) {
            if (matcher.matches(descriptor)) {
                return false;
            }
        }

        return true;
    }
}
