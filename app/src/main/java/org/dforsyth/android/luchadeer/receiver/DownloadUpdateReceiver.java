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

package org.dforsyth.android.luchadeer.receiver;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.dforsyth.android.luchadeer.DownloadsActivity;
import org.dforsyth.android.luchadeer.MainActivity;
import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;


public class DownloadUpdateReceiver extends BroadcastReceiver {
    private final static String TAG = DownloadUpdateReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            onNotificationClicked(context, intent);
        } else if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            onDownloadComplete(context, intent);
        }
    }

    private void onNotificationClicked(Context context, Intent intent) {
        Intent appIntent = new Intent(context, DownloadsActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(appIntent);
    }

    private void onDownloadComplete(Context context, Intent intent) {
        Log.d(TAG, "onDownloadComplete");

        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        int downloadStatus = LuchadeerPersist.getInstance(context).updateDownloadStatus(downloadId);

        String notificationTitle;
        int notificationIcon;

        switch (downloadStatus) {
            case (DownloadManager.STATUS_SUCCESSFUL):
                notificationTitle = context.getString(R.string.download_complete);
                notificationIcon = android.R.drawable.stat_sys_download_done;
                break;
            case (DownloadManager.STATUS_FAILED):
                notificationTitle = context.getString(R.string.download_failed);
                notificationIcon = android.R.drawable.stat_sys_warning;
                break;
            case (DownloadManager.STATUS_PENDING):
            case (DownloadManager.STATUS_PAUSED):
            case (DownloadManager.STATUS_RUNNING):
                notificationTitle = context.getString(R.string.downloading);
                notificationIcon = android.R.drawable.stat_sys_download;
                break;
            default:
                return;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context)
                .addNextIntent(new Intent(context, MainActivity.class))
                .addNextIntent(new Intent(context, DownloadsActivity.class));

        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(notificationTitle)
                .setSmallIcon(notificationIcon)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(0, notification);
    }
}
