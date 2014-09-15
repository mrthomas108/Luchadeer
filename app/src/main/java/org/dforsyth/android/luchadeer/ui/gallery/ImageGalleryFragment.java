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

package org.dforsyth.android.luchadeer.ui.gallery;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.giantbomb.Image;

import java.util.ArrayList;


public class ImageGalleryFragment extends Fragment {
    private static final String ARG_IMAGES = "images";
    private static final String ARG_START_URL = "start_url";

    private ViewPager mViewPager;

    private ArrayList<Image> mImages;

    public ImageGalleryFragment(){}

    public static ImageGalleryFragment newInstance(ArrayList<Image> images, String startUrl) {
        ImageGalleryFragment fragment = new ImageGalleryFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_IMAGES, images);
        args.putString(ARG_START_URL, startUrl);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_gallery, container, false);

        mImages = getArguments().getParcelableArrayList(ARG_IMAGES);
        String startUrl = getArguments().getString(ARG_START_URL);

        mViewPager = (ViewPager) rootView.findViewById(R.id.image_pager);
        mViewPager.setAdapter(new ImagePagerAdapter(getFragmentManager()));

        for (int i = 0; i < mImages.size(); ++i) {
            String url = mImages.get(i).getSuperUrl();
            if (url != null && url.equals(startUrl)) {
                mViewPager.setCurrentItem(i);
            }
        }

        return rootView;
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public Fragment getItem(int i) {
            Image image = mImages.get(i);
            return ImageFragment.newInstance(image);
        }
    }
}
