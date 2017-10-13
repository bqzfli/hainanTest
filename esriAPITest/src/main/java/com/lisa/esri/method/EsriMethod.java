package com.lisa.esri.method;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.LicenseInfo;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.LayerContent;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.security.UserCredential;
import com.example.hnTest.R;
import com.example.hnTest.Util;
import com.lisa.esri.manager.EsriManager;
import com.lisa.esri.manager.Selection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import spinner.ItemData;
import spinner.SpinnerAdapter;

/**
 * Created by lisa on 2017/10/3.
 */

public class EsriMethod {

    private int requestCode = 2;
    private String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION};

    private EsriManager mEsriManager = new EsriManager();

    /**
     * 完成空间查询的图层个数
     */
    public int CountCompleteSelect = 0;

    /**
     * 空间查询目标的个数
     */
    public int CountFeatureSelect = 0;


    /**
     * 空间查找是否完成
     * @return
     */
    public boolean isSelectByGeometryComplete(){
        if(CountCompleteSelect==mEsriManager.getFeatureLayerSelectByGeometry().length){
            return true;
        }
        return  false;
    }

    /**
     * 设置Portal验证
     * 在所有ArcGIS API 功能之前调用
     * @param portalUrl     门户的url
     * @param user          账户名
     * @param password      密码
     */
    public void initAuthenticate(String portalUrl,String user,String password){
        // connect to ArcGIS Online or an ArcGIS portal as a named user
        // The code below shows the use of token based security but
        // for ArcGIS Online you may consider using Oauth authentication.
        UserCredential credential = new UserCredential(user, password);

        // replace the URL with either the ArcGIS Online URL or your portal URL
        final Portal portal = new Portal(portalUrl);
        portal.setCredential(credential);

        // load portal and listen to done loading event
        portal.loadAsync();
        portal.addDoneLoadingListener(new Runnable(){
            @Override
            public void run() {
                // get license info from the portal
                LicenseInfo licenseInfo = portal.getPortalInfo().getLicenseInfo();
                // Apply the license at Standard level
                ArcGISRuntimeEnvironment.setLicense(licenseInfo);
            }
        });
    }


    /**
     * 设置当前位置的显示
     * @param mapView   地图控件
     */
    public void initLocationDisplay(MapView mapView, final Context context){
        LocationDisplay locationDisplay = mapView.getLocationDisplay();
        locationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {
                if (dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError() == null) {
                    Util.showMessage(context,"定位信息显示状态：" + dataSourceStatusChangedEvent.isStarted());
                } else {
                    // Deal with problems starting the LocationDisplay...
                    Util.showMessage(context,dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                }
            }
        });
        locationDisplay.startAsync();
    }

    /**
     * 初始化MapView的基础底图
     * @param context
     * @param mapView   地图控件
     */
    public void iniBaseMap(Context context,MapView mapView){
        ArcGISMap map = mEsriManager.initMap(context);
        mEsriManager.iniDisplayLayers(context,map);
        mapView.setMap(map);
    }


    /**
     * 设置操作图层数据
     * @param context
     * @param mapView   地图控件
     */
    public void initLayerOperatinal(Context context,final MapView mapView){
        mEsriManager.initOperationLayers(context,mapView.getMap());
    }

    /**
     * 设置GraphicOverlay
     * @param mapView   底图控件
     */
    public void initGraphicOverlay(MapView mapView){
        if(mapView.getGraphicsOverlays().size()!=0) {
            mapView.getGraphicsOverlays().clear();
        }
        mEsriManager.initGraphiclayers(mapView.getGraphicsOverlays());
    }

    /**
     * 清空GraphicOverlay 中的元素
     * @param mapView   地图控件
     */
    public void clearGraphicOverlay(MapView mapView){
        if(mapView.getGraphicsOverlays().size()==0) {
            initGraphicOverlay(mapView);
        }
        mapView.getGraphicsOverlays().get(Util.IndexGrighicOverlayPolygon).getGraphics().clear();
        mapView.getGraphicsOverlays().get(Util.IndexGrighicOverlayPolyline).getGraphics().clear();
        mapView.getGraphicsOverlays().get(Util.IndexGrighicOverlayPoint).getGraphics().clear();
    }

    /**
     * 地图画面截屏
     * @param context
     * @param picName   图片名称，不要带后缀名
     * @param mapView   地图控件
     */
    public void exportScreenCapture(final Context context,MapView mapView, final String picName){
        final ListenableFuture<Bitmap> exportImageFuture = mapView.exportImageAsync();
        exportImageFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get the resulting Bitmap from the future
                    Bitmap bitmap = exportImageFuture.get();

                    if (bitmap != null) {
                        // Create a File to write the Bitmap into
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), picName+".png");
                        FileOutputStream fileOutputStream;
                        try {
                            // Write the Bitmap into the file and close the file stream.
                            fileOutputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                            fileOutputStream.close();
                        } catch (IOException e) {
                            Util.showMessage(context,e.getMessage());
                            // Deal with exception writing file...
                        }
                    }

                } catch (InterruptedException | ExecutionException e) {
                    // Deal with exception during export...
                }
            }
        });
    }


    /**
     * 设置地图渲染完成状态的监控
     * @param mapView       地图控件
     * @param progressBar   进图条
     */
    public void initDrawStatusChanged(MapView mapView,final ProgressBar progressBar){

        //[DocRef: Name=Monitor map drawing, Category=Work with maps, Topic=Display a map]
        mapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
            @Override
            public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
                if(drawStatusChangedEvent.getDrawStatus() == DrawStatus.IN_PROGRESS){
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d("drawStatusChanged", "spinner visible");
                }else if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        //[DocRef: END]
    }


    /**
     * 显示设备位置的相关操作
     * @param context
     * @param mapView   地图控件
     * @param spinner   设备位置显示切换按钮
     */
    public void initLocationOperation(final Activity context,MapView mapView,final Spinner spinner) {
        final LocationDisplay locationDisplay = mapView.getLocationDisplay();

        // Listen to changes in the status of the location data source.
        locationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                // If LocationDisplay started OK, then continue.
                if (dataSourceStatusChangedEvent.isStarted())
                    return;

                // No error is reported, then continue.
                if (dataSourceStatusChangedEvent.getError() == null)
                    return;

                // If an error is found, handle the failure to start.
                // Check permissions to see if failure may be due to lack of permissions.
                boolean permissionCheck1 = ContextCompat.checkSelfPermission(context, reqPermissions[0]) ==
                        PackageManager.PERMISSION_GRANTED;
                boolean permissionCheck2 = ContextCompat.checkSelfPermission(context, reqPermissions[1]) ==
                        PackageManager.PERMISSION_GRANTED;
                if (!(permissionCheck1 && permissionCheck2)) {
                    // If permissions are not already granted, request permission from the user.
                    ActivityCompat.requestPermissions(context, reqPermissions, requestCode);
                } else {
                    // Report other unknown failure types to the user - for example, location services may not
                    // be enabled on the device.
                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                            .getSource().getLocationDataSource().getError().getMessage());
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                    // Update UI to reflect that the location display did not actually start
                    spinner.setSelection(0, true);
                }
            }
        });

        // Populate the list for the Location display options for the spinner's Adapter
        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("关闭GPS", R.drawable.locationdisplaydisabled));
        list.add(new ItemData("开启GPS", R.drawable.locationdisplayon));
        list.add(new ItemData("设备位置居中", R.drawable.locationdisplayrecenter));
        list.add(new ItemData("汽车导航", R.drawable.locationdisplaynavigation));
        list.add(new ItemData("步行导航", R.drawable.locationdisplayheading));

        SpinnerAdapter adapter = new SpinnerAdapter((Activity) context, R.layout.spinner_layout, R.id.txt, list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        // Stop Location Display
                        if (locationDisplay.isStarted())
                            locationDisplay.stop();
                        break;
                    case 1:
                        // Start Location Display
                        if (!locationDisplay.isStarted())
                            locationDisplay.startAsync();
                        break;
                    case 2:
                        // Re-Center MapView on Location
                        // AutoPanMode - Default: In this mode, the MapView attempts to keep the location symbol on-screen by
                        // re-centering the location symbol when the symbol moves outside a "wander extent". The location symbol
                        // may move freely within the wander extent, but as soon as the symbol exits the wander extent, the MapView
                        // re-centers the map on the symbol.
                        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                        if (!locationDisplay.isStarted())
                            locationDisplay.startAsync();
                        break;
                    case 3:
                        // Start Navigation Mode
                        // This mode is best suited for in-vehicle navigation.
                        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                        if (!locationDisplay.isStarted())
                            locationDisplay.startAsync();
                        break;
                    case 4:
                        // Start Compass Mode
                        // This mode is better suited for waypoint navigation when the user is walking.
                        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
                        if (!locationDisplay.isStarted())
                            locationDisplay.startAsync();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }

        });
    }

    /**
     * 查询在范围内或与范围相交的要素
     * @param context
     * @param mapView           地图控件
     * @param layers            查询图层
     * @param geo               查询范围
     * @param touchMapEvent     查询后View要做的
     */
    private void searchInLayersByGeometry(final Context context, final MapView mapView, final FeatureLayer[] layers, Geometry geo,final OnTouchMapEvent touchMapEvent) {

        //重置已查询图层的个数
        CountCompleteSelect = 0;
        //重置空间查询的结果个数
        CountFeatureSelect = 0;
        //设置查询条件
        QueryParameters query = new QueryParameters();
        //设置查询范围
        query.setGeometry(geo);
        //查询方式为相交
        query.setSpatialRelationship(Util.SelectRelationship);

        // call select features
        for(int i=0;i<layers.length;i++) {
            final FeatureLayer layer = layers[i];
            final ListenableFuture<FeatureQueryResult> future = layer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW);
            // add done loading listener to fire when the selection returns
            future.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    CountCompleteSelect += 1;
                    try {
                        //call get on the future to get the result
                        FeatureQueryResult result = future.get();

                        // create an Iterator
                        Iterator<Feature> iterator = result.iterator();
                        List<Field> fields = result.getFields();
                        Feature feature = null;

                        while (iterator.hasNext()) {
                            // get the extend of the first feature in the result to zoom to
                            feature = iterator.next();
                            Envelope envelope = feature.getGeometry().getExtent();

                            // TODO: 2017/10/8 显示结果
                            processGeoFromeFeature(feature, mapView);
                            //CountFeatureSelect += processRecordFromFeature(context, feature, layer);
                            CountFeatureSelect += processRecordFromFeature(context, feature, fields, layer);
                            //Select the feature
//                        mFeaturelayer.selectFeature(feature);
                        }
                        // View显示查找结果
                        if(touchMapEvent!=null){
                            touchMapEvent.refreshViewOnSearchSuccess("选中目标个数：" + CountFeatureSelect,CountFeatureSelect);
                        }
                    } catch(InterruptedException | ExecutionException ex){
                        // must deal with checked exceptions thrown from the async identify operation
                        // View显示失败信息
                        if(touchMapEvent!=null){
                            touchMapEvent.refreshViewOnSearchFailed("查询失败：\n"+layer.getName(), ex );
                        }
                    }
                }
            });
        }
    }


    /**
     * 通过地图的Identify查找对应的位置
     * @param context
     * @param mapView       地图控件
     * @param layer         查询图层
     * @param screenPoint   屏幕点击点
     * @param tolerance     容错距离；单位：像素；不能大于100
     *
     */
    private void searchInMapByIdentify(final Context context, final MapView mapView, final Layer layer, android.graphics.Point screenPoint,int tolerance,final OnTouchMapEvent touchMapEvent) {
        //清空历史选中结果
        Selection.SearchResultFromOperationLayer.clear();
        if(layer!=null){
            if(layer instanceof FeatureLayer){
                ((FeatureLayer)layer).clearSelection();
            }
            //单图层查找
            // call identifyLayerAsync, specifying the layer to identify, the screen location, tolerance, types to return, and maximum results
            final ListenableFuture<IdentifyLayerResult> identifyFuture = mapView.identifyLayerAsync(layer, screenPoint, tolerance, false, 25);

            // add a listener to the future
            identifyFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    int Count = 0;
                    try {
                        // get the identify results from the future - returns when the operation is complete
                        IdentifyLayerResult identifyLayerResult = identifyFuture.get();
                        // 处理查找的目标
                        Count = prosessResultFromSearchLayers(context,mapView,identifyLayerResult);

                        // View显示查找结果
                        if(touchMapEvent!=null){
                            touchMapEvent.refreshViewOnSearchSuccess("选中目标个数：" + Count,Count);
                        }
                    } catch(InterruptedException | ExecutionException ex){
                        // must deal with checked exceptions thrown from the async identify operation
                        // View显示失败信息
                        if(touchMapEvent!=null){
                            touchMapEvent.refreshViewOnSearchFailed("查询失败：" + Count,ex);
                        }
                    }

                }
            });
        }else{
            //全部图层查找
            final ListenableFuture<List<IdentifyLayerResult>> identifyFutures = mapView.identifyLayersAsync(screenPoint, tolerance, false,25);
            identifyFutures.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        // get the identify results from the future - returns when the operation is complete
                        List<IdentifyLayerResult> identifyLayersResults = identifyFutures.get();

                        int Count = 0;
                        // iterate all the layers in the identify result
                        for (IdentifyLayerResult identifyLayerResult : identifyLayersResults) {
                            Count += prosessResultFromSearchLayers(context,mapView,identifyLayerResult);
                        }

                        // View显示查找结果
                        if(touchMapEvent!=null){
                            touchMapEvent.refreshViewOnSearchSuccess("选中目标个数：" + Count,Count);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        // must deal with exceptions thrown from the async identify operation
                        // View显示失败信息
                        if(touchMapEvent!=null){
                            touchMapEvent.refreshViewOnSearchFailed("查询失败：", ex);
                        }
                    }
                }
            });
        }
    }

    /**
     * 处理各图层搜索后的结果
     */
    private int prosessResultFromSearchLayers(final Context context,final MapView mapView,IdentifyLayerResult identifyLayerResult){
        int Count = 0;
        if (identifyLayerResult.getLayerContent() instanceof FeatureLayer) {
            FeatureLayer featureLayer = (FeatureLayer) identifyLayerResult.getLayerContent();
            // iterate each identified geoelement from the specified layer and cast to Feature
            for (GeoElement identifiedElement : identifyLayerResult.getElements()) {
                if (identifiedElement instanceof Feature) {
                    // access attributes or geometry of the feature, or select it as shown below
                    Feature identifiedFeature = (Feature) identifiedElement;
                    if (featureLayer != null) {
                        Count += processIdentifyImageLayerResult(context,identifiedFeature,featureLayer,mapView);
                        //直接在图层上标注为已选中
                        //featureLayer.selectFeature(identifiedFeature);
                        //Count+=1;
                    }
                }
            }
        }else if(identifyLayerResult.getLayerContent() instanceof ArcGISMapImageLayer){
            //处理ImagLayer的Identify
            List<IdentifyLayerResult> subLayersResults = identifyLayerResult.getSublayerResults();
            Count += iterateIdentifyImageLayerResults(context,subLayersResults,mapView);

        }else{
            // iterate each result in each identified layer, and check for Feature results
            for (GeoElement identifiedElement : identifyLayerResult.getElements()) {
                if (identifiedElement instanceof Feature) {
                    Feature identifiedFeature = (Feature) identifiedElement;
                    // Use feature as required, for example access attributes or geometry, select, build a table, etc...
                    Count += processIdentifyFeatureResult(context,identifiedFeature, identifyLayerResult.getLayerContent());
                }
            }
        }
        return  Count;
    }



    /**
     * 根据输入内容请求服务查询
     * @param context
     * @param mapView   地图控件
     * @param f_caption 字段名
     * @param value     字段值
     * @param viewEvent 界面更新
     *
     */
    public void initQuaryByField(final Context context, final MapView mapView, final String f_caption, final String value,final OnTouchMapEvent viewEvent) {
        //refresh view
        if(viewEvent!=null){
            viewEvent.refreshViewOnStartSearch(null);
        }

        // clear the result of searching in history
        clearGraphicOverlay(mapView);

        // clear the result of searching in history
        Selection.SearchResultFromOperationLayer.clear();

        // create objects required to do a selection with a query
        QueryParameters query = new QueryParameters();
        //make search case insensitive
        //String strQuery = "upper("+f_caption+") LIKE '%" + value + "%'";
        String strQuery = f_caption+" LIKE '%" + value + "%'";
        query.setWhereClause(strQuery);
        query.setReturnGeometry(true);

        // call select features
        final ListenableFuture<FeatureQueryResult> future =  mEsriManager.getFeatureLayerQuaryField().getFeatureTable().queryFeaturesAsync(query);

        // add done loading listener to fire when the selection returns
        future.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // call get on the future to get the result
                    FeatureQueryResult result = future.get();
                    int countMax = 0;
                    Iterator<Feature> results = result.iterator();
                    List<Field> fields = result.getFields();
                    // check there are some results
                    while (results.hasNext()) {

                        // get the extend of the first feature in the result to zoom to
                        Feature feature = results.next();
                        Envelope envelope = feature.getGeometry().getExtent();

                        //// TODO: 2017/10/8 显示结果
                        processGeoFromeFeature(feature,mapView);
                        /*countMax += processRecordFromFeature(context,feature,getFeatureLayerQuaryField());*/
                        countMax += processRecordFromFeature(context,feature,fields,mEsriManager.getFeatureLayerQuaryField());
                        //Select the feature
//                        mFeaturelayer.selectFeature(feature);
                    }

                    String info = "选中目标个数：" + countMax;
                    Log.i("属性查找——成功",info);
                    if(viewEvent!=null){
                        viewEvent.refreshViewOnSearchSuccess(info,countMax);
                    }
                } catch (Exception e) {
                    String info = "Feature search failed for: " + value + ". Error=" + e.getMessage();
                    Log.e("属性查找——失败", info);
                    if(viewEvent!=null){
                        viewEvent.refreshViewOnSearchFailed(info,e);
                    }
                }
                Util.dismissProgressDialog();
            }
        });
    }


    /**
     * 设置地图的点选查询方法
     * @param context
     * @param mapView           地图控件
     * @param touchMapEvent     查询是View层要做的事情
     */
    public void initIdentifyOperation(final Context context, final MapView mapView,final OnTouchMapEvent touchMapEvent){
        mapView.setOnTouchListener(new DefaultMapViewOnTouchListener(context,mapView){

            // override the onSingleTapConfirmed gesture to handle a single tap on the MapView
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // get the screen point where user tapped
                android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());
                // clear the result of searching in history
                clearGraphicOverlay(mapView);
                // clear the list of searching in history
                Selection.SearchResultFromOperationLayer.clear();
                // refresh the View
                if(touchMapEvent!=null){
                    touchMapEvent.refreshViewOnStartSearch(e);
                }

                // -------------- 点击位置查找 --------------
                int toleranceMap = 5000;
                int tolerancePixel = 1;//Math.round(toleranceMap/Math.round(mapView.getUnitsPerDensityIndependentPixel()));
                //tolerancePixel = 25;//tolerancePixel>100?99:tolerancePixel;//tolerancePixel不能大于100
                searchInMapByIdentify(context,mapView,mEsriManager.getIdentifyLayers(),screenPoint,tolerancePixel,touchMapEvent);
                return true;
            }
        });
    }

    /**
     * 设置地图的点选查询方法
     * @param context
     * @param mapView           地图控件
     *                           当为null时对地图中所有操作图层进行查询；
     *                           不为null时对layer进行查询
     * @param touchMapEvent     查询是View层要做的事情
     */
    public void initSelectByGeometry(final Context context, final MapView mapView,final OnTouchMapEvent touchMapEvent){
        //TODO 使用GPS位置，而不是屏幕点击位置
        mapView.setOnTouchListener(new DefaultMapViewOnTouchListener(context,mapView){

            // override the onSingleTapConfirmed gesture to handle a single tap on the MapView
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // clear the result of searching in history
                clearGraphicOverlay(mapView);
                // clear the list of searching in history
                Selection.SearchResultFromOperationLayer.clear();

                // clear the selected history of each layer
                for(FeatureLayer layer:mEsriManager.getFeatureLayerSelectByGeometry()){
                    layer.clearSelection();
                }

                // get the screen point where user tapped
                android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

                //todo 输入点使用GPS获取的位置，注意转换为地图坐标
                // get the map location where user tapped
                Point clickPoint = mMapView.screenToLocation(screenPoint);
                // create select envelope by tolerance setting by users
                Envelope envelope = new Envelope(
                        clickPoint.getX() - Util.MapSelectDistance,
                        clickPoint.getY() - Util.MapSelectDistance,
                        clickPoint.getX() + Util.MapSelectDistance,
                        clickPoint.getY() + Util.MapSelectDistance,
                        mapView.getSpatialReference());

                //refresh the view
                if(touchMapEvent!=null){
                    touchMapEvent.refreshViewOnStartSearch(e);
                }
                if(mEsriManager.getFeatureLayerSelectByGeometry()!=null&&mEsriManager.getFeatureLayerSelectByGeometry().length>0) {
                    //设置多图层查询
                    searchInLayersByGeometry(context, mapView, mEsriManager.getFeatureLayerSelectByGeometry(), envelope, touchMapEvent);
                }else{
                    if(touchMapEvent!=null){
                        String info = "没有可供查询的图层";
                        Exception ex = new Exception();
                        touchMapEvent.refreshViewOnSearchFailed(info,ex);
                    }
                }
                return true;
            }
        });
    }

    /**
     * 切换MapView的基础底图
     * @param mapView       地图控件
     * @param urlMapServer  作为底图的服务地址
     */
    public void switchBaseMap (MapView mapView,String urlMapServer){
        //create new Tiled Layer from service url
        ArcGISTiledLayer tiledLayerBaseMap =  new ArcGISTiledLayer(urlMapServer);
        //change the baselayer
        mapView.getMap().getBasemap().getBaseLayers().set(0,tiledLayerBaseMap);
    }


    /**
     * 设置地图的显示范围
     * @param mapView   地图控件
     */
    public void changeViewPoint(final MapView mapView){
        //海南项目范围
        Envelope env = new Envelope(108.62333711241558,18.159500862569853,111.04646712614706,20.16107656389238, SpatialReferences.getWgs84());
        //USA Census 2000
        //Envelope env = new Envelope( -179.6191629086413,17.881242000418013, -65.2442340001989,71.40623536706858, SpatialReference.create(4269));

        //设置地图的初始范围
//        mMap.setInitialViewpoint(new Viewpoint(env));

        final ListenableFuture<Boolean> viewpointSetFuture = mapView.setViewpointAsync(new Viewpoint(env), 2);
        viewpointSetFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean completed = viewpointSetFuture.get();
                    if (completed)
                        Util.showMessage(mapView.getContext(),"动画完成");
                } catch (InterruptedException e) {
                    Util.showMessage(mapView.getContext(),"动画被中断");
                } catch (ExecutionException e) {
                    Util.showMessage(mapView.getContext(),"动画异常：\n"+e.getMessage());
                }
            }
        });
    }


    /**
     * 对ImageLayer迭代查询
     * @param context
     * @param identifyLayerResults         查询结果
     * @param mapView                      地图控件
     * @return                             目标个数
     */
    public int iterateIdentifyImageLayerResults(Context context,List<IdentifyLayerResult> identifyLayerResults, MapView mapView) {
        int countMax = 0;
        if ((identifyLayerResults == null) || (identifyLayerResults.size() < 1)) return countMax;
        // a reference to the feature layer can be used, for example, to select identified features

        // iterate all the layers in the identify result
        for (IdentifyLayerResult identifyLayerResult : identifyLayerResults) {
            // for each result, get the GeoElements
            for (GeoElement identifiedElement : identifyLayerResult.getElements()) {

                if (identifiedElement instanceof Feature) {
                    // Map image layer identify results are returned as Features with a null FeatureTable; they cannot be
                    // selected, but you can get the attributes and geometry of them...
                    int count = processIdentifyImageLayerResult(context,((Feature)identifiedElement), identifyLayerResult.getLayerContent(),mapView);
                    countMax += count;
                }
            }

            // for each result, get the sublayer results by recursion
            int count = iterateIdentifyImageLayerResults(context,identifyLayerResult.getSublayerResults(),mapView);
            countMax += count;
        }
        return countMax;
    }


    /**
     * 选中目标后触发的事件
     * @param context               父页面
     * @param identifiedFeature     点选内容
     * @param layerContent          图层内容
     */
    public int processIdentifyFeatureResult(Context context,Feature identifiedFeature, LayerContent layerContent) {
        FeatureTable table = identifiedFeature.getFeatureTable();
        String strResult = "查询结果";
        List<Field> fields = table.getFields();
        String layerName = layerContent.getName();
        strResult = "图层："+layerName;
        for(Field field:fields){
            String value = (String)identifiedFeature.getAttributes().get(field.getName());
            strResult += "\n"+field.getName()+":"+value+";";
        }
        Util.showMessage(context,strResult);
        return  1;
    }

    /**
     * 处理查找到的矢量的显示
     * @param identifiedElement     矢量要素
     * @param mapView               地图控件
     */
    private Geometry processGeoFromeFeature(Feature identifiedElement,MapView mapView){
        Geometry geo =  identifiedElement.getGeometry();
        Graphic graphic = null;
        if(geo.getGeometryType() == GeometryType.POLYGON){
            //define the fill symbol and outline
            graphic = new Graphic(geo, Util.SymbolFill);
            mapView.getGraphicsOverlays().get(Util.IndexGrighicOverlayPolygon).getGraphics().add(graphic);
        }else if(geo.getGeometryType() == GeometryType.POLYLINE){
            graphic = new Graphic(geo,Util.SymbolOutline);
            mapView.getGraphicsOverlays().get(Util.IndexGrighicOverlayPolyline).getGraphics().add(graphic);
        }else if(geo.getGeometryType() == GeometryType.POINT){
            graphic = new Graphic(geo,Util.getSymbolDefaultPictureMapker(mapView.getContext()));
            mapView.getGraphicsOverlays().get(Util.IndexGrighicOverlayPoint).getGraphics().add(graphic);
        }
        return geo;
    }

    /**
     * 处理查找到结果的属性信息
     * @param context
     * @param identifiedElement 查询结果
     * @param layerContent      所属图层
     * @return                  1：成功处理一条；0：失败
     */
    private int processRecordFromFeature(Context context,Feature identifiedElement, List<Field> fields,LayerContent layerContent){
        Geometry geo = identifiedElement.getGeometry();
        try {
            Map<String, Object> attributes = identifiedElement.getAttributes();
            Map<String, Object> result = new HashMap<String, Object>();
            String layerName = layerContent.getName();
            /*String geoJson = geo.toJson();*/
            if (layerName != null &&/* geoJson != null*/ geo!=null&& attributes != null) {
                result.put(Util.KEY_LAYERNAME, layerName);
                /*result.put(Util.KEY_GEOJSON,geoJson);*/
                result.put(Util.KEY_GEO,geo);
                for(Field field:fields){
                    result.put(field.getName(),attributes.get(field.getName()));
                }
                Selection.SearchResultFromOperationLayer.add(result);
            }else{
                return 0;
            }
        }catch (Exception ex){
            Util.showMessage(context,ex.getMessage());
            return 0;
        }
        return 1;
    }


    /**
     * 处理查找到结果的属性信息
     * @param context
     * @param identifiedElement 查询结果
     * @param layerContent      所属图层
     * @return                  1：成功处理一条；0：失败
     */
    private int processRecordFromFeature(Context context,Feature identifiedElement, LayerContent layerContent){
        Geometry geo = identifiedElement.getGeometry();
        try {
            Map<String, Object> attributes = identifiedElement.getAttributes();
            String layerName = layerContent.getName();
            /*String geoJson = geo.toJson();*/
            if (layerName != null &&/* geoJson != null*/geo!=null && attributes != null) {
                attributes.put(Util.KEY_LAYERNAME, layerName);
                /*attributes.put(Util.KEY_GEOJSON, geoJson);*/
                attributes.put(Util.KEY_GEO,geo);
                Selection.SearchResultFromOperationLayer.add(attributes);
            }else{
                return 0;
            }
        }catch (Exception ex){
            Util.showMessage(context,ex.getMessage());
            return 0;
        }
        return 1;
    }


    /**
     * 处理查询到的Feature的内容
     * @param context
     * @param identifiedElement             查询到的对象
     * @param layerContent                  所属图层
     * @param mapView                       地图控件
     */
    public int processIdentifyImageLayerResult(Context context,Feature identifiedElement, LayerContent layerContent, MapView mapView) {
        Geometry geo = processGeoFromeFeature(identifiedElement,mapView);
        int countMax = processRecordFromFeature(context,identifiedElement,layerContent);
        return countMax;
    }


    /**
     * 用户触摸地图时触发的事件
     */
    public interface OnTouchMapEvent{
        /**
         * 开始搜索时触发的事件
         * @param e      用户手指触摸地图控件时需要实现的方法
         */
        void refreshViewOnStartSearch(MotionEvent e);

        /**
         * 搜索成功时触发的事件
         *
         * @param info      弹窗内可显示的内容
         * @param count     查询记录数量；
         */
        void refreshViewOnSearchSuccess(String info,int count);

        /**
         * 搜索失败时触发的事件
         * @param info      弹窗内可显示的内容
         * @param ex        异常
         */
        void refreshViewOnSearchFailed(String info,Exception ex);
    }

}
