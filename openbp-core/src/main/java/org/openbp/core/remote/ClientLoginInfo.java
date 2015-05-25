/*
 *   Copyright 2007 skynamics AG
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
package org.openbp.core.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Container for login information.
 * The data of this class is used to look up and verify a user
 * who wants to login to an OpenBP server.
 *
 * @author Falk Hartmann
 */
public class ClientLoginInfo
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Login */
	private transient String login;

	/** Password */
	private transient String password;

	/** The secret string */
	private static final String SECRET = "schlagersuesstafel";

	/** A buffer holding the transient data in an encoded form (for serialization). */
	private StringBuffer encoded = new StringBuffer();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ClientLoginInfo()
	{
	}

	/**
	 * Default constructor.
	 *
	 * @param login Login
	 * @param password Password
	 */
	public ClientLoginInfo(String login, String password)
	{
		this.login = login;
		this.password = password;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the login.
	 * @nowarn
	 */
	public String getLogin()
	{
		return login;
	}

	/**
	 * Sets the login.
	 * @nowarn
	 */
	public void setLogin(String login)
	{
		this.login = login;
	}

	/**
	 * Gets the password.
	 * @nowarn
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Sets the password.
	 * @nowarn
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	//////////////////////////////////////////////////
	// @@ Serialization
	//////////////////////////////////////////////////

	/**
	 * This method is called when the class gets serialized
	 * into an {@link java.io.ObjectOutputStream}
	 *
	 * @param out The ObjectOutputStream to write the object state to
	 * @throws IOException If the operations on the stream fail
	 */
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		// Encrypt the transient data.
		encrypt();

		// Use the default write method to write the object state.
		out.defaultWriteObject();
	}

	/**
	 * This method is called when the class gets initialized
	 * from an {@link java.io.ObjectInputStream}
	 *
	 * @param in The ObjectInputStream to read the object state from
	 * @throws IOException If the operations on the stream fail
	 * @throws ClassNotFoundException If a class contained in the serialization stream could not be found
	 */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		// Use the default read method to read the object state.
		in.defaultReadObject();

		// Decrypt the data read into the transient fields.
		decrypt();
	}

	//////////////////////////////////////////////////
	// @@ Encryption
	//////////////////////////////////////////////////

	/**
	 * This method decrypts the value of the {@link #encoded} field into the transient fields.
	 * For an explanation on how the "decryption" works, see {@link #encrypt}.
	 *
	 * @throws IOException If something goes wrong during decryption
	 */
	private void decrypt()
		throws IOException
	{
		// The random key has the same length as the secret string.
		char [] key = new char [SECRET.length()];

		// Get key from transport string.
		for (int i = 0; i < key.length; i++)
		{
			key [i] = (char) (encoded.charAt(i) ^ SECRET.charAt(i));
		}

		// Do the decryption.
		StringBuffer plain = new StringBuffer();
		int keyPos = 0;
		for (int encodedPos = 0; encodedPos < encoded.length(); encodedPos++)
		{
			plain.append((char) (encoded.charAt(encodedPos) ^ key [keyPos++]));

			// Rotate the key, if it's end has been reached.
			if (keyPos == key.length)
			{
				keyPos = 0;
			}
		}

		// Tokenize the result.
		StringTokenizer tokenizer = new StringTokenizer(plain.toString(), "\n");

		// Check, whether we found 3 tokens.
		if (tokenizer.countTokens() != 3)
		{
			// No -> the input must have been malicious.
			throw new IOException();
		}

		// Get the parts of it (skip the first, which is the secret).
		tokenizer.nextToken();
		login = tokenizer.nextToken();
		password = tokenizer.nextToken();
	}

	/**
	 * This method encrpyts the data of the transient fields into the {@link #encoded}
	 * buffer for serialization. As an encryption, a random number is generated.
	 * The random number is used to XOR the transmitted login and password. To
	 * allow re-extraction of the random number during decryption, a secret but
	 * constant string is encoded and transmitted, too.
	 */
	private void encrypt()
	{
		// The random key has the same length as the secret string.
		char [] key = new char [SECRET.length()];

		// Create a random key.
		for (int i = 0; i < key.length; i++)
		{
			key [i] = (char) Math.floor(256 * 256 * Math.random());
		}

		// We need a temporary buffer for the plain text.
		StringBuffer plain = new StringBuffer();

		// Append the data to be encrypted (separated by newline).
		plain.append(SECRET);
		plain.append('\n');
		plain.append(login);
		plain.append('\n');
		plain.append(password);

		// XOR the key with the plain text into the encoded buffer.
		encoded.setLength(0);
		int keyPos = 0;
		for (int plainPos = 0; plainPos < plain.length(); plainPos++)
		{
			encoded.append((char) (plain.charAt(plainPos) ^ key [keyPos++]));

			// Rotate the key, if it's end has been reached.
			if (keyPos == key.length)
			{
				keyPos = 0;
			}
		}
	}
}
