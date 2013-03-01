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
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Gamer
 * Date: 2013-03-01
 * Time: 02:42
 * To change this template use File | Settings | File Templates.
 */
public class EntryAdapter extends ArrayAdapter<PoeEntry> {
    private ArrayList<PoeEntry> entries;
    private Activity activity;
    private Resources res;

    public EntryAdapter(Context context, ArrayList<PoeEntry> entries, Activity activity, Resources res) {
        super(context, android.R.layout.simple_list_item_1, entries);

        this.entries = entries;
        this.activity = activity;
        this.res = res;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public PoeEntry getItem(int position) {
        if (position < 0 || position >= entries.size())
            return null;
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < 0 || position >= entries.size())
            return null;

        PoeEntry entry = entries.get(position);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.list_item, null);

        ((TextView) view.findViewById(R.id.item_rank)).setText(String.valueOf(entry.getRank()));
        ((TextView) view.findViewById(R.id.item_name)).setText(entry.getName());
        ((TextView) view.findViewById(R.id.item_level)).setText(String.valueOf(entry.getLevel()));
        ((TextView) view.findViewById(R.id.item_class)).setText(entry.getClassName());
        ((TextView) view.findViewById(R.id.item_experience)).setText(entry.getShortExpString());

        if (entry.isOnline())
            ((TextView)view.findViewById(R.id.item_name)).setTextColor(res.getColor(R.color.online));

        return view;
    }
}
