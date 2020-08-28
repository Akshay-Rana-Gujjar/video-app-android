package in.akshayrana.videoupload.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.List;

import in.akshayrana.videoupload.R;
import in.akshayrana.videoupload.model.VideoModel;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    Context context;
    List<VideoModel> videoModelList;
    List<SimpleExoPlayer> simpleExoPlayerList = new ArrayList<>();

    public VideoAdapter(Context context, List<VideoModel> videoModelList) {
        this.context = context;
        this.videoModelList = videoModelList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoViewHolder holder, int position) {

        VideoModel videoModel = videoModelList.get(position);
        final SimpleExoPlayer player;
        if(simpleExoPlayerList.size() > position && simpleExoPlayerList.get(position) != null){
            player = simpleExoPlayerList.get(position);
        }else{
            player = new SimpleExoPlayer.Builder(context).build();
            MediaSource mediaSource = buildMediaSource(videoModel.getVideoSrc());
            player.prepare(mediaSource, false, false);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            holder.videoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            holder.videoPlayerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (player.isPlaying()) {
                        player.setPlayWhenReady(false);
                        holder.videoPlayerView.showController();
                    }
                }
            });
            simpleExoPlayerList.add(player);
        }

        holder.videoPlayerView.setPlayer(player);
        holder.videoTitleTextView.setText(videoModel.getVideoTitle());
    }

    @Override
    public int getItemCount() {
        return videoModelList.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder{

        PlayerView videoPlayerView;
        TextView videoTitleTextView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoPlayerView = itemView.findViewById(R.id.videoPlayerView);
            videoTitleTextView = itemView.findViewById(R.id.videoTitleTextView);
        }
    }

    private MediaSource buildMediaSource(String url) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(context, "android-app");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url));
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {

        releaseAllPlayer();

        super.onDetachedFromRecyclerView(recyclerView);
    }

    public void releaseAllPlayer(){
        for (SimpleExoPlayer player :
                simpleExoPlayerList) {
            if (player != null) {
                player.stop();
                player.release();
            }
        }

    }
}
