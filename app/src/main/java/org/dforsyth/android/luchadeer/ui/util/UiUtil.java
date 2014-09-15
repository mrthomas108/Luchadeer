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

package org.dforsyth.android.luchadeer.ui.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class UiUtil {
    public static void overrideListView(View view, ListView newListView) {
        ListView oldListView = (ListView) view.findViewById(android.R.id.list);
        ViewGroup parent = (ViewGroup) oldListView.getParent();

        int idx = parent.indexOfChild(oldListView);
        parent.removeViewAt(idx);
        newListView.setId(android.R.id.list);
        parent.addView(newListView, idx, oldListView.getLayoutParams());
    }

    public static void actionBarItemOnDrawerSlide(View view, float fadeOffset) {
        if (view != null) {
            view.setAlpha(1 - fadeOffset);
            view.setVisibility(View.VISIBLE);
            view.setClickable(false);
        }
    }

    public static void actionBarItemOnDrawerOpened(View view) {
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
            view.setClickable(false);
        }
    }

    public static void actionBarItemOnDrawerClosed(View view) {
        if (view != null) {
            view.setAlpha(1);
            view.setVisibility(View.VISIBLE);
            view.setClickable(true);
        }
    }
}
