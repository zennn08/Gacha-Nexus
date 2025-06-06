package com.example.gachanexus;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private ImageView ivFeatured;
    private TextView tvTitle;
    private TextView tvMeta;
    private TextView tvContent;
    private RecyclerView rvRelated;
    private ProgressBar progressBar;
    private LinearLayout relatedSection;

    private RequestQueue requestQueue;
    private RelatedPostAdapter relatedAdapter;
    private List<Post> relatedPosts;

    private int postId;
    private String postTitle;
    private int categoryId;

    private static final String BASE_URL = "https://gachanexus.web.id/wp-json/wp/v2/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();
        setupToolbar();
        setupRelatedPosts();

        requestQueue = Volley.newRequestQueue(this);

        // Get data from intent
        postId = getIntent().getIntExtra("post_id", 0);
        postTitle = getIntent().getStringExtra("post_title");

        if (postId > 0) {
            loadPostDetail();
        } else {
            Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        ivFeatured = findViewById(R.id.ivFeatured);
        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvContent = findViewById(R.id.tvContent);
        rvRelated = findViewById(R.id.rvRelated);
        progressBar = findViewById(R.id.progressBar);
        relatedSection = findViewById(R.id.relatedSection);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Detail Artikel");
        }
    }

    private void setupRelatedPosts() {
        relatedPosts = new ArrayList<>();
        relatedAdapter = new RelatedPostAdapter(this, relatedPosts);
        rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelated.setAdapter(relatedAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPostDetail() {
        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "posts/" + postId + "?_embed";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Set title
                        String title = response.getJSONObject("title").getString("rendered");
                        tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));

                        // Set meta info (date and category)
                        String date = formatDate(response.getString("date"));
                        String categoryName = "Uncategorized";

                        if (response.has("_embedded") &&
                                response.getJSONObject("_embedded").has("wp:term")) {
                            try {
                                JSONObject categoryObj = response.getJSONObject("_embedded")
                                        .getJSONArray("wp:term")
                                        .getJSONArray(0)
                                        .getJSONObject(0);
                                categoryName = categoryObj.getString("name");
                                categoryId = categoryObj.getInt("id");
                            } catch (JSONException e) {
                                // Use default category
                            }
                        }

                        tvMeta.setText(categoryName + " • " + date);

                        // Load featured image
                        if (response.has("_embedded") &&
                                response.getJSONObject("_embedded").has("wp:featuredmedia")) {
                            try {
                                String imageUrl = response.getJSONObject("_embedded")
                                        .getJSONArray("wp:featuredmedia")
                                        .getJSONObject(0)
                                        .getJSONObject("media_details")
                                        .getJSONObject("sizes")
                                        .getJSONObject("large")
                                        .getString("source_url");

                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.placeholder_image)
                                        .error(R.drawable.placeholder_image)
                                        .into(ivFeatured);

                                ivFeatured.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                ivFeatured.setVisibility(View.GONE);
                            }
                        } else {
                            ivFeatured.setVisibility(View.GONE);
                        }

                        // Set content
                        String content = response.getJSONObject("content").getString("rendered");
                        // Remove HTML tags and set to TextView
                        String cleanContent = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
                        tvContent.setText(cleanContent);

                        // Load related posts
                        if (categoryId > 0) {
                            loadRelatedPosts();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DetailActivity.this, "Error parsing post data", Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.GONE);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity.this, "Failed to load post", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void loadRelatedPosts() {
        String url = BASE_URL + "posts?categories=" + categoryId + "&exclude=" + postId + "&per_page=5&_embed";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        relatedPosts.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObj = response.getJSONObject(i);

                            Post post = new Post();
                            post.setId(postObj.getInt("id"));
                            post.setTitle(postObj.getJSONObject("title").getString("rendered"));
                            post.setExcerpt(postObj.getJSONObject("excerpt").getString("rendered"));
                            post.setDate(postObj.getString("date"));

                            // Get featured image
                            if (postObj.has("_embedded") &&
                                    postObj.getJSONObject("_embedded").has("wp:featuredmedia")) {
                                try {
                                    String imageUrl = postObj.getJSONObject("_embedded")
                                            .getJSONArray("wp:featuredmedia")
                                            .getJSONObject(0)
                                            .getJSONObject("media_details")
                                            .getJSONObject("sizes")
                                            .getJSONObject("medium")
                                            .getString("source_url");
                                    post.setFeaturedImage(imageUrl);
                                } catch (JSONException e) {
                                    // No featured image
                                }
                            }

                            // Get category
                            if (postObj.has("_embedded") &&
                                    postObj.getJSONObject("_embedded").has("wp:term")) {
                                try {
                                    String categoryName = postObj.getJSONObject("_embedded")
                                            .getJSONArray("wp:term")
                                            .getJSONArray(0)
                                            .getJSONObject(0)
                                            .getString("name");
                                    post.setCategoryName(categoryName);
                                } catch (JSONException e) {
                                    post.setCategoryName("Uncategorized");
                                }
                            }

                            relatedPosts.add(post);
                        }

                        if (!relatedPosts.isEmpty()) {
                            relatedAdapter.updatePosts(relatedPosts);
                            relatedSection.setVisibility(View.VISIBLE);
                        } else {
                            relatedSection.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        relatedSection.setVisibility(View.GONE);
                    }
                },
                error -> {
                    // Related posts failed to load, hide section
                    relatedSection.setVisibility(View.GONE);
                });

        requestQueue.add(request);
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
}