package in.akshayrana.videoupload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import in.akshayrana.videoupload.adapter.VideoAdapter;
import in.akshayrana.videoupload.model.VideoModel;

import static in.akshayrana.videoupload.MainActivity.COLLECTION_NAME;

public class VideoList extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<VideoModel> videoModelList = new ArrayList<>();
    ShimmerFrameLayout facebookShimmerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        facebookShimmerLayout=findViewById(R.id.facebookShimmerLayout);
        facebookShimmerLayout.startShimmer();


        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final VideoAdapter adapter = new VideoAdapter(this, videoModelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

//        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    VideoModel videoModel = doc.toObject(VideoModel.class);
                    videoModelList.add(videoModel);
                }
                facebookShimmerLayout.stopShimmer();
                facebookShimmerLayout.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                recyclerView.suppressLayout(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        recyclerView.suppressLayout(false);
                    }
                }, 2000);

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}