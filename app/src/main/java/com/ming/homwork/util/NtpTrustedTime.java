/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ming.homwork.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.ming.homwork.DemoActivity;
import com.ming.homwork.R;

/**
 * {@link TrustedTime} that connects with a remote NTP server as its trusted
 * time source.
 *
 * @hide
 */
public class NtpTrustedTime implements TrustedTime {
    private static final String TAG = "NtpTrustedTime";
    private static final boolean LOGD = false;

    private static NtpTrustedTime sSingleton;
    private static Context sContext;

    //private final String mServer;
    private String mServer;
    private final long mTimeout;

    private ConnectivityManager mCM;

    private boolean mHasCache;
    private long mCachedNtpTime;
    private long mCachedNtpElapsedRealtime;
    private long mCachedNtpCertainty;

    String[] backupNtpServers = new String[]{
            "cn.pool.ntp.org",
            "jp.pool.ntp.org",
            "hk.pool.ntp.org",
            "tw.pool.ntp.org",
            "time.nist.gov",
            "time-a.nist.gov"
    };
    int index = -1;

    private NtpTrustedTime(String server, long timeout) {
        if (LOGD) Log.d(TAG, "creating NtpTrustedTime using " + server);
        mServer = server;
        mTimeout = timeout;
    }

    public static synchronized NtpTrustedTime getInstance(Context context) {
        if (sSingleton == null) {
            final Resources res = context.getResources();
            final ContentResolver resolver = context.getContentResolver();

            final String defaultServer = res.getString(
                    R.string.config_ntpServer);
            final long defaultTimeout = res.getInteger(
                    R.integer.config_ntpTimeout);

            final String secureServer = Settings.Global.getString(
                    resolver, "ntp_server");
            final long timeout = Settings.Global.getLong(
                    resolver, "ntp_timeout", defaultTimeout);

            final String server = defaultServer;
            sSingleton = new NtpTrustedTime(server, timeout);
            sContext = context;
        }

        return sSingleton;
    }

    @Override
    public boolean forceRefresh() {
        if (TextUtils.isEmpty(mServer)) {
            // missing server, so no trusted time available
            return false;
        }

        // We can't do this at initialization time: ConnectivityService might not be running yet.
        synchronized (this) {
            if (mCM == null) {
                mCM = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            }
        }

        final NetworkInfo ni = mCM == null ? null : mCM.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            if (LOGD) Log.d(TAG, "forceRefresh: no connectivity");
            return false;
        }


        if (LOGD) Log.d(TAG, "forceRefresh() from cache miss");
        final SntpClient client = new SntpClient();
        boolean result = false;
        mServer = backupNtpServers[0];
        while (!(result = client.requestTime(mServer, (int) mTimeout)) && index < (backupNtpServers.length - 1)) {
            index++;
            mServer = backupNtpServers[index];
        }
        index = -1;
        Resources res = sContext.getResources();
//		   String defaultServer = res.getString(
//				com.android.internal.R.string.config_ntpServer);
//		   String secureServer = Settings.Global.getString(
//				 sContext.getContentResolver(), Settings.Global.NTP_SERVER);
//
//		   mServer  = secureServer != null ? secureServer : defaultServer;

        if (result) {
            mHasCache = true;
            mCachedNtpTime = client.getNtpTime();
            String ElapsedRealtime = DemoActivity.getDateformat2(client.getNtpTimeReference());
            mCachedNtpElapsedRealtime = client.getNtpTimeReference();
            String Certainty = DemoActivity.getDateformat2(client.getRoundTripTime() / 2);
            mCachedNtpCertainty = client.getRoundTripTime() / 2;
        }
        return result;
    }

    @Override
    public boolean hasCache() {
        return mHasCache;
    }

    @Override
    public long getCacheAge() {
        if (mHasCache) {
            return SystemClock.elapsedRealtime() - mCachedNtpElapsedRealtime;
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public long getCacheCertainty() {
        if (mHasCache) {
            return mCachedNtpCertainty;
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public long currentTimeMillis() {
        if (!mHasCache) {
            throw new IllegalStateException("Missing authoritative time source");
        }
        if (LOGD) Log.d(TAG, "currentTimeMillis() cache hit");

        // current time is age after the last ntp cache; callers who
        // want fresh values will hit makeAuthoritative() first.
        String Certainty = DemoActivity.getDateformat2(mCachedNtpTime);
        String cachet=DemoActivity.getDateformat2(getCacheAge());
        return mCachedNtpTime + getCacheAge();
    }

    public long getCachedNtpTime() {
        if (LOGD) Log.d(TAG, "getCachedNtpTime() cache hit");
        return mCachedNtpTime;
    }

    public long getCachedNtpTimeReference() {
        return mCachedNtpElapsedRealtime;
    }
}
