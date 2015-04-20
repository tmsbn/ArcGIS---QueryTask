package ae.gisworx.querytask;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.osm.OpenStreetMapLayer;
import com.esri.core.geometry.Point;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements OnStatusChangedListener, LocationListener, OnSingleTapListener {


    MapView mMapView;

    OpenStreetMapLayer openStreetMapLayer;

    Button goBtn;

    EditText searchEt;

    GraphicsLayer graphicsLayer;

    ProgressDialog progressDialog;

    LinearLayout searchContainer;

    boolean gotLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //binding map to xml
        mMapView = (MapView) findViewById(R.id.map);

        //binding go button to variable
        goBtn = (Button) findViewById(R.id.go_button);

        //binding go button to variable
        searchEt = (EditText) findViewById(R.id.searchPlaces);

        //search container
        searchContainer = (LinearLayout) findViewById(R.id.searchContainer);

        //initialize the open street map as the basemap
        openStreetMapLayer = new OpenStreetMapLayer();

        //add open street map layer
        mMapView.addLayer(openStreetMapLayer);

        //add listener to check the status of map
        mMapView.setOnStatusChangedListener(this);

        mMapView.setOnSingleTapListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");


        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (searchEt.getText().length() != 0) {

                    Toast.makeText(MainActivity.this, "Here is the go button", Toast.LENGTH_SHORT).show();

                    String whereClause = searchEt.getText().toString();
                    whereClause = whereClause.toUpperCase(Locale.ENGLISH);

                    QueryParameters queryParameters = new QueryParameters();
                    queryParameters.setWhere("POI_NAME_EN like '%" + whereClause + "%'");
                    queryParameters.setOutSpatialReference(mMapView.getSpatialReference());
                    queryParameters.setReturnGeometry(true);

                    QueryTask queryTask;
                    try {
                        queryTask = new QueryTask(Constants.QUERY_URL, Constants.getUserCredentials());
                    } catch (EsriSecurityException e) {
                        e.printStackTrace();
                        return;
                    }
                    progressDialog.show();


                    queryTask.execute(queryParameters, new CallbackListener<FeatureResult>() {


                        @Override
                        public void onCallback(final FeatureResult results) {


                            if (results != null) {

                                graphicsLayer.removeAll();
                                List<Point> pointList = new ArrayList<>();


                                for (Object element : results) {


                                    if (element instanceof Feature) {
                                        Feature feature = (Feature) element;
                                        // turn feature into graphic

                                        PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(MainActivity.this, getResources().getDrawable(R.drawable.ic_venue));

                                        Graphic graphic = new Graphic(feature.getGeometry(), pictureMarkerSymbol, feature.getAttributes());
                                        // add graphic to layer
                                        graphicsLayer.addGraphic(graphic);

                                        pointList.add((Point) feature.getGeometry());
                                    }

                                    //calculate and set the extent to cover all the results
                                }

                                mMapView.setExtent(Constants.calculateEnvelope(pointList), getResources().getDimensionPixelSize(R.dimen.map_padding));


                            }

                            //the callback runs on a different thread, so all UI elements have to be updated on the UI thread
                            runOnUiThread(new Runnable() {
                                public void run() {

                                    long size = results != null ? results.featureCount() : 0;

                                    Toast.makeText(MainActivity.this, size
                                            + " results have returned from query.", Toast.LENGTH_LONG).show();

                                    progressDialog.dismiss();
                                }
                            });


                        }

                        @Override
                        public void onError(Throwable throwable) {

                            //the callback runs on a different thread, so all UI elements have to be updated on the UI thread
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // UI code goes here
                                    Toast.makeText(getApplicationContext(), "Could not perform query", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });


                        }
                    });


                }


            }
        });


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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStatusChanged(Object object, STATUS status) {

        if (object == mMapView) {
            if (status == STATUS.INITIALIZED) {

                Toast.makeText(this, "map loaded", Toast.LENGTH_SHORT).show();

                //start the location manager
                graphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
                mMapView.addLayer(graphicsLayer);

                mMapView.getLocationDisplayManager().start();
                mMapView.getLocationDisplayManager().setLocationListener(this);

                searchContainer.setVisibility(View.VISIBLE);

                //show the venue point

            } else if (status == STATUS.INITIALIZATION_FAILED) {
                Toast.makeText(this, "could not load map!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        if (!gotLocation) {
            Toast.makeText(this, "Got the current location...", Toast.LENGTH_SHORT).show();
            mMapView.zoomToScale(mMapView.getLocationDisplayManager().getPoint(), mMapView.getMaxScale());
            gotLocation = true;
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onSingleTap(float x, float y) {

        try {
            //find graphic ids around a particular point
            int ids[] = graphicsLayer.getGraphicIDs(x, y, 30);

            //get graphic from graphic id
            Graphic graphic = graphicsLayer.getGraphic(ids[0]);
            if (graphic != null) {
                Toast.makeText(this, (String) graphic.getAttributeValue("POI_NAME_EN"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {

            Toast.makeText(this, "Tapped on map", Toast.LENGTH_SHORT).show();

        }

    }
}
