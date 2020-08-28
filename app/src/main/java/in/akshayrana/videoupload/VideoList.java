package in.akshayrana.videoupload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.akshayrana.videoupload.adapter.VideoAdapter;
import in.akshayrana.videoupload.model.VideoModel;

import static in.akshayrana.videoupload.MainActivity.COLLECTION_NAME;
import static in.akshayrana.videoupload.constants.constants.SERVER_IP_ADDRESS;
import static in.akshayrana.videoupload.constants.constants.VIDEO_LIST_URL;

public class VideoList extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<VideoModel> videoModelList = new ArrayList<>();
    ShimmerFrameLayout facebookShimmerLayout;
    VideoAdapter adapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        facebookShimmerLayout=findViewById(R.id.facebookShimmerLayout);
        facebookShimmerLayout.startShimmer();


        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new VideoAdapter(this, videoModelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



        AndroidNetworking.get(VIDEO_LIST_URL)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if(response.getInt("status") == 200){

                                JSONArray videoList = response.getJSONArray("videoList");

                                for (int i = 0; i < videoList.length(); i++) {

                                    JSONObject videoObject = (JSONObject) videoList.get(i);
                                    String videoTitle = videoObject.getString("videoTitle");
                                    String videoSrc = videoObject.getString("videoSrc");

                                    VideoModel videoModel = new VideoModel();
                                    videoModel.setVideoTitle(videoTitle);
                                    videoModel.setVideoSrc(SERVER_IP_ADDRESS+videoSrc);
                                    videoModelList.add(videoModel);

                                }

                                facebookShimmerLayout.stopShimmer();
                                facebookShimmerLayout.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                                recyclerView.suppressLayout(true);

                            }else{
                                Toast.makeText(VideoList.this, "Error while getting videos.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(VideoList.this, "Error while getting videos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        if(adapter != null){
            adapter.releaseAllPlayer();
        }
        super.onPause();
    }
}