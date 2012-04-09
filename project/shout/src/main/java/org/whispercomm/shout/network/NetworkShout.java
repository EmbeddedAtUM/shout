package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.interfaces.ECPublicKey;

import org.joda.time.DateTime;
import org.whispercomm.shout.AbstractShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.SignatureUtility;
import org.whispercomm.shout.User;
import org.whispercomm.shout.provider.BasicUser;
import org.whispercomm.shout.provider.ShoutProviderContract;

/**
 * Shouts processed on the network, which can be constructed from and serialized
 * to byte array. This object can be also constructed given the shout_if in the
 * database.
 * 
 * @author Yue Liu
 * 
 */
public class NetworkShout extends AbstractShout {

	/**
	 * Maximum length (in bytes) of a user name.
	 */
	static final int MAX_USER_NAME_LEN = 20;
	/**
	 * Maximum length (in bytes) of a shout message
	 */
	static final int MAX_CONTENT_LEN = 140;
	/**
	 * Length (in bytes) of the signing key
	 */
	static final int KEY_LENGTH = 256/8;
	/**
	 * Length (in bytes) of the digital signature
	 */
	static final int SIGNATURE_LENGTH = 256/8;
	/**
	 * Maximum length of re-shout chain
	 */
	static final int MAX_SHOUT_NUM = 3;
	/**
	 * Maximum length (in bytes) of a shout message
	 */
	public static final int MAX_LEN = (8 + 4 + MAX_USER_NAME_LEN + KEY_LENGTH + 4
			+ MAX_CONTENT_LEN + 1 + SIGNATURE_LENGTH + 1)
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
	public NetworkShout(long shout_id) {
		Shout shout = ShoutProviderContract.retrieveShoutById(shout_id);
		this.timestamp = shout.getTimestamp();
		this.sender = shout.getSender();
		this.signature = shout.getSignature();
		this.shoutOri = shout.getOriginalShout();
	}

	/***
	 * Build a NetworkShout object from bytes received from the network
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws AuthenticityFailureException
	 *             the network data fails authenticity check.
	 */
	public NetworkShout(byte[] rawData) throws UnsupportedEncodingException,
			AuthenticityFailureException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawData);
		// Get all the signatures
		byte[][] sigs = new byte[MAX_SHOUT_NUM][];
		byte[] sig = null;
		char hasNext = 1;
		int sigNum = 0;
		for (; hasNext != 0 && sigNum < MAX_SHOUT_NUM; sigNum++) {
			sig = new byte[SIGNATURE_LENGTH];
			byteBuffer.get(sig, 0, SIGNATURE_LENGTH);
			sigs[sigNum] = sig;
			hasNext = byteBuffer.getChar();
		}
		// verify all the signatures
		NetworkShout[] shouts = new NetworkShout[3];
		for (int i = 0; i < sigNum; i++) {
			sig = sigs[i];
			shouts[i] = verifyShoutSignature(sig, byteBuffer);
			if (shouts[i] == null) {
				// Verification fails
				throw new AuthenticityFailureException();
			}
		}
		// Ensemble the final shout
		for (int i = 0; i < MAX_SHOUT_NUM; i++) {
			if (shouts[i + 1] == null)
				break;
			shouts[i].shoutOri = shouts[i + 1];
		}
		this.timestamp = shouts[0].getTimestamp();
		this.sender = shouts[0].getSender();
		this.signature = shouts[0].getSignature();
		this.shoutOri = shouts[0].getOriginalShout();
	}

	/**
	 * Serialize the NetworkShout object into byte array
	 * 
	 * @return bytes ready to be transmitted across network
	 * @throws ShoutChainTooLongException
	 * @throws UnsupportedEncodingException
	 */
	public byte[] toNetworkBytes() throws ShoutChainTooLongException,
			UnsupportedEncodingException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_LEN);
		// Get signatures
		int sigNum = 0;
		NetworkShout shout = this;
		char hasReshout;
		while (shout != null && sigNum < MAX_SHOUT_NUM) {
			// signature
			byteBuffer.put(shout.getSignature());
			// hasNext
			hasReshout = (char) (shout.getOriginalShout() == null ? 0 : 1);
			byteBuffer.putChar(hasReshout);
			shout = (NetworkShout) shout.shoutOri;
			sigNum++;
		}
		// sanity check
		if (shout != null)
			throw new ShoutChainTooLongException();
		// put the body of the shout into the ByteBuffer
		SignatureUtility.serialize(byteBuffer, this.timestamp, this.sender, this.content,
				this.shoutOri);
		return byteBuffer.array();
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
	private NetworkShout(DateTime timestamp, User sender, String content,
			byte[] signature, Shout shoutOri) {
		this.timestamp = timestamp;
		this.sender = sender;
		this.content = content;
		this.signature = signature;
		this.shoutOri = shoutOri;
	}

	/**
	 * Verify whether a shout is authentic
	 * 
	 * @param signature
	 * @param byteBuffer
	 *            that holds the body of a shout
	 * 
	 * @return a new NetworkShout (with its original shout yet to be completed)
	 *         if authentic, null otherwise
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private static NetworkShout verifyShoutSignature(byte[] signature,
			ByteBuffer byteBuffer) throws UnsupportedEncodingException {
		// Slice a new ByteBuffer
		ByteBuffer data = byteBuffer.slice();
		// extract the public key
		NetworkShout shout = getShoutBody(byteBuffer);
		ECPublicKey pubKey = shout.getSender().getPublicKey();
		// verify the signature
		if (SignatureUtility.verifySignature(signature, pubKey, data)) {
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
	 */
	private static NetworkShout getShoutBody(ByteBuffer byteBuffer)
			throws UnsupportedEncodingException {
		// time
		long time = byteBuffer.getLong();
		DateTime timestamp = new DateTime(time);
		// senderNameLen
		int senderNameLen = byteBuffer.getInt();
		// senderName
		byte[] senderNameBytes = new byte[senderNameLen];
		byteBuffer.get(senderNameBytes, 0, senderNameLen);
		String senderName = new String(senderNameBytes, CHARSET_NAME);
		// pubKey
		byte[] pubKeyBytes = new byte[KEY_LENGTH];
		byteBuffer.get(pubKeyBytes, 0, KEY_LENGTH);
		// TODO get ECPublicKey from pubKeyBytes
		ECPublicKey pubKey = null;
		User sender = new BasicUser(senderName, pubKey);
		// contentLen
		int contentLen = byteBuffer.getInt();
		// content
		byte[] contentBytes = new byte[contentLen];
		byteBuffer.get(contentBytes, 0, contentLen);
		String content = new String(contentBytes, CHARSET_NAME);
		// isReshout (***no need to use this value so far)
		byteBuffer.getChar();
		NetworkShout shout = new NetworkShout(timestamp, sender, content, null,
				null);
		return shout;
	}

	@Override
	public User getSender() {
		return this.sender;
	}

	@Override
	public String getContent() {
		return this.content;
	}

	@Override
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public Shout getOriginalShout() {
		return this.shoutOri;
	}

	@Override
	public byte[] getSignature() {
		return this.signature;
	}

}
