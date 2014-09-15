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

package org.dforsyth.android.luchadeer;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import org.dforsyth.android.luchadeer.ui.downloads.DownloadsListFragment;
import org.dforsyth.android.luchadeer.ui.video.CantCastLocalDialogFragment;
import org.dforsyth.android.luchadeer.util.VideoUtil;


public class DownloadsActivity extends BaseActivity implements DownloadsListFragment.OnStartVideoListener {
    // XXX: Even though we don't allow downloaded files to be Casted yet, we add the media router
    // so that the user can disable it if they want.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_container_with_minicontroller);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, DownloadsListFragment.newInstance())
                    .commit();
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.favorites, menu);

        getVideoCastManager().addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onStartVideo(String name, String uri, int giantBombId, String imageUrl) {
        if (getVideoCastManager().isConnected()) {
            CantCastLocalDialogFragment popup = CantCastLocalDialogFragment.newInstance(
                    uri,
                    null,
                    name,
                    "",
                    giantBombId
            );
            popup.show(getFragmentManager(), "cant_cast_local_dialog_fragment");
        } else {
            VideoUtil.playVideo(
                    this,
                    name,
                    uri,
                    giantBombId,
                    imageUrl,
                    true);
        }
    }
}
