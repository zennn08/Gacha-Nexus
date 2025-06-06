package com.example.gachanexus;

import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<Category> categoryList;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private LinearLayout emptyView;
    private String currentSearchQuery = "";
    private int currentCategoryId = 0;

    private static final String BASE_URL = "https://gachanexus.web.id/wp-json/wp/v2/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupRecyclerView();

        requestQueue = Volley.newRequestQueue(this);

        loadCategories();
        loadPosts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("GachaNexus");
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setupRecyclerView() {
        postList = new ArrayList<>();
        categoryList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                loadPosts();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    currentSearchQuery = "";
                    loadPosts();
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu) {
            showCategoryMenu(findViewById(R.id.action_menu));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCategoryMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);

        // Add "All Categories" option
        popupMenu.getMenu().add(0, 0, 0, "Semua Kategori");

        // Add categories
        for (int i = 0; i < categoryList.size(); i++) {
            Category category = categoryList.get(i);
            String menuTitle = category.getName() + " (" + category.getCount() + ")";
            popupMenu.getMenu().add(0, category.getId(), i + 1, menuTitle);
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            currentCategoryId = menuItem.getItemId();
            loadPosts();
            return true;
        });

        popupMenu.show();
    }

    private void loadCategories() {
        String url = BASE_URL + "categories?per_page=20&hide_empty=true";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        categoryList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject categoryObj = response.getJSONObject(i);

                            Category category = new Category();
                            category.setId(categoryObj.getInt("id"));
                            category.setName(Html.fromHtml(categoryObj.getString("name"), Html.FROM_HTML_MODE_LEGACY).toString());
                            category.setSlug(categoryObj.getString("slug"));
                            category.setCount(categoryObj.getInt("count"));

                            categoryList.add(category);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Categories loading failed, but continue with posts
                });

        requestQueue.add(request);
    }

    private void loadPosts() {
        showLoading(true);

        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "posts?per_page=20&_embed");

        if (currentCategoryId > 0) {
            urlBuilder.append("&categories=").append(currentCategoryId);
        }

        if (!currentSearchQuery.isEmpty()) {
            urlBuilder.append("&search=").append(currentSearchQuery);
        }

        String url = urlBuilder.toString();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        postList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObj = response.getJSONObject(i);

                            Post post = new Post();
                            post.setId(postObj.getInt("id"));
                            post.setTitle(postObj.getJSONObject("title").getString("rendered"));
                            post.setContent(postObj.getJSONObject("content").getString("rendered"));
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
                                    // No featured image or different structure
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
                            } else {
                                post.setCategoryName("Uncategorized");
                            }

                            postList.add(post);
                        }

                        postAdapter.updatePosts(postList);
                        showEmptyView(postList.isEmpty());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                },
                error -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                    showEmptyView(true);
                });

        requestQueue.add(request);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}