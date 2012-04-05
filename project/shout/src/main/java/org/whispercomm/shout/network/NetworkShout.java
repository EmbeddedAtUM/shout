package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.provider.BasicUser;
import org.whispercomm.shout.provider.ShoutProviderContract;

public class NetworkShout implements Shout {

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
	static final int KEY_LENGTH = 256;
	/**
	 * Length (in bytes) of the digital signature
	 */
	static final int SIGNATURE_LENGTH = 256;
	/**
	 * Maximum length of shout-reshout-comment chain
	 */
	static final int MAX_SHOUT_NUM = 3;
	/**
	 * Maximum length (in bytes) of a shout message
	 */
	static final int MAX_LEN = (8 + 4 + MAX_USER_NAME_LEN + KEY_LENGTH + 4
			+ MAX_CONTENT_LEN + 1 + SIGNATURE_LENGTH + 1)
			* MAX_SHOUT_NUM;
	/**
	 * String encode/decode charset
	 */
	static String CHARSET_NAME = "UTF-8";

	long id;
	DateTime timestamp;
	User sender;
	String content;
	String signature;
	Shout shoutOri;
	char hasReshout;

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
			String signature, Shout shoutOri, char hasReshout) {
		this.id = -1;
		this.timestamp = timestamp;
		this.sender = sender;
		this.content = content;
		this.signature = signature;
		this.shoutOri = shoutOri;
		this.hasReshout = hasReshout;
	}

	/**
	 * Generate a NetworkShout from a shout stored in the database
	 * 
	 * @param shout_id
	 *            the _ID of the source shout in the database
	 */
	public NetworkShout(long shout_id) {
		Shout shout = ShoutProviderContract.retrieveShoutById(shout_id);
		this.id = shout.getId();
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
	public static NetworkShout getShoutFromNetwork(byte[] rawData)
			throws UnsupportedEncodingException, AuthenticityFailureException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawData);
		// Get all the signatures
		String[] sigs = new String[MAX_SHOUT_NUM];
		String sig = null;
		char hasNext = 1;
		int sigNum = 0;
		for (; hasNext != 0 && sigNum < MAX_SHOUT_NUM; sigNum++) {
			byte[] sigBytes = new byte[SIGNATURE_LENGTH];
			byteBuffer.get(sigBytes, 0, SIGNATURE_LENGTH);
			sig = new String(sigBytes, CHARSET_NAME);
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
		return shouts[0];
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
	static NetworkShout verifyShoutSignature(String signature,
			ByteBuffer byteBuffer) throws UnsupportedEncodingException {
		// Slice a new ByteBuffer
		ByteBuffer data = byteBuffer.slice();
		// extract the public key
		NetworkShout shout = getShoutBody(byteBuffer);
		String pubKey = shout.getSender().getPublicKey();
		// verify the signature
		if (verifySignature(signature, pubKey, data)) {
			shout.signature = signature;
			return shout;
		} else
			return null;
	}

	// TODO
	static boolean verifySignature(String signature, String pubKey,
			ByteBuffer data) {
		return true;
	}

	/**
	 * Get one layer of shout from raw network bytes
	 * 
	 * @param byteBuffer
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	static NetworkShout getShoutBody(ByteBuffer byteBuffer)
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
		String pubKey = new String(pubKeyBytes, CHARSET_NAME);
		User sender = new BasicUser(senderName, pubKey);
		// contentLen
		int contentLen = byteBuffer.getInt();
		// content
		byte[] contentBytes = new byte[contentLen];
		byteBuffer.get(contentBytes, 0, contentLen);
		String content = new String(contentBytes, CHARSET_NAME);
		// isReshout
		char hasReshout = byteBuffer.getChar();
		NetworkShout shout = new NetworkShout(timestamp, sender, content, null,
				null, hasReshout);
		return shout;
	}

	/**
	 * Serialize the NetworkShout object into byte array
	 * 
	 * @return
	 * @throws ShoutChainTooLongException
	 * @throws UnsupportedEncodingException
	 */
	public byte[] toBytes() throws ShoutChainTooLongException,
			UnsupportedEncodingException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_LEN);
		// Get signatures
		int sigNum = 0;
		NetworkShout shout = this;
		char hasReshout;
		while (shout != null && sigNum < MAX_SHOUT_NUM) {
			//signature
			byteBuffer.put(shout.getSignature().getBytes(CHARSET_NAME));
			//hasNext
			hasReshout = (char) (shout.hasReshout()?1:0);
			byteBuffer.putChar(hasReshout);
			shout = (NetworkShout) shout.shoutOri;
			sigNum++;
		}
		// sanity check
		if (shout != null)
			throw new ShoutChainTooLongException();
		shout = this;
		while (shout != null) {
			// time
			byteBuffer.putLong(shout.getTimestamp().getMillis());
			// senderNameLen and senderName
			User sender = shout.getSender();
			int senderNameLen = sender.getUsername().length();
			byteBuffer.putInt(senderNameLen);
			byteBuffer.put(sender.getUsername().getBytes(CHARSET_NAME));
			// senderPubKey
			byteBuffer.put(sender.getPublicKey().getBytes(CHARSET_NAME));
			// contentLen and content
			int contentLen = shout.getContent().length();
			byteBuffer.putInt(contentLen);
			byteBuffer.put(shout.getContent().getBytes(CHARSET_NAME));
			// hasReshout
			byteBuffer.putChar((char) (shout.hasReshout() ? 1 : 0));
			shout = shout.hasReshout() ? (NetworkShout) shout
					.getOriginalShout() : null;
		}
		return null;
	}

	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
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
	public String getSignature() {
		return this.signature;
	}

	@Override
	public boolean hasReshout() {
		return this.hasReshout == 0 ? false : true;
	}
}
