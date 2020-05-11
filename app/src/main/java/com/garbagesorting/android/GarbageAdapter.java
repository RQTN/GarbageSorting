package com.garbagesorting.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.garbagesorting.android.db.Garbage;
import com.garbagesorting.android.util.Utility;

import java.util.List;

public class GarbageAdapter extends ArrayAdapter<Garbage> {

    private int resourceId;

    public GarbageAdapter(Context context, int resource, List<Garbage> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Garbage garbage = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.garbageName = (TextView) view.findViewById(R.id.garbage_name);
            viewHolder.garbageLabel = (TextView) view.findViewById(R.id.garbage_label);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.garbageName.setText(garbage.getName());
        viewHolder.garbageLabel.setText("[" + Utility.labelMapping(garbage.getLabel()) + "]");
        return view;
    }

    class ViewHolder {
        TextView garbageName;
        TextView garbageLabel;
    }
}
