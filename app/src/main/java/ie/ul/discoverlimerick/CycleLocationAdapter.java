package ie.ul.discoverlimerick;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CycleLocationAdapter extends RecyclerView.Adapter {
    private ArrayList<MyLocation> mDataset;
    private CycleLocationAdapter.OnItemClickListner mListener;

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListner listener){
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
                    if(listner != null){
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            listner.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }

    public CycleLocationAdapter(ArrayList<MyLocation> data) {
        mDataset = data;
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
        ((MyViewHolder)holder).tv1.setText(mDataset.get(position).getName());
        String distanceString = "";

        if(MainActivity.getLastLocation() != null)
        {
            double d = mDataset.get(position).getDistance(MainActivity.getLastLocation());
            distanceString = String.format("%.1f%n", d) + "km away";
        }

        ((MyViewHolder)holder).tv2.setText(distanceString);
    }

    @Override
    public int getItemCount() {
        return mDataset != null ? mDataset.size() : 0;
    }
}
