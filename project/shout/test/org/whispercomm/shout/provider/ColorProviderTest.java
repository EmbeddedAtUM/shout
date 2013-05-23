package org.whispercomm.shout.provider;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.colorstorage.ShoutBorder;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.provider.ColorProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

@RunWith(ShoutTestRunner.class)
public class ColorProviderTest {

	private Context context;
	@SuppressWarnings("unused")
	private ContentResolver cr;
	private String username = "JonathanBecauseImWritingTheTest";
	private String key = "alsdkjfal;kdjfkl;asjdf";

	@Before
	public void setUp() {
		this.context = new Activity();
		this.cr = context.getContentResolver();
	}

	@After
	public void takeDown() {
		context = null;
		cr = null;
	}

	@Test
	public void testInsert() {
		ContentValues values = new ContentValues();
		ColorProvider cr = new ColorProvider();
		cr.onCreate();
		values.put(ColorProvider.ColorDatabase.KEY_USERNAME, username);
		values.put(ColorProvider.ColorDatabase.KEY_PUBLIC_KEY, key);
		values.put(ColorProvider.ColorDatabase.KEY_COLOR, Integer.valueOf("7effff", 16));
		values.put(ColorProvider.ColorDatabase.WARNING_SEEN, "false");
		assertNotNull(values);
		assertNotNull(ColorProvider.COLOR_URI);
		Uri location = cr.insert(ColorProvider.COLOR_URI, values);
		assertNotNull(location);
		assertEquals(1, ContentUris.parseId(location));

	}

	@Test
	public void testaddShoutBorder(){
		org.whispercomm.shout.crypto.ECPublicKey pubKey = TestFactory.TEST_USER_1.getPublicKey();		
		assertNotNull(pubKey);		
		ShoutBorder test = new ShoutBorder(username,pubKey,12345);		
		Uri addedUri1 = ShoutColorContract.addShoutBorder(context, test);		
		assertNotNull(addedUri1);		
		ShoutBorder returntest1 = ShoutColorContract.getShoutBorder(context,username,pubKey);		
		assertNotNull(returntest1);		
		assertEquals(test.getUsername(),returntest1.getUsername());
		assertEquals(test.getPublicKey(),returntest1.getPublicKey());
		boolean truth = false;
		assertEquals(test.getWarningStatus(), truth);
	}
	
	@Test
	public void testShoutBorderUpdate(){
		org.whispercomm.shout.crypto.ECPublicKey pubKey = TestFactory.TEST_USER_1.getPublicKey();				
		assertNotNull(pubKey);
		ShoutBorder test1 = new ShoutBorder(username,pubKey, 12345);
		ShoutBorder test2 = new ShoutBorder("test1", TestFactory.TEST_USER_2.getPublicKey(), 123456);
		ShoutBorder test3 = new ShoutBorder("test1", TestFactory.TEST_USER_3.getPublicKey(), 129308);
		Uri addedUri1 = ShoutColorContract.addShoutBorder(context, test1);
		Uri addedUri2 = ShoutColorContract.addShoutBorder(context, test2);
		Uri addedUri3 = ShoutColorContract.addShoutBorder(context, test3);
		assertNotNull(addedUri1);
		assertNotNull(addedUri2);
		assertNotNull(addedUri3);
		test1.setUsername("newUsername");
		test1.setBorderColor(11111);
		test2.setUsername("Batman");
		int rows1 = ShoutColorContract.updateShoutBorder(context,test1);
		int rows2 = ShoutColorContract.updateShoutBorder(context, test2);
		int rows3 = ShoutColorContract.updateShoutBorder(context, test3);
		assertEquals(0,rows1);
		assertEquals(0,rows2);
		assertEquals(1,rows3);
		ShoutBorder returntest1 = ShoutColorContract.getShoutBorder(context,username,pubKey);
		ShoutBorder returntest2 = ShoutColorContract.getShoutBorder(context, "test1", test2.getPublicKey());
		ShoutBorder returntest3 = ShoutColorContract.getShoutBorder(context, test3.getUsername(), test3.getPublicKey());
		assertNotNull(returntest1);
		assertNotNull(returntest2);
		assertNotNull(returntest3);
		//System.out.println("returntest2username: " + returntest2.getUsername() + " returntest3username: " + returntest3.getUsername());
		assertEquals(username,returntest1.getUsername());
		assertEquals(test1.getPublicKey(),returntest1.getPublicKey());
		boolean flag1 = false;
		boolean flag = true;
		assertEquals(returntest3.getWarningStatus(), flag);		
		assertEquals(returntest2.getWarningStatus(), flag1);
		assertEquals(returntest1.getWarningStatus(), flag1);
	}
	

	public void testcompareUsername(){
		ShoutBorder test1 = new ShoutBorder("test1", TestFactory.TEST_USER_1.getPublicKey(), 872657);
		ShoutBorder test2 = new ShoutBorder("test1", TestFactory.TEST_USER_2.getPublicKey(), 123456);
		ShoutBorder test3 = new ShoutBorder("test1", TestFactory.TEST_USER_3.getPublicKey(), 129308);
		ShoutColorContract.addShoutBorder(context,test1);
		ShoutColorContract.addShoutBorder(context,test2);
		ShoutColorContract.addShoutBorder(context,test3);
		int[] colorsInUse = ShoutColorContract.compareUsernames(context,"test1");
		assertEquals(872657,colorsInUse[0]);
		assertEquals(123456, colorsInUse[1]);
		assertEquals(129308, colorsInUse[2]);
		
	}
	@Test
	public void testDoesPublicKeyUsernamePairExist(){
		ShoutBorder test1 = new ShoutBorder("test1", TestFactory.TEST_USER_1.getPublicKey(), 872657);
		ShoutBorder test2 = new ShoutBorder("test1", TestFactory.TEST_USER_2.getPublicKey(), 123456);
		ShoutBorder test3 = new ShoutBorder("test1", TestFactory.TEST_USER_3.getPublicKey(), 129308);
		ShoutColorContract.addShoutBorder(context, test1);
		ShoutColorContract.addShoutBorder(context,test2);
		ShoutColorContract.addShoutBorder(context,test3);
		boolean flag = ShoutColorContract.doesPublicKeyUsernamePairExist(context,test1.getUsername(), TestFactory.TEST_USER_1.getPublicKey());
		boolean flag1 = ShoutColorContract.doesPublicKeyUsernamePairExist(context,test2.getUsername(), TestFactory.TEST_USER_2.getPublicKey());
		boolean flag2 = ShoutColorContract.doesPublicKeyUsernamePairExist(context, test3.getUsername(), TestFactory.TEST_USER_3.getPublicKey());
		assertEquals(true,flag);
		assertEquals(true, flag1);
		assertEquals(true, flag2);	
	}
	
	@Test
	public void testSaveShoutBorder(){
		org.whispercomm.shout.crypto.ECPublicKey pubKey = TestFactory.TEST_USER_1.getPublicKey();		
		org.whispercomm.shout.crypto.ECPublicKey pubKey1 = TestFactory.TEST_USER_2.getPublicKey();
		org.whispercomm.shout.crypto.ECPublicKey pubKey2 = TestFactory.TEST_USER_3.getPublicKey();
		assertNotNull(pubKey1);
		ShoutColorContract.saveShoutBorder(context,TestFactory.TEST_USER_1);
		ShoutColorContract.saveShoutBorder(context,TestFactory.TEST_USER_2);
		ShoutColorContract.saveShoutBorder(context,TestFactory.TEST_USER_3);
		assertNotNull(pubKey2);
		ShoutBorder test1 = new ShoutBorder("Jonathan", pubKey1, -11234);
		ShoutBorder test2 = new ShoutBorder("Batman", pubKey2, -98776);
		ShoutBorder test3 = new ShoutBorder("Jonathan", pubKey, -12390);
		ShoutBorder test4 = new ShoutBorder(username, pubKey1, -11234);
		Uri addedUri1 = ShoutColorContract.addShoutBorder(context, test1);
		Uri addedUri2 = ShoutColorContract.addShoutBorder(context, test2);
		Uri addedUri3 = ShoutColorContract.addShoutBorder(context, test3);
		Uri addedUri4 = ShoutColorContract.addShoutBorder(context, test4);
		assertNotNull(addedUri1);
		assertNotNull(addedUri2);
		assertNotNull(addedUri3);
		assertNotNull(addedUri4);
		ShoutBorder returntest1 = ShoutColorContract.getShoutBorder(context,"Jonathan", pubKey1);
		ShoutBorder returntest2 = ShoutColorContract.getShoutBorder(context, "Batman", pubKey2);
		ShoutBorder returntest3 = ShoutColorContract.getShoutBorder(context, "Jonathan", pubKey);
		ShoutBorder returntest4 = ShoutColorContract.getShoutBorder(context, username, pubKey1);
		assertNotNull(returntest1);
		assertNotNull(returntest2);
		assertNotNull(returntest3);
		assertNotNull(returntest4);
	}
	
}
