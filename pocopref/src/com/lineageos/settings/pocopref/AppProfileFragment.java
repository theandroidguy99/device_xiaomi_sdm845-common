/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

package com.lineageos.settings.pocopref;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.util.Log;

import android.os.ServiceManager;
import android.os.RemoteException;


import android.content.res.Resources;

import com.android.internal.baikalos.AppProfileSettings;
import com.android.internal.baikalos.AppProfile;

import com.lineageos.settings.pocopref.BaseSettingsFragment;
import com.lineageos.settings.pocopref.R;

public class AppProfileFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "ApplicationProfile";

    private static final String APP_PROFILE_READER = "app_profile_reader";
    private static final String APP_PROFILE_PERF = "app_profile_performance";
    private static final String APP_PROFILE_THERM = "app_profile_thermal";
    private static final String APP_PROFILE_BRIGHTNESS = "app_profile_brightness";
    private static final String APP_PROFILE_FPS = "app_profile_fps";
//    private static final String APP_PROFILE_CAMERA_HAL1 = "app_profile_camera_hal1";
    private static final String APP_PROFILE_PINNED = "app_profile_pinned";
//    private static final String APP_PROFILE_DISABLE_TWL = "app_profile_disable_twl";

    private String mPackageName;
    private Context mContext;

    private SwitchPreference mAppReader;
    private SwitchPreference mAppPinned;
    private ListPreference mAppPerfProfile;
    private ListPreference mAppThermProfile;
    private ListPreference mAppBrightnessProfile;
    private ListPreference mAppFpsProfile;

    private AppProfileSettings mAppSettings;
    private com.android.internal.baikalos.AppProfile mProfile;


    //IBaikalServiceController mBaikalService;

    public AppProfileFragment(String packageName) {
        mPackageName = packageName;
    }

    @Override
    protected int getPreferenceResource() {
        return R.xml.app_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();


        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1") ||
                            SystemProperties.get("spectrum.support", "0").equals("1");

        boolean thermProf  = SystemProperties.get("baikal.eng.therm", "0").equals("1");


        mAppSettings = new  AppProfileSettings(new Handler(),mContext, mContext.getContentResolver(),null);
        mProfile = mAppSettings.getProfile(mPackageName);
        if( mProfile == null ) { 
            mProfile = new com.android.internal.baikalos.AppProfile();
            mProfile.mPackageName = mPackageName;
        }

        try {

            mAppReader = (SwitchPreference) findPreference(APP_PROFILE_READER);
            if( mAppReader != null ) {
                mAppReader.setChecked(mProfile.mReader);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppReader.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mReader = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppReader: mPackageName=" + mPackageName + ",setReader=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppReader Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }


            //initBaikalAppOp(APP_PROFILE_PINNED,BaikalServiceManager.OP_PINNED);
            //initBaikalAppOp(APP_PROFILE_DISABLE_TWL,BaikalServiceManager.OP_DISABLE_TWL);

            mAppPerfProfile = (ListPreference) findPreference(APP_PROFILE_PERF);
            if( mAppPerfProfile != null ) { 
                if(!perfProf) {
                    mAppPerfProfile.setVisible(false);
                } else {
                    String profile = mProfile.mPerfProfile;
                    Log.e(TAG, "getAppPerfProfile: mPackageName=" + mPackageName + ",getProfile=" + profile);
                    if( profile == null ) profile = "default";
                    mAppPerfProfile.setValue(profile);
                    mAppPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mPerfProfile = newValue.toString();
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();

                                //mBaikalService.setAppPerfProfile(mPackageName, newValue.toString() );
                                Log.e(TAG, "mAppPerfProfile: mPackageName=" + mPackageName + ",setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppPerfProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppThermProfile = (ListPreference) findPreference(APP_PROFILE_THERM);
            if( mAppThermProfile != null ) {
                if(!thermProf) {
                    mAppThermProfile.setVisible(false);
                } else {
                    String profile = mProfile.mThermalProfile;
                    Log.e(TAG, "getAppThermProfile: mPackageName=" + mPackageName + ",getProfile=" + profile);
                    if( profile == null ) profile = "default";
                    mAppThermProfile.setValue(profile);
                    mAppThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mThermalProfile = newValue.toString();
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();

                                //mBaikalService.setAppThermProfile(mPackageName, newValue.toString() );
                                Log.e(TAG, "mAppThermProfile: mPackageName=" + mPackageName + ",setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppThermProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

        
            mAppBrightnessProfile = (ListPreference) findPreference(APP_PROFILE_BRIGHTNESS);
            if( mAppBrightnessProfile != null ) {
                int brightness = mProfile.mBrightness;
                Log.e(TAG, "getAppBrightness: mPackageName=" + mPackageName + ",brightness=" + brightness);
                mAppBrightnessProfile.setValue(Integer.toString(brightness));
                mAppBrightnessProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mBrightness = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "setAppBrightness: mPackageName=" + mPackageName + ",brightness=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppBrightness Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppFpsProfile = (ListPreference) findPreference(APP_PROFILE_FPS);
            if( mAppFpsProfile != null ) {
                int fps = mProfile.mFrameRate;
                Log.e(TAG, "setAppFps: mPackageName=" + mPackageName + ",fps=" + fps);
                mAppFpsProfile.setValue(Integer.toString(fps));
                mAppFpsProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mFrameRate = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "setAppFps: mPackageName=" + mPackageName + ",fps=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppFps Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppPinned = (SwitchPreference) findPreference(APP_PROFILE_PINNED);
            if( mAppPinned != null ) {
                mAppPinned.setChecked(mProfile.mPinned);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppPinned.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mPinned = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppPinned: mPackageName=" + mPackageName + ",setReader=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppPinned Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }
        
        
        } catch(Exception re) {
            Log.e(TAG, "onCreate: Fatal! exception", re );
        }

    }


    private void initBaikalAppOp(String XML_KEY, int baikalOption) {
        try {
            SwitchPreference pref = (SwitchPreference) findPreference(XML_KEY);
            if( pref != null ) { 
                //pref.setChecked(mBaikalService.getAppOption(mPackageName,baikalOption) == 1);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        //mBaikalService.setAppOption(mPackageName, baikalOption, ((Boolean)newValue) ? 1 : 0 );
                        Log.e(TAG, "setAppOption: mPackageName=" + mPackageName + ",option="+ baikalOption + ", value=" + (Boolean)newValue);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppOption Fatal! exception", re );
                    }
                    return true;
                }
                });
            }
        } catch(Exception re) {
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
