package com.lisa.esri.method;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.LicenseInfo;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.security.UserCredential;

/**
 * Created by WANT on 2017/10/26.
 */

public class Authorization {
    /**
     * 授权
     */
    public void initAuthenticate(){
        // connect to ArcGIS Online or an ArcGIS portal as a named user
        // The code below shows the use of token based security but
        // for ArcGIS Online you may consider using Oauth authentication.
        String strKey = "runtimelite,1000,rud7032709275,none,C6JC7XLS1M0H8YAJM150";
        ArcGISRuntimeEnvironment.setLicense(strKey);
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
}
