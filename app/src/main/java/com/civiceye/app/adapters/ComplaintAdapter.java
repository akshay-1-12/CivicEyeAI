package com.civiceye.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.civiceye.app.R;
import com.civiceye.app.activities.ComplaintDetailActivity;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.models.Complaint;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {
    private List<Complaint> complaints;
    private Context context;

    public ComplaintAdapter(List<Complaint> complaints, Context context) {
        this.complaints = complaints;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint complaint = complaints.get(position);
        holder.tvType.setText(complaint.getIssueType());
        holder.tvStatus.setText(complaint.getStatus());
        holder.tvLocation.setText(complaint.getAddress());
        
        String status = complaint.getStatus() != null ? complaint.getStatus().toLowerCase() : "";
        if (status.contains("pending")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatus.setTextColor(context.getColor(R.color.pendingText));
        } else if (status.contains("progress")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_inprogress);
            holder.tvStatus.setTextColor(context.getColor(R.color.primary));
        } else if (status.contains("resolved")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_resolved);
            holder.tvStatus.setTextColor(context.getColor(R.color.white));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_closed);
            holder.tvStatus.setTextColor(context.getColor(R.color.textMuted));
        }

        String imageUrl = complaint.getImageUrl();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = ApiClient.BASE_URL + (imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl);
        }
        
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.ivImage);

        // Click listener to open details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComplaintDetailActivity.class);
            intent.putExtra("complaint", complaint);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvStatus, tvLocation;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvIssueType);
            tvStatus = itemView.findViewById(R.id.tvIssueStatus);
            tvLocation = itemView.findViewById(R.id.tvIssueLocation);
            ivImage = itemView.findViewById(R.id.ivIssueImage);
        }
    }
}
