
package org.whispercomm.shout.provider;

import org.whispercomm.shout.LocalShout;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable wrapper around a {@code LocalShout}. This class stores the hash of
 * the shout for parceling and retrieves the shout from the content provider for
 * unparceling.
 * 
 * @author David R. Bild
 */
public class ParcelableShout implements Parcelable {

	private byte[] hash;

	public ParcelableShout(LocalShout shout) {
		this.hash = shout.getHash();
	}

	private ParcelableShout(byte[] hash) {
		this.hash = hash;
	}

	/**
	 * Retrieve the wrapped shout from the content provider.
	 * 
	 * @param context the {@code Context} used to access the content provider
	 * @return the LocalShout or {@code null} if not in the content provider
	 */
	public LocalShout getShout(Context context) {
		return ShoutProviderContract.retrieveShoutByHash(context, hash);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByteArray(hash);
	}

	public static final Parcelable.Creator<ParcelableShout> CREATOR = new Parcelable.Creator<ParcelableShout>() {
		public ParcelableShout createFromParcel(Parcel in) {
			return new ParcelableShout(in.createByteArray());
		}

		public ParcelableShout[] newArray(int size) {
			return new ParcelableShout[size];
		}
	};

}
