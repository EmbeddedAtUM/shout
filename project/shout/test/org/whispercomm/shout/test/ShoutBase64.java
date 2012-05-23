
package org.whispercomm.shout.test;

import android.util.Base64;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Base64.class)
public class ShoutBase64 {

	@Implementation
	public static String encodeToString(byte[] input, int flags) {
		if (input == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < input.length; i++) {
			builder.append(Byte.toString(input[i]));
			builder.append('.');
		}
		return builder.toString();
	}

	@Implementation
	public static byte[] decode(String str, int flags) {
		if (str == null) {
			return null;
		}
		String[] values = str.split("\\.");
		byte[] output = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			output[i] = Byte.valueOf(values[i]);
		}
		return output;
	}

}
