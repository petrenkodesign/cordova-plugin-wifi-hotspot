package com.petrenkodesign.cordova.plugins.wifihotspot;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.widget.Toast;
import android.util.Log;

import android.os.Build;
import android.provider.Settings;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;

import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.lang.reflect.Method;


public class WifiHotspot extends CordovaPlugin {

    private static final String LOG_H = "[WifiHotSpot]";
    private static String SSID = "HOT";
    private static String PASSWORD = "";
    private static boolean SEC = false;
    private static boolean HIDDEN = false;


    @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

            if(action.equals("setup")){
                SSID = args.getString(0);
                PASSWORD = args.getString(1);
                SEC = args.getBoolean(2);
                HIDDEN = args.getBoolean(3);

                callbackContext.success("Setup done");
                return true;
            }

            /*Shows Toast*/
            if(action.equals("say_hello")){
                this.sayToast(args.getString(0), callbackContext);
                return true;
            }

            if (action.equals("isApOn")) {
                Context context = this.cordova.getActivity().getApplicationContext();
                this.isApOn(context);

                callbackContext.success("Call isApOn function");
                return true;
            }

            if (action.equals("configApState")) {
                Context context = this.cordova.getActivity().getApplicationContext();
                boolean res = this.configApState(context);
                if(res) {
                  this.sayToast("HotSpot started", callbackContext);
                  callbackContext.success("Call configApState function");
                  return true;
                }
                else {
                  callbackContext.error("Call configApState with error");
                  return false;
                }

            }

            if (action.equals("chekPermission")) {
                Context context = this.cordova.getActivity().getApplicationContext();
                this.chekPermission(context);

                callbackContext.success("Call writePermission function");
                return true;
            }

            callbackContext.success("Sync. No execution action");
            return true;
        }


    private void sayToast (final String msg, CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
        callbackContext.success("Showing toast");
    }


    private static boolean isApOn(Context context) {
        Log.v(LOG_H, "isApOn function");
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }

    private static boolean configApState(Context context) {
        Log.v(LOG_H, "configApState function");
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = getWifiApConfiguration();
        Log.v(LOG_H, "wificonfiguration=" + wificonfiguration);
        try {
            boolean APstatus = isApOn(context);
            // if WiFi is on, turn it off
            Log.v(LOG_H, "isAPon - " + APstatus);
            if(APstatus) {
                wifimanager.setWifiEnabled(false);
            }

            wifimanager.addNetwork(wificonfiguration);

            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(wifimanager, wificonfiguration, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static WifiConfiguration getWifiApConfiguration() {
      WifiConfiguration conf = new WifiConfiguration();
      conf.SSID =  SSID;
      conf.preSharedKey  = PASSWORD;

      conf.allowedKeyManagement.set(4);
      conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

      // conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
      // conf.hiddenSSID = false;
      // conf.status = WifiConfiguration.Status.ENABLED;
      // conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
      // conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
      // conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
      // conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
      // conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

      // conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

      return conf;
    }

    private void chekPermission(Context context) {
        Log.v(LOG_H, "chekPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //implode here popup for ask user
            askUser("Please configure turn on HotSpot with data<br>Name: " + SSID + "<br>Password: " + PASSWORD + "<br><b>Otherwise you can`t start job!<b>", "Attention!", "OK", false);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // chek if build SDK greater than or equal to Android 6 Marshmallow
          if (!Settings.System.canWrite(context)) {
            Log.v(LOG_H, "i don`t have write permissions");
            askUser("You must provide permission to continue", "Attention!", "OK", true);
          }
        }
    }

    public void switchToWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cordova.getActivity().startActivity(intent);
    }

    public void switchToWifiSettings() {
        Log.v(LOG_H, "Switch to Wifi Settings");
        Intent settingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        cordova.getActivity().startActivity(settingsIntent);
    }

    public synchronized void askUser(final String msg, final String title, final String positiveButton, Boolean type) {

    	final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() { // background processing, for procesing optimisation
            public void run() {

                Builder dlog = createDialog(cordova); // new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                dlog.setMessage(msg);
                dlog.setTitle(title);
                dlog.setCancelable(true);
                dlog.setPositiveButton(positiveButton,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (type) {
                                  switchToWriteSettings();
                                } else {
                                  switchToWifiSettings();
                                }
                                // callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                            }
                        });
                dlog.setOnCancelListener(new AlertDialog.OnCancelListener() {
                    public void onCancel(DialogInterface dialog)
                    {
                        dialog.dismiss();
                        // callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                    }
                });

                dlog.create();
                dlog.show();
            };
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    private Builder createDialog(CordovaInterface cordova) { // define the method using by SDK
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            return new Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            return new Builder(cordova.getActivity());
        }
    }


}
