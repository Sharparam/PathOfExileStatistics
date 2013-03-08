/*
 * Copyright (c) 2013 by Adam Hellberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.f16gaming.pathofexilestatistics;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Gamer
 * Date: 2013-02-28
 * Time: 02:48
 * To change this template use File | Settings | File Templates.
 */
public class RetrieveStatsTask extends AsyncTask<String, Void, StatsResponse> {
    private RetrieveStatsListener listener;
    private boolean hardcore;
    private boolean refresh;
    private boolean jump;

    public RetrieveStatsTask(RetrieveStatsListener listener, boolean hardcore, boolean refresh, boolean jump) {
        this.listener = listener;
        this.hardcore = hardcore;
        this.refresh = refresh;
        this.jump = jump;
    }

    @Override
    protected StatsResponse doInBackground(String... urls) {
        try {
            HttpGet get = new HttpGet(urls[0]);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(get);
            StatsResponse statsResponse = null;
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);

                out.close();
                String responseString = out.toString();
                statsResponse = new StatsResponse(status, responseString);
            } else {
                response.getEntity().getContent().close();
            }
            return statsResponse;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(StatsResponse response) {
        listener.handleResponse(response, hardcore, refresh, jump);
    }
}
