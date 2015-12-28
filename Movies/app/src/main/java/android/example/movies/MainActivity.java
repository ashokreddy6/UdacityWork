    package android.example.movies;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class MainActivity extends AppCompatActivity {
    public final static String TAG=MainActivity.class.getSimpleName();

    GridView gridView;
    EditText et1;
    JSONObject jsonobject;
    int offset=0;

    private String tag_json_objects="results";
    String finalUrl=null;
    public List<MoviesModel> moviesList=new ArrayList<MoviesModel>();

    GridViewAdapter adapter;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        gridView = (GridView) findViewById(R.id.moviesGrid);

// = new FetchImages();


        finalUrl=
                MovieConstants.GIPHY_URL+MovieConstants.GIPHY_API_KEY;
        makeJsonObjReq(finalUrl);
      //  imageFetchTask.execute(finalUrl);;
        adapter=new GridViewAdapter(MainActivity.this,moviesList);
       // EventBus.getDefault().register(this);
        gridView.setAdapter(adapter);

      //  ib1.setOnClickListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*Toast.makeText(getApplicationContext(),
                        ((TextView) view.findViewById(R.id.movie_rating))
                        .getText(), Toast.LENGTH_SHORT).show();*/

                MoviesModel mModel=moviesList.get(position);

                Intent intent=new Intent(getApplicationContext(),MovieDetailsActivity.class);
                intent.putExtra("movie", new MoviesModel(mModel.getId(), mModel.getTitle(),mModel.getRating(), mModel.getOverView(),""+MoviesUtil.getYearFromDate(mModel.getReleaseDate()), mModel.getMovieImage()));
                startActivity(intent);


            }
        });
    }


    public void onEventMainThread(List<MoviesModel> images) {
        moviesList=images;
        adapter=new GridViewAdapter(MainActivity.this,moviesList);
        gridView.setAdapter(adapter);
    }

        private void makeJsonObjReq(String url) {

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            JSONArray jsonArray;
                            moviesList.clear();
                            try {
                                if (isOnline(getBaseContext())) {
                                    jsonArray = response.getJSONArray("results");
                                    MoviesModel iModel = null;
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        iModel = new MoviesModel();
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                       iModel.setId(jsonObject.getString("id"));
                                        iModel.setMovieImage(jsonObject.getString("poster_path"));
                                        iModel.setOverView(jsonObject.getString("overview"));
                                        iModel.setTitle(jsonObject.getString("original_title"));
                                        iModel.setRating(jsonObject.getString("vote_average"));
                                        iModel.setReleaseDate(jsonObject.getString("release_date"));
                                        moviesList.add(iModel);

                                    }
                                }else{
                                    moviesList.add(new MoviesModel());
                                }
                                        adapter.notifyDataSetChanged();
                            }catch(JSONException e){
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
                    tag_json_objects);

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }else if(id==R.id.action_rated){
            String finalURL=
                    MovieConstants.GIPHY_URL+MovieConstants.GIPHY_API_KEY;
            makeJsonObjReq(finalUrl);
        }else if(id==R.id.action_popular){
            String finalURL=
                    MovieConstants.GIPHY_URL+MovieConstants.GIPHY_API_KEY;
            makeJsonObjReq(finalUrl);
        }else if(id==R.id.action_favorate){
            moviesList.clear();
            String URL = "content://android.example.movies/movies";

            Uri movies = Uri.parse(URL);
            Cursor cursor = managedQuery(movies, null, null, null, MoviesProvider.TITLE);
            while (cursor.moveToNext()) {
                MoviesModel moviesModel=new MoviesModel();
                moviesModel.setMovieImage(cursor.getString(cursor.getColumnIndex(MoviesProvider.POSTER)));
                moviesModel.setOverView(cursor.getString(cursor.getColumnIndex(MoviesProvider.OVERVIEW)));
                moviesModel.setTitle(cursor.getString(cursor.getColumnIndex(MoviesProvider.TITLE)));
                moviesModel.setId(cursor.getString(cursor.getColumnIndex(MoviesProvider._ID)));
                moviesModel.setRating(cursor.getString(cursor.getColumnIndex(MoviesProvider.RATING)));
                moviesModel.setReleaseDate(cursor.getString(cursor.getColumnIndex(MoviesProvider.RELEASE_DATE)));
                moviesList.add(moviesModel);
            }
            adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }
}
