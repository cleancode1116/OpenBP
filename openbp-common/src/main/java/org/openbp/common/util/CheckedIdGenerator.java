/*
 *   Copyright 2008 skynamics AG
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openbp.common.util;

import java.util.UUID;

/**
 * This IDGenerator returns an ID unique across all Java virtual machines with a very high probability.
 * The id is based on java.util.UUID (type 4 of http://www.ietf.org/rfc/rfc4122.txt) plus an appended checksum.<br>
 * Note: jakarta commons id could be used to create other UID types of RFC 4122.<p>
 * 
 * This class is a singleton.
 * The methods of this class are thread-safe.
 *
 * @author Dr. Achim Leubner
 */
public class CheckedIdGenerator
{
	/** Singleton instance */
	private static CheckedIdGenerator singletonInstance;

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized CheckedIdGenerator getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new CheckedIdGenerator();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private CheckedIdGenerator()
	{
	}

	/** 
	 * Returns a new ID (type 4 of http://www.ietf.org/rfc/rfc4122.txt with an additional checksum).
	 * <p>Examples:
	 * <ul>
	 * <li> b748df7b-440b-41dc-abe9-12b7add9d833-F7
	 * <li> 47de5460-6c5a-45dc-bf2b-bf1eb743b5a0-9F
	 * <li> 245b9652-7082-443c-90ae-5af01e2b4de1-D2
	 * <li> c85a0a32-33e2-44f8-9fda-bee4162cd655-6C
	 * <li> fcc1eafc-eb8e-4e53-8f64-cbdd2e72e234-4E
	 * <li> 21cdf84c-d588-4687-9051-a52de0e50f85-FC
	 * <li> c1ea6760-b2ed-4c63-abf0-46ba6a311ad1-39
	 * <li> 2e715c61-8692-4095-9f87-8bac1a0cce01-8C
	 * <li> 45fc09a4-8edb-43ab-9333-e93502ac85e5-FA
	 * <li> 3ad66333-0bb6-42ce-b36f-f3a56f2dc5e1-61
	 * </ul>
	 * @return The new id (36 + 1 byte '-' + 2 bytes check sum)
	 */
	public String getID()
	{
		final UUID id = UUID.randomUUID();
		final byte ck = getNewIdChecksum(id);
		return id.toString() + "-" + ByteArrayUtil.toHexString(ck);
	}

	private static int maxLength = - 1;

	/**
	 * Gets the maximum length of this type of id.
	 * @return The maximum length
	 */
	public final static synchronized int getMaxLength()
	{
		if (maxLength == - 1)
		{
			maxLength = getInstance().getID().length();
		}
		return maxLength;
	}

	/**
	 * Returns true, if the checksum of this id is ok.
	 * @param id Id to check
	 * @return true if the checksum of this id is ok
	 */
	public static boolean checksumOk(final String id)
	{
		byte ck = getNewIdChecksum(id);
		if (id.length() < 36 + 3)
			return false; // no/incomplete checksum

		final byte s = getEmbeddedIdChecksum(id);
		return ck == s;
	}

	/**
	 * Returns the checksum embedded in the id. 
	 * @param id It to check
	 * @return The checksum
	 */
	public static byte getEmbeddedIdChecksum(final String id)
	{
		if (id == null)
			throw new IllegalArgumentException("id should not be null");

		if (id.length() < getMaxLength())
			throw new IllegalArgumentException("id is to short");

		return ByteArrayUtil.fromHex(id.substring(37));
	}

	/**
	 * Returns the (newly calculated) checksum of the given id.
	 * @param ids UUID w/o checksum as string
	 * @return The checksum
	 */
	public static byte getNewIdChecksum(final String ids)
	{
		if (ids == null)
			throw new IllegalArgumentException("id should not be null");

		if (ids.length() < 36)
			throw new IllegalArgumentException("id is to short");

		// remove checksum
		String idStr = ids.length() > 36 ? ids.substring(0, 36) : ids;

		return getNewIdChecksum(UUID.fromString(idStr));
	}

	/**
	 * Returns the (newly calculated) checksum of the given id.
	 * @param id UUID to generate the checksum for
	 */
	private static byte getNewIdChecksum(final UUID id)
	{
		if (id == null)
			throw new IllegalArgumentException("id should not be null");

		/*
		Checksum cs = new CRC32();
		cs.update(ByteArrayUtil.toBytes(id.getMostSignificantBits()), 0, 8);
		cs.update(ByteArrayUtil.toBytes(id.getLeastSignificantBits()), 0, 8);
		return (byte) (cs.getValue() & 0xff);
		 */

		CS cs = new CS();
		cs.update(ByteArrayUtil.toBytes(id.getMostSignificantBits()));
		cs.update(ByteArrayUtil.toBytes(id.getLeastSignificantBits()));
		return (byte) (cs.getValue() & 0xff);
	}

	private static class CS
	{
		int sum;

		public CS()
		{
		}

		public void update(byte [] b)
		{
			for (int i = 0; i < b.length; ++i)
			{
				sum += b[i];
			}
		}

		public int getValue()
		{
			return sum;
		}
	}

	public static void main(String args[])
	{
		CheckedIdGenerator idg = CheckedIdGenerator.getInstance();
		for (int i = 0; i < 10; i++)
		{
			System.out.println(idg.getID());
		}
		System.out.println(CheckedIdGenerator.checksumOk(idg.getID()));
		System.out.println(CheckedIdGenerator.checksumOk(idg.getID().substring(0, 37) + "00"));
		System.out.println(CheckedIdGenerator.getMaxLength());
	}
}
