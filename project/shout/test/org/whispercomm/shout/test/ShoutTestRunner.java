
package org.whispercomm.shout.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.bytecode.ShadowMap;

public class ShoutTestRunner extends RobolectricTestRunner {

	public ShoutTestRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected ShadowMap createShadowMap() {
		return super.createShadowMap()
				.newBuilder()
				.addShadowClass(ShoutContentResolver.class)
				.addShadowClass(ShoutSQLiteQueryBuilder.class)
				.build();
	}

}
