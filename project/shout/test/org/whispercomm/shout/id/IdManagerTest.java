
package org.whispercomm.shout.id;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.test.ShoutTestRunner;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class IdManagerTest {

	private static final String USERNAME_EXCEPTION_FAIL = "Username is valid";
	private static final String INIT_EXCEPTION_FAIL = "UserNotInitiated exception thrown incorrectly";
	private static final String USERNAME = "catherine";
	private Context context;
	private IdManager idManager;

	@Before
	public void setUp() {
		this.context = new Activity();
		idManager = new IdManager(context);
	}

	@After
	public void takeDown() {
		this.context = null;
		this.idManager = null;
	}

	@Test
	public void testResetUserFirstTime() {
		try {
			idManager.resetUser(USERNAME);
			Me me = idManager.getMe();
			assertNotNull(me);
			assertNotNull(me.getPrivateKey());
			assertNotNull(me.getPublicKey());
			assertEquals(USERNAME, me.getUsername());
		} catch (UserNotInitiatedException e) {
			fail(INIT_EXCEPTION_FAIL);
		} catch (UserNameInvalidException e) {
			fail(USERNAME_EXCEPTION_FAIL);
		}
	}

	@Test
	public void testResetUserOverwritesOldUser() {
		try {
			String newUsername = "dadrian";
			idManager.resetUser(USERNAME);
			Me first = idManager.getMe();
			idManager.resetUser(newUsername);
			Me second = idManager.getMe();
			assertThat(first.getUsername(), is(not(second.getUsername())));
			assertThat(first.getPrivateKey(), is(not(second.getPrivateKey())));
			assertThat(first.getPublicKey(), is(not(second.getPublicKey())));
		} catch (UserNotInitiatedException e) {
			e.printStackTrace();
			fail(INIT_EXCEPTION_FAIL);
		} catch (UserNameInvalidException e) {
			e.printStackTrace();
			fail(USERNAME_EXCEPTION_FAIL);
		}
	}

	@Test
	public void testThrowExceptionOnGetUnsetUser() {
		try {
			idManager.getMe();
		} catch (UserNotInitiatedException e) {
			return;
		}
		fail("Exception not thrown");
	}
}
