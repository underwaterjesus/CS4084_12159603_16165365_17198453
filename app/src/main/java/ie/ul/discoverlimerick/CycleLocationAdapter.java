package ie.ul.discoverlimerick;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CycleLocationAdapter extends RecyclerView.Adapter implements Filterable, Serializable {
    private ArrayList<MyLocation> mDataset;
    private ArrayList<MyLocation> mDatasetAlwaysFull;
    private CycleLocationAdapter.OnItemClickListner mListener;

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListner listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv1;
        public TextView tv2;

        public MyViewHolder(View v, final OnItemClickListner listner) {
            super(v);
            tv1 = v.findViewById(R.id.locationTxtView);
            tv2 = v.findViewById(R.id.locationTxtView2);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listner != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listner.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }

    public CycleLocationAdapter(ArrayList<MyLocation> data) {
        mDataset = data;
        mDatasetAlwaysFull = new ArrayList<>(data);
        Log.d("AdapterConstructor", Integer.toString(mDataset.size()) + "|" + Integer.toString(mDatasetAlwaysFull.size()));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_location_item, parent, false);

        return new MyViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder) holder).tv1.setText(mDataset.get(position).getName());
        String distanceString = "";

        if (MainActivity.getLastLocation() != null) {
            double d = mDataset.get(position).getDistance(MainActivity.getLastLocation());
            distanceString = String.format("%.1f%n", d) + "km away";
        }

        ((MyViewHolder) holder).tv2.setText(distanceString);
       Log.d("onBind", "******" + Integer.toString(mDataset.size()) + "|" + Integer.toString(mDatasetAlwaysFull.size()));
    }

    @Override
    public int getItemCount() {
        return mDataset != null ? mDataset.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<MyLocation> filteredList = new ArrayList<MyLocation>();
            Log.d("performFiltering()", Arrays.toString(mDatasetAlwaysFull.toArray()));
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mDatasetAlwaysFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();Log.d("performFiltering()1", filterPattern);

                for (MyLocation location : mDatasetAlwaysFull) {Log.d("foreach", location.getName().toLowerCase().trim());
                    if (location.getName().toLowerCase().trim().contains(filterPattern)) {
                        filteredList.add(location);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mDataset.clear();
            mDataset.addAll((ArrayList<MyLocation>)results.values);
            Log.d("CycleLocationAdapter", Arrays.toString(mDatasetAlwaysFull.toArray()));

            notifyDataSetChanged();
        }
    };
}
