package in.akshayrana.videoupload;

import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;

import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import static in.akshayrana.videoupload.constants.constants.UPLOADING_SERVER_URL;

public class NodeJsFileUploadActivity extends AppCompatActivity {

    private ImageView chooseVideoImageView;
    private Uri fileUri = null;
    private ProgressBar uploadingProgress;
    private TextView progressPercentageTextView;
    private EditText videoTitleEditText;
    private Button UploadButton;
    private TextView chooseVideoLabel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseVideoImageView = findViewById(R.id.chooseVideoImageView);
        videoTitleEditText = findViewById(R.id.videoTitleEditText);
        progressPercentageTextView = findViewById(R.id.progressPercentageTextView);
        chooseVideoLabel = findViewById(R.id.chooseVideoLabel);

        chooseVideoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VideoPicker.Builder(NodeJsFileUploadActivity.this)
                        .mode(VideoPicker.Mode.GALLERY)
                        .directory(VideoPicker.Directory.DEFAULT)
                        .enableDebuggingMode(true)
                        .build();
            }
        });

        AndroidNetworking.initialize(this);


        UploadButton = findViewById(R.id.UploadButton);

        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fileUri == null) {
                    Toast.makeText(NodeJsFileUploadActivity.this, "File is not valid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                enableInputs(false);

                String fileName = fileUri.getLastPathSegment();

                String videoTitle = videoTitleEditText.getText().toString();

                if (videoTitle.isEmpty()) {
                    videoTitle = fileName;
                }
                uploadingProgress = findViewById(R.id.uploadingProgress);
                uploadingProgress.setVisibility(View.VISIBLE);
                progressPercentageTextView.setVisibility(View.VISIBLE);

                File videoFile = new File(fileUri.getPath());

                AndroidNetworking.upload(UPLOADING_SERVER_URL)
                        .addMultipartFile("video", videoFile)
                        .addMultipartParameter("videoTitle", videoTitle)
                        .setPriority(Priority.HIGH)
                        .build()
                        .setUploadProgressListener(new UploadProgressListener() {
                            @Override
                            public void onProgress(long bytesUploaded, long totalBytes) {
                                double progress =  (100.0 * bytesUploaded) / totalBytes;
                                uploadingProgress.setProgress((int) progress);
                                String progressText = ((int)progress)+"% done";
                                progressPercentageTextView.setText(progressText);
                            }
                        })
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int status = response.getInt("status");

                                    if(status == 200){

                                        String uploadedImageUrl = response.getString("videoUrl");
                                        uploadingProgress.setVisibility(View.GONE);
                                        progressPercentageTextView.setVisibility(View.GONE);
                                        chooseVideoLabel.setVisibility(View.VISIBLE);
                                        chooseVideoImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.video_placeholder, null));
                                        videoTitleEditText.setText(null);
                                        enableInputs(true);
                                        fileUri = null;
                                        Toast.makeText(NodeJsFileUploadActivity.this, "Successful Uploaded!", Toast.LENGTH_SHORT).show();


                                    }else{
                                        Toast.makeText(NodeJsFileUploadActivity.this, "Error while uploading video.", Toast.LENGTH_SHORT).show();
                                        // send report to the IT support through api.
                                        Log.e("Error 500", "onResponse: "+response.getString("error") );
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }finally {
                                    enableInputs(true);
                                }


                            }
                            @Override
                            public void onError(ANError error) {

                                Toast.makeText(NodeJsFileUploadActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                                enableInputs(true);
                                Log.e("Error Error", error.getMessage());
                            }
                        });
            }
        });

        Button viewVideosButton = findViewById(R.id.viewVideosButton);
        viewVideosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NodeJsFileUploadActivity.this, VideoList.class));
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);


            String videoPath = mPaths.get(0);
            fileUri = Uri.parse(videoPath);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(NodeJsFileUploadActivity.this, fileUri);

            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            chooseVideoImageView.setImageBitmap(retriever.getFrameAtTime(Math.round(duration*0.4), MediaMetadataRetriever.OPTION_CLOSEST));
            UploadButton.setVisibility(View.VISIBLE);

            chooseVideoLabel.setVisibility(View.GONE);
        }
    }

    void enableInputs(boolean doEnable) {
        chooseVideoImageView.setClickable(doEnable);
        videoTitleEditText.setEnabled(doEnable);
        UploadButton.setClickable(doEnable);
    }
}
