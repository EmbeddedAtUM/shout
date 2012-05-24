
package org.whispercomm.shout.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShoutBase64Test {

	private byte[] arrRandom;
	private byte[] hardcoded = {
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a
	};
	private String encoded = "1.2.3.4.5.6.7.8.9.10.";
	
	private Random rand = new Random();

	@Before
	public void setUp() {
		this.arrRandom = new byte[16];
		rand.nextBytes(arrRandom);
	}

	@After
	public void takeDown() {
		this.arrRandom = null;
	}

	@Test
	public void testEncode() {
		String str = ShoutBase64.encodeToString(hardcoded, 0);
		assertEquals(str, this.encoded);
	}
	
	@Test
	public void testDecodeOfEncodeIsOriginal() {
		String output = ShoutBase64.encodeToString(arrRandom, 0);
		byte[] revert = ShoutBase64.decode(output, 0);
		assertArrayEquals(revert, this.arrRandom);
	}

}
