
package com.example.packagepermissionlister;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        StringBuffer keyboardAppNames = new StringBuffer();
        
        StringBuffer appNameServicesAndPermissions = new StringBuffer();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            Log.d("test", "App: " + applicationInfo.name + " Package: " + applicationInfo.packageName);

            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS | PackageManager.GET_SERVICES);
                appNameServicesAndPermissions.append(packageInfo.packageName+"*:\n");

                //Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if(requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        Log.d("test", requestedPermissions[i]);
                        appNameServicesAndPermissions.append(requestedPermissions[i]+"\n");
                    }
                    

                }
                
                // Get services
                ServiceInfo[] services = packageInfo.services;
                
                if(services != null) {
                    for(ServiceInfo si : services) {
                        appNameServicesAndPermissions.append("Service name: "+si.name+"\n");
                        appNameServicesAndPermissions.append("permission: "+si.permission+"\n");
                        Log.d("test", "Service name: "+si.name);
                        Log.d("test", "permission: "+si.permission);
                        if((si.permission != null) && (si.permission.equals("android.permission.BIND_INPUT_METHOD"))){
//                            keyboardAppNames.append(si.name.split("\\.")[si.name.split("\\.").length-1] + "\n");
//                            String[] tmp = si.name.split("\\.");
                            keyboardAppNames.append(si.name + "\n");
                        }
                    }
                }else {
                    appNameServicesAndPermissions.append("NO services\n");
                    Log.d("test", "NO services");
                }
                
                appNameServicesAndPermissions.append("\n");

            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        TextView tv = (TextView) findViewById(R.id.tv_log_vindow);
        tv.setText("Keyboard Apps:\n"+ keyboardAppNames.toString() + "\n ALL packages: \n\n"+ appNameServicesAndPermissions.toString());
        
    }


}
