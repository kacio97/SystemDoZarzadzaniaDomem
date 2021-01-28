package com.mainactivity.systemdozarzadzaniadomem.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.util.ArrayList;

/**
 * Klasa ta odpowiada za prawidłowe wyświetlanie oraz zacządzanie revycler view dla klasy MainActivity
 */
public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private final ArrayList<ServerDevice> data;
    private final LayoutInflater mInflater;
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
     * Metoda która odpowiada za usunięcie elementu z listy recycler view
     * @param position pozycja wybranego elementu
     */
    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Metoda odpowiada za przywrócenie skasowanego elementu z listy
     * @param item Obiekt typu ServerDevice
     * @param position pozycja wybranego elementu
     */
    public void restoreItem(ServerDevice item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
    }

    /**
     * Przechowuje i przetwarza widoki przewijane poza ekran
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView tvServerDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServerDevice = itemView.findViewById(R.id.tvServerDeviceName);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, getAdapterPosition());
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

    public void setOnCLickListener(ItemClickListener item) {
        this.itemClickListener = item;
    }

    public String getItemName(int id) {
        return data.get(id).getDeviceName();
    }

    public ServerDevice getItem(int id) {
        return data.get(id);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int positon);

        void onLongItemClick(View view, int position);
    }

}
