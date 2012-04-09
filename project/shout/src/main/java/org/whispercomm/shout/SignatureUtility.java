package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.joda.time.DateTime;
import org.whispercomm.shout.network.NetworkShout;
import org.whispercomm.shout.provider.BasicUser;

import android.app.Activity;
import android.content.SharedPreferences;

public class SignatureUtility {

	public static final String SHARED_PREFS = "shout_user_keys";
	static final String USER_NAME = "user_name";
	static final String USER_PUB_KEY = "user_pub_key";
	static final String USER_PRIV_KEY = "user_priv_key";

	SharedPreferences sharedPrefs;

	public SignatureUtility(Activity callerActivity) {
		this.sharedPrefs = callerActivity.getSharedPreferences(SHARED_PREFS, 0);
	}

	/**
	 * Generate the public/private key pair. This is called when the application
	 * is first launched.
	 */
	public void genKeyPairs() {
		// TODO generate key pairs and store them in sharedPrefs
	}

	/**
	 * @return the current user's public key from the sharedPrefs
	 */
	public ECPublicKey getPublicKey() {
		// TODO
		return null;
	}

	/**
	 * @return the current user's private key from the sharedPrefs
	 */
	private ECPrivateKey getPrivateKey() {
		// TODO
		return null;
	}

	/**
	 * @return information of current user in form of User object.
	 */
	public User getUser() {
		String userName = sharedPrefs.getString(USER_NAME, null);
		ECPublicKey pubKey = getPublicKey();
		User sender = new BasicUser(userName, pubKey);
		return sender;
	}

	/**
	 * Verify the signature of data using pubKey
	 * 
	 * @param signature
	 * @param pubKey
	 * @param data
	 *            in ByteBuffer
	 * @return
	 */
	public static boolean verifySignature(byte[] signature, ECPublicKey pubKey,
			ByteBuffer data) {
		// TODO
		return true;
	}

	/**
	 * Sign data using privKey
	 * 
	 * @param data
	 * @param privKey
	 * @return signature
	 */
	public static byte[] genSignature(ByteBuffer data, ECPrivateKey privKey) {
		// TODO
		return null;
	}

	/**
	 * Generate the hash-code of the given shout.
	 * 
	 * @param timestamp
	 * @param sender
	 * @param content
	 * @param shoutOri
	 * @return hash-code of the shout
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] genShoutHash(DateTime timestamp, User sender,
			String content, Shout shoutOri) throws UnsupportedEncodingException {
		// Serialize the shout
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		serialize(byteBuffer, timestamp, sender, content, shoutOri);
		// TODO generate hash
		return null;
	}

	/**
	 * Generate the signature of a given shout message.
	 * 
	 * @param timestamp
	 * @param content
	 * @param shoutOri
	 * @param sender
	 * @return signature
	 * @throws UnsupportedEncodingException
	 */
	public byte[] genShoutSignature(DateTime timestamp, User sender,
			String content, Shout shoutOri) throws UnsupportedEncodingException {
		// Serialize the shout
		ByteBuffer byteBuffer = ByteBuffer.allocate(NetworkShout.MAX_LEN);
		serialize(byteBuffer, timestamp, sender, content, shoutOri);
		ECPrivateKey privKey = getPrivateKey();
		byte[] signature = genSignature(byteBuffer, privKey);
		return signature;
	}

	/**
	 * Serialize a shout message (not including signature) into the destination
	 * buffer
	 * 
	 * @param byteBuffer
	 *            the destination ByteButter
	 * @param timestamp
	 *            send time of the shout
	 * @param sender
	 *            sender of the shout
	 * @param content
	 *            content of the shout
	 * @param shoutOri
	 *            the original shout of the shout
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static void serialize(ByteBuffer byteBuffer, DateTime timestamp,
			User sender, String content, Shout shoutOri)
			throws UnsupportedEncodingException {
		while (timestamp != null) {
			// time
			byteBuffer.putLong(timestamp.getMillis());
			// senderNameLen and senderName
			int senderNameLen = sender.getUsername().length();
			byteBuffer.putInt(senderNameLen);
			byteBuffer.put(sender.getUsername().getBytes(Shout.CHARSET_NAME));
			// senderPubKey
			byte[] pubKeyBytes = sender.getPublicKey().getEncoded();
			if (pubKeyBytes == null)
				throw new UnsupportedEncodingException(
						"ECPublicKey does not support encoding.");
			byteBuffer.put(pubKeyBytes);
			// contentLen and content
			int contentLen = content.length();
			byteBuffer.putInt(contentLen);
			byteBuffer.put(content.getBytes(Shout.CHARSET_NAME));
			// hasReshout
			byteBuffer.putChar((char) (shoutOri == null ? 0 : 1));
			// update
			timestamp = shoutOri.getTimestamp();
			sender = shoutOri.getSender();
			content = shoutOri.getContent();
			shoutOri = shoutOri.getOriginalShout();
		}
	}

}
