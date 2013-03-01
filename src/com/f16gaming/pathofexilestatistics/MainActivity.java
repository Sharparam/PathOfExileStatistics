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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: Gamer
 * Date: 2013-02-28
 * Time: 02:27
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends Activity {
    private final String statsUrl = "http://api.pathofexile.com/leagues/%s?ladder=1&ladderOffset=%d&ladderLimit=%d";
    private final String normalLeague = "Default";
    private final String hardcoreLeague = "Hardcore";
    private final int limit = 200;
    private final int max = 15000;

    private ProgressDialog progressDialog;
    private CharSequence progressDialogText;

    private ListView statsView;
    private View listFooter;

    private int offset = 0; // Num entries to load = limit + limit * offset
    private boolean showHardcore = false;
    private int refreshOffset = 0; // Current refresh offset

    private ArrayList<PoeEntry> poeEntries;
    private EntryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        poeEntries = new ArrayList<PoeEntry>();

        statsView = (ListView) findViewById(R.id.statsView);

        listFooter = getLayoutInflater().inflate(R.layout.list_footer, null);

        statsView.addFooterView(listFooter);

        statsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onStatsViewClick(position);
            }
        });

        offset = -1;
        updateList(showHardcore);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem toggleItem = menu.findItem(R.id.menu_toggle);
        toggleItem.setTitle(showHardcore ? R.string.menu_normal : R.string.menu_hardcore);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshOffset = 0;
                updateList(showHardcore, true);
                return true;
            case R.id.menu_reset:
                offset = -1;
                updateList(showHardcore);
            case R.id.menu_toggle:
                updateList(!showHardcore);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(CharSequence text) {
        showProgress("", text);
    }

    private void showProgress(CharSequence title, CharSequence text) {
        if (progressDialog != null) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(text);
            return;
        }

        progressDialogText = text;
        progressDialog = ProgressDialog.show(this, title, progressDialogText, true, false);
    }

    private void showProgress(CharSequence title, String format, Object... args) {
        showProgress(title, String.format(format, args));
    }

    private void hideProgress() {
        if (progressDialog == null)
            return;
        progressDialog.dismiss();
        progressDialog = null;
    }

    private void updateList(boolean hardcore) {
        updateList(hardcore, false);
    }

    private void updateList(boolean hardcore, boolean refresh) {
        if (hardcore != showHardcore)
            offset = 0;
        else if (!refresh)
            offset++;

        if (refresh)
            showProgress("Refreshing", "Refreshing data (%d/%d)...", refreshOffset + 1, offset + 1);
        else
            showProgress("Retrieving stats data...");
        String league = hardcore ? hardcoreLeague : normalLeague;
        String url = refresh ?
                String.format(statsUrl, league, limit * refreshOffset, limit)
                : String.format(statsUrl, league, limit * offset, limit);
        new RetrieveStatsTask(new RetrieveStatsListener() {
            @Override
            public void handleResponse(StatsResponse response, boolean hardcore, boolean refresh) {
                updateList(response, hardcore, refresh);
            }
        }, hardcore, refresh).execute(url);
    }

    private void updateList(StatsResponse response, boolean hardcore, boolean refresh) {
        if (response == null) {
            showToast("Failed to retrieve PoE stats data!");
            hideProgress();
            return;
        }

        try {
            StatusLine status = response.getStatus();
            if (status.getStatusCode() == HttpStatus.SC_OK) {
                String responseString = response.getResponseString();
                PoeEntry[] newEntries = PoeEntry.getEntriesFromJSONString(responseString);

                if (hardcore != showHardcore || (refresh && refreshOffset == 0))
                    poeEntries.clear();

                Collections.addAll(poeEntries, newEntries);

                if (refresh && refreshOffset < offset) {
                    refreshOffset++;
                    updateList(hardcore, true);
                    return;
                }

                if (adapter == null) {
                    adapter = new EntryAdapter(this, poeEntries, this, getResources());
                    statsView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

                showToast(String.format("%s stats updated", hardcore ? "Hardcore" : "Normal"));
                if (hardcore == showHardcore && limit + limit * offset >= max)
                    statsView.removeFooterView(listFooter);
                else if (hardcore != showHardcore)
                {
                    if (statsView.getFooterViewsCount() == 0)
                        statsView.addFooterView(listFooter);
                    statsView.setSelection(0);
                }
                showHardcore = hardcore;
                setTitle(showHardcore ? R.string.hardcore : R.string.normal);
                if (Build.VERSION.SDK_INT >= 11)
                    invalidateOptionsMenu();
            } else {
                showToast("Failed to retrieve PoE stats data!");
            }
        } catch (JSONException e) {
            showToast("Error while parsing JSON data");
        }

        hideProgress();
    }

    private void onStatsViewClick(int index) {
        if (poeEntries == null || index < 0)
            return;

        if (index >= poeEntries.size() && index == statsView.getCount() - 1) {
            updateList(showHardcore);
        } else {
            PoeEntry entry = poeEntries.get(index);
            AlertDialog dialog = entry.getInfoDialog(this, getResources());
            dialog.show();
        }
    }
}
