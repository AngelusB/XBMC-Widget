package com.anderspersson.xbmcwidget.recentvideo;

import java.util.Date;

import com.anderspersson.xbmcwidget.common.ITimerCallback;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class VideoUpdaterService implements ITimerCallback {
	     
    protected Context _ctx;

	protected abstract Intent createRecentVideoRefreshIntent(boolean isSuccess);
    protected abstract boolean tryRefreshData();
    
    public VideoUpdaterService(Context ctx) {
    	this._ctx = ctx;
    }
    
	public void performUpdate() {
		if(isConnectedToWifi() == false) {
			_ctx.startService(createRecentVideoRefreshIntent(false));
			return;
		}
		
		boolean succeeded = tryRefreshData();

		if(succeeded == false) {
			_ctx.startService(createRecentVideoRefreshIntent(false));
			return;
		}
		
		setLastUpdateTime();
		
		_ctx.startService(createRecentVideoRefreshIntent(true));
	}


	private void setLastUpdateTime() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
		Editor editor = prefs.edit();
		editor.putString("recent_video_last_refresh", new Date().getTime() + "");
		editor.commit();
	}

	private boolean isConnectedToWifi() {
		
		try {
			if(android.os.Debug.isDebuggerConnected()) {
				return true;
			}
		
			ConnectivityManager connManager = (ConnectivityManager) _ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return wifi != null && wifi.isAvailable() && wifi.isConnected();
		}
		catch(Exception ex) {
			Log.w(
				this.getClass().getSimpleName(), 
				"Unable to determine WIFI state - widget cannot be refreshed.");
			return false;
		}
	}
}