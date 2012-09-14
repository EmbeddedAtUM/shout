
package org.whispercomm.shout.crypto;

import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

public class ECUtil {

	/**
	 * Equals method for {@link ECDomainParameters} objects.
	 * <p>
	 * {@link ECDomainParameters} does not override the
	 * {@link ECDomainParameters#equals(Object) equals(Object)} method, hence
	 * this utility method.
	 */
	public static boolean equals(ECDomainParameters lhs, Object rhs) {
		if (lhs == rhs)
			return true;
		if (lhs == null)
			if (rhs == null)
				return true;
			else
				return false;
		if (rhs == null)
			return false;
		if (!lhs.getClass().equals(rhs.getClass()))
			return false;
		ECDomainParameters other = (ECDomainParameters) rhs;
		if (!lhs.getCurve().equals(other.getCurve()))
			return false;
		if (!lhs.getG().equals(other.getG()))
			return false;
		if (!lhs.getH().equals(other.getH()))
			return false;
		if (!lhs.getN().equals(other.getN()))
			return false;
		/*
		 * Do not compare the seeds, because they do not affect operation and
		 * are stripped by some bouncycastle methods.
		 */
		// return Arrays.areEqual(lhs.getSeed(), other.getSeed());
		return true;
	}

	/**
	 * Hashcode method for {@link ECDomainParameters} objects.
	 * <p>
	 * {@link ECDomainParameters} does not override the
	 * {@link ECDomainParameters#hashCode() hashCode()} method, hence this
	 * utility method.
	 */
	public static int hashCode(ECDomainParameters obj) {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj.getCurve() == null) ? 0 : obj.getCurve().hashCode());
		result = prime * result + ((obj.getG() == null) ? 0 : obj.getG().hashCode());
		result = prime * result + ((obj.getH() == null) ? 0 : obj.getH().hashCode());
		result = prime * result + ((obj.getN() == null) ? 0 : obj.getN().hashCode());
		/*
		 * Do not include the seed, because it do not affect operation and are
		 * stripped by some bouncycastle methods.
		 */
		// result = prime * result + ((obj.getSeed() == null) ? 0 :
		// Arrays.hashCode(obj.getSeed()));
		return result;
	}

	/**
	 * Equals method for {@link AsymmetrticKeyParameter} objects.
	 * <p>
	 * {@link AsymmetricKeyParameter} does not override the
	 * {@link AsymmetricKeyParameter#equals(Object) equals(Object)} method,
	 * hence this utility method.
	 */
	public static boolean equals(AsymmetricKeyParameter lhs, Object rhs) {
		if (lhs == rhs)
			return true;
		if (lhs == null)
			if (rhs == null)
				return true;
			else
				return false;
		if (rhs == null)
			return false;
		if (!lhs.getClass().equals(rhs.getClass()))
			return false;
		return equalsShort(lhs, (AsymmetricKeyParameter) rhs);
	}

	/**
	 * Equals method that assumes the objects are different instances and of the
	 * same type.
	 */
	private static boolean equalsShort(AsymmetricKeyParameter lhs, AsymmetricKeyParameter rhs) {
		return lhs.isPrivate() == rhs.isPrivate();
	}

	/**
	 * Hashcode method for {@link AsymmetricKeyParameter} objects.
	 * <p>
	 * {@link AsymmetricKeyParameter} does not override the
	 * {@link AsymmetricKeyParameter#hashCode() hashCode()} method, hence this
	 * utility method.
	 */
	public static int hashCode(AsymmetricKeyParameter obj) {
		return obj.isPrivate() ? 1231 : 1237;
	}

	/**
	 * Equals method for {@link ECKeyParameters} objects.
	 * <p>
	 * {@link ECKeyParameters} does not override the
	 * {@link ECKeyParameters#equals(Object) equals(Object)} method, hence this
	 * utility method.
	 */
	public static boolean equals(ECKeyParameters lhs, Object rhs) {
		if (lhs == rhs)
			return true;
		if (lhs == null)
			if (rhs == null)
				return true;
			else
				return false;
		if (rhs == null)
			return false;
		if (!lhs.getClass().equals(rhs.getClass()))
			return false;
		return equalsShort(lhs, (ECKeyParameters) rhs);
	}

	/**
	 * Equals method that assumes the objects are different instances and of the
	 * same type.
	 */
	private static boolean equalsShort(ECKeyParameters lhs, ECKeyParameters rhs) {
		if (!equalsShort((AsymmetricKeyParameter) lhs, (AsymmetricKeyParameter) rhs))
			return false;
		return equals(lhs.getParameters(), rhs.getParameters());
	}

	/**
	 * Hashcode method for {@link ECKeyParameters} objects.
	 * <p>
	 * {@link ECKeyParameters} does not override the
	 * {@link ECKeyParameters#hashCode() hashCode()} method, hence this utility
	 * method.
	 */
	public static int hashCode(ECKeyParameters obj) {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode((AsymmetricKeyParameter) obj);
		result = prime * result + (obj.getParameters() == null ? 0 : hashCode(obj.getParameters()));
		return result;
	}

	/**
	 * Equals method for {@link ECPublicKeyParameters} objects.
	 * <p>
	 * {@link ECKeyPublicParameters} does not override the
	 * {@link ECKeyPublicParameters#equals(Object) equals(Object)} method, hence
	 * this utility method.
	 */
	public static boolean equals(ECPublicKeyParameters lhs, Object rhs) {
		if (lhs == rhs)
			return true;
		if (lhs == null)
			if (rhs == null)
				return true;
			else
				return false;
		if (rhs == null)
			return false;
		if (!lhs.getClass().equals(rhs.getClass()))
			return false;
		return equalsShort(lhs, (ECPublicKeyParameters) rhs);
	}

	/**
	 * Equals method that assumes the objects are different instances and of the
	 * same type.
	 */
	private static boolean equalsShort(ECPublicKeyParameters lhs, ECPublicKeyParameters rhs) {
		if (!equalsShort((ECKeyParameters) lhs, (ECKeyParameters) rhs))
			return false;
		return lhs.getQ().equals(rhs.getQ());
	}

	/**
	 * Hashcode method for {@link ECPublicKeyParameters} objects.
	 * <p>
	 * {@link ECPublicKeyParameters} does not override the
	 * {@link ECPUblicKeyParameters#hashCode() hashCode()} method, hence this
	 * utility method.
	 */
	public static int hashCode(ECPublicKeyParameters obj) {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode((ECKeyParameters) obj);
		result = prime * result + ((obj.getQ() == null) ? 0 : obj.getQ().hashCode());
		return result;
	}

	/**
	 * Equals method for {@link ECPrivateKeyParameters} objects.
	 * <p>
	 * {@link ECKeyPrivateParameters} does not override the
	 * {@link ECKeyPrivateParameters#equals(Object) equals(Object)} method,
	 * hence this utility method.
	 */
	public static boolean equals(ECPrivateKeyParameters lhs, Object rhs) {
		if (lhs == rhs)
			return true;
		if (lhs == null)
			if (rhs == null)
				return true;
			else
				return false;
		if (rhs == null)
			return false;
		if (!lhs.getClass().equals(rhs.getClass()))
			return false;
		return equalsShort(lhs, (ECPrivateKeyParameters) rhs);
	}

	/**
	 * Equals method that assumes the objects are different instances and of the
	 * same type.
	 */
	private static boolean equalsShort(ECPrivateKeyParameters lhs, ECPrivateKeyParameters rhs) {
		if (!equalsShort((ECKeyParameters) lhs, (ECKeyParameters) rhs))
			return false;
		return lhs.getD().equals(rhs.getD());
	}

	/**
	 * Hashcode method for {@link ECPrivateKeyParameters} objects.
	 * <p>
	 * {@link ECPrivateKeyParameters} does not override the
	 * {@link ECPrivateKeyParameters#hashCode() hashCode()} method, hence this
	 * utility method.
	 */
	public static int hashCode(ECPrivateKeyParameters obj) {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode((ECKeyParameters) obj);
		result = prime * result + ((obj.getD() == null) ? 0 : obj.getD().hashCode());
		return result;
	}

}
