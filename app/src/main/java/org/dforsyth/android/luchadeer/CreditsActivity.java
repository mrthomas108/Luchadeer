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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class CreditsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView listView = (ListView) findViewById(R.id.listview);

        listView.setAdapter(new ArrayAdapter<AboutInfo>(this, 0, aboutList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // return super.getView(position, convertView, parent);
                if (convertView == null) {
                    // convertView = LayoutInflater.from(AboutActivity.this).inflate(android.R.layout.simple_list_item_2, listView, false);
                    convertView = new LinearLayout(getContext());
                    convertView.setBackgroundResource(0);
                }

                LinearLayout ll = (LinearLayout) convertView;
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.removeAllViews();

                AboutInfo info = getItem(position);

                TextView name = new TextView(getContext());
                name.setText(info.name);
                int textSize = (int) name.getTextSize();

                // special case
                if (info.name.equals("Luchadeer image")) {
                    Drawable d = getResources().getDrawable(R.drawable.lucha_deer_p4_cropped);
                    d.setBounds(0, 0, textSize, textSize);
                    name.setCompoundDrawables(d, null, null, null);
                }
                ll.addView(name);

                TextView copyright  = new TextView(getContext());
                copyright.setText((info.copyright == null ? "" : "Copyright " + info.copyright + ", ")+ info.creator);
                ll.addView(copyright);

                for (String url : info.links) {
                    TextView link = new TextView(getContext());
                    link.setText("(" + url + ")");
                    ll.addView(link);
                }

                int pad = textSize / 2;
                if (convertView.getPaddingBottom() != pad) {
                    convertView.setPadding(pad, pad, pad, pad);
                }

                return convertView;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        });

        listView.setDivider(null);
    }

    private static class AboutInfo {
        String name;
        String copyright;
        String creator;
        String[] links;

        private AboutInfo(String name, String creator, String copyright, String[] links) {
            this.creator = creator;
            this.name = name;
            this.copyright = copyright;
            this.links = links;
        }
    }

    private static AboutInfo[] aboutList = {
            new AboutInfo(
                    "Giant Bomb API",
                    "CBSi",
                    "2014",
                    new String[] {
                            "http://www.giantbomb.com/api"
                    }
            ),
            new AboutInfo(
                    "Giant Bomb Assets",
                    "CBSi",
                    "2014",
                    new String[] {
                            "http://www.giantbomb.com"
                    }
            ),
            new AboutInfo(
                    "Luchadeer image",
                    "Michael Lee Lunsford",
                    null,
                    new String[] {
                            "http://supernormalstep.tumblr.com",
                            "http://www.supernormalstep.com"
                    }
            ),
            new AboutInfo(
                    "Volley",
                    "Google Inc.",
                    "2014",
                    new String[] {
                            "https://android.googlesource.com/platform/frameworks/volley/"
                    }
            ),
            new AboutInfo(
                    "CastCompanionLibrary-android",
                    "Google Inc.",
                    "2014",
                    new String[] {
                            "https://github.com/googlecast/CastCompanionLibrary-android"
                    }
            ),
            new AboutInfo(
                    "Gson",
                    "Google Inc.",
                    "2014",
                    new String[] {
                            "https://code.google.com/p/google-gson/"
                    }
            ),
            new AboutInfo(
                    "BezelImageView",
                    "Google Inc.",
                    "2014",
                    new String[] {
                            "https://github.com/google/iosched/blob/master/android/src/main/java/com/google/samples/apps/iosched/ui/widget/BezelImageView.java"
                    }
            ),
            new AboutInfo(
                    "This project contains code derived from The Android Open Source Project",
                    "The Android Open Source Project",
                    null,
                    new String[] {
                            "http://www.android.com"
                    }
            ),
    };
}
