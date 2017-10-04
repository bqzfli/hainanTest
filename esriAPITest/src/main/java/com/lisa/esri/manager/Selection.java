package com.lisa.esri.manager;

import com.example.hnTest.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by WANT on 2017/10/4.
 * 操作图层的相关信息
 */
public class Selection {

    /**
     * 查询结果
     */
    public static List<Map<String,Object>> SearchResultFromOperationLayer = new ArrayList<>();

    public static String getSearchResultFromOperationLayer(){
        String strResult ="";
        if(SearchResultFromOperationLayer==null||SearchResultFromOperationLayer.size()==0){
            strResult +="无";
            return strResult;
        }else{
            for(int i=0;i<SearchResultFromOperationLayer.size();i++){
                Map<String,Object> result = SearchResultFromOperationLayer.get(i);
                String strReuslt = "id:'"+String.valueOf(i)+"',";
                strResult += strReuslt;
                strResult += "Layer:'"+result.get(Util.LAYERNAME)+"',";

                Iterator<String> keys = result.keySet().iterator();
                int sizeKeys = result.size()>5?5:result.size();
                int j=0;
                while (keys.hasNext()&&j<sizeKeys){
                    String key = keys.next();
                    if(result.get(key)!=null) {
                        String value = String.valueOf(result.get(key));
                        strResult += key + ":'" + value + "',";
                        j+=1;
                    }
                }
                strResult +="\n";
            }
        }
        return strResult;
    }
}
