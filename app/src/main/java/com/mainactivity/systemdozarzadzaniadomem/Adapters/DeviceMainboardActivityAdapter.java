package com.mainactivity.systemdozarzadzaniadomem.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mainactivity.systemdozarzadzaniadomem.Models.TopicModel;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.util.HashMap;
import java.util.Map;

public class DeviceMainboardActivityAdapter extends RecyclerView.Adapter<DeviceMainboardActivityAdapter.ViewHolder> {



//    private HashMap<String, String> values;
    private final LayoutInflater inflater;
    private ItemClickListener itemClickListener;
    private final HashMap<String, TopicModel> topics;


    public DeviceMainboardActivityAdapter(Context context,HashMap<String, TopicModel> topics) {
        this.inflater = LayoutInflater.from(context);
        this.topics = topics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_devicemainboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopicModel topic = topics.get((topics.keySet().toArray())[position]);
//        String topicValue = getHashMapKeyFromIndex(topicsValue, position);
        holder.tvTopic.setText(topic.getTopicName());
        holder.tvTopicInfo.setText(topic.getValue());
    }

    public static String getHashMapKeyFromIndex(HashMap<String, String> hashMap, int index){
        String key = null;
        int pos=0;
        for(Map.Entry<String, String> entry : hashMap.entrySet())
        {
            if(index==pos){
                key=entry.getKey();
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvTopicName);
            tvTopicInfo = itemView.findViewById(R.id.tvTopicInfo);
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

    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onLongItemClick(View view, int position);
    }
}
