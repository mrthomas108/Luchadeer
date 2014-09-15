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

import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.FlakyTest;
import android.test.UiThreadTest;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dforsyth.android.luchadeer.ui.game.GameListFragment;
import org.dforsyth.android.luchadeer.ui.video.VideoListFragment;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void testStartWithVideoListFragment() {
        MainActivity activity = getActivity();

        Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.content_list);

        assertNotNull(fragment);
        assertTrue(fragment instanceof VideoListFragment);
    }

    @UiThreadTest
    @FlakyTest
    public void testStartGameListFragment() {
        setActivityInitialTouchMode(true);

        MainActivity activity = getActivity();

        LinearLayout drawerLayout = (LinearLayout) activity.findViewById(R.id.navdrawer);
        TextView navGames = (TextView) drawerLayout.getChildAt(1);

        assertEquals("Games", navGames.getText());

        assertTrue(navGames.isClickable());
        assertTrue(navGames.isEnabled());

        navGames.performClick();

        activity.getFragmentManager().executePendingTransactions();

        Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.content_list);

        assertTrue(fragment instanceof GameListFragment);
    }
}
