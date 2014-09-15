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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.ui.util.DisableableViewPager;


public class FavoritesViewPagerFragment extends Fragment implements ViewPager.OnPageChangeListener, FavoritesListFragment.ActionModeListener {

    private Activity mActivity;
    private ActionBar mActionBar;

    private DisableableViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_viewpager, container, false);

        mViewPager = (DisableableViewPager) view.findViewById(R.id.favorites_pager);

        mActivity = getActivity();
        mActionBar = mActivity.getActionBar();

        if (mActionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
            return view;

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mActionBar.addTab(
                mActionBar.newTab()
                        .setText(R.string.tab_videos)
                        .setTabListener(new FavoritesTabListener(mViewPager, 0)));

        mActionBar.addTab(
                mActionBar.newTab()
                        .setText(R.string.tab_games)
                        .setTabListener(new FavoritesTabListener(mViewPager, 1)));

        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(new FavoritesFragmentPagerAdapter(getFragmentManager()));

        return view;
    }

    @Override
    public void onCreateActionMode() {
        mViewPager.setPagerEnabled(false);
    }

    @Override
    public void onDestroyActionMode() {
        mViewPager.setPagerEnabled(true);
    }

    private class FavoritesTabListener implements ActionBar.TabListener {
        private int mItem;
        private ViewPager mPager;

        public FavoritesTabListener(ViewPager pager, int pagerItem) {
            mItem = pagerItem;
            mPager = pager;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mPager.setCurrentItem(mItem);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }

    private class FavoritesFragmentPagerAdapter extends FragmentPagerAdapter {

        public FavoritesFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
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

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        // tabs might be gone in context menu...
        if (mActionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
            mActionBar.setSelectedNavigationItem(i);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
