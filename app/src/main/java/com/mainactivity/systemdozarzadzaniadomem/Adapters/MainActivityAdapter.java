package com.mainactivity.systemdozarzadzaniadomem.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mainactivity.systemdozarzadzaniadomem.Activities.MainActivity;
import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.util.ArrayList;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private ArrayList<ServerDevice> data;
    private LayoutInflater mInflater;
    private ItemClickListener itemClickListener;

    public MainActivityAdapter(ArrayList<ServerDevice> data, Context context) {
        this.data = data;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_main_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String deviceName = data.get(position).getDeviceName();
        holder.tvServerDevice.setText(deviceName);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    /**
     * przechowuje i przetwarza widoki przewijane poza ekran
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvServerDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServerDevice = itemView.findViewById(R.id.tvServerDeviceName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setOnCLickListener(ItemClickListener item) {
        this.itemClickListener = item;
    }

    public String getItem(int id) {
        return data.get(id).getDeviceName();
    }

    public interface ItemClickListener {
        void onItemClick(View view, int positon);
    }

}
