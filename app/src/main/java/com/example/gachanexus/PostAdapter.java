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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Set title
        holder.tvTitle.setText(Html.fromHtml(post.getTitle(), Html.FROM_HTML_MODE_LEGACY));

        // Set excerpt
        String excerpt = post.getExcerpt();
        if (excerpt != null && !excerpt.isEmpty()) {
            holder.tvExcerpt.setText(Html.fromHtml(excerpt, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvExcerpt.setVisibility(View.GONE);
        }

        // Set category
        holder.tvCategory.setText(post.getCategoryName());

        // Set date
        holder.tvDate.setText(formatDate(post.getDate()));

        // Load featured image
        if (post.getFeaturedImage() != null && !post.getFeaturedImage().isEmpty()) {
            Glide.with(context)
                    .load(post.getFeaturedImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivFeatured);
            holder.ivFeatured.setVisibility(View.VISIBLE);
        } else {
            holder.ivFeatured.setVisibility(View.GONE);
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

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public void updatePosts(List<Post> newPosts) {
        this.postList = newPosts;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivFeatured;
        TextView tvTitle;
        TextView tvExcerpt;
        TextView tvCategory;
        TextView tvDate;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivFeatured = itemView.findViewById(R.id.ivFeatured);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvExcerpt = itemView.findViewById(R.id.tvExcerpt);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}