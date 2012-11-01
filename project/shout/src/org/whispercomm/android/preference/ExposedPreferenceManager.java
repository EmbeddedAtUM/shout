
package org.whispercomm.android.preference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.media.RingtoneManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.preference.PreferenceManager.OnActivityStopListener;

/**
 * Provides access to the package-private methods of {@link PreferenceManager}
 * needed to start an {@code Activity} for a result from a {@code Preference}.
 * <p>
 * Access to the {@link Activity} containing a {@link Preference} is necessary
 * when the preference must start an {@code Activity} to chose the new
 * preference value, for example, selecting an image from the gallery. In fact,
 * the built-in {@link RingtonePreference} uses this scheme.
 * <p>
 * {@code PreferenceManager} has methods to support this functionality, but they
 * are unfortunately scoped to package-private. This wrapper provides access to
 * those methods using reflection. To access such a method, simply wrap the
 * {@code PreferenceManager} and call the intended method.
 * <p>
 * <code>
 * ExposedPreferenceManager.expose(preferenceManager).registerOnActivityResultListener();
 * </code>
 * <p>
 * N.B., the methods exposed by the wrapper are not part of the public API and
 * could break in a future version of the Android API.
 * 
 * @see PreferenceManager
 * @see RingtoneManager
 * @author David R. Bild
 */

public final class ExposedPreferenceManager {

	public static ExposedPreferenceManager expose(PreferenceManager preferenceManager) {
		return new ExposedPreferenceManager(preferenceManager);
	}

	private final PreferenceManager mPreferenceManager;
	private final Class<? extends PreferenceManager> mClazz;

	public ExposedPreferenceManager(PreferenceManager preferenceManager) {
		mPreferenceManager = preferenceManager;
		mClazz = preferenceManager.getClass();
	}

	/**
	 * Gets the {@link PreferenceManager} exposed by this object.
	 * 
	 * @return the exposed preference manager
	 */
	public PreferenceManager getPreferenceManager() {
		return mPreferenceManager;
	}

	/**
	 * Returns the activity that shows the preferences. This is useful for doing
	 * managed queries, but in most cases the use of {@link #getContext()} is
	 * preferred.
	 * <p>
	 * This will return null if this class was instantiated with a Context
	 * instead of Activity. For example, when setting the default values.
	 * <p>
	 * Documentation copied from the {@code PreferenceManager#getActivity()}
	 * documentation.
	 * 
	 * @return The activity that shows the preferences.
	 * @see #mContext
	 */
	public Activity getActivity() {
		return invokeMethod("getActivity", null, null);
	}

	/**
	 * Registers a listener.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#registerOnActivityResultListener(OnActivityResultListener)}
	 * documentation.
	 * 
	 * @see OnActivityResultListener
	 */
	void registerOnActivityResultListener(OnActivityResultListener listener) {
		invokeMethod("registerOnActivityResultListener", new Class<?>[] {
				OnActivityResultListener.class
		}, new Object[] {
				listener
		});
	}

	/**
	 * Unregisters a listener.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#unregisterOnActivityResultListener(OnActivityResultListener)}
	 * documentation.
	 * 
	 * @see OnActivityResultListener
	 */
	public void unregisterOnActivityResultListener(OnActivityResultListener listener) {
		invokeMethod("unregisterOnActivityResultListener", new Class<?>[] {
				OnActivityResultListener.class
		}, new Object[] {
				listener
		});
	}

	/**
	 * Registers a listener.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#registerOnActivityStopListener(OnActivityStopListener)}
	 * documentation.
	 * 
	 * @see OnActivityStopListener
	 */
	public void registerOnActivityStopListener(OnActivityStopListener listener) {
		invokeMethod("registerOnActivityStopListener", new Class<?>[] {
				OnActivityStopListener.class
		}, new Object[] {
				listener
		});
	}

	/**
	 * Unregisters a listener.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#unregisterOnActivityStopListener(OnActivityStopListener)}
	 * documentation.
	 * 
	 * @see OnActivityStopListener
	 */
	public void unregisterOnActivityStopListener(OnActivityStopListener listener) {
		invokeMethod("unregisterOnActivityStopListener", new Class<?>[] {
				OnActivityStopListener.class
		}, new Object[] {
				listener
		});
	}

	/**
	 * Registers a listener.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#registerOnActivityDestroyListener(OnActivityDestroyListener)}
	 * documentation.
	 * 
	 * @see OnActivityDestroyListener
	 */
	public void registerOnActivityDestroyListener(OnActivityDestroyListener listener) {
		invokeMethod("registerOnActivityDestroyListener", new Class<?>[] {
				OnActivityDestroyListener.class
		}, new Object[] {
				listener
		});
	}

	/**
	 * Unregisters a listener.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#unregisterOnActivityDestroyListener(OnActivityDestroyListener)}
	 * documentation.
	 * 
	 * @see OnActivityDestroyListener
	 */
	public void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
		invokeMethod("unregisterOnActivityDestroyListener", new Class<?>[] {
				OnActivityDestroyListener.class
		}, new Object[] {
				listener
		});
	}

	/**
	 * Returns a request code that is unique for the activity. Each subsequent
	 * call to this method should return another unique request code.
	 * <p>
	 * Documentation copied from the
	 * {@code PreferenceManager#getNextRequestCode()} documentation.
	 * 
	 * @return A unique request code that will never be used by anyone other
	 *         than the caller of this method.
	 */
	public int getNextRequestCode() {
		return invokeMethod("getNextRequestCode", null, null);
	}

	@SuppressWarnings("unchecked")
	private <T> T invokeMethod(String name, Class<?>[] parameterTypes, Object[] args) {
		Method method;
		try {
			method = mClazz.getDeclaredMethod(name, parameterTypes);
			if (!method.isAccessible())
				method.setAccessible(true); // This can fail if a security
											// manager is in place.
			return (T) method.invoke(mPreferenceManager, args);
		} catch (SecurityException e) {
			throw invokationException(name, e);
		} catch (NoSuchMethodException e) {
			throw invokationException(name, e);
		} catch (IllegalArgumentException e) {
			throw invokationException(name, e);
		} catch (IllegalAccessException e) {
			throw invokationException(name, e);
		} catch (InvocationTargetException e) {
			throw invokationException(name, e);
		}

	}

	private RuntimeException invokationException(String methodName, Exception e) {
		return new RuntimeException(String.format(
				"Error invoking exposed method %s#%s via reflection",
				mClazz.getCanonicalName(), methodName), e);
	}

}
