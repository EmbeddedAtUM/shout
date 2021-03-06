
package org.whispercomm.shout.network.service;

import android.os.Parcel;
import android.os.Parcelable;

enum ErrorCode implements Parcelable {
	SUCCESS, SHOUT_CHAIN_TOO_LONG, MANES_NOT_INSTALLED, MANES_NOT_REGISTERED, IO_ERROR;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name());
	}

	public static final Parcelable.Creator<ErrorCode> CREATOR = new Parcelable.Creator<ErrorCode>() {

		@Override
		public ErrorCode createFromParcel(Parcel source) {
			return ErrorCode.valueOf(source.readString());
		}

		@Override
		public ErrorCode[] newArray(int size) {
			return new ErrorCode[size];
		}

	};
}
