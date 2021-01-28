package com.mainactivity.systemdozarzadzaniadomem.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.mainactivity.systemdozarzadzaniadomem.Models.TopicModel;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.util.HashMap;
import java.util.Map;

public class DeviceMainboardActivityAdapter extends RecyclerView.Adapter<DeviceMainboardActivityAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private ItemClickListener itemClickListener;
    private final HashMap<String, TopicModel> topics;
    int red, green, blue;

    public DeviceMainboardActivityAdapter(Context context, HashMap<String, TopicModel> topics) {
        this.inflater = LayoutInflater.from(context);
        this.topics = topics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_devicemainboard_item, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopicModel topic = topics.get((topics.keySet().toArray())[position]);
        holder.tvTopic.setText(topic.getTopicName());
        holder.tvTopicInfo.setText(topic.getValue());
        if(topic.getTypeOfTopic().equals("button") && topic.getValue() == "Wyłącz") {
            holder.background.setBackgroundColor(0x8B36C13C);
            holder.tvTopicInfo.setBackgroundColor(0xB2DCE775);
            holder.tvTopic.setBackgroundColor(0xB2DCE775);
        }
        if(topic.getTypeOfTopic().equals("button") && topic.getValue() == "Włącz") {
            holder.background.setBackgroundColor(0xC91B5E20);
            holder.tvTopicInfo.setBackgroundColor(0x667CB342);
            holder.tvTopic.setBackgroundColor(0x667CB342);
        }

        if(topic.getTypeOfTopic().equals("led")) {
            parseColor(topic.getValue());
            holder.background.setBackgroundColor(Color.argb(60,red,green,blue));
            holder.tvTopic.setBackgroundColor(Color.argb(90,red,green,blue));
            holder.tvTopicInfo.setBackgroundColor(Color.argb(90,red,green,blue));
        }

    }

    public static String getHashMapKeyFromIndex(HashMap<String, String> hashMap, int index) {
        String key = null;
        int pos = 0;
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            if (index == pos) {
                key = entry.getKey();
            }
            pos++;
        }
        return key;
    }

    @Override
    public int getItemCount() {
        return this.topics.size();
    }

    public void setOnClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void removeItem(String key, int position) {
        topics.remove(key);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView tvTopic;
        TextView tvTopicInfo;
        LinearLayout background;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvTopicName);
            tvTopicInfo = itemView.findViewById(R.id.tvTopicInfo);
            background = itemView.findViewById(R.id.layoutBackground);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onLongItemClick(v, getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    private void parseColor(String color) {
        int index = 0;
        String tmp = "";
        for (int i = 0; i < color.length(); i++) {
            if (color.charAt(i) == ',' && index == 0) {
                red = Integer.parseInt(tmp);
                index++;
                tmp = "";
                continue;
            }
            if (color.charAt(i) == ',' && index == 1) {
                green = Integer.parseInt(tmp);
                index++;
                tmp = "";
                continue;
            }
            if (color.charAt(i) == ',' && index == 2) {
                blue = Integer.parseInt(tmp);
                index++;
                tmp = "";
                continue;
            }
            tmp += color.charAt(i);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onLongItemClick(View view, int position);
    }
}
