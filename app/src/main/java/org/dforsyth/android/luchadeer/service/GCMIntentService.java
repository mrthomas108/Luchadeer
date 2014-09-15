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

package org.dforsyth.android.luchadeer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.dforsyth.android.luchadeer.MainActivity;
import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.VideoDetailActivity;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;
import org.dforsyth.android.luchadeer.receiver.GCMBroadcastReceiver;
import org.dforsyth.android.luchadeer.util.VideoUtil;


public class GCMIntentService extends IntentService {
    private final static String TAG = GCMIntentService.class.getName();

    private final static String PUSH_VIDEO_ID = "video_id";
    private final static String PUSH_VIDEO_NAME = "video_name";
    private final static String PUSH_VIDEO_TYPE = "video_type";

    public GCMIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                int videoId = Integer.parseInt(extras.getString(PUSH_VIDEO_ID));
                String videoType = extras.getString(PUSH_VIDEO_TYPE);
                handleNewVideoNotification(
                        extras.getString(PUSH_VIDEO_NAME),
                        videoId,
                        videoType);
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleNewVideoNotification(String videoName, int videoId, String videoType) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d(TAG, "Received notification for video " + videoId);

        // make sure we're not being annoying
        LuchadeerPreferences preferences = LuchadeerPreferences.getInstance(this);
        if (!preferences.getNotificationsEnabled()) {
            return;
        }

        int defaults = 0;
        if (preferences.getNotificationVibrationEnabled()) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (preferences.getNotificationLEDEnabled()) {
            defaults |= Notification.DEFAULT_LIGHTS;
        }


        NotificationCompat.Builder builder;


        PendingIntent pendingIntent;
        String contentTitle;
        if (videoType.equals(VideoUtil.VIDEO_TYPE_LIVE)) {
            contentTitle = getString(R.string.live_chat_detected);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.giant_bomb_web_uri)));
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        } else {
            Intent intent = new Intent(this, VideoDetailActivity.class);
            intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_ID, videoId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
                    .addNextIntent(new Intent(this, MainActivity.class))
                    .addNextIntent(intent);

            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            contentTitle = getString(R.string.new_giant_bomb_video);
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.lucha_deer_p4_cropped_white_48)
                .setContentText(videoName)
                .setDefaults(defaults)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(videoId, notification);
    }
}