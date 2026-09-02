/*
 * Copyright 2017 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.tasks.utils;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.dmfs.android.retentionmagic.RetentionMagic;
import org.dmfs.tasks.R;
import org.dmfs.tasks.utils.permission.BasicAppPermissions;
import org.dmfs.tasks.utils.permission.Permission;
import org.dmfs.tasks.utils.permission.dialog.PermissionRequestDialogFragment;


/**
 * Base class for all Activities in the app.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public abstract class BaseActivity extends AppCompatActivity
{
    private SharedPreferences mPrefs;

    private Permission mGetAccountsPermission;
    private Permission mPostNotificationsPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mGetAccountsPermission = new BasicAppPermissions(this).forName(Manifest.permission.GET_ACCOUNTS);
        if (Build.VERSION.SDK_INT >= 33)
        {
            mPostNotificationsPermission = new BasicAppPermissions(this).forName(Manifest.permission.POST_NOTIFICATIONS);
        }

        mPrefs = getSharedPreferences(getPackageName() + ".sharedPrefences", 0);

        RetentionMagic.init(this, getIntent().getExtras());

        if (savedInstanceState == null)
        {
            RetentionMagic.init(this, mPrefs);
        }
        else
        {
            RetentionMagic.restore(this, savedInstanceState);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        requestMissingGetAccountsPermission();
        requestMissingPostNotificationsPermission();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        RetentionMagic.store(this, outState);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        RetentionMagic.persist(this, mPrefs);
    }


    private void requestMissingGetAccountsPermission()
    {
        /* This is only a thing on Android SDK Level <26. The permission has been replaced with per-account visibility. */
        if (Build.VERSION.SDK_INT < 26 && !mGetAccountsPermission.isGranted()
                && getSupportFragmentManager().findFragmentByTag("permission-dialog") == null)
        {
            PermissionRequestDialogFragment.newInstance(
                    mGetAccountsPermission.name(),
                    mGetAccountsPermission.isRequestable(this),
                    R.string.opentasks_permission_request_dialog_getaccounts_message,
                    false).show(getSupportFragmentManager(), "permission-dialog");
        }
    }


    private void requestMissingPostNotificationsPermission()
    {
        /* POST_NOTIFICATIONS is required on Android 13 (API 33) and above to show any notifications. */
        /* Only ask as long as the permission can actually be requested, otherwise the user would be nagged on every resume. */
        if (Build.VERSION.SDK_INT >= 33 && mPostNotificationsPermission != null && !mPostNotificationsPermission.isGranted()
                && mPostNotificationsPermission.isRequestable(this)
                && getSupportFragmentManager().findFragmentByTag("notification-permission-dialog") == null)
        {
            PermissionRequestDialogFragment.newInstance(
                    mPostNotificationsPermission.name(),
                    true,
                    R.string.opentasks_permission_request_dialog_notifications_message,
                    true).show(getSupportFragmentManager(), "notification-permission-dialog");
        }
    }

}
