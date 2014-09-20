/*
 * Copyright (c) 2014, David Forsythe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of Luchadeer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dforsyth.android.luchadeer.ui.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import org.dforsyth.android.luchadeer.CreditsActivity;
import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.giantbomb.VideoType;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;
import org.dforsyth.android.luchadeer.ui.account.LinkSubscriptionFragment;
import org.dforsyth.android.luchadeer.ui.account.OnAccountStateChangedListener;
import org.dforsyth.android.luchadeer.util.Util;
import org.dforsyth.android.luchadeer.util.VideoUtil;

import java.util.ArrayList;

/**
 * Created by dforsyth on 8/12/14.
 */
public class PreferencesFragment extends PreferenceFragment {
    private LuchadeerPreferences mPreferences;
    private LuchadeerApi mApi;
    private Activity mActivity;
    private Preference mAccountPreference;

    private OnAccountStateChangedListener mOnAccountStateChangedListener;

    private static final String LINK_SUBSCRIPTION_DIALOG_FRAGMENT = "link_subscription_dialog_fragment";

    private boolean mIsLinked;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mPreferences = LuchadeerPreferences.getInstance(mActivity.getApplicationContext());
        mApi = LuchadeerApi.getInstance(mActivity.getApplicationContext());

        addPreferencesFromResource(R.xml.preferences);

        mAccountPreference = findPreference("pref_account_link");

        mIsLinked = !mPreferences.apiKeyIsDefault();

        if (!mIsLinked) {
            mAccountPreference.setTitle("Link Account");
        } else {
            mAccountPreference.setTitle("Unlink Account");
        }

        mAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialogFragment;
                if (!mIsLinked) {
                    LinkSubscriptionFragment linkSubscriptionFragment = LinkSubscriptionFragment.newInstance();
                    linkSubscriptionFragment.setOnAccountLinkedListener(new LinkSubscriptionFragment.OnAccountLinkedListener() {
                        @Override
                        public void onAccountLinked() {
                            // the fragment takes care of the actual link and reload
                            mAccountPreference.setTitle("Unlink account");
                            mIsLinked = true;
                        }
                    });
                    dialogFragment = linkSubscriptionFragment;
                } else {
                    VerifyFragment verifyFragment = VerifyFragment.newInstance();
                    verifyFragment.setOnYesListener(new VerifyFragment.OnYesListener() {
                        @Override
                        public void onYes() {
                            mPreferences.removeApiKey();
                            mApi.reloadApiKey();

                            mAccountPreference.setTitle("Link Account");
                            mIsLinked = false;
                            mOnAccountStateChangedListener.onAccountStateChanged();
                        }
                    });
                    dialogFragment = verifyFragment;
                }
                dialogFragment.show(getFragmentManager().beginTransaction(), LINK_SUBSCRIPTION_DIALOG_FRAGMENT);
                return false;
            }
        });


        if (!Util.checkGooglePlayServices(getActivity())) {
            SwitchPreference pushNotificationEnable = (SwitchPreference) findPreference(getString(R.string.pref_push_notification));
            pushNotificationEnable.setChecked(false);
            pushNotificationEnable.setEnabled(false);
        }

        MultiSelectListPreference pushCategoryPreference = (MultiSelectListPreference) findPreference(getActivity().getResources().getString(R.string.pref_push_notification_categories));

        // XXX: Being pretty lazy here. This could come back empty if the client somehow hasn't gotten a list of video types yet.
        // In that case, we'll just show an empty list of video types. If the client has somehow registered for push notifications
        // but hasn't loaded a video types list, then they're SOL.
        ArrayList<VideoType> videoTypes = mPreferences.getVideoTypeCache();

        ArrayList<String> entries = new ArrayList<String>();
        ArrayList<String> entryValues = new ArrayList<String>();

        entries.add("Live Streams");
        entryValues.add(VideoUtil.VIDEO_TYPE_LIVE);

        for (VideoType vt : videoTypes) {
            entries.add(vt.getName());
            entryValues.add(vt.getName());
        }

        pushCategoryPreference.setEntries(entries.toArray(new String[entries.size()]));
        pushCategoryPreference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));


        // version and credits
        Preference mAppVersion = findPreference("app_version");
        String versionName;
        try {
            versionName = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "";
        }
        mAppVersion.setSummary(versionName);

        Preference mCredits = findPreference("app_credits");
        mCredits.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), CreditsActivity.class));
                return true;
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnAccountStateChangedListener = (OnAccountStateChangedListener) activity;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(getString(R.string.pref_filter_trailers))) {
                        mOnAccountStateChangedListener.onAccountStateChanged();
                    }
                }
            };

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    public static  class VerifyFragment extends DialogFragment {
        private interface OnYesListener {
            public void onYes();
        }

        private OnYesListener mOnYesListener;

        public void setOnYesListener(OnYesListener onYesListener) {
            mOnYesListener = onYesListener;
        }

        public static VerifyFragment newInstance() {
            return new VerifyFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to unlink your account?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int button) {
                            mOnYesListener.onYes();
                            dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int button) {
                            dismiss();
                        }
                    })
                    .create();
        }
    }
}
