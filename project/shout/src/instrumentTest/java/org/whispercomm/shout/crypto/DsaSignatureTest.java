
package org.whispercomm.shout.crypto;

import java.math.BigInteger;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class DsaSignatureTest {

	@Test
	public void testEncode() {
		BigInteger r = new BigInteger(1,
				Hex.decode("7214bc9647160bbd39ff2f80533f5dc6ddd70ddf86bb815661e805d5d4e6f27c"));
		BigInteger s = new BigInteger(1,
				Hex.decode("7d1ff961980f961bdaa3233b6209f4013317d3e3f9e1493592dbeaa1af2bc367"));
		DsaSignature sig = new DsaSignature(r, s);

		byte[] encoded = Hex
				.decode("207214bc9647160bbd39ff2f80533f5dc6ddd70ddf86bb815661e805d5d4e6f27c7d1ff961980f961bdaa3233b6209f4013317d3e3f9e1493592dbeaa1af2bc367");

		assertThat(DsaSignature.encode(sig), is(encoded));
	}

	@Test
	public void testDecode() {
		byte[] encoded = Hex
				.decode("21007214bc9647160bbd39ff2f80533f5dc6ddd70ddf86bb815661e805d5d4e6f27c007d1ff961980f961bdaa3233b6209f4013317d3e3f9e1493592dbeaa1af2bc367");

		BigInteger r = new BigInteger(1,
				Hex.decode("007214bc9647160bbd39ff2f80533f5dc6ddd70ddf86bb815661e805d5d4e6f27c"));
		BigInteger s = new BigInteger(1,
				Hex.decode("007d1ff961980f961bdaa3233b6209f4013317d3e3f9e1493592dbeaa1af2bc367"));
		DsaSignature sig = new DsaSignature(r, s);

		assertThat(DsaSignature.decode(encoded), is(sig));
	}

}
