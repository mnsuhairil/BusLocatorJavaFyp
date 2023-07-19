package com.example.buslocatorsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeletedDriverAdapter extends RecyclerView.Adapter<DeletedDriverAdapter.ViewHolder> {

    private Context context;
    private List<Driver> deletedDriversList;

    public DeletedDriverAdapter(Context context, List<Driver> deletedDriversList) {
        this.context = context;
        this.deletedDriversList = deletedDriversList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deleted_driver, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Driver deletedDriver = deletedDriversList.get(position);
        holder.bind(deletedDriver);
    }

    @Override
    public int getItemCount() {
        return deletedDriversList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;
        private TextView textViewBusId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewBusId = itemView.findViewById(R.id.textViewBusId);
        }

        public void bind(Driver deletedDriver) {
            textViewName.setText(deletedDriver.getName());
            textViewBusId.setText(deletedDriver.getBusId());
        }
    }
}
