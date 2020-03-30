package ie.ul.discoverlimerick;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter {
    private ArrayList<Review> mDataset;
    private ReviewAdapter.OnItemClickListner mListener;

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListner listener) {
        mListener = listener;
    }

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        public TextView tv1;
        public TextView tv2;
        public TextView tv3;
        public TextView tv4;

        public ReviewHolder(View v, final OnItemClickListner listner) {
            super(v);
            tv1 = v.findViewById(R.id.review_tv1);
            tv2 = v.findViewById(R.id.review_tv2);
            tv3 = v.findViewById(R.id.review_tv3);
            tv4 = v.findViewById(R.id.review_tv4);

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

    public ReviewAdapter(ArrayList<Review> data){
        mDataset = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);

        return new ReviewHolder(v, mListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ReviewHolder)holder).tv1.setText(Review.getLocation().getName());
        ((ReviewHolder)holder).tv2.setText(mDataset.get(position).getReview());

        SimpleDateFormat form = new SimpleDateFormat("d MMM yyyy");
        String date = form.format(mDataset.get(position).getStamp().toDate());

        ((ReviewHolder)holder).tv4.setText( (mDataset.get(position).getUsername()) + " @ " + (date));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
