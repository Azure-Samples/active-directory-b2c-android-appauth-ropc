/*
 * Copyright 2015 The AppAuth for Android Authors. All Rights Reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.text.TextUtils;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.openid.appauth.BuildConfig;
import org.apache.tools.ant.taskdefs.Pack;
import org.apache.tools.ant.types.resources.comparators.Name;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk=16)
public class BrowserSelectorTest {

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    private static final boolean USE_CUSTOM_TAB = true;
    private static final boolean USE_STANDALONE = false;

    private static final TestBrowser CHROME =
            new TestBrowserBuilder("com.android.chrome")
                    .withBrowserDefaults()
                    .setVersion("50")
                    .addSignature("ChromeSignature")
                    .build();

    private static final TestBrowser FIREFOX =
            new TestBrowserBuilder("org.mozilla.firefox")
                    .withBrowserDefaults()
                    .setVersion("10")
                    .addSignature("FirefoxSignature")
                    .build();

    private static final TestBrowser DOLPHIN =
            new TestBrowserBuilder("mobi.mgeek.TunnyBrowser")
                    .withBrowserDefaults()
                    .setVersion("1.4.1")
                    .addSignature("DolphinSignature")
                    .build();

    private static final TestBrowser[] NO_BROWSERS = new TestBrowser[0];

    @Mock Context mContext;
    @Mock PackageManager mPackageManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
    }

    @Test
    public void testSelect_warmUpSupportOnFirstMatch() throws NameNotFoundException {
        setBrowserList(CHROME, FIREFOX, DOLPHIN);
        setBrowsersWithWarmupSupport(CHROME, FIREFOX);
        checkSelectedBrowser(CHROME, USE_CUSTOM_TAB);
    }

    @Test
    public void testSelect_warmUpSupportOnAlternateBrowser()
            throws NameNotFoundException {
        setBrowserList(DOLPHIN, FIREFOX);
        setBrowsersWithWarmupSupport(FIREFOX);
        checkSelectedBrowser(FIREFOX, USE_CUSTOM_TAB);
    }

    @Test
    public void testSelect_warmUpSupportOnAlternateBrowsers()
            throws NameNotFoundException {
        setBrowserList(DOLPHIN, CHROME, FIREFOX);
        setBrowsersWithWarmupSupport(CHROME, FIREFOX);
        checkSelectedBrowser(CHROME, USE_CUSTOM_TAB);
    }

    @Test
    public void testSelect_noWarmUpSupportOnAnyBrowser() throws NameNotFoundException {
        setBrowserList(CHROME, DOLPHIN);
        setBrowsersWithWarmupSupport(NO_BROWSERS);
        checkSelectedBrowser(CHROME, USE_STANDALONE);
    }

    @Test
    public void testSelect_noBrowsers() throws NameNotFoundException {
        setBrowserList(NO_BROWSERS);
        setBrowsersWithWarmupSupport(NO_BROWSERS);
        checkSelectedBrowser(null, false);
    }

    @Test
    public void testSelect_ignoreAuthorityRestrictedBrowsers()
            throws NameNotFoundException {
        TestBrowser authorityRestrictedBrowser =
                new TestBrowserBuilder("com.badguy.proxy")
                        .withBrowserDefaults()
                        .addAuthority("www.example.com")
                        .build();
        setBrowserList(authorityRestrictedBrowser, CHROME);
        setBrowsersWithWarmupSupport(authorityRestrictedBrowser, CHROME);
        checkSelectedBrowser(CHROME, USE_CUSTOM_TAB);
    }

    @Test
    public void testSelect_ignoreBrowsersWithoutBrowseableCategory()
            throws NameNotFoundException {
        TestBrowser misconfiguredBrowser =
                new TestBrowserBuilder("com.broken.browser")
                        .addAction(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .addScheme(SCHEME_HTTP)
                        .addScheme(SCHEME_HTTPS)
                        .build();
        setBrowserList(misconfiguredBrowser, CHROME);
        setBrowsersWithWarmupSupport(misconfiguredBrowser, CHROME);
        checkSelectedBrowser(CHROME, USE_CUSTOM_TAB);
    }

    @Test
    public void testSelect_ignoreBrowsersWithoutHttpsSupport()
            throws NameNotFoundException {
        TestBrowser noHttpsBrowser =
                new TestBrowserBuilder("com.broken.browser")
                        .addAction(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .addScheme(SCHEME_HTTP)
                        .build();
        setBrowserList(DOLPHIN, noHttpsBrowser);
        setBrowsersWithWarmupSupport(noHttpsBrowser);
        checkSelectedBrowser(DOLPHIN, USE_STANDALONE);
    }

    @Test
    public void testSelect_matcherPrefersStandaloneChrome() throws NameNotFoundException {
        // in this scenario, the user has firefox as their default but the app insists on using
        // chrome via a browser whitelist.
        setBrowserList(FIREFOX, CHROME, DOLPHIN);
        setBrowsersWithWarmupSupport(FIREFOX, CHROME);
        checkSelectedBrowser(CHROME,
                USE_STANDALONE,
                new VersionedBrowserMatcher(
                        CHROME.mPackageName,
                        CHROME.mSignatureHashes,
                        USE_STANDALONE,
                        VersionRange.ANY_VERSION));
    }

    @Test
    public void testSelect_noMatchingBrowser() throws NameNotFoundException {
        setBrowserList(FIREFOX, DOLPHIN);
        setBrowsersWithWarmupSupport(NO_BROWSERS);

        checkSelectedBrowser(
                null,
                USE_STANDALONE,
                new VersionedBrowserMatcher(
                        CHROME.mPackageName,
                        CHROME.mSignatureHashes,
                        USE_STANDALONE,
                        VersionRange.ANY_VERSION));
    }

    /**
     * Browsers are expected to be in priority order, such that the default would be first.
     */
    private void setBrowserList(TestBrowser... browsers) throws NameNotFoundException {
        if (browsers == null) {
            return;
        }

        List<ResolveInfo> resolveInfos = new ArrayList<>();

        for (TestBrowser browser : browsers) {
            when(mPackageManager.getPackageInfo(
                    eq(browser.mPackageInfo.packageName),
                    eq(PackageManager.GET_SIGNATURES)))
                    .thenReturn(browser.mPackageInfo);
            resolveInfos.add(browser.mResolveInfo);
        }

        when(mPackageManager.queryIntentActivities(
                BrowserSelector.BROWSER_INTENT,
                PackageManager.GET_RESOLVED_FILTER))
                .thenReturn(resolveInfos);
    }

    private void setBrowsersWithWarmupSupport(TestBrowser... browsers) {
        if (browsers == null) {
            return;
        }
        for (TestBrowser browser : browsers) {
            when(mPackageManager.resolveService(
                    serviceIntentEq(browser.mResolveInfo.activityInfo.packageName),
                    eq(0)))
                    .thenReturn(browser.mResolveInfo);
        }
    }

    private void checkSelectedBrowser(TestBrowser expected, boolean expectCustomTabUse) {
        checkSelectedBrowser(expected, expectCustomTabUse, AnyBrowserMatcher.INSTANCE);
    }

    private void checkSelectedBrowser(
            TestBrowser expected,
            boolean expectCustomTabUse,
            BrowserMatcher browserMatcher) {
        BrowserDescriptor result = BrowserSelector.select(mContext, browserMatcher);
        if (expected == null) {
            assertThat(result).isNull();
        } else {
            assertThat(result).isNotNull();
            assertThat(result.packageName).isEqualTo(expected.mPackageName);
            assertThat(result.useCustomTab).isEqualTo(expectCustomTabUse);
        }
    }

    private static class TestBrowser {
        final String mPackageName;
        final ResolveInfo mResolveInfo;
        final PackageInfo mPackageInfo;
        final Set<String> mSignatureHashes;

        TestBrowser(
                String packageName,
                PackageInfo packageInfo,
                ResolveInfo resolveInfo,
                Set<String> signatureHashes) {
            mPackageName = packageName;
            mResolveInfo = resolveInfo;
            mPackageInfo = packageInfo;
            mSignatureHashes = signatureHashes;
        }
    }

    private static class TestBrowserBuilder {
        private final String mPackageName;
        private final List<byte[]> mSignatures = new ArrayList<>();
        private final List<String> mActions = new ArrayList<>();
        private final List<String> mCategories = new ArrayList<>();
        private final List<String> mSchemes = new ArrayList<>();
        private final List<String> mAuthorities = new ArrayList<>();
        private String mVersion;

        TestBrowserBuilder(String packageName) {
            mPackageName = packageName;
        }

        public TestBrowserBuilder withBrowserDefaults() {
            return addAction(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .addScheme(SCHEME_HTTP)
                    .addScheme(SCHEME_HTTPS);
        }

        public TestBrowserBuilder addAction(String action) {
            mActions.add(action);
            return this;
        }

        public TestBrowserBuilder addCategory(String category) {
            mCategories.add(category);
            return this;
        }

        public TestBrowserBuilder addScheme(String scheme) {
            mSchemes.add(scheme);
            return this;
        }

        public TestBrowserBuilder addAuthority(String authority) {
            mAuthorities.add(authority);
            return this;
        }

        public TestBrowserBuilder addSignature(String signature) {
            mSignatures.add(signature.getBytes(Charset.forName("UTF-8")));
            return this;
        }

        public TestBrowserBuilder setVersion(String version) {
            mVersion = version;
            return this;
        }

        public TestBrowser build() {
            PackageInfo pi = new PackageInfo();
            pi.packageName = mPackageName;
            pi.versionName = mVersion;
            pi.signatures = new Signature[mSignatures.size()];

            for (int i = 0; i < mSignatures.size(); i++) {
                pi.signatures[i] = new Signature(mSignatures.get(i));
            }

            Set<String> signatureHashes = BrowserDescriptor.generateSignatureHashes(pi.signatures);

            ResolveInfo ri = new ResolveInfo();
            ri.activityInfo = new ActivityInfo();
            ri.activityInfo.packageName = mPackageName;
            ri.filter = new IntentFilter();

            for (String action : mActions) {
                ri.filter.addAction(action);
            }

            for (String category : mCategories) {
                ri.filter.addCategory(category);
            }

            for (String scheme : mSchemes) {
                ri.filter.addDataScheme(scheme);
            }

            for (String authority: mAuthorities) {
                ri.filter.addDataAuthority(authority, null);
            }

            return new TestBrowser(mPackageName, pi, ri, signatureHashes);
        }
    }

    /**
     * Custom matcher for verifying the intent fired during token request.
     */
    private static class ServiceIntentMatcher implements ArgumentMatcher<Intent> {

        private String mPackage;

        ServiceIntentMatcher(String pkg) {
            mPackage = pkg;
        }

        @Override
        public boolean matches(Intent intent) {
            return (intent != null)
                    && (BrowserSelector.ACTION_CUSTOM_TABS_CONNECTION.equals(
                            intent.getAction()))
                    && (TextUtils.equals(mPackage, intent.getPackage()));
        }
    }

    private static Intent serviceIntentEq(String pkg) {
        return argThat(new ServiceIntentMatcher(pkg));
    }
}
