package ie.ul.discoverlimerick;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {
    private final static int SELECT_FILE_REQUEST = 1;

    private ImageView mImageView;
    private Button chooseFile;
    private Button uploadFile;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageReference;
    private FirebaseFirestore db;
    private StorageTask mUploadTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        if(!isConnected())
            startActivity(new Intent(getContext(), ConnectionActivity.class));

        mImageView = (ImageView) view.findViewById(R.id.upload_image);
        chooseFile = (Button) view.findViewById(R.id.choose_file);
        uploadFile = (Button) view.findViewById(R.id.upload_file);
        mProgressBar = (ProgressBar) view.findViewById(R.id.prog_bar);

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    MainActivity.showToast(getContext(), "Upload in progress");
                } else {
                    openFileChooser();
                }
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    MainActivity.showToast(getContext(), "Upload in progress");
                } else {
                    uploadChosenFile();
                }

            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference("user_img");
        db = FirebaseFirestore.getInstance();

        return view;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadChosenFile() {
        if (MainActivity.mAuth.getCurrentUser() != null) {
            if (mImageUri != null) {
                int r = (int) (Math.random() * 100);
                final Timestamp stamp = Timestamp.now();
                final String fileName = Integer.toString(r) + Long.toString(System.currentTimeMillis()) + "." + getFileExtension(mImageUri);
                StorageReference fileReference = mStorageReference.child(fileName);

                mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(0);
                            }
                        }, 1000);

                        Map<String, Object> upload = new HashMap<>();
                        upload.put("time", stamp);
                        upload.put("username", MainActivity.mAuth.getCurrentUser().getDisplayName());
                        upload.put("userID", MainActivity.mAuth.getCurrentUser().getUid());
                        upload.put("fileName", fileName);

                        db.collection(MainActivity.selected_category).document(MainActivity.selected_location).collection("Images").add(upload)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        MainActivity.showToast(getContext(), "Upload Successful");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                MainActivity.showToast(getContext(), "Error: Upload Unsuccessful");
                            }
                        });

                        MainActivity.showToast(getContext(), "Upload Successful");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MainActivity.showToast(getContext(), e.getMessage());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = 100.0 * (((double) taskSnapshot.getBytesTransferred()) / ((double) taskSnapshot.getTotalByteCount()));
                        mProgressBar.setProgress((int) progress);
                    }
                });
            } else {
                MainActivity.showToast(getContext(), "No file selected");
            }
        } else {
            MainActivity.showToast(getContext(), "Please login to upload an image");
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, SELECT_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_FILE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mImageView.setImageURI(mImageUri);
        }
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;

        try {

            for (Network network : connMgr.getAllNetworks()) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(network);

                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn |= networkInfo.isConnected();
                }

                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    isMobileConn |= networkInfo.isConnected();
                }

                return isWifiConn || isMobileConn;

            }
        } catch (Exception e) {

            String s = e.getMessage() == null ? "unable to give more details" : e.getMessage();
            Log.d("isConnected", s);
        }

        return false;
    }

    @Override
    public void onDestroy() {
        if(mUploadTask != null && !mUploadTask.isComplete()){
            mUploadTask.cancel();
            MainActivity.showToast(getContext(), "Pressing Back has cancelled upload.");
        }
        super.onDestroy();
    }
}
