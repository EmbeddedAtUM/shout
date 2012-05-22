package org.whispercomm.shout.provider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import java.security.interfaces.ECPublicKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutTestRunner;
import org.whispercomm.shout.User;
import org.whispercomm.shout.Utility;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderContractTest {

	private static final String NAME = "duiu";
	private static final String MESSAGE = "Can you repeat the part of the stuff where you said all about the things?";
	private static final long TIME = 8675309L;
	private static final byte[] SIGNATURE = Utility.genByteArray(10);
	private static final byte[] HASH = Utility.genByteArray(16);

	private static final int AUTHOR = 1;
	private static final int PARENT = 1;

	Uri firstUserLocation;
	Uri firstShoutLocation;

	private Context context;
	private ContentResolver cr;

	private ECPublicKey ecPubKey;
	private byte[] keyBytes;

	@Before
	public void setUp() {
		this.context = new Activity();
		this.cr = this.context.getContentResolver();

		this.ecPubKey = (ECPublicKey) Utility.genKeyPair().getPublic();
		this.keyBytes = this.ecPubKey.getEncoded();

		this.firstUserLocation = ShoutProviderTestUtility.insertIntoUserTable(
				cr, NAME, this.keyBytes);
		assertEquals(AUTHOR,
				Integer.valueOf(firstUserLocation.getLastPathSegment())
						.intValue());
		this.firstShoutLocation = ShoutProviderTestUtility
				.insertIntoShoutTable(cr, 1, -1, MESSAGE, TIME, SIGNATURE, HASH);
		assertEquals(PARENT,
				Integer.valueOf(firstShoutLocation.getLastPathSegment())
						.intValue());
	}

	@After
	public void takeDown() {
		this.cr = null;
	}

	@Test
	public void testStoreUser() {
		User fromDb = ShoutProviderContract.retrieveUserById(context, AUTHOR);
		assertNotNull(fromDb);
		assertEquals(NAME, fromDb.getUsername());
		assertEquals(this.ecPubKey, fromDb.getPublicKey());
		assertArrayEquals(this.keyBytes, fromDb.getPublicKey().getEncoded());
	}

	@Test
	public void testStoreShout() {
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, PARENT);
		assertNotNull(fromDb);
		assertNotNull(fromDb.getSender());
		assertEquals(MESSAGE, fromDb.getMessage());
		assertEquals(TIME, fromDb.getTimestamp().getMillis());
		assertArrayEquals(SIGNATURE, fromDb.getSignature());
		assertArrayEquals(HASH, fromDb.getHash());
	}
}
