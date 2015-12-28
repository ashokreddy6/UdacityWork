package android.example.movies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.example.moviesdata.MoviesProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieDetailsActivity extends AppCompatActivity {
    public final static String TAG = MovieDetailsActivity.class.getSimpleName();


    ImageView moviePoster,starIcon;
    TextView title, overview, rating, releaseDate, movieID, movieImage;
    Button favorateButton;
    WebView webView;
    List<MoviesTrailorModel> trailorModelList = new ArrayList<MoviesTrailorModel>();
    ListView trailorListView;
    MovieRatingModel reviewModel=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        movieID = (TextView) findViewById(R.id.movie_id);
        favorateButton = (Button) findViewById(R.id.fav_button);
        movieImage = (TextView) findViewById(R.id.movie_image_hidden);
        starIcon=(ImageView)findViewById(R.id.star_icon);
        moviePoster = (ImageView) findViewById(R.id.movie_big_image);
        title = (TextView) findViewById(R.id.movie_title);
        overview = (TextView) findViewById(R.id.movie_overview);
        rating = (TextView) findViewById(R.id.movie_rating);
        releaseDate = (TextView) findViewById(R.id.movie_releasedate);
        trailorListView = (ListView) findViewById(R.id.trailors_list);
        Intent intent = getIntent();
        if (intent != null) {
            MoviesModel movie = intent.getParcelableExtra("movie");
            trailorRequest(MovieConstants.BASE_URL + movie.getId() + MovieConstants.VEDIO_KEY);
            Picasso.with(this).load( MovieConstants.IMAGE_URL+ movie.getMovieImage()).into(moviePoster);
            movieID.setText(movie.getId());
            movieImage.setText(movie.getMovieImage());
            title.setText(movie.getTitle());
            overview.setText(movie.getOverView());
            rating.setText(movie.getRating());
            releaseDate.setText(movie.getReleaseDate());
                     if (isFavorate(movie.getId())) {
                favorateButton.setVisibility(View.GONE);
                starIcon.setVisibility(View.VISIBLE);
            }


        }

    }

    private boolean isFavorate(String movieID) {
        String URL = "content://android.example.movies/movies";
        Uri movies = Uri.parse(URL + "/" + movieID);
        Cursor cursor = managedQuery(movies, null, null, null, MoviesProvider.TITLE);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    private void trailorRequest(String url) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        JSONArray jsonArray;
                        trailorModelList.clear();
                        try {
                            if (isOnline(getBaseContext())) {
                                jsonArray = response.getJSONArray("results");
                                MoviesTrailorModel iModel = null;
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    iModel = new MoviesTrailorModel();
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    iModel.setTrailorId(jsonObject.getString("id"));
                                    iModel.setKey(jsonObject.getString("key"));
                                    iModel.setTrailorName(jsonObject.getString("name"));

                                    trailorModelList.add(iModel);

                                }
                                trailorListView.setAdapter(new TrailorListAdapter(MovieDetailsActivity.this, trailorModelList));
                            } else {
                                trailorModelList.add(new MoviesTrailorModel());
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }


        };

        // Adding request to request queue
        MoviesController.getInstance().addToRequestQueue(jsonObjReq,
                "results");

    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playVedio(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(MovieConstants.YOUTUBE_URL+ view.getContentDescription()));
        startActivity(intent);
    }

    public void markAsFavorate(View view) {
        ContentValues values = new ContentValues();
        values.put(MoviesProvider._ID, (((TextView) findViewById(R.id.movie_id)).getText()).toString());
        values.put(MoviesProvider.POSTER,   (((TextView) findViewById(R.id.movie_image_hidden)).getText()).toString());
        values.put(MoviesProvider.TITLE, (((TextView) findViewById(R.id.movie_title)).getText()).toString());
        values.put(MoviesProvider.OVERVIEW, (((TextView) findViewById(R.id.movie_overview)).getText()).toString());
        values.put(MoviesProvider.RATING, (((TextView) findViewById(R.id.movie_rating)).getText()).toString());
        values.put(MoviesProvider.RELEASE_DATE, (((TextView) findViewById(R.id.movie_releasedate)).getText()).toString());
        Uri uri = getContentResolver().insert(MoviesProvider.CONTENT_URI, values);
        if(uri!=null) {
            Toast.makeText(this,
                    "Movie marked as favorate",
                    Toast.LENGTH_SHORT).show();
            favorateButton.setVisibility(View.GONE);
            starIcon.setVisibility(View.VISIBLE);
        }

    }
public void getReview(View view){
    String movieId=(((TextView) findViewById(R.id.movie_id)).getText()).toString();
    String movieTitle= (((TextView) findViewById(R.id.movie_title)).getText()).toString();
    Intent intent=new Intent(this,MovieRatingActivity.class);
    System.out.println("movieTitle::"+movieTitle);
    intent.putExtra("movieID", movieId);
    intent.putExtra("movieTitle",movieTitle);
    startActivity(intent);
}

}
