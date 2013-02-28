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
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.json.JSONException;

/**
 * Created with IntelliJ IDEA.
 * User: Gamer
 * Date: 2013-02-28
 * Time: 02:27
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends Activity {
    private final String statsUrl = "http://api.pathofexile.com/leagues/%s?ladder=1&ladderOffset=0&ladderLimit=%d";
    private final String normalLeague = "Default";
    private final String hardcoreLeague = "Hardcore";
    private final int limit = 200;

    private Button toggleButton;
    private Button updateButton;
    private ListView statsView;

    private boolean showHardcore = false;

    private PoeEntry[] poeEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        poeEntries = null;

        toggleButton = (Button) findViewById(R.id.toggleButton);
        updateButton = (Button) findViewById(R.id.updateButton);
        statsView = (ListView) findViewById(R.id.statsView);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHardcore = !showHardcore;
                updateList(showHardcore);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(showHardcore);
            }
        });

        statsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onStatsViewClick(position);
            }
        });

        updateList(showHardcore);
    }

    private void showToast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void updateList(boolean hardcore) {
        toggleButton.setEnabled(false);
        updateButton.setEnabled(false);
        String url = String.format(statsUrl, hardcore ? hardcoreLeague : normalLeague, limit);
        new RetrieveStatsTask(new RetrieveStatsListener() {
            @Override
            public void handleResponse(StatsResponse response, boolean hardcore) {
                updateList(response, hardcore);
            }
        }, hardcore).execute(url);
    }

    private void updateList(StatsResponse response, boolean hardcore) {
        if (response == null) {
            showToast("Failed to retrieve PoE stats data!");
            return;
        }

        try {
            StatusLine status = response.getStatus();
            if (status.getStatusCode() == HttpStatus.SC_OK) {
                String responseString = response.getResponseString();
                poeEntries = PoeEntry.getEntriesFromJSONString(responseString);
                String[] data = new String[poeEntries.length];
                for (int i = 0; i < poeEntries.length; i++) {
                    data[i] = poeEntries[i].toString();
                }
                ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        int colorId = R.color.offline;
                        if (position >= 0 && position < poeEntries.length)
                            colorId = poeEntries[position].isOnline() ? R.color.online : R.color.offline;
                        Resources res = getResources();
                        ((TextView)view).setTextColor(res.getColor(colorId));
                        return view;
                    }
                };
                statsView.setAdapter(adapter);
                toggleButton.setText(hardcore ? R.string.show_normal : R.string.show_hardcore);
                showToast(String.format("%s stats updated", hardcore ? "Hardcore" : "Normal"));
            } else {
                showToast("Failed to retrieve PoE stats data!");
            }
        } catch (JSONException e) {
            showToast("Error while parsing JSON data");
        } finally {
            toggleButton.setEnabled(true);
            updateButton.setEnabled(true);
        }
    }

    private void onStatsViewClick(int index) {
        if (poeEntries == null || (index < 0 || index >= poeEntries.length))
            return;
        PoeEntry entry = poeEntries[index];
        AlertDialog dialog = entry.getInfoDialog(this, getResources());
        dialog.show();
    }
}
