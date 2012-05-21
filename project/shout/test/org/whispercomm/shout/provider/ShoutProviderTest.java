package org.whispercomm.shout.provider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.ECPublicKey;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutTestRunner;
import org.whispercomm.shout.SimpleShout;
import org.whispercomm.shout.SimpleUser;
import org.whispercomm.shout.User;
import org.whispercomm.shout.Utility;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderTest {

	private static Context context;
	private static User userA;
	private static User userB;
	
	private static Shout shoutA;
	
	private static int id;
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	@Before
	public void setUp() {
		context = new Activity();
		try {
			userA = new SimpleUser("userA", (ECPublicKey) Utility.genKeyPair().getPublic());
			userB = new SimpleUser("userB", (ECPublicKey) Utility.genKeyPair().getPublic());
			shoutA = new SimpleShout(new DateTime(), userA, "contentA", null, new String("signature").getBytes("US-ASCII"));
			return;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fail("Exception thrown dumbass");
	}
	
	@Test
	public void testQueryNoParameters() {
		id = ShoutProviderContract.storeUser(context, userA);
		User inDb = ShoutProviderContract.retrieveUserById(context, id);
		assertEquals(inDb.getUsername(), "userA");
	}
}
