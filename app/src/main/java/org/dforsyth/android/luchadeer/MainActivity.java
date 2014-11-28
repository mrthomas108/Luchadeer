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

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.BaseCastManager;

import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.model.youtube.YouTubeVideo;
import org.dforsyth.android.luchadeer.ui.account.LinkSubscriptionFragment;
import org.dforsyth.android.luchadeer.ui.account.OnAccountStateChangedListener;
import org.dforsyth.android.luchadeer.ui.account.UnlinkSubscriptionFragment;
import org.dforsyth.android.luchadeer.ui.game.GameDetailFragment;
import org.dforsyth.android.luchadeer.ui.game.GameListFragment;
import org.dforsyth.android.luchadeer.ui.unarchived.UnarchivedDetailFragment;
import org.dforsyth.android.luchadeer.ui.unarchived.UnarchivedListFragment;
import org.dforsyth.android.luchadeer.ui.video.VideoDetailFragment;
import org.dforsyth.android.luchadeer.ui.video.VideoListFragment;
import org.dforsyth.android.luchadeer.util.IntentUtil;
import org.dforsyth.android.luchadeer.util.Util;


public class MainActivity extends BaseActivity implements
        VideoListFragment.OnVideoSelectedListener,
        GameListFragment.OnGameSelectedListener,
        VideoDetailFragment.OnDetailLoadFailedListener,
        GameDetailFragment.OnDetailLoadFailedListener,
        UnarchivedListFragment.OnUnarchivedSelectedListener,
        OnAccountStateChangedListener {

    private static final String TAG = MainActivity.class.getName();

    private ActionBar mActionBar;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private FragmentManager mFragmentManager;

    private View mMediaRoute;
    private LinearLayout mNavLayout;

    private int mSelectedId;
    private String mSelectedUnarchivedId;

    LayoutInflater mLayoutInflater;

    private static final String GAME_LIST_FRAGMENT = "game_list_fragment";
    private static final String VIDEO_LIST_FRAGMENT = "video_list_fragment";
    private static final String UNARCHIVED_LIST_FRAGMENT = "unarchived_list_fragment";
    private static final String LINK_ACCOUNT_FRAGMENT = "link_account_fragment";
    private static final String GAME_DETAIL_FRAGMENT = "game_detail_fragment";
    private static final String VIDEO_DETAIL_FRAGMENT = "video_detail_fragment";
    private static final String UNARCHIVED_DETAIL_FRAGMENT = "unarchived_detail_fragment";

    private static final String STATE_SELECTED_ID = "selected_id";
    private static final String STATE_SELECTED_UNARCHIVED_ID = "unarchived_selected_id";

    private static final int DRAWER_CLOSE_DELAY = 300;

    private static final int REQUEST_CODE_ACCOUNT_LINKED = 0;

    private final static int NAV_VIDEOS = 0;
    private final static int NAV_GAMES = 1;
    private final static int NAV_UNARCHIVED = 2;

    private final static int NAV_SEARCH = 3;

    private final static int NAV_FAVORITES = 5;
    private final static int NAV_DOWNLOADS = 6;

    private final static int NAV_SUBSCRIPTION = 8;
    private final static int NAV_SETTINGS = 9;

    private final static int NAV_GIANTBOMB = 11;
    private final static int NAV_GITHUB = 12;

    private final static int[] NAV_STRINGS = new int[]{
            R.string.nav_videos,
            R.string.nav_games,
            R.string.nav_unarchived,
            R.string.nav_search,
            0,
            R.string.nav_favorites,
            R.string.nav_downloads,
            0,
            R.string.nav_account,
            R.string.nav_preferences,
            0,
            R.string.nav_giant_bomb,
            R.string.nav_github,
    };

    private final static int[] NAV_ICONS = new int[] {
            R.drawable.ic_videocam_black_24dp,
            R.drawable.ic_games_black_24dp,
            R.drawable.ic_movie_black_24dp,
            R.drawable.ic_search_black_24dp,
            0,
            R.drawable.ic_favorite_black_24dp,
            R.drawable.ic_get_app_black_24dp,
            0,
            R.drawable.ic_account_circle_black_24dp,
            R.drawable.ic_settings_black_24dp,
            0,
            R.drawable.ic_public_black_24dp,
            R.drawable.ic_public_black_24dp,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mLayoutInflater = LayoutInflater.from(this);

        mFragmentManager = getFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();

        // boolean play = Util.checkGooglePlayServices(this);
        boolean play = BaseCastManager.checkGooglePlayServices(this);
        if (play) {
            Util.registerGCMIfNecessary(getApplicationContext());
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        ) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setupNavDrawer();

        // Title will be app_name to start, we'll work from there.

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                .add(R.id.content_list, VideoListFragment.newInstance(null), VIDEO_LIST_FRAGMENT)
                .commit();
        } else {
            mSelectedId = savedInstanceState.getInt(STATE_SELECTED_ID);
            mSelectedUnarchivedId = savedInstanceState.getString(STATE_SELECTED_UNARCHIVED_ID);
        }

        getVideoCastManager().reconnectSessionIfPossible(this, false, 3);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.video_list, menu);
        MenuItem item = getVideoCastManager().addMediaRouterButton(menu, R.id.media_route_menu_item);

        mMediaRoute = item.getActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case (R.id.search_menu_item):
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_ID, mSelectedId);
        outState.putString(STATE_SELECTED_UNARCHIVED_ID, mSelectedUnarchivedId);
        super.onSaveInstanceState(outState);
    }

    public void setupNavDrawer() {
        mNavLayout = (LinearLayout) findViewById(R.id.navdrawer);

        for (int position = 0; position < NAV_STRINGS.length; ++position) {
            int option = NAV_STRINGS[position];

            if (option == 0) {
                mLayoutInflater.inflate(R.layout.divider, mNavLayout);
            } else {
                TextView navItem = (TextView) mLayoutInflater.inflate(R.layout.drawer_list_item, null);

                navItem.setText(option);

                if (position < NAV_ICONS.length) {
                    navItem.setCompoundDrawablesWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
                }

                mNavLayout.addView(navItem);

                setupNavOnClickListener(navItem, position);
            }
        }
    }

    private void setupNavOnClickListener(View navItem, final int position) {
        navItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleNavItemClick(position);
            }
        });
    }

    private void handleNavItemClick(int position) {
        Fragment fragment;
        switch (position) {
            case (NAV_VIDEOS):
                mDrawerLayout.closeDrawers();
                if (mFragmentManager.findFragmentByTag(VIDEO_LIST_FRAGMENT) != null) {
                    break;
                }

                fragment = mFragmentManager.findFragmentById(R.id.content_detail);
                if (fragment != null) {
                    mFragmentManager.beginTransaction().remove(fragment).commit();
                }

                mFragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.no_op, R.animator.no_op)
                        .replace(R.id.content_list, VideoListFragment.newInstance(null), VIDEO_LIST_FRAGMENT)
                        .commit();
                break;
            case (NAV_GAMES):
                mDrawerLayout.closeDrawers();
                if (mFragmentManager.findFragmentByTag(GAME_LIST_FRAGMENT) != null) {
                    break;
                }

                fragment = mFragmentManager.findFragmentById(R.id.content_detail);
                if (fragment != null) {
                    mFragmentManager.beginTransaction().remove(fragment).commit();
                }

                mFragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.no_op, R.animator.no_op)
                        .replace(R.id.content_list, GameListFragment.newInstance(), GAME_LIST_FRAGMENT)
                        .commit();
                break;
            case (NAV_UNARCHIVED):
                mDrawerLayout.closeDrawers();
                if (mFragmentManager.findFragmentByTag(UNARCHIVED_LIST_FRAGMENT) != null) {
                    break;
                }

                fragment = mFragmentManager.findFragmentById(R.id.content_detail);
                if (fragment != null) {
                    mFragmentManager.beginTransaction().remove(fragment).commit();
                }

                mFragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.no_op, R.animator.no_op)
                        .replace(R.id.content_list, UnarchivedListFragment.newInstance(), UNARCHIVED_LIST_FRAGMENT)
                        .commit();
                break;
            case (NAV_SEARCH):
                mDrawerLayout.closeDrawers();
                Intent searchActivity = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(searchActivity);
                break;
            case (NAV_FAVORITES):
                mDrawerLayout.closeDrawers();
                startActivity(IntentUtil.getFavoritesActivityIntent(MainActivity.this));
                // favorites
                break;
            case (NAV_DOWNLOADS):
                mDrawerLayout.closeDrawers();
                Intent downloadsActivity = new Intent(getApplicationContext(), DownloadsActivity.class);
                startActivity(downloadsActivity);
                // downloads
                break;
            case (NAV_SUBSCRIPTION):
                DialogFragment f;
                if (Util.giantBombAccountLinked(getApplicationContext())) {
                    f = UnlinkSubscriptionFragment.newInstance();
                } else {
                    f = LinkSubscriptionFragment.newInstance();
                }
                f.show(getFragmentManager().beginTransaction(), LINK_ACCOUNT_FRAGMENT);
                break;
            case (NAV_SETTINGS):
                Intent preferencesIntent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivityForResult(preferencesIntent, REQUEST_CODE_ACCOUNT_LINKED);
                break;
            case (NAV_GIANTBOMB):
                Intent giantBombIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.giant_bomb_web_uri)));
                startActivity(giantBombIntent);
                break;
            case (NAV_GITHUB):
                Intent githubIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_web_uri)));
                startActivity(githubIntent);
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ACCOUNT_LINKED && resultCode == Activity.RESULT_OK) {
            onAccountStateChanged();
        }
    }

    public void onVideoSelected(Video video) {
        if (findViewById(R.id.content_detail) != null) {
            if (mSelectedId == video.getId()) {
                return;
            }
            mSelectedId = video.getId();
        }

        if (findViewById(R.id.content_detail) != null) {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.content_detail, VideoDetailFragment.newInstance(video), VIDEO_DETAIL_FRAGMENT)
                    .commit();
        } else {
            if (mSelectedId != video.getId()) {
                Fragment fragment = mFragmentManager.findFragmentById(R.id.content_detail);
                if (fragment != null) {
                    mFragmentManager.beginTransaction().remove(fragment).commit();
                }
            }
            startActivity(IntentUtil.getPopulatedVideoDetailActivityIntent(this, video));
        }
    }

    public void onGameSelected(int gameId) {
        if (findViewById(R.id.content_detail) != null) {
            if (mSelectedId == gameId) {
                return;
            }
            mSelectedId = gameId;
        }

        if (findViewById(R.id.content_detail) != null) {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.content_detail, GameDetailFragment.newInstance(gameId), GAME_DETAIL_FRAGMENT)
                    .commit();
        } else {
            if (mSelectedId != gameId) {
                Fragment fragment = mFragmentManager.findFragmentById(R.id.content_detail);
                if (fragment != null) {
                    mFragmentManager.beginTransaction().remove(fragment).commit();
                }
            }
            startActivity(IntentUtil.getGameDetailActivityIntent(this, gameId));
        }
    }

    public void onDetailLoadFailed() {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.content_detail);
        if (fragment != null) {
            mFragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onAccountStateChanged() {
        VideoListFragment videoListFragment = (VideoListFragment) mFragmentManager.findFragmentByTag(VIDEO_LIST_FRAGMENT);
        if (videoListFragment != null) {
            videoListFragment.reloadVideosList();
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();
                }
            }, DRAWER_CLOSE_DELAY);
        }
    }

    @Override
    public void onUnarchivedSelected(YouTubeVideo video) {
        if (findViewById(R.id.content_detail) != null) {
            if (mSelectedUnarchivedId != null && mSelectedUnarchivedId.equals(video.getVideoId())) {
                return;
            }
            mSelectedUnarchivedId = video.getVideoId();
        }


        if (findViewById(R.id.content_detail) != null) {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.content_detail, UnarchivedDetailFragment.newInstance(video), UNARCHIVED_DETAIL_FRAGMENT)
                    .commit();
        } else {
            if (mSelectedUnarchivedId != video.getVideoId()) {
                Fragment fragment = mFragmentManager.findFragmentById(R.id.content_detail);
                if (fragment != null) {
                    mFragmentManager.beginTransaction().remove(fragment).commit();
                }
            }
            Intent intent = new Intent(this, UnarchivedDetailActivity.class);
            intent.putExtra(UnarchivedDetailActivity.EXTRA_YOUTUBE_VIDEO, video);
            startActivity(intent);
        }
    }
}