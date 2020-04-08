package ie.ul.discoverlimerick;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

        mImageView = (ImageView) view.findViewById(R.id.upload_image);
        chooseFile = (Button) view.findViewById(R.id.choose_file);
        uploadFile = (Button) view.findViewById(R.id.upload_file);
        mProgressBar = (ProgressBar) view.findViewById(R.id.prog_bar);

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask != null && mUploadTask.isInProgress()) {
                    MainActivity.showToast(getContext(), "Upload in progress");
                }else {
                    uploadChosenFile();
                }

            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference("user_img");
        db = FirebaseFirestore.getInstance();

        return view;
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadChosenFile() {
        if(mImageUri != null){
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
                    double progress = 100.0 * ( ((double) taskSnapshot.getBytesTransferred()) / ((double) taskSnapshot.getTotalByteCount()) );
                    mProgressBar.setProgress((int) progress);
                }
            });
        }else {
            MainActivity.showToast(getContext(), "No file selected");
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

        if(requestCode == SELECT_FILE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){
            mImageUri = data.getData();
            mImageView.setImageURI(mImageUri);
        }
    }
}
