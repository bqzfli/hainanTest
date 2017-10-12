package com.lisa.esri.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hnTest.R;
import com.example.hnTest.Util;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 查询结果的显示
 * Created by WANT on 2017/10/8.
 */
public class CoverFlowAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData = new ArrayList<>(0);
    private Context mContext;

    public CoverFlowAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<Map<String,Object>> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int pos) {
        return mData.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_result_search, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.Title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.Info = (TextView) rowView.findViewById(R.id.info);
            viewHolder.Layer = (TextView) rowView.findViewById(R.id.layer);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();


        Map<String,Object> result = mData.get(position);

        Iterator<String> keys = result.keySet().iterator();
        String strId = "编号："+(String) result.get(Util.KEY_OBJECTID);
        String strLayerName = "图层：" + (String) result.get(Util.KEY_LAYERNAME);
        String strResult = "";
        while (keys.hasNext()){
            String key = keys.next();
            if(result.get(key)!=null
                    &&!key.equalsIgnoreCase(Util.KEY_OBJECTID)
                    &&!key.equalsIgnoreCase(Util.KEY_GEO)
                    &&!key.equalsIgnoreCase(Util.KEY_GEOJSON)
                    &&!key.equalsIgnoreCase(Util.KEY_LAYERNAME)) {
                String value = String.valueOf(result.get(key));
                strResult += key + "：'" + value + "'\n";
            }
        }

        holder.Title.setText(strId);
        holder.Layer.setText(strLayerName);
        holder.Info.setText(strResult);

        return rowView;
    }


    static class ViewHolder {
        public TextView Title;
        public TextView Layer;
        public TextView Info;
    }
}