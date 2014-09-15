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

package org.dforsyth.android.luchadeer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.dforsyth.android.luchadeer.FavoritesActivity;
import org.dforsyth.android.luchadeer.GameDetailActivity;
import org.dforsyth.android.luchadeer.VideoDetailActivity;
import org.dforsyth.android.luchadeer.VideoPlayerActivity;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;


public class IntentUtil {

    public static Intent getGameDetailActivityIntent(Context context, int gameId) {
        Intent intent = new Intent(context, GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME_ID, gameId);
        return intent;
    }

    public static Intent getVideoDetailActivityIntent(Context context, int videoId) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_ID, videoId);
        return intent;
    }

    public static Intent getPopulatedVideoDetailActivityIntent(Context context, Video video) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_OBJ, video);
        return intent;
    }

    public static Intent getFavoritesActivityIntent(Context context) {
        return new Intent(context, FavoritesActivity.class);
    }

    public static Intent getVideoPlayerActivityIntent(Context context, String videoName, String videoUri, int giantBombId) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URI, videoUri);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_NAME, videoName);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_ID, giantBombId);

        return intent;
    }

    public static Intent getExternalVideoPlayerIntent(String videoUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(videoUri), "video/mp4");
        return intent;
    }

}
