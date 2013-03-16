
package org.whispercomm.shout.test.util;

import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.spongycastle.util.encoders.Hex;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;
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

	/*
	 * Key generation helper functions
	 */
	private static ECPublicKey generatePublic(String encodedKey) {
		try {
			return KeyGenerator.generatePublic(Hex.decode(encodedKey));
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	private static ECPrivateKey generatePrivate(String encodedKey) {
		try {
			return KeyGenerator.generatePrivate(Hex.decode(encodedKey));
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
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
	 * Key material as bytes
	 */
	public static final byte[] USER1_PUBKEY_BYTES = KeyGenerator
			.encodePublic(generatePublic(USER1_PUBLIC_KEY));
	public static final byte[] USER2_PUBKEY_BYTES = KeyGenerator
			.encodePublic(generatePublic(USER2_PUBLIC_KEY));
	public static final byte[] USER3_PUBKEY_BYTES = KeyGenerator
			.encodePublic(generatePublic(USER3_PUBLIC_KEY));

	/*
	 * Avatars
	 */
	public static final HashReference<ShoutImage> TEST_AVATAR_1 = new SimpleHashReference<ShoutImage>(
			new Hash(Hex.decode("316216ECC384213EDCC34ABDE19A8271ED84E9ED869624D00A4875F363A45B58")));
	public static final HashReference<ShoutImage> TEST_AVATAR_2 = new SimpleHashReference<ShoutImage>(
			new Hash(Hex.decode("752D08E18906B7B2318E9C6B14AB61434C3EE06706E1919E3018017E66A1929F")));
	public static final HashReference<ShoutImage> TEST_AVATAR_3 = new SimpleHashReference<ShoutImage>(
			new Hash(Hex.decode("5D507BA1730FB5CC0640BD7D869D4828B61ECD2F7241CFEB85A0085FB090FBC5")));

	/*
	 * Users
	 */
	public static final Me TEST_ME_1 = new TestMe("Me 1 ٩(͡๏̯͡๏)۶",
			generatePublic(USER1_PUBLIC_KEY), generatePrivate(USER1_PRIVATE_KEY), TEST_AVATAR_1);
	public static final Me TEST_ME_2 = new TestMe("Me 2 ٩(-̮̮̃•̃)",
			generatePublic(USER2_PUBLIC_KEY), generatePrivate(USER2_PRIVATE_KEY), TEST_AVATAR_2);
	public static final Me TEST_ME_3 = new TestMe("Me 3 ٩(-̮̮̃-̃)۶",
			generatePublic(USER3_PUBLIC_KEY), generatePrivate(USER3_PRIVATE_KEY), TEST_AVATAR_3);

	/*
	 * Downcast Me to User
	 */
	public static final User TEST_USER_1 = TEST_ME_1;
	public static final User TEST_USER_2 = TEST_ME_2;
	public static final User TEST_USER_3 = TEST_ME_3;

	/*
	 * Shouts
	 */

	/*
	 * TODO: Manually generate sigs and hashes are wrong, so this method
	 * overrides them with auto-generated ones. When the packet format is
	 * finalized, manually generate the correct sigs and hashes and remove this
	 * method.
	 */
	private static Shout SignAndHashShout(TestShout shout, Me me) {
		Shout signed = SignatureUtility.signShout(shout, me);
		shout.version = signed.getVersion();
		shout.signature = signed.getSignature();
		shout.hash = SerializeUtility.generateHash(shout);
		return shout;
	}

	/*
	 * Locations
	 */
	public static final Location ROOT_LOCATION = new TestLocation(42.2738372, -83.7316855);
	public static final Location RESHOUT_LOCATION = new TestLocation(42.26585285315397,
			-83.7486183643341);
	public static final Location COMMENT_LOCATION = new TestLocation(42.269890052534585,
			-83.74889731407166);
	public static final Location RECOMMENT_LOCATION = new TestLocation(42.29234200677788,
			-83.71379256248474);

	public static Shout ROOT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_1,
					null,
					"٩(-̮̮̃-̃)۶: Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus congue rutrum quam quis sollicitudin.",
					new DateTime(2010, 9, 8, 7, 6, 5, DateTimeZone.UTC),
					null,
					Hex.decode("9C019522796E25E742AEDFBA0844A1EF344F4897F88BFABD39365A553941F137"),
					ROOT_LOCATION),
			TEST_ME_1);

	public static Shout RESHOUT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_2,
					ROOT_SHOUT,
					null,
					new DateTime(2011, 10, 9, 8, 7, 6, 5, DateTimeZone.UTC),
					null,
					Hex.decode("1A604881053AE042E1A5DC02ED0D275B9206AF00EDE5399739510B91CDC348E8"),
					RESHOUT_LOCATION),
			TEST_ME_2);

	public static Shout COMMENT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_2,
					ROOT_SHOUT,
					"٩(͡๏̯͡๏)۶: Sed vehicula placerat velit, sed pretium lacus luctus tincidunt. Vestibulum suscipit elit et turpis tristique lobortis.",
					new DateTime(2011, 10, 9, 8, 7, 6, 5, DateTimeZone.UTC),
					null,
					Hex.decode("1A604881053AE042E1A5DC02ED0D275B9206AF00EDE5399739510B91CDC348E8"),
					COMMENT_LOCATION),
			TEST_ME_2);

	public static Shout RECOMMENT_SHOUT = SignAndHashShout(
			new TestShout(
					TEST_USER_3,
					COMMENT_SHOUT,
					null,
					new DateTime(2012, 11, 10, 9, 8, 7, DateTimeZone.UTC),
					null,
					Hex.decode("B2E74FC24E02A31CF8DF4F2826F2CD0E97B68F876E3AC85A30E51F9A26A144EB"),
					RECOMMENT_LOCATION),
			TEST_ME_3);

	/**
	 * Generate a unique, random byte array of a given size
	 * 
	 * @param size
	 */
	public static byte[] genByteArray(int size) {
		byte[] arr = new byte[size];
		Random rand = new Random();
		rand.nextBytes(arr);
		return arr;
	}

	public static String generateRandomBase64String(int approxLength) {
		byte[] arr = new byte[approxLength * 6 / 8];
		Random rand = new Random();
		rand.nextBytes(arr);
		String encoded = Base64.encodeToString(arr, Base64.DEFAULT);
		return encoded;
	}

}
