
package org.whispercomm.shout.test;

import java.util.ArrayList;

import org.whispercomm.shout.colorstorage.ShoutBorder;
import org.whispercomm.shout.provider.ColorProvider;
import org.whispercomm.shout.provider.ShoutColorContract;
import org.whispercomm.shout.provider.ShoutProvider;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ContentResolver.class)
public class ShoutContentResolver {

	ContentProvider shoutProvider;
	ContentProvider colorProvider;

	public ShoutContentResolver() {
		shoutProvider = new ShoutProvider();
		colorProvider = new ColorProvider();
		colorProvider.onCreate();
		shoutProvider.onCreate();
	}

	@Implementation
	public final Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if(uri.getAuthority().equals("org.whispercomm.shout.color.provider")){
		return colorProvider.query(uri, projection, selection, selectionArgs, sortOrder);
		}
		else{
			return shoutProvider.query(uri, projection, selection, selectionArgs, sortOrder);
		}
	}

	@Implementation
	public final Uri insert(Uri url, ContentValues values) {
		if(url.getAuthority().equals("org.whispercomm.shout.color.provider") ){
			return colorProvider.insert(url, values);
		}
		else{
			return shoutProvider.insert(url, values);
		}
	}

	@Implementation
	public ContentProviderResult[] applyBatch(String authority,
			ArrayList<ContentProviderOperation> operations) throws RemoteException,
			OperationApplicationException {
		return null;
	}

	@Implementation
	public void cancelSync(Uri uri) {
		return;
	}

	@Implementation
	public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
		return;
	}

	@Implementation
	public void notifyChange(Uri uri, ContentObserver observer) {
		return;
	}

	@Implementation
	public void startSync(Uri uri, Bundle extras) {
		return;
	}
	
	@Implementation
	public final int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs){
		return colorProvider.update(uri,values,selection,selectionArgs);
	}

}