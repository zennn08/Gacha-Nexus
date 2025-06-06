package com.example.gachanexus;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RelatedPostAdapter extends RecyclerView.Adapter<RelatedPostAdapter.RelatedViewHolder> {
    private Context context;
    private List<Post> postList;

    public RelatedPostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_post, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        Post post = postList.get(position);

        // Set title
        holder.tvTitle.setText(Html.fromHtml(post.getTitle(), Html.FROM_HTML_MODE_LEGACY));

        // Load featured image
        if (post.getFeaturedImage() != null && !post.getFeaturedImage().isEmpty()) {
            Glide.with(context)
                    .load(post.getFeaturedImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivFeatured);
        } else {
            holder.ivFeatured.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("post_id", post.getId());
            intent.putExtra("post_title", post.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updatePosts(List<Post> newPosts) {
        this.postList = newPosts;
        notifyDataSetChanged();
    }

    static class RelatedViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivFeatured;
        TextView tvTitle;

        public RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivFeatured = itemView.findViewById(R.id.ivFeatured);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}