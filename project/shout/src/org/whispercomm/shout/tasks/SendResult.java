
package org.whispercomm.shout.tasks;

import java.io.IOException;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.network.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;

/**
 * A class encapsulating the result of a
 * {@link NetworkInterface#send(LocalShout)} method call, providing type safety
 * for the thrown exceptions.
 * <p>
 * This class must be updated when the type-signature of
 * {@code NetworkInterface#send(LocalShout)} changes.
 * 
 * @author David R. Bild
 */
public class SendResult extends MaybeResult<Void> {

	/**
	 * A factory method to encapsulate the execution of a
	 * {@link NetworkInterface#send(LocalShout)} method call, providing type
	 * safety for the thrown exceptions.
	 * <p>
	 * By executing the {@code send()} in this method, if the type-signature of
	 * {@code send()} changes the compiler will force this class to be updated.
	 * 
	 * @param iface the interface whose {@code send()} method to call
	 * @param shout the shout to be sent
	 * @return the encapsulated result
	 */
	public static SendResult encapsulateSend(NetworkInterface iface, LocalShout shout) {
		try {
			iface.send(shout);
			return new SendResult();
		} catch (NotConnectedException e) {
			return new SendResult(e);
		} catch (ShoutChainTooLongException e) {
			return new SendResult(e);
		} catch (ManesNotInstalledException e) {
			return new SendResult(e);
		} catch (ManesNotRegisteredException e) {
			return new SendResult(e);
		} catch (IOException e) {
			return new SendResult(e);
		}
	}

	public SendResult() {
		super();
	}

	public SendResult(NotConnectedException e) {
		super(e);
	}

	public SendResult(ShoutChainTooLongException e) {
		super(e);
	}

	public SendResult(ManesNotInstalledException e) {
		super(e);
	}

	public SendResult(ManesNotRegisteredException e) {
		super(e);
	}

	public SendResult(IOException e) {
		super(e);
	}

	/**
	 * Returns the result of the {@link NetworkInterface#send(LocalShout)}
	 * method, if it succeeded, or throws the wrapped exception
	 * 
	 * @throws NotConnectedException see
	 *             {@link NetworkInterface#send(LocalShout)}
	 * @throws ShoutChainTooLongException see
	 *             {@link NetworkInterface#send(LocalShout)}
	 * @throws ManesNotInstalledException see
	 *             {@link NetworkInterface#send(LocalShout)}
	 * @throws NotRegisteredException see
	 *             {@link NetworkInterface#send(LocalShout)}
	 * @throws IOException see {@link NetworkInterface#send(LocalShout)}
	 */
	public void getResultOrThrow() throws NotConnectedException, ShoutChainTooLongException,
			ManesNotInstalledException, ManesNotRegisteredException, IOException {
		if (this.threwException()) {
			try {
				throw this.getThrowable();
			} catch (NotConnectedException e) {
				throw e;
			} catch (ShoutChainTooLongException e) {
				throw e;
			} catch (ManesNotInstalledException e) {
				throw e;
			} catch (ManesNotRegisteredException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} catch (Throwable e) {
				// Cannot occur if code is correct.
				throw new RuntimeException("Invalid exception encaspulated in SendResult.", e);
			}
		} else {
			return;
		}
	}

}
