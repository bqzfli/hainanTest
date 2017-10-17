package com.example.offpackage;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    /**
     * 压缩地图文件名
     */
    private String mNamePackage = "devlabs-package.mmpk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mapView);
        /* *** ADD *** */
        setupMobileOfflineMap();
        /* *** ADD *** */
    }

    /**
     * 设置离线地图
     */
    private void setupMobileOfflineMap() {
        if (mMapView != null) {
            String strFile = Environment.getExternalStorageDirectory() +"/"+ mNamePackage;
            File mmpkFile = new File(strFile);
            if(mmpkFile.exists()) {
                final MobileMapPackage mapPackage = new MobileMapPackage(mmpkFile.getAbsolutePath());
                mapPackage.addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {
                        // Verify the file loaded and there is at least one map
                        if (mapPackage.getLoadStatus() == LoadStatus.LOADED && mapPackage.getMaps().size() > 0) {
                            mMapView.setMap(mapPackage.getMaps().get(0));
                        } else {
                            // Error if the mobile map package fails to load or there are no maps included in the package
                            setupMap();
                        }
                    }
                });
                mapPackage.loadAsync();
            }else{
                setupMap();
            }
        }
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
