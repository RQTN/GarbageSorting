package com.garbagesorting.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.garbagesorting.android.R;
import com.garbagesorting.android.Result;
import com.garbagesorting.android.util.Utility;

import java.util.List;

public class ResultAdapter extends ArrayAdapter<Result> {

    private int resourceId;

    public ResultAdapter(Context context, int resource, List<Result> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Result result = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.garbageName = (TextView) view.findViewById(R.id.garbage_name);
            viewHolder.garbageProb = (TextView) view.findViewById(R.id.garbage_prob);
            viewHolder.garbageLabel = (TextView) view.findViewById(R.id.garbage_label);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.garbageName.setText(result.getName());
        viewHolder.garbageProb.setText(String.format("%2.2f%%", 100*result.getProb()));
        viewHolder.garbageLabel.setText("[" + Utility.labelMapping(result.getLabel()) + "]");
        return view;
    }

    class ViewHolder {
        TextView garbageName;
        TextView garbageProb;
        TextView garbageLabel;
    }
}
