
package org.whispercomm.shout.test;

import java.util.ArrayList;

import org.whispercomm.shout.provider.ShoutProvider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
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

	ContentProvider provider;

	public ShoutContentResolver() {
		provider = new ShoutProvider();
		provider.onCreate();
	}

	@Implementation
	public final Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return provider.query(uri, projection, selection, selectionArgs, sortOrder);
	}

	@Implementation
	public final Uri insert(Uri url, ContentValues values) {
		return provider.insert(url, values);
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

}
