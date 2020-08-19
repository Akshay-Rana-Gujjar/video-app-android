package in.akshayrana.videoupload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import in.akshayrana.videoupload.model.VideoModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Video selected";
    private ImageView chooseVideoImageView;
    private Uri fileUri = null;
    private ProgressBar uploadingProgress;
    private TextView progressPercentageTextView;
    private EditText videoTitleEditText;
    private Button UploadButton;
    static final String COLLECTION_NAME = "videos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseVideoImageView = findViewById(R.id.chooseVideoImageView);
        videoTitleEditText = findViewById(R.id.videoTitleEditText);
        progressPercentageTextView = findViewById(R.id.progressPercentageTextView);

        chooseVideoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VideoPicker.Builder(MainActivity.this)
                        .mode(VideoPicker.Mode.GALLERY)
                        .directory(VideoPicker.Directory.DEFAULT)
                        .enableDebuggingMode(true)
                        .build();
            }
        });


        UploadButton = findViewById(R.id.UploadButton);

        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fileUri == null) {
                    Toast.makeText(MainActivity.this, "File is not valid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                enableInputs(false);

                String fileName = fileUri.getLastPathSegment();;



                String videoTitle = videoTitleEditText.getText().toString();

                if(videoTitle.isEmpty()){
                    videoTitle = fileName;
                }

                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageRef = storage.getReference();

                final StorageReference videoRef = storageRef.child("videos/"+fileName);

                try {
                    InputStream stream = new FileInputStream(fileUri.getPath());
                    final int totalByte = stream.available();

                final UploadTask uploadTask = videoRef.putStream(stream);


                uploadingProgress = findViewById(R.id.uploadingProgress);
                uploadingProgress.setVisibility(View.VISIBLE);

                final String finalVideoTitle = videoTitle;
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        Log.e(TAG, "onFailure: "+exception.getMessage() );

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String uploadedImageUrl = uri.toString();
                                Log.d(TAG, "onSuccess "+uploadedImageUrl);
                                VideoModel videoModel = new VideoModel();
                                videoModel.setVideoSrc(uploadedImageUrl);
                                videoModel.setVideoTitle(finalVideoTitle);
                                AddVideoDataToFireStore(videoModel);
                            }
                        });

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress =  (100.0 * taskSnapshot.getBytesTransferred()) / totalByte;
                        uploadingProgress.setProgress((int) progress);
                        String progressText = ((int)progress)+"% done";
                        progressPercentageTextView.setText(progressText);
                        Log.d(TAG, "onProgress: taskSnapshot.getBytesTransferred() = "+taskSnapshot.getBytesTransferred());
                        Log.d(TAG, "onProgress: totalByte = "+totalByte);
                    }
                });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button viewVideosButton = findViewById(R.id.viewVideosButton);
        viewVideosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoList.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
            Log.d(TAG, "onActivityResult: "+mPaths.get(0));

            String videoPath = mPaths.get(0);
            fileUri = Uri.parse(videoPath);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(MainActivity.this, fileUri);

            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            chooseVideoImageView.setImageBitmap(retriever.getFrameAtTime(Math.round(duration*0.4), MediaMetadataRetriever.OPTION_CLOSEST));
            UploadButton.setVisibility(View.VISIBLE);
        }
    }

    void AddVideoDataToFireStore(VideoModel videoModel){



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION_NAME)
                .add(videoModel)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        uploadingProgress.setVisibility(View.GONE);
                        progressPercentageTextView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Successful Uploaded!", Toast.LENGTH_SHORT).show();
                        fileUri = null;
                        chooseVideoImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.video_placeholder, null));
                        enableInputs(true);
                        videoTitleEditText.setText(null);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to Upload.", Toast.LENGTH_SHORT).show();
                        enableInputs(true);
                    }
                });

    }

    void enableInputs(boolean doEnable){
        chooseVideoImageView.setClickable(doEnable);
        videoTitleEditText.setEnabled(doEnable);
        UploadButton.setClickable(doEnable);
    }
}

//