
package org.whispercomm.shout.tasks;

import java.io.IOException;

import org.joda.time.DateTime;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.NotRegisteredException;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.network.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Asynchronously construct and broadcast the reshout, informing the user of
 * success or failure via a Toast.
 * 
 * @author David Adrian
 */
public class ReshoutTask extends AsyncTask<LocalShout, Void, SendResult> {
	private final static String TAG = ReshoutTask.class.getSimpleName();

	private Context context;
	private NetworkInterface network;
	private Me me;

	public ReshoutTask(Context context, NetworkInterface network, Me me) {
		this.context = context;
		this.network = network;
		this.me = me;
	}

	@Override
	protected SendResult doInBackground(LocalShout... shouts) {
		Shout shout = shouts[0];

		Shout parent;
		ShoutType type = ShoutMessageUtility.getShoutType(shout);
		switch (type) {
			case RESHOUT:
			case RECOMMENT:
				parent = shout.getParent();
				break;
			default:
				parent = shout;
				break;
		}

		ShoutCreator creator = new ShoutCreator(context);
		LocalShout reshout = creator.createReshout(DateTime.now(), parent, me);
		return SendResult.encapsulateSend(network, reshout);
	}

	@Override
	protected void onPostExecute(SendResult result) {
		try {
			result.getResultOrThrow();
			Toast.makeText(context, R.string.reshoutSuccess, Toast.LENGTH_SHORT)
					.show();
		} catch (NotConnectedException e) {
			Toast.makeText(context, R.string.reshoutFail, Toast.LENGTH_LONG)
					.show();
		} catch (ShoutChainTooLongException e) {
			Toast.makeText(context, R.string.reshoutFail, Toast.LENGTH_LONG)
					.show();
			Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
		} catch (ManesNotInstalledException e) {
			Toast.makeText(context, "Send failed.  Please install MANES client.",
					Toast.LENGTH_LONG).show();
		} catch (NotRegisteredException e) {
			Toast.makeText(context,
					"Send failed.  Please register with MANES client.",
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(context, R.string.reshoutFail, Toast.LENGTH_LONG)
					.show();
		}
	}

}
