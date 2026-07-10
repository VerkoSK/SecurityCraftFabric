package net.geforcemods.securitycraft.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Salted SHA-256 hashing for passcodes so the plaintext is never stored on disk.
 * A trimmed stand-in for the upstream {@code misc.SaltData} system.
 */
public final class PasscodeUtils {
	private PasscodeUtils() {}

	public static String hash(String passcode, String salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest((salt + ":" + passcode).getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(bytes.length * 2);

			for (byte b : bytes)
				sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));

			return sb.toString();
		}
		catch (NoSuchAlgorithmException e) {
			// SHA-256 is guaranteed present on every JRE; if it ever isn't, fail loudly rather than store plaintext.
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	/** Constant-time-ish comparison of two hex hashes. */
	public static boolean matches(String hashA, String hashB) {
		if (hashA == null || hashB == null || hashA.length() != hashB.length())
			return false;

		int diff = 0;

		for (int i = 0; i < hashA.length(); i++)
			diff |= hashA.charAt(i) ^ hashB.charAt(i);

		return diff == 0;
	}
}
