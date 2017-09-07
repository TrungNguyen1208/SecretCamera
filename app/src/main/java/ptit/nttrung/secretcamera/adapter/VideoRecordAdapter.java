package ptit.nttrung.secretcamera.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanks.library.AnimateCheckBox;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ptit.nttrung.secretcamera.R;
import ptit.nttrung.secretcamera.helper.TimeHelper;
import ptit.nttrung.secretcamera.model.VideoRecord;


/**
 * Created by TrungNguyen on 5/9/2017.
 */

public class VideoRecordAdapter extends RecyclerView.Adapter<VideoRecordAdapter.VideoHolder> {

    private Context mContext;
    private List<VideoRecord> mList;
    private LayoutInflater mLayoutInflater;

    private ItemClickCallBack itemClickCallBack;

    public interface ItemClickCallBack{
        void onImageClick(View view, int position);
        void onChkBoxClick(View view, int position, boolean isChecked);
    }

    public void setItemClickCallBack(final ItemClickCallBack itemClickCallBack) {
        this.itemClickCallBack = itemClickCallBack;
    }

    public VideoRecordAdapter(Context context, List<VideoRecord> list) {
        this.mContext = context;
        this.mList = list;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_video, parent, false);
        return new VideoHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {
        VideoRecord videoRecord = mList.get(position);

        holder.ivVideo.setImageBitmap(videoRecord.getThumbnails());
        holder.tvVideoName.setText(videoRecord.getFileName());
        long videoDuration = Long.parseLong(videoRecord.getTimeVideo());
        holder.tvVideoDuration.setText(TimeHelper.convertMilliSecondsToHMmSs(videoDuration));
        holder.tvVideoDate.setText(videoRecord.getDate());
        holder.checkBox.setChecked(videoRecord.isSelect());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class VideoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_item_video)
        ImageView ivVideo;
        @BindView(R.id.tv_video_name)
        TextView tvVideoName;
        @BindView(R.id.tv_video_duration)
        TextView tvVideoDuration;
        @BindView(R.id.tv_video_date)
        TextView tvVideoDate;
        @BindView(R.id.item_cb)
        AnimateCheckBox checkBox;

        public VideoHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            ivVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallBack.onImageClick(itemView,getAdapterPosition());
                }
            });

            checkBox.setOnCheckedChangeListener(new AnimateCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View buttonView, boolean isChecked) {
                    itemClickCallBack.onChkBoxClick(buttonView,getAdapterPosition(),isChecked);
                }
            });

        }

    }
}
