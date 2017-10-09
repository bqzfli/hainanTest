package com.example.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.example.hnTest.R;
import com.example.hnTest.Util;
import com.lisa.esri.adapter.CoverFlowAdapter;
import com.lisa.esri.manager.Selection;
import com.lisa.esri.method.EsriMethod;
import com.lisa.esri.method.EsriMethod.OnTouchMapEvent;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;

import static com.example.hnTest.R.id.mapView;

public class esriApiTest extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner mSpin;
    private MapView mMapView;
    private EsriMethod mEsriMethod = new EsriMethod();
    private SearchView mSearchView = null;
    private FeatureCoverFlow mFeatureCoverFlow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //授权app
        //mEsriMethod.initAuthenticate(getResources().getString(R.string.portal_arcgisonline),"用户名","密码");

        //界面设置
        setContentView(R.layout.activity_esri_api_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "查询结果";//"Replace with your own action";
                String info =  "Action";
                if(Selection.SearchResultFromOperationLayer!=null&&Selection.SearchResultFromOperationLayer.size()!=0){
                    CoverFlowAdapter adapter = new CoverFlowAdapter(esriApiTest.this);
                    adapter.setData(Selection.SearchResultFromOperationLayer);
                    mFeatureCoverFlow.setAdapter(adapter);
                    mFeatureCoverFlow.setVisibility(View.VISIBLE);
                }else {
                    info = Selection.getSearchResultFromOperationLayer();
                    Snackbar.make(view, title, Snackbar.LENGTH_INDEFINITE)
                            .setAction(info, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    return;
                                }
                            }).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // 获取地图控件
        mMapView = (MapView) findViewById(mapView);
        // 获取设备位置显示控制的控件
        mSpin = (Spinner) findViewById(R.id.spinner);
        // 获取查找结果的属性显示
        mFeatureCoverFlow = (FeatureCoverFlow)findViewById(R.id.coverflow);
        mFeatureCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //提取选中要素
                Map<String,Object> result = Selection.SearchResultFromOperationLayer.get(position);
                String strGeo = (String)result.get(Util.GEOJSON);
                Geometry geo = Geometry.fromJson(strGeo);
                Envelope envelope = geo.getExtent();
                final ListenableFuture<Boolean> viewpointSetFuture = mMapView.setViewpointAsync(new Viewpoint(envelope), 2);
                mFeatureCoverFlow.setVisibility(View.GONE);
            }
        });

        //设置基础底图
        //天地图底图
        //mEsriMethod.initMap(mMapView,getResources().getString(R.string..world_TDT_service));
        //世界影像底图
        mEsriMethod.iniBaseMap(this,mMapView,getResources().getString(R.string.world_imagery_service));

        //设置GraphicOverlay
        mEsriMethod.initGraphicOverlay(mMapView);

        //设置操作图层
        mEsriMethod.initOperatinalLayer(this,mMapView);

        //设置地图identify事件
        mEsriMethod.initIdentifyOperation(this,mMapView,mEsriMethod.getOperationalLayers()/*mFeatureLayer*/,new OnTouchMapEvent(){
            @Override
            public void refreshViewOnStartSearch(MotionEvent e) {
                mFeatureCoverFlow.setVisibility(View.GONE);
            }
        });

        //设置显示当前位置，第一次加载界面时就显示
        //initLocationDisplay(mMapView);
        //设置显示设备位置的相关操作；默认不显示当前位置，需要用户手动切换
        mEsriMethod.initLocationOperation(this,mMapView,mSpin);

        //设置地图显示范围
        mEsriMethod.changeViewPoint(mMapView);

        //设置地图渲染状态
        mEsriMethod.initDrawStatusChanged(mMapView,progressBar);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.esri_api_test, menu);
        
        final MenuItem item = menu.findItem(R.id.action_search);
    
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView.setIconifiedByDefault(false);
        SearchView.SearchAutoComplete edit = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        String value = "u";
        edit.setText(value);
        edit.setSelection(value.length());
        final String strQueryField = getResources().getString(R.string.query_field);
        mSearchView.setQueryHint("输入查找对象的“"+strQueryField+"”");
    
        final LinearLayout search_edit_frame = (LinearLayout) mSearchView.findViewById(R.id.search_edit_frame);
        search_edit_frame.setClickable(true);
    
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                search_edit_frame.setPressed(hasFocus);
            }
        });
    
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_edit_frame.setPressed(true);
            }
        });
    
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             /*判断是否是“GO”键*/
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //查询结果列表关闭
                    mFeatureCoverFlow.setVisibility(View.GONE);
                    /*隐藏软键盘*/
                    mSearchView.clearFocus();
                    search_edit_frame.setPressed(false);
                    String value = v.getText().toString();
                    mEsriMethod.initSearchByField(esriApiTest.this,mMapView,strQueryField, value);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
    
    
    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_screencapture){
            Date now =new Date();
            String time = now.toString();
            mEsriMethod.exportScreenCapture(this,mMapView,time);
        }

        if (id == R.id.item_world_imagery) {
            mEsriMethod.switchBaseMap(mMapView,getResources().getString(R.string.world_imagery_service));
        } else if (id == R.id.item_world_cities) {
            mEsriMethod.switchBaseMap(mMapView,getResources().getString(R.string.world_cities_service));
        } else if (id == R.id.item_world_streets) {
            mEsriMethod.switchBaseMap(mMapView,getResources().getString(R.string.world_street_service));
        } else if (id == R.id.item_world_physical) {
            mEsriMethod.switchBaseMap(mMapView,getResources().getString(R.string.world_physical_service));
        } else if (id == R.id.item_world_elevation) {
            mEsriMethod.switchBaseMap(mMapView,getResources().getString(R.string.world_elevation_service));
        } else if (id == R.id.item_world_shaded) {
            mEsriMethod.switchBaseMap(mMapView,getResources().getString(R.string.world_shaded_service));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
