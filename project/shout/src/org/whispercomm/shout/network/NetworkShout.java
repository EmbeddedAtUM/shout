package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.DateTime;
import org.whispercomm.shout.AbstractShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutMessageUtility;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.SimpleUser;
import org.whispercomm.shout.User;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;
import org.whispercomm.shout.util.Arrays;

import android.content.Context;

/**
 * Shouts processed on the network, which can be constructed from and serialized
 * to byte array. This object can be also constructed given the shout_if in the
 * database.
 * 
 * @author Yue Liu
 */
public class NetworkShout extends AbstractShout implements Shout {

	/**
	 * Maximum length (in bytes) of a user name.
	 */
	public static final int MAX_USER_NAME_LEN = 20 * 2;
	/**
	 * Maximum length (in bytes) of a shout message
	 */
	public static final int MAX_CONTENT_LEN = 140 * 2;
	/**
	 * Length (in bytes) of the signing key
	 */
	public static final int KEY_LENGTH = 91;
	/**
	 * Length (in bytes) of the digital signature
	 */
	public static final int MAX_SIGNATURE_LENGTH = 80;
	/**
	 * Maximum length of re-shout chain
	 */
	public static final int MAX_SHOUT_NUM = 3;
	/**
	 * size of a long variable (which holds the time stamp of the shout)
	 */
	public static final int TIME_STAMP_SIZE = 8;
	/**
	 * use a byte to hold the size of the sender name
	 */
	public static final int SENDER_NAME_LEN_SIZE = 1;
	/**
	 * use a char to hold the size of the content
	 */
	public static final int CONTENT_LEN_SIZE = 2;
	/**
	 * use a byte to hold has_reshout
	 */
	public static final int HAS_RESHOUT_SIZE = 1;
	/**
	 * use a char to hold the size of signature
	 */
	public static final int SIGNATURE_LENTH_SIZE = 2;
	/**
	 * use a byte to hold has_next in "signatures" fields
	 */
	public static final int SIGN_HAS_NEXT_SIZE = 1;
	/**
	 * Maximum length (in bytes) of a shout message
	 */
	public static final int MAX_LEN = (TIME_STAMP_SIZE + SENDER_NAME_LEN_SIZE
			+ MAX_USER_NAME_LEN + KEY_LENGTH + CONTENT_LEN_SIZE
			+ MAX_CONTENT_LEN + HAS_RESHOUT_SIZE + SIGNATURE_LENTH_SIZE
			+ MAX_SIGNATURE_LENGTH + SIGN_HAS_NEXT_SIZE)
			* MAX_SHOUT_NUM;

	private DateTime timestamp;
	private User sender;
	private String content;
	private byte[] signature;
	private Shout shoutOri;

	/**
	 * Generate a NetworkShout from a shout stored in the database
	 * 
	 * @param shout_id
	 *            the _ID of the source shout in the database
	 */
	public NetworkShout(int shout_id, Context context) {
		Shout shout = ShoutProviderContract
				.retrieveShoutById(context, shout_id);
		this.timestamp = shout.getTimestamp();
		this.sender = shout.getSender();
		this.signature = shout.getSignature();
		this.shoutOri = shout.getParent();
		this.content = shout.getMessage();
	}

	/**
	 * Usually used to construct a shout from data received from the network.
	 * This shout needs to be stored into the database.
	 * 
	 * @param timestamp
	 * @param sender
	 * @param content
	 * @param signature
	 * @param shoutOri
	 * @param hasReshout
	 */
	public NetworkShout(DateTime timestamp, User sender, String content,
			byte[] signature, Shout shoutOri) {
		this.timestamp = timestamp;
		this.sender = sender;
		this.content = content;
		this.signature = signature;
		this.shoutOri = shoutOri;
	}

	/**
	 * extract all the signatures of the network shout
	 * 
	 * @param sigs
	 *            array to hold all the signatures
	 * @param byteBuffer
	 *            raw data in ByteBuffer
	 * @return
	 */
	public static int getSignatures(byte[][] sigs, ByteBuffer byteBuffer) {
		byte[] sig = null;
		byte hasNext = 1;
		int sigNum = 0;
		for (; hasNext != 0 && sigNum < MAX_SHOUT_NUM; sigNum++) {
			// signature_len
			int signature_len = byteBuffer.getChar();
			// signature
			sig = new byte[signature_len];
			byteBuffer.get(sig, 0, signature_len);
			sigs[sigNum] = sig;
			// has_next
			hasNext = byteBuffer.get();
		}
		return sigNum;
	}

	/***
	 * Build a NetworkShout object from bytes received from the network
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws AuthenticityFailureException
	 *             the network data fails authenticity check.
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	public NetworkShout(byte[] rawData) throws UnsupportedEncodingException,
			AuthenticityFailureException, InvalidKeyException,
			NoSuchAlgorithmException, SignatureException,
			NoSuchProviderException, InvalidKeySpecException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawData);
		// Get all the signatures
		byte[][] sigs = new byte[MAX_SHOUT_NUM][];
		int sigNum = getSignatures(sigs, byteBuffer);
		// verify all the signatures
		NetworkShout[] shouts = new NetworkShout[3];
		for (int i = 0; i < sigNum; i++) {
			shouts[i] = verifyShoutSignature(sigs[i], byteBuffer);
			if (shouts[i] == null) {
				// Verification fails
				throw new AuthenticityFailureException();
			}
		}
		// Ensemble the final shout
		for (int i = 0; i < MAX_SHOUT_NUM - 1; i++) {
			if (shouts[i + 1] == null)
				break;
			shouts[i].shoutOri = shouts[i + 1];
		}
		this.timestamp = shouts[0].getTimestamp();
		this.sender = shouts[0].getSender();
		this.content = shouts[0].getMessage();
		this.signature = shouts[0].getSignature();
		this.shoutOri = shouts[0].getParent();
	}

	/**
	 * Serialize the NetworkShout object into byte array
	 * 
	 * @return bytes ready to be transmitted across network
	 * @throws ShoutChainTooLongException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] toNetworkBytes(Shout shout)
			throws ShoutChainTooLongException, UnsupportedEncodingException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_LEN);
		// Get signatures
		int sigNum = 0;
		byte hasReshout;
		Shout current = shout;
		while (current != null && sigNum < MAX_SHOUT_NUM) {
			byte[] signature = current.getSignature();
			// signature_len
			byteBuffer.putChar((char) signature.length);
			// signature
			byteBuffer.put(signature);
			// hasNext
			hasReshout = (byte) (current.getParent() == null ? 0 : 1);
			byteBuffer.put(hasReshout);
			current = current.getParent();
			sigNum++;
		}
		// sanity check
		if (current != null)
			throw new ShoutChainTooLongException();
		// put the body of the shout into the ByteBuffer
		byte[] shoutBodyBytes = SerializeUtility.serializeShoutData(shout);
		byteBuffer.put(shoutBodyBytes);

		return Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.position());
	}

	/**
	 * Verify whether a shout is authentic
	 * 
	 * @param signature
	 * @param byteBuffer
	 *            that holds the body of a shout
	 * @return a new NetworkShout (with its original shout yet to be completed)
	 *         if authentic, null otherwise
	 * @throws UnsupportedEncodingException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	public static NetworkShout verifyShoutSignature(byte[] signature,
			ByteBuffer byteBuffer) throws UnsupportedEncodingException,
			InvalidKeyException, NoSuchAlgorithmException, SignatureException,
			NoSuchProviderException, InvalidKeySpecException {
		// Slice a new ByteBuffer
		ByteBuffer data = byteBuffer.slice();
		byte[] dataBytes = data.array();
		byte[] dataBytes1 = Arrays.copyOfRange(dataBytes,
				byteBuffer.position(), dataBytes.length);
		// extract the public key
		NetworkShout shout = getShoutBody(byteBuffer);
		ECPublicKey pubKey = shout.getSender().getPublicKey();
		if (SignatureUtility.verifySignature(dataBytes1, signature, pubKey)) {
			shout.signature = signature;
			return shout;
		} else
			return null;
	}

	/**
	 * Get one layer of shout from raw network bytes
	 * 
	 * @param byteBuffer
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 */
	protected static NetworkShout getShoutBody(ByteBuffer byteBuffer)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {
		// time
		long time = byteBuffer.getLong();
		DateTime timestamp = new DateTime(time);
		// senderNameLen
		int senderNameLen = byteBuffer.get();
		// senderName
		byte[] senderNameBytes = new byte[senderNameLen];
		byteBuffer.get(senderNameBytes, 0, senderNameLen);
		String senderName = new String(senderNameBytes, CHARSET_NAME);
		// pubKey
		byte[] pubKeyBytes = new byte[KEY_LENGTH];
		byteBuffer.get(pubKeyBytes, 0, KEY_LENGTH);
		// get ECPublicKey from pubKeyBytes
		ECPublicKey pubKey = SignatureUtility
				.getPublicKeyFromBytes(pubKeyBytes);
		User sender = new SimpleUser(senderName, pubKey);
		// contentLen
		int contentLen = byteBuffer.getChar();
		String content;
		if (contentLen > 0) {
			// content
			byte[] contentBytes = new byte[contentLen];
			byteBuffer.get(contentBytes, 0, contentLen);
			content = new String(contentBytes, CHARSET_NAME);
		} else {
			content = null;
		}
		// isReshout (***no need to use this value so far)
		byteBuffer.get();
		NetworkShout shout = new NetworkShout(timestamp, sender, content, null,
				null);
		return shout;
	}

	@Override
	public User getSender() {
		return this.sender;
	}

	@Override
	public String getMessage() {
		return this.content;
	}

	@Override
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public Shout getParent() {
		return this.shoutOri;
	}

	@Override
	public byte[] getSignature() {
		return this.signature;
	}

	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

}
