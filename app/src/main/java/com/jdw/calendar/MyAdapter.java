package com.jdw.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by JDW on 2018-10-31.
 */

public class MyAdapter extends BaseAdapter {
    private List<String> list;
    private List<Boolean> hasSchedule;
    private LayoutInflater inflater;
    private Context context;
    private Calendar focusCal;

    public MyAdapter(Context context, List<String> list, List<Boolean> hasSchedule) {
        this.list = list;
        this.hasSchedule = hasSchedule;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_calendar, parent, false);
            holder = new ViewHolder();
            holder.gridItemView = (TextView) convertView.findViewById(R.id.tv_item_gridview);
            holder.gridCheckView = (TextView) convertView.findViewById(R.id.tv_item_checkview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.gridItemView.setText("" + getItem(position));
        if (hasSchedule.get(position)) {
            holder.gridCheckView.setText("O");
        }else{
            holder.gridCheckView.setText("");
        }

        // 활성화 날짜 확인
        boolean isFocus = Integer.toString(focusCal.get(Calendar.DATE)).equals(getItem(position));

        if (isFocus) { // 활성화 색 변경
            holder.gridItemView.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }

        return convertView;
    }

    private class ViewHolder {
        TextView gridItemView;
        TextView gridCheckView;
    }

    public void setFocusCal(Calendar focusCal) {
        this.focusCal = focusCal;
    }
}
