package com.example.hnTest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

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
    public static String LAYERNAME = "LAYERNAME";
    /**
     * 矢量信息
     */
    public static String GEOJSON = "GOEJSON";

    /**
     * 查询用进度条
     */
    private static ProgressDialog mProgressDialogSearching = null;

    public static int IndexGrighicOverlayPolygon = 0;
    public static int IndexGrighicOverlayPolyline = 1;
    public static int IndexGrighicOverlayPoint = 2;


    /**
     * color：blue
     * width：1
     * style：solid
     */
    public static SimpleLineSymbol SymbolOutline =
            new SimpleLineSymbol(
                    SimpleLineSymbol.Style.SOLID,
                    Color.argb(255, 0, 0, 128), 1.0f);
    /**
     * color：green
     * line：OutlineSymbol
     * style：solid
     */
    public static SimpleFillSymbol SymbolFill =
            new SimpleFillSymbol(
                    SimpleFillSymbol.Style.SOLID,
                    Color.argb(64, 255, 255, 0),
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
        Toast.makeText(context,info,Toast.LENGTH_SHORT).show();
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
}
