package com.example.esrigeocode;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.util.ListenableList;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private SearchView mSearchView = null;
    private GraphicsOverlay mGraphicsOverlay;
    private LocatorTask mLocatorTask = null;
    private GeocodeParameters mGeocodeParameters = null;
    private MapView mMapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mapView);
        setupMap();
        setupLocator();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        if (searchMenuItem != null) {
            mSearchView = (SearchView) searchMenuItem.getActionView();
            if (mSearchView != null) {
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                mSearchView.setIconifiedByDefault(false);
            }
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            queryLocator(intent.getStringExtra(SearchManager.QUERY));
        }
    }


    /**
     * Verify a search request is provided as we do not want to attempt to search on something invalid.

     If a current search is running then cancel it as you can only perform one search at a time.

     The search request is sent to a server over the network. This requires an asynchronous task because we do not want to block the device while we wait for the server reply.

     Geocoding results may return more than one possible match. In this lab you are only going to use the first match returned. get(0) returns the first result in the list. You have not yet written an implementation for  displaySearchResult but that is next.

     If there are no matches display a message to the user using Toast, a quick and standard means to display a message to the user then clear it.

     The code will require new imports for GeocodeResult, List, ListenableFuture, InterruptedException,  ExecutionException, and Toast.
     * @param query
     */
    private void queryLocator(final String query) {
        if (query != null && query.length() > 0) {
            mLocatorTask.cancelLoad();
            final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask.geocodeAsync(query, mGeocodeParameters);
            geocodeFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<GeocodeResult> geocodeResults = geocodeFuture.get();
                        if (geocodeResults.size() > 0) {
                            displaySearchResult(geocodeResults);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.nothing_found) + " " + query, Toast.LENGTH_LONG).show();
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        // ... determine how you want to handle an error
                    }
                    geocodeFuture.removeDoneListener(this); // Done searching, remove the listener.
                }
            });
        }
    }

    /**
     * A very simple display is used for this lab: a square marker and a text label drawn on a graphics layer.

     Any existing graphics from a prior search are removed before displaying the current search result.

     The map view is updated to center the view on the search location.

     This code requires additional imports for TextSymbol, Color, Graphic, SimpleMarkerSymbol, and  ListenableList.
     * @param geocodedLocation
     */
    private void displaySearchResult(List<GeocodeResult> geocodedLocations) {
        ListenableList allGraphics = mGraphicsOverlay.getGraphics();
        allGraphics.clear();
        String displayLabel ="";
        for(GeocodeResult geocodedLocation:geocodedLocations) {
            displayLabel = geocodedLocation.getLabel();
            TextSymbol textLabel = new TextSymbol(18, displayLabel, Color.rgb(192, 32, 32), TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.BOTTOM);
            Graphic textGraphic = new Graphic(geocodedLocation.getDisplayLocation(), textLabel);
            Graphic mapMarker = new Graphic(geocodedLocation.getDisplayLocation(), geocodedLocation.getAttributes(),
                    new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.rgb(255, 0, 0), 12.0f));
            allGraphics.add(mapMarker);
            allGraphics.add(textGraphic);
        }
        //mMapView.setViewpointCenterAsync(geocodedLocation.getDisplayLocation());
    }

    /**
     * Before you can run a geocode search the LocatorTask and GeocodeParameters must be initialized.
     * This should only be done once.
     * The initialization code is separated into its own method of the main activity that is called when the app starts.
     *
     * Identify which locator service you will use. This lab uses Esri's World Geocode service.

     Initializing the locator task is an asynchronous process because you do not want to block the device while contacting the service over the network. Wait until the locator task is fully loaded before continuing.

     Once the locator task is loaded, create the geocode parameters object. Request that the geocode service returns all attributes in the results and at most one result per request.

     Create a graphics layer where the search results are displayed, and then add the graphics overlay to the map view.

     This code requires an additional import for LoadStatus.
     */
    private void setupLocator() {
        String locatorLocation = "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";
        mLocatorTask = new LocatorTask(locatorLocation);
        mLocatorTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
                    mGeocodeParameters = new GeocodeParameters();
                    mGeocodeParameters.getResultAttributeNames().add("*");
                    mGeocodeParameters.setMaxResults(1);
                    mGraphicsOverlay = new GraphicsOverlay();
                    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
                } else if (mSearchView != null) {
                    mSearchView.setEnabled(false);
                }
            }
        });
        mLocatorTask.loadAsync();
    }


    private void setupMap() {
        if (mMapView != null) {
            String strUrl = "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer";

            ArcGISTiledLayer tiledLayerBaseMap =  new ArcGISTiledLayer(strUrl);
            //set tiled layer as basemap
            Basemap basemap = new Basemap(tiledLayerBaseMap);
            // create a map with the basemap
            ArcGISMap map = new ArcGISMap(basemap);

            mMapView.setMap(map);
        }
    }
}
