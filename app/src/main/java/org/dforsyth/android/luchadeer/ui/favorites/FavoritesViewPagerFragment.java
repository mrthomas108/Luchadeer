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

package org.dforsyth.android.luchadeer.ui.favorites;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.ui.util.DisableableViewPager;
import org.dforsyth.android.luchadeer.ui.util.SlidingTabLayout;


public class FavoritesViewPagerFragment extends Fragment implements FavoritesListFragment.ActionModeListener {

    private ActionBarActivity mActivity;
    private ActionBar mActionBar;

    private SlidingTabLayout mSlidingTabLayout;

    private DisableableViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_viewpager, container, false);

        mActivity = (ActionBarActivity) getActivity();
        mActionBar = mActivity.getSupportActionBar();

        mViewPager = (DisableableViewPager) view.findViewById(R.id.favorites_pager);

        mViewPager.setAdapter(new FavoritesFragmentPagerAdapter(getChildFragmentManager()));

        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tab_layout);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.white);
            }

            @Override
            public int getDividerColor(int position) {
                return getResources().getColor(R.color.white);
            }
        });
        mSlidingTabLayout.setTextColor(getResources().getColor(R.color.white));

        // call this last so textcolor takes
        mSlidingTabLayout.setViewPager(mViewPager);

        return view;
    }

    @Override
    public void onCreateActionMode() {
        mSlidingTabLayout.disableTabs();
        mViewPager.setPagerEnabled(false);
    }

    @Override
    public void onDestroyActionMode() {
        mSlidingTabLayout.enableTabs();
        mViewPager.setPagerEnabled(true);
    }

    private class FavoritesFragmentPagerAdapter extends FragmentPagerAdapter {

        public FavoritesFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case (0):
                    return "Videos";
                case (1):
                    return "Games";
                default:
            }

            throw new RuntimeException("no position");
        }

        @Override
        public Fragment getItem(int i) {
            FavoritesListFragment fragment = null;
            switch (i) {
                case (0):
                    fragment = new VideoFavoritesListFragment();
                    break;
                case (1):
                    fragment = new GameFavoritesListFragment();
                    break;
            }

            if (fragment != null) {
                fragment.setActionModeListener(FavoritesViewPagerFragment.this);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
