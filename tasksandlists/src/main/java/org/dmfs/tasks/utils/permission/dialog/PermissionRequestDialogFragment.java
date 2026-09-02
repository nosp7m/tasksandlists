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

package org.dmfs.tasks.utils.permission.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.dmfs.tasks.R;
import org.dmfs.tasks.utils.ManifestAppName;
import org.dmfs.tasks.utils.permission.BasicAppPermissions;
import org.dmfs.tasks.utils.permission.utils.AppSettingsIntent;


/**
 * @author Gabor Keszthelyi
 */
public final class PermissionRequestDialogFragment extends DialogFragment
{

    private static final String KEY_IS_NORMALLY_REQUESTABLE = "org.dmfs.tasks.permission.isRequestable";
    private static final String KEY_PERMISSION_NAME = "org.dmfs.tasks.permission.name";
    private static final String KEY_MESSAGE_RES = "org.dmfs.tasks.permission.messageRes";
    private static final String KEY_IS_DISMISSIBLE = "org.dmfs.tasks.permission.isDismissible";


    public static DialogFragment newInstance(String permissionName, boolean isNormallyRequestable, @StringRes int messageRes, boolean isDismissible)
    {
        PermissionRequestDialogFragment dialogFragment = new PermissionRequestDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_NORMALLY_REQUESTABLE, isNormallyRequestable);
        args.putString(KEY_PERMISSION_NAME, permissionName);
        args.putInt(KEY_MESSAGE_RES, messageRes);
        args.putBoolean(KEY_IS_DISMISSIBLE, isDismissible);
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(isDismissible);
        return dialogFragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final String permissionName = getArguments().getString(KEY_PERMISSION_NAME);

        TextView messageView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.dialog_message, null, false);
        messageView.setText(
                Html.fromHtml(
                        getString(getArguments().getInt(KEY_MESSAGE_RES),
                                new ManifestAppName(getContext()).value())));
        messageView.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.opentasks_permission_request_dialog_getaccounts_title)
                .setView(messageView);

        if (getArguments().getBoolean(KEY_IS_NORMALLY_REQUESTABLE, true))
        {
            builder.setPositiveButton(R.string.opentasks_permission_request_dialog_getaccounts_button_continue,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new BasicAppPermissions(getContext()).forName(permissionName)
                                    .request().send(getActivity());
                        }
                    });
        }
        else
        {
            builder.setPositiveButton(R.string.opentasks_permission_request_dialog_getaccounts_button_settings,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Activity activity = getActivity();
                            if (activity != null)
                            {
                                getActivity().startActivity(new AppSettingsIntent(getContext()).value());
                            }
                        }
                    });
        }

        if (getArguments().getBoolean(KEY_IS_DISMISSIBLE, false))
        {
            builder.setNegativeButton(R.string.opentasks_permission_request_dialog_button_not_now, null);
        }

        return builder.create();
    }

}
