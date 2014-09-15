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

package org.dforsyth.android.luchadeer.ui.video;

import android.os.Bundle;
import android.view.View;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.ui.util.BaseDialogFragment;
import org.dforsyth.android.luchadeer.util.IntentUtil;
import org.dforsyth.android.luchadeer.util.VideoUtil;

import java.util.ArrayList;


public class CantCastLocalDialogFragment extends BaseDialogFragment {

    private String mRemoteVideoUri;
    private String mLocalVideoUri;
    private String mName;
    private String mImageUrl;
    private int mGiantBombId;

    private static final String ARG_LOCAL_URI = "local_uri";
    private static final String ARG_REMOTE_URI = "remote_uri";


    public static CantCastLocalDialogFragment newInstance(String localVideoUri, String videoUri, String name, String imageUrl, int giantBombId) {
        CantCastLocalDialogFragment fragment = new CantCastLocalDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LOCAL_URI, localVideoUri);
        args.putString(ARG_REMOTE_URI, videoUri);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mLocalVideoUri = args.getString(ARG_LOCAL_URI);
        mRemoteVideoUri = args.getString(ARG_REMOTE_URI);
    }

    @Override
    public ArrayList<DialogButton> getDialogButtons() {
        ArrayList<DialogButton> buttons = new ArrayList<DialogButton>();

        if (mRemoteVideoUri != null && !mRemoteVideoUri.isEmpty()) {
            DialogButton playWeb = new DialogButton();
            playWeb.text = "Play video from web";
            playWeb.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoUtil.playVideo(
                            getActivity(),
                            mName,
                            mRemoteVideoUri,
                            mGiantBombId,
                            mImageUrl,
                            false
                    );
                    dismiss();
                }
            };
            buttons.add(playWeb);
        }

        DialogButton otherPlayers = new DialogButton();
        otherPlayers.text = "Show other players";
        otherPlayers.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(IntentUtil.getExternalVideoPlayerIntent(mLocalVideoUri));
                dismiss();
            }
        };
        buttons.add(otherPlayers);

        DialogButton backOut = new DialogButton();
        backOut.text = "Cancel";
        backOut.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        };
        buttons.add(backOut);


        return buttons;
    }

    @Override
    public int getDialogText() {
        return R.string.cant_cast_local_content;
    }
}
