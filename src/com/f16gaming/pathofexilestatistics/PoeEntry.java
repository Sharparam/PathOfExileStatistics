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
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created with IntelliJ IDEA.
 * User: Gamer
 * Date: 2013-02-28
 * Time: 03:05
 * To change this template use File | Settings | File Templates.
 */
public class PoeEntry {
    private final String toStringFormat = "#%03d: %s (%d)";

    private String account;
    private int rank;
    private String name;
    private int level;
    private boolean online;
    private String className;
    private long experience;

    public PoeEntry(String account, int rank, String name, int level, boolean online, String className, long experience) {
        this.account = account;
        this.rank = rank;
        this.name = name;
        this.level = level;
        this.online = online;
        this.className = className;
        this.experience = experience;
    }

    public static PoeEntry[] getEntriesFromJSONString(String jsonData) throws JSONException {
        JSONObject root = (JSONObject) new JSONTokener(jsonData).nextValue();
        JSONObject ladder = root.getJSONObject("ladder");
        JSONArray entries = ladder.getJSONArray("entries");
        int length = entries.length();
        PoeEntry[] result = new PoeEntry[length];
        for (int i = 0; i < length; i++) {
            JSONObject entry = entries.getJSONObject(i);
            String account = entry.getJSONObject("account").getString("name");
            int rank = entry.getInt("rank");
            JSONObject character = entry.getJSONObject("character");
            String name = character.getString("name");
            int level = character.getInt("level");
            boolean online = entry.getBoolean("online");
            String className = character.getString("class");
            long experience = character.getLong("experience");
            PoeEntry poeEntry = new PoeEntry(account, rank, name, level, online, className, experience);
            result[i] = poeEntry;
        }
        return result;
    }

    public String getAccount() {
        return account;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isOnline() {
        return online;
    }

    public String getClassName() {
        return className;
    }

    public long getExperience() {
        return experience;
    }

    public String getShortExpString() {
        float exp = experience;

        final String[] types =
        {
            "",  //  999
            "K", //   1K
            "M", //   1M
            "B"  //   1B
        };

        int order = 0;

        while (exp >= 1000 && order + 1 < types.length) {
            order++;
            exp /= 1000;
        }

        String expString = String.format("%.2f", exp);
        if (expString.length() > 5) {
            expString = expString.substring(0, 4);
            if (expString.charAt(expString.length() - 1) == '.')
                expString = expString.substring(0, 3);
        }

        return String.format("%s%s", expString, types[order]);
    }

    public AlertDialog getInfoDialog(Activity activity, Resources res) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.info, null);
        String nameFormat = res.getString(R.string.info_name);
        String accountFormat = res.getString(R.string.info_account);
        String rankFormat = res.getString(R.string.info_rank);
        String levelFormat = res.getString(R.string.info_level);
        String classFormat = res.getString(R.string.info_class);
        String experienceFormat = res.getString(R.string.info_experience);
        ((TextView) view.findViewById(R.id.info_name)).setText(String.format(nameFormat, name));
        ((TextView) view.findViewById(R.id.info_account)).setText(String.format(accountFormat, account));
        ((TextView) view.findViewById(R.id.info_rank)).setText(String.format(rankFormat, rank));
        ((TextView) view.findViewById(R.id.info_level)).setText(String.format(levelFormat, level));
        ((TextView) view.findViewById(R.id.info_class)).setText(String.format(classFormat, className));
        ((TextView) view.findViewById(R.id.info_experience)).setText(String.format(experienceFormat, experience));

        TextView status = (TextView) view.findViewById(R.id.info_status);
        status.setText(online ? R.string.online : R.string.offline);
        status.setTextColor(online ? res.getColor(R.color.online) : res.getColor(R.color.offline));

        builder.setTitle(R.string.info_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public String toString() {
        return String.format(toStringFormat, rank, name, level);
    }
}
