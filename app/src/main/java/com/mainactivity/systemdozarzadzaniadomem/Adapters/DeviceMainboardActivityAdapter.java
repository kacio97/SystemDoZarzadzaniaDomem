package com.mainactivity.systemdozarzadzaniadomem.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mainactivity.systemdozarzadzaniadomem.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceMainboardActivityAdapter extends RecyclerView.Adapter<DeviceMainboardActivityAdapter.ViewHolder> {

    private final ArrayList<String> topics;
    private final LayoutInflater inflater;
    private ItemClickListener itemClickListener;
    private final HashMap<String, String> topicsValue;


    public DeviceMainboardActivityAdapter(Context context, ArrayList<String> topics, @Nullable HashMap<String, String> topicsValue) {
        this.inflater = LayoutInflater.from(context);
        this.topics = topics;
        this.topicsValue = topicsValue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_devicemainboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String topic = topics.get(position);
        String topicValue = this.topicsValue.get(topic);
        holder.tvTopic.setText(topic);
        holder.tvTopicInfo.setText(topicValue);

    }

    @Override
    public int getItemCount() {
        return this.topics.size();
    }

    public void setOnClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
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
