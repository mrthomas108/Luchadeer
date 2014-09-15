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

package org.dforsyth.android.luchadeer.ui.account;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.model.giantbomb.ApiKey;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;


public class LinkSubscriptionFragment extends DialogFragment {
    private Activity mActivity;
    private OnAccountLinkedListener mOnAccountLinkedListener;
    private OnAccountStateChangedListener mOnAccountStateChangedListener;
    private EditText mLinkCode;

    private LuchadeerApi mApi;
    private LuchadeerPreferences mPreferences;

    public LinkSubscriptionFragment() {
    }

    public static LinkSubscriptionFragment newInstance() {
        return new LinkSubscriptionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());

        mActivity = getActivity();
        mApi = LuchadeerApi.getInstance(mActivity.getApplicationContext());
        mPreferences = LuchadeerPreferences.getInstance(mActivity.getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_link_account, container, false);

        mLinkCode = (EditText) rootView.findViewById(R.id.link_code_text);

        mLinkCode.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        TextView helpText = (TextView) rootView.findViewById(R.id.link_code_help);
        helpText.setMovementMethod(LinkMovementMethod.getInstance());

        Button button = (Button) rootView.findViewById(R.id.link_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String linkCode = mLinkCode.getText().toString();

                validateLinkCode(linkCode);
            }
        });

        return rootView;
    }

    private void validateLinkCode(String linkCode) {
        mApi.validate(
                linkCode,
                new Response.Listener<ApiKey>() {
                    @Override
                    public void onResponse(ApiKey apiKey) {
                        onValidateRequestCompleted(apiKey);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onValidateRequestFailed();
                    }
        });
    }

    public void onValidateRequestCompleted(ApiKey apiKey) {
        String linkedApiKey = apiKey.getApiKey();
        if (linkedApiKey == null) {
            Toast.makeText(mActivity, getString(R.string.account_link_code_invalid), Toast.LENGTH_SHORT).show();
            mLinkCode.setText("");
            return;
        }

        Toast.makeText(mActivity, getString(R.string.account_linked_message), Toast.LENGTH_SHORT).show();
        mPreferences.setApiKey(linkedApiKey);
        mApi.reloadApiKey();

        if (mOnAccountStateChangedListener != null) {
            mOnAccountStateChangedListener.onAccountStateChanged();
        }

        if (mOnAccountLinkedListener != null) {
            mOnAccountLinkedListener.onAccountLinked();
        }

        dismiss();
    }

    public void onValidateRequestFailed() {
        Toast.makeText(mActivity, getString(R.string.validate_request_error), Toast.LENGTH_SHORT).show();
        return;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnAccountStateChangedListener = (OnAccountStateChangedListener) activity;
    }

    public interface OnAccountLinkedListener {
        public void onAccountLinked();
    }

    public void setOnAccountLinkedListener(OnAccountLinkedListener onAccountLinkedListener) {
        mOnAccountLinkedListener = onAccountLinkedListener;
    }
}