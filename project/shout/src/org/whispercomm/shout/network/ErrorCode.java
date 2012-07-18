package org.whispercomm.shout.network;

import android.os.Parcel;
import android.os.Parcelable;

public enum ErrorCode implements Parcelable {
	SUCCESS, MANES_NOT_INSTALLED;

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