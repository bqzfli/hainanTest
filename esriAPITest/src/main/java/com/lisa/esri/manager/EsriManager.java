package com.lisa.esri.manager;

import android.content.Context;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.util.ListenableList;
import com.example.hnTest.R;
import com.example.hnTest.Util;

import java.util.Arrays;

/**
 * Created by WANT on 2017/10/13.
 */

public class EsriManager {

    public EsriManager() {
    }

    /**
     * 设置Map基础图层
     * @param context
     * @return
     */
    public ArcGISMap initMap(Context context){
        /*create map based on default map
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);*/
        //create new Tiled Layer from service url
        ArcGISTiledLayer tiledLayerBaseMap =  new ArcGISTiledLayer(context.getResources().getString(R.string.world_imagery_service));
        //set tiled layer as basemap
        Basemap basemap = new Basemap(tiledLayerBaseMap);
        // create a map with the basemap
        ArcGISMap map = new ArcGISMap(basemap);
        return map;
    }

    /**
     * 设置业务参考底图：不可操作图层
     * @param context
     * @param map       地图
     */
    public void iniDisplayLayers(Context context,ArcGISMap map ){
        //--------海南项目显示图层-----------
        //
        mDisplayLayerProtectionZoneLand = new ArcGISMapImageLayer(context.getResources().getString(R.string.service_map_protection_land));
        map.getBasemap().getBaseLayers().add(mDisplayLayerProtectionZoneLand);
        mDisplayLayerProtectionZoneSea = new ArcGISMapImageLayer(context.getResources().getString(R.string.service_map_protection_sea));
        map.getBasemap().getBaseLayers().add(mDisplayLayerProtectionZoneSea);

    }

    /**
     * 设置GraphicOverlay
     * @param graphicsOverlays  graphic层列表
     */
    public void initGraphiclayers(ListenableList<GraphicsOverlay> graphicsOverlays){

        if(graphicsOverlays.size()!=0) {
            graphicsOverlays.clear();
        }


        Util.IndexGrighicOverlaySelectBoundary = graphicsOverlays.size();
        GraphicsOverlay graphicOverlay0 = new GraphicsOverlay();
        graphicsOverlays.add(graphicOverlay0);

        Util.IndexGrighicOverlayPolygon = graphicsOverlays.size();
        GraphicsOverlay graphicOverlay1 = new GraphicsOverlay();
        graphicsOverlays.add(graphicOverlay1);

        Util.IndexGrighicOverlayPolyline = graphicsOverlays.size();
        GraphicsOverlay graphicOverlay2 = new GraphicsOverlay();
        graphicsOverlays.add(graphicOverlay2);

        Util.IndexGrighicOverlayPoint = graphicsOverlays.size();
        GraphicsOverlay graphicOverlay3 = new GraphicsOverlay();
        graphicsOverlays.add(graphicOverlay3);
    }

    /**
     * 设置操作图层数据
     * @param context
     * @param map       地图
     */
    public void initOperationLayers(Context context, ArcGISMap map){

        //-------海南项目空间查询图层
        ServiceFeatureTable tableProtectionZoneLand = new ServiceFeatureTable(context.getResources().getString(R.string.service_layer_feature_protection_land));
        FeatureLayer protectionZoneLand = new FeatureLayer(tableProtectionZoneLand);
        protectionZoneLand.setOpacity(0);
        /*map.getOperationalLayers().add(protectionZoneLand);*/

        ServiceFeatureTable tableProtectionZoneSea = new ServiceFeatureTable(context.getResources().getString(R.string.service_layer_feature_protection_sea));
        FeatureLayer protectionZoneSea = new FeatureLayer(tableProtectionZoneSea);
        protectionZoneSea.setOpacity(0);
        /*map.getOperationalLayers().add(protectionZoneSea);*/

        mFeatureLayerSelectByGeometry = new FeatureLayer[]{protectionZoneLand,protectionZoneSea};
        map.getOperationalLayers().addAll(Arrays.asList(mFeatureLayerSelectByGeometry));
        //----------------------------------------------------------------------------------------

        //-----------------------------------设置identify的图层-------------------
        // 海南项目查找图层
        mIdentifyLayers = new ArcGISMapImageLayer(context.getResources().getString(R.string.service_map_identify_protection));
        mIdentifyLayers.setOpacity(1);
        map.getOperationalLayers().add(mIdentifyLayers);

        //USA Census 2000
        //This service presents various population statistics from Census 2000,
        // including total population, population density, racial counts, and more.
        // The map service presents statistics at the state, county, block group, and block point levels.
        /*mIdentifyLayers = new ArcGISMapImageLayer(context.getResources().getString(R.string.world_census_service));*/
        //------------------------------------------------------------------------------------

        // -----------------------------属性查找图层------------------------------------------
       //ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(context.getResources().getString(R.string.sample6_service_url));
        ServiceFeatureTable serviceQuaryFieldTable = new ServiceFeatureTable(context.getResources().getString(R.string.service_layer_quaryfield_address));
        mFeatureLayerQuaryField  = new FeatureLayer(serviceQuaryFieldTable);
        /*mFeatureLayerQuaryField.setSelectionColor(Color.YELLOW);
        mFeatureLayerQuaryField.setSelectionWidth(10);*/
        mFeatureLayerQuaryField.setOpacity(0);
        map.getOperationalLayers().add(mFeatureLayerQuaryField);

        /* demo
        // create feature layer with its service feature table
        // create the service feature table
        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(context.getResources().getString(R.string.sample6_service_url));
        // create the feature layer using the service feature table
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
        // add the layer to the map
        mapView.getMap()..getOperationalLayers().add(featureLayer);*/
    }


    /**
     * Identify图层
     */
    public ArcGISMapImageLayer getIdentifyLayers() {
        return mIdentifyLayers;
    }

    /**
     * 属性查找图层
     */
    public FeatureLayer getFeatureLayerQuaryField() {
        return mFeatureLayerQuaryField;
    }

    /**
     * 海洋保护区——显示
     */
    public ArcGISMapImageLayer getDisplayLayerProtectionZoneSea() {
        return mDisplayLayerProtectionZoneSea;
    }

    /**
     * 陆地保护区——显示
     */
    public ArcGISMapImageLayer getDisplayLayerProtectionZoneLand() {
        return mDisplayLayerProtectionZoneLand;
    }

    /**
     * 保护区空间查找图层
     */
    public FeatureLayer[] getFeatureLayerSelectByGeometry() {
        return mFeatureLayerSelectByGeometry;
    }

    /**
     * Identify图层
     */
    ArcGISMapImageLayer mIdentifyLayers = null;
    /**
     * 属性查找图层
     */
    FeatureLayer mFeatureLayerQuaryField = null;

    /**
     * 海洋保护区——显示
     */
    ArcGISMapImageLayer mDisplayLayerProtectionZoneSea = null;

    /**
     * 陆地保护区——显示
     */
    ArcGISMapImageLayer mDisplayLayerProtectionZoneLand = null;

    /**
     * 保护区空间查找图层
     */
    FeatureLayer[]  mFeatureLayerSelectByGeometry = null;
}
