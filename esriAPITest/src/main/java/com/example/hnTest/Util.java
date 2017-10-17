package com.example.hnTest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

/**
 * Created by WANT on 2017/10/2.
 */

public class Util {

    /**
     * 图层名称关键字
     */
    public static String KEY_LAYERNAME = "LAYERNAME";
    /**
     * 矢量信息：字符串形式
     */
    public static String KEY_GEOJSON = "GOEJSON";
    /**
     * 矢量信息：实体类型
     */
    public static String KEY_GEO = "GOE";

    /**
     * 调查对象主键
     */
    public static String KEY_OBJECTID = "OBJECTID";

    /**
     * 地图容差距离，用户可在系统设置里配置
     * 单位：米
     */
    public static double MapSelectDistance = /*2700*1000;*/ 5000;

    /**
     * 空间查询方式，
     * APP研发中设置
     */
    public static QueryParameters.SpatialRelationship SelectRelationship = QueryParameters.SpatialRelationship.INTERSECTS;

    /**
     * 查询用进度条
     */
    private static ProgressDialog mProgressDialogSearching = null;

    public static int IndexGrighicOverlaySelectBoundary = 0;
    public static int IndexGrighicOverlayPolygon = 1;
    public static int IndexGrighicOverlayPolyline = 2;
    public static int IndexGrighicOverlayPoint = 3;


    /**
     * color：blue
     * width：1
     * style：solid
     */
    public static SimpleLineSymbol SymbolOutline =
            new SimpleLineSymbol(
                    SimpleLineSymbol.Style.SOLID,
                    Color.argb(255, 0, 0, 128), 4.0f);
    /**
     * color：grenn
     * line：blue、1、solod
     * style：DIAGONAL_CROSS
     */
    public static SimpleFillSymbol SymbolFill =
            new SimpleFillSymbol(
                    SimpleFillSymbol.Style.DIAGONAL_CROSS,
                    Color.argb(128, 128, 0, 128),
                    SymbolOutline);

    /**
     * color：Color.argb(128, 0, 255, 128)
     * line：blue、1、solod
     * style：Solid
     */
    public static SimpleFillSymbol SymbolFill_SelectBoundary =
            new SimpleFillSymbol(
                    SimpleFillSymbol.Style.SOLID,
                    Color.argb(128, 128, 0, 128),
                    SymbolOutline);

    /**
     * color：green
     * line：OutlineSymbol
     * style：solid
     */
    public static SimpleMarkerSymbol SymbolMarker =
            new SimpleMarkerSymbol(
                    SimpleMarkerSymbol.Style.CIRCLE,
                    Color.argb(255, 255, 0, 0),
                    8f);

    /**
     *
     */
    private static PictureMarkerSymbol SymbolPictureMarker = null;

    /**
     * 获取默认图片标注
     * @param context
     * @return
     */
    public static PictureMarkerSymbol getSymbolDefaultPictureMapker(Context context){
        if(SymbolPictureMarker == null){
            initSymbolDefaultPictureMapker(context);
        }
        return SymbolPictureMarker;
    }

    /**
     * 设置默认图片标注
     * @param context
     */
    public static void initSymbolDefaultPictureMapker(Context context){
        //Create a picture marker symbol from an app resource
        BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.pic_target_31_blue);
        SymbolPictureMarker = new PictureMarkerSymbol(pinStarBlueDrawable);
        //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
        //its appearance would then differ across devices with different resolutions.
        SymbolPictureMarker.setHeight(31);
        SymbolPictureMarker.setWidth(23);
        //Optionally set the offset, to align the base of the symbol aligns with the point geometry
        SymbolPictureMarker.setOffsetY(11);
        //The image used for the symbol has a transparent buffer around it, so the offset is not simply height/2
        SymbolPictureMarker.loadAsync();
    }



    /**
     * 处理返回的异常信息
     * @param context
     * @param ex
     */
    public static void dealWithException(Context context,Exception ex) {
        showMessage(context,ex.getMessage());
    }


    /**
     * 显示提示信息
     * @param context   上下文
     * @param info      提示信息
     */
    public static void showMessage(Context context,String info){
        Toast.makeText(context,info,Toast.LENGTH_LONG).show();
    }

    /**
     * 显示进度条
     * @param context
     * @param info          提示信息
     */
    public static void showProgressDialog(Context context,String info){
        //显示进度条
        if(mProgressDialogSearching != null){
            mProgressDialogSearching = null;
        }
        mProgressDialogSearching = new ProgressDialog(context);
        mProgressDialogSearching.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialogSearching.setMessage(info);
        mProgressDialogSearching.setCancelable(false);
        mProgressDialogSearching.show();
    }

    public static void dismissProgressDialog(){
        if(mProgressDialogSearching!=null&&mProgressDialogSearching.isShowing()){
            mProgressDialogSearching.dismiss();
        }
        mProgressDialogSearching = null;
    }

    /**
     * 获取圆形缓冲区
     * @param centerPoint       中心点
     * @param distance          缓冲距离，单位：米
     * @param count             数量
     * @param spatialReference  地图投影
     */
    public static Polygon GetCircleBoundary(Point centerPoint, double distance, int count, SpatialReference spatialReference){
        PointCollection pointCollection = new PointCollection(spatialReference);
        for(int i=0;i<count;i++){
            double angle = Math.PI*2/count;
            pointCollection.add(new Point(
                            centerPoint.getX()+distance*Math.cos(angle*i),
                            centerPoint.getY()+distance*Math.sin(angle*i)));
        }
        Polygon polygon  = new Polygon(pointCollection);
        return  polygon;
    }
}
