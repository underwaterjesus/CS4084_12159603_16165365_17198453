package ie.ul.discoverlimerick;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CycleAdapter extends RecyclerView.Adapter {
    private CategoryItem[] mDataset;
    private OnItemClickListner mListener;

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListner listener){
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public MyViewHolder(View v, final OnItemClickListner listner) {
            super(v);
            imageView = v.findViewById(R.id.imgView);
            textView = v.findViewById(R.id.txtView);

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

    public CycleAdapter(CategoryItem[] data) {
        mDataset = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);

        return new MyViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CategoryItem categoryItem = mDataset[position];

        ((MyViewHolder)holder).imageView.setImageResource(categoryItem.getImageresource());
        ((MyViewHolder)holder).textView.setText(categoryItem.getCategoryName());
    }

    @Override
    public int getItemCount() {
        return mDataset != null ? mDataset.length : 0;
    }
}