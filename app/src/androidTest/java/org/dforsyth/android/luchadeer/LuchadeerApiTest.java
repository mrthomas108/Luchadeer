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

import android.test.AndroidTestCase;

public class LuchadeerApiTest extends AndroidTestCase {

    /*
    private LuchadeerApi mApi;
    private LuchadeerPreferences mPreferences;
    private MockHttpStack mMockHttpStack;

    private static final String FAKE_API_KEY = "fake-api-key";
    private static final String TEST_PREFERENCE_NAME = "luchdeer_test";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPreferences = new LuchadeerPreferences(getContext(), TEST_PREFERENCE_NAME);
        mPreferences.clearPreferences();

        mMockHttpStack = new MockHttpStack();
        mApi = new LuchadeerApi(getContext(), mMockHttpStack, mPreferences);
    }

    public void testNoApiKey() throws InterruptedException {
        mMockHttpStack.setResponseToReturn(
                new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));

        RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<Video>>> future = RequestFuture.newFuture();

        mApi.videos(
                this,
                null,
                0,
                future,
                future
        );

        try {
            future.get();
        } catch (ExecutionException e) {
            // expect an error because of the empty response body
            assertTrue(e.getCause() instanceof LuchadeerApi.GiantBombVolleyError);
        }

        String lastUrl = mMockHttpStack.getLastUrl();
        String param = Uri.parse(lastUrl).getQueryParameter("api_key");

        assertEquals("", param);
    }

    public void testReloadApiKey() {
        mPreferences.setApiKey(FAKE_API_KEY);

        assertEquals("", mApi.getApiKey());

        mApi.reloadApiKey();

        assertEquals(FAKE_API_KEY, mApi.getApiKey());
    }

    public void testApiKey() throws InterruptedException {
        mPreferences.setApiKey(FAKE_API_KEY);

        mApi.reloadApiKey();

        mMockHttpStack.setResponseToReturn(
                new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));

        RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<Video>>> future = RequestFuture.newFuture();

        mApi.videos(
                this,
                null,
                0,
                future,
                future
        );

        try {
            future.get();
        } catch (ExecutionException e) {
            // expect an error because of the empty response body
            assertTrue(e.getCause() instanceof LuchadeerApi.GiantBombVolleyError);
        }

        String lastUrl = mMockHttpStack.getLastUrl();
        String param = Uri.parse(lastUrl).getQueryParameter("api_key");

        assertEquals(FAKE_API_KEY, param);
    }

    public void testHandleGiantBombErrorResponse() {
        HttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        BasicHttpEntity entity = new BasicHttpEntity();
        String body = "{status_code: 101, results: {}}";
        try {
            entity.setContent(new ByteArrayInputStream(body.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            assertTrue(false);
        }
        response.setEntity(entity);
        mMockHttpStack.setResponseToReturn(response);

        RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<Video>>> future = RequestFuture.newFuture();

        mApi.videos(
                this,
                null,
                0,
                future,
                future
        );

        Exception error = null;

        try {
            future.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            error = e;
        }

        assertTrue(error.getCause() instanceof LuchadeerApi.GiantBombVolleyError);
        LuchadeerApi.GiantBombVolleyError giantBombVolleyError = (LuchadeerApi.GiantBombVolleyError) error.getCause();
        assertEquals(101, giantBombVolleyError.getResponse().getStatusCode());
    }

    public void testHandleAppEngineQuotaLimit() {
        HttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 503, "Over Quota");
        mMockHttpStack.setResponseToReturn(response);

        RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<Video>>> future = RequestFuture.newFuture();

        mApi.videos(
                this,
                null,
                0,
                future,
                future
        );

        Exception error = null;

        try {
            future.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            error = e;
        }

        assertTrue(error.getCause() instanceof VolleyError);
        VolleyError volleyError = (VolleyError) error.getCause();
        assertEquals(503, volleyError.networkResponse.statusCode);
    }
    */
}
