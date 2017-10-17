package com.example.esri3d;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;


public class MainActivity extends AppCompatActivity {

    private SceneView mSceneView;
    private TextView mTvCameraChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSceneView = (SceneView) findViewById(R.id.sceneView);
        mTvCameraChanged = (TextView) findViewById(R.id.tv_cameral_changed);
        setupMap();

        Camera camera = new Camera(
                33.950896,
                -118.525341,
                16000.0,
                0.0,
                50.0,
                0.0);
        mSceneView.setViewpointCamera(camera);
        mSceneView.addViewpointChangedListener(new ViewpointChangedListener() {
            @Override
            public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
                SceneView sceneView = (SceneView)viewpointChangedEvent.getSource();
                Camera camera = sceneView.getCurrentViewpointCamera();
                refresh(camera,mTvCameraChanged);
            }
        });
    }

    private  void refresh(Camera camera, TextView tvCameraChanged){
        String info = "";
        String x = String.valueOf(camera.getLocation().getX());
        String y = String.valueOf(camera.getLocation().getY());
        String z = String.valueOf(camera.getLocation().getZ());
        String heading = String.valueOf(camera.getHeading());
        String pitch = String.valueOf(camera.getPitch());
        String roll = String.valueOf(camera.getRoll());
        info = "X："+ x
                +"\nY："+y
                +"\nZ："+z
                +"\n方向角："+ heading
                +"\n俯仰角："+ pitch
                +"\n翻滚角："+ roll;
        tvCameraChanged.setText(info);

    }

    private void setupMap() {
        if (mSceneView != null) {
            Basemap.Type basemapType = Basemap.Type.IMAGERY_WITH_LABELS;
            ArcGISScene scene = new ArcGISScene(basemapType);
            mSceneView.setScene(scene);
        /* ** ADD ** */
            addTrailsLayer();
            setElevationSource(scene);
        }
    }


    private void addTrailsLayer(){
        String url = "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Trails/FeatureServer/0";
        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(url);
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
        mSceneView.getScene().getOperationalLayers().add(featureLayer);
    }


    /**
     * Use an elevation service provided by Esri and add it to the scene. Create a separate method to add the elevation service.
     * @param scene
     */
    private void setElevationSource(ArcGISScene scene) {
        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
        scene.getBaseSurface().getElevationSources().add(elevationSource);
    }


}
