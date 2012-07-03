
package org.whispercomm.shout.test;

import java.io.File;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.util.SQLiteMap;

public class ShoutTestRunner extends RobolectricTestRunner {

	public ShoutTestRunner(Class<?> testClass) throws InitializationError {
		super(testClass, new RobolectricConfig(new File(".")), new SQLiteMap());
	}

	@Override
	protected void bindShadowClasses() {
		Robolectric.bindShadowClass(ShoutBase64.class);
		Robolectric.bindShadowClass(ShoutContentResolver.class);
		Robolectric.bindShadowClass(ShoutSQLiteQueryBuilder.class);
	}

}
