package ie.ul.discoverlimerick;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class UploadAdapter extends RecyclerView.Adapter {
    private ArrayList<Upload> mDataset;
    private UploadAdapter.OnItemClickListner mListener;
    private Context context;
    private StorageReference mStorageReference;

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(UploadAdapter.OnItemClickListner listener) {
        mListener = listener;
    }

    public static class UploadHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView imageView;
        private TextView textView;

        public UploadHolder(View view, final OnItemClickListner listner) {
            super(view);

            cardView = (CardView) view.findViewById(R.id.card);
            imageView = (ImageView) view.findViewById(R.id.card_image);
            textView = (TextView) view.findViewById(R.id.card_desc);

            view.setOnClickListener(new View.OnClickListener() {
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

    public UploadAdapter(ArrayList<Upload> dataset, Context context) {
        mDataset = dataset;
        this.context = context;
        mStorageReference = FirebaseStorage.getInstance().getReference("user_img");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.upload_item, parent, false);

        return new UploadHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Upload upload = mDataset.get(position);

        StorageReference fileReference = mStorageReference.child(upload.getfileName());
        Glide.with(context).load(fileReference).centerCrop().into(((UploadHolder)holder).imageView);

        SimpleDateFormat form = new SimpleDateFormat("d MMM yyyy");
        String date = form.format(upload.getTimestamp().toDate());

        ((UploadHolder)holder).textView.setText("Left by: " + upload.getUsername() + " @ " + date);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
