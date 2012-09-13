
package org.whispercomm.shout.test.util;

import static org.junit.Assert.fail;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.serialization.SerializeUtility;

import android.util.Base64;

/**
 * Factory class to be used when generating objects for testing
 * 
 * @author David Adrian
 * @author Yue Liu
 */
public class TestFactory {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/*
	 * Key generation helper functions
	 */
	private static ECPublicKey generatePublic(String encodedKey) {
		try {
			return (ECPublicKey) KeyFactory.getInstance("ECDSA")
					.generatePublic(
							new X509EncodedKeySpec(Hex.decode(encodedKey)));
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static ECPrivateKey generatePrivate(String encodedKey) {
		try {
			return (ECPrivateKey) KeyFactory.getInstance("ECDSA").generatePrivate(
					new PKCS8EncodedKeySpec(Hex.decode(encodedKey)));
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static KeyPair generateKeyPair(String encodedPublicKey, String encodedPrivateKey) {
		return new KeyPair(generatePublic(encodedPublicKey), generatePrivate(encodedPrivateKey));
	}

	/*
	 * Key material for users
	 */
	private static final String USER1_PUBLIC_KEY = "3059301306072A8648CE3D020106082A8648CE3D0301070342000474BC24849CF115A40665C465F8EE11D42C7F3ABBC61602DC5FB1FD8C05649A92B5D6987652AACA7FAA6D161CCDCC272C02F3520BFF753D5FDDD08C1C4D70B07B";
	private static final String USER1_PRIVATE_KEY = "308193020100301306072A8648CE3D020106082A8648CE3D030107047930770201010420DE48DD0A4DD3A3D9D9030F47CF069829055611F5D6FF06D03BAF8F7A72DDA969A00A06082A8648CE3D030107A1440342000474BC24849CF115A40665C465F8EE11D42C7F3ABBC61602DC5FB1FD8C05649A92B5D6987652AACA7FAA6D161CCDCC272C02F3520BFF753D5FDDD08C1C4D70B07B";

	private static final String USER2_PUBLIC_KEY = "3059301306072A8648CE3D020106082A8648CE3D0301070342000434160B8041501DF3D5423D6F63C6C5E387290CFB0232EB5C988960C167A010EE5AD6D163F2BAF70733DAC9B46D07F01F72EF8C8AC9BDA4A206EBAE880A6338BA";
	private static final String USER2_PRIVATE_KEY = "308193020100301306072A8648CE3D020106082A8648CE3D0301070479307702010104203A84141D31C579FCFB730E0B0E54E9BBF5E86E8A3B9E7CAC82F2752168AF861DA00A06082A8648CE3D030107A1440342000434160B8041501DF3D5423D6F63C6C5E387290CFB0232EB5C988960C167A010EE5AD6D163F2BAF70733DAC9B46D07F01F72EF8C8AC9BDA4A206EBAE880A6338BA";

	private static final String USER3_PUBLIC_KEY = "3059301306072A8648CE3D020106082A8648CE3D030107034200045F36A31792965FC24B2BBC0EAD43F6228DE7B9BFA2B34AC6C47B143EBFB167E927B1A6427CFAF403D4275C709DE23F912576D1137E015A36A0C611558B378FCE";
	private static final String USER3_PRIVATE_KEY = "308193020100301306072A8648CE3D020106082A8648CE3D030107047930770201010420CF1DF8F8BBFF54FA37D42E9BA2493434A060A3DD3E27DC3C89DFE230EFAF90BBA00A06082A8648CE3D030107A144034200045F36A31792965FC24B2BBC0EAD43F6228DE7B9BFA2B34AC6C47B143EBFB167E927B1A6427CFAF403D4275C709DE23F912576D1137E015A36A0C611558B378FCE";

	/*
	 * Users
	 */
	public static final Me TEST_ME_1 = new TestMe("Me 1 ٩(͡๏̯͡๏)۶", generateKeyPair(
			USER1_PUBLIC_KEY, USER1_PRIVATE_KEY));
	public static final Me TEST_ME_2 = new TestMe("Me 2 ٩(-̮̮̃•̃)", generateKeyPair(
			USER2_PUBLIC_KEY, USER2_PRIVATE_KEY));
	public static final Me TEST_ME_3 = new TestMe("Me 3 ٩(-̮̮̃-̃)۶", generateKeyPair(
			USER3_PUBLIC_KEY, USER3_PRIVATE_KEY));

	/*
	 * Downcast Me to User
	 */
	public static final User TEST_USER_1 = TEST_ME_1;
	public static final User TEST_USER_2 = TEST_ME_2;
	public static final User TEST_USER_3 = TEST_ME_3;

	/*
	 * Shouts
	 */

	/**
	 * TODO: Manually generate sigs and hashes are wrong, so this method
	 * overrides them with auto-generated ones. When the packet format is
	 * finalized, manually generate the correct sigs and hashes and remove this
	 * method.
	 */
	private static Shout SignAndHashShout(TestShout shout, Me me) {
		shout.signature = SignatureUtility.generateSignature(shout, me);
		shout.hash = SerializeUtility.generateHash(shout);
		return shout;
	}

	public static Shout ROOT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_1,
					null,
					"٩(-̮̮̃-̃)۶: Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus congue rutrum quam quis sollicitudin.",
					new DateTime(2010, 9, 8, 7, 6, 5, DateTimeZone.UTC),
					Hex.decode("304402205fdfb5963542a59ebb68771063c3117d4a63b080743ce8707d53ab80a70c21ff02204a091e4394ab2b95c1eb201694f35ac84993e9b85a6b5fb78e009e3ba380e7a9"),
					Hex.decode("9C019522796E25E742AEDFBA0844A1EF344F4897F88BFABD39365A553941F137")),
			TEST_ME_1);

	public static Shout RESHOUT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_2,
					ROOT_SHOUT,
					null,
					new DateTime(2011, 10, 9, 8, 7, 6, 5, DateTimeZone.UTC),
					Hex.decode("30450220453471c42d032fd8ee1708d70644da4c99c72f797479199796527172df0598d8022100af718b5e41e3f4437780ef57325dca25ff3ee35332f9539621e9e7d3cdab238b"),
					Hex.decode("1A604881053AE042E1A5DC02ED0D275B9206AF00EDE5399739510B91CDC348E8")),
			TEST_ME_2);

	public static Shout COMMENT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_2,
					ROOT_SHOUT,
					"٩(͡๏̯͡๏)۶: Sed vehicula placerat velit, sed pretium lacus luctus tincidunt. Vestibulum suscipit elit et turpis tristique lobortis.",
					new DateTime(2011, 10, 9, 8, 7, 6, 5, DateTimeZone.UTC),
					Hex.decode("30450220453471c42d032fd8ee1708d70644da4c99c72f797479199796527172df0598d8022100af718b5e41e3f4437780ef57325dca25ff3ee35332f9539621e9e7d3cdab238b"),
					Hex.decode("1A604881053AE042E1A5DC02ED0D275B9206AF00EDE5399739510B91CDC348E8")),
			TEST_ME_2);

	public static Shout RECOMMENT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_3,
					COMMENT_SHOUT,
					null,
					new DateTime(2012, 11, 10, 9, 8, 7, DateTimeZone.UTC),
					Hex.decode("304402203e00d21a26028659e2c1b46a8fb27d0fe307204afe289d8ff74f588ddf372aef02206e559b2c993cd8928e67949241067957863d60a644c291325121b2244b8eec29"),
					Hex.decode("B2E74FC24E02A31CF8DF4F2826F2CD0E97B68F876E3AC85A30E51F9A26A144EB")),
			TEST_ME_3);

	/**
	 * Generate a unique, random byte array of a given size
	 * 
	 * @param size
	 */
	public static byte[] genByteArray(int size) {
		byte[] arr = new byte[size];
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(arr);
		return arr;
	}

	public static int[] genArrayWithSingleValue(int size, int value) {
		int[] arr = new int[size];
		for (int i = 0; i < size; i++) {
			arr[i] = value;
		}
		return arr;
	}

	public static String generateRandomBase64String(int approxLength) {
		byte[] arr = new byte[approxLength * 6 / 8];
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(arr);
		String encoded = Base64.encodeToString(arr, Base64.DEFAULT);
		return encoded;
	}

}
