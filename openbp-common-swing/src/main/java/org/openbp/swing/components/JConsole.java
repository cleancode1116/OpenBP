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
package org.openbp.swing.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * This component can redirect output/input from various output/input streams to/from
 * the document of the text pane.
 * It can be used to redirect e. g. System.out/err/in to the text pane.
 *
 * @author Heiko Erhardt
 */
public class JConsole extends JScrollPane
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Text pane */
	private JTextPane textPane;

	/** Attribute set for standard text output */
	private MutableAttributeSet standardAttributeSet;

	/** Old input stream */
	private transient InputStream oldIn;

	/** Old output stream */
	private transient PrintStream oldOut;

	/** Old error output tream. */
	private transient PrintStream oldErr;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JConsole()
	{
		super();

		textPane = new JTextPane();
		textPane.setOpaque(false);

		// textPane.setAutoscrolls (true);
		setViewportView(textPane);
	}

	//////////////////////////////////////////////////
	// @@ Stream mapping
	//////////////////////////////////////////////////

	/**
	 * Redirects the system output, error and input streams to this component.
	 *
	 * @param passThrough
	 *		true	Data written to System.out or System.err will also be echoed to the regular
	 *				System.out/System.err stream<br>
	 *		false	Data written to System.out or System.err will appear only in this component.
	 */
	public void mapAll(boolean passThrough)
	{
		oldOut = mapSystemOut(passThrough);
		oldErr = mapSystemErr(passThrough);
		oldIn = mapSystemIn();
	}

	/**
	 * Reset the redirections caused by the {@link #mapAll} method.
	 */
	public void unmapAll()
	{
		if (oldOut != null)
		{
			System.setOut(oldOut);
			oldOut = null;
		}

		if (oldErr != null)
		{
			System.setErr(oldErr);
			oldErr = null;
		}

		if (oldIn != null)
		{
			System.setIn(oldIn);
			oldIn = null;
		}
	}

	/**
	 * Redirects the system output stream to this component.
	 *
	 * @param passThrough
	 *		true	Data written to System.out will also be echoed to the regular System.out stream<br>
	 *		false	Data written to System.out will appear only in this component.
	 * @return The old value of System.out
	 */
	public PrintStream mapSystemOut(boolean passThrough)
	{
		GUIMappedOutputStream mapper = createOutputStream(passThrough ? System.out : null);

		PrintStream oldOut = System.out;
		System.setOut(new PrintStream(mapper));
		return oldOut;
	}

	/**
	 * Redirects the system error stream to this component.
	 *
	 * @param passThrough
	 *		true	Data written to System.err will also be echoed to the regular System.err stream<br>
	 *		false	Data written to System.err will appear only in this component.
	 * @return The old value of System.err
	 */
	public PrintStream mapSystemErr(boolean passThrough)
	{
		// By default, display error output using red-colored monospace font
		int fontSize = getFont().getSize();
		GUIMappedOutputStream mapper = createOutputStream(passThrough ? System.err : null, Color.RED, new Font("Monospaced", Font.PLAIN, fontSize));

		PrintStream oldErr = System.err;
		System.setErr(new PrintStream(mapper));
		return oldErr;
	}

	/**
	 * Redirects the system input stream to this component.
	 *
	 * @return The old value of System.in
	 */
	public InputStream mapSystemIn()
	{
		GUIMappedInputStream mapper = createInputStream();
		InputStream oldIn = System.in;
		System.setIn(mapper);
		return oldIn;
	}

	/**
	 * Creates an output stream that writes it output into this component.
	 *
	 * @return The new stream
	 */
	public GUIMappedOutputStream createOutputStream()
	{
		return createOutputStream(null, null, null);
	}

	/**
	 * Creates an output stream that writes it output into this component.
	 *
	 * @param forwardTo If not null, the content written to the returned stream
	 * will also be echoed to this stream
	 * @return The new stream
	 */
	public GUIMappedOutputStream createOutputStream(OutputStream forwardTo)
	{
		return createOutputStream(forwardTo, null, null);
	}

	/**
	 * Creates an output stream that writes it output into this component.
	 *
	 * @param forwardTo If not null, the content written to the returned stream
	 * will also be echoed to this stream
	 * @param color Output text color for this stream
	 * @return The new stream
	 */
	public GUIMappedOutputStream createOutputStream(OutputStream forwardTo, Color color)
	{
		return createOutputStream(forwardTo, color, null);
	}

	/**
	 * Creates an output stream that writes it output into this component.
	 *
	 * @param forwardTo If not null, the content written to the returned stream
	 * will also be echoed to this stream
	 * @param color Output text color for this stream
	 * @param font Output font for this stream
	 * @return The new stream
	 */
	public GUIMappedOutputStream createOutputStream(OutputStream forwardTo, Color color, Font font)
	{
		GUIMappedOutputStream ret = new GUIMappedOutputStream(forwardTo);
		if (color != null)
			ret.setColor(color);
		if (font != null)
			ret.setFont(font);
		return ret;
	}

	/**
	 * Creates an input stream that reads its input from this component.
	 *
	 * @return The new stream
	 */
	public GUIMappedInputStream createInputStream()
	{
		return new GUIMappedInputStream();
	}

	/**
	 * Clears the text of the component.
	 */
	public void flush()
	{
		textPane.setText("");
	}

	/**
	 * Writes text to the console.
	 *
	 * @param text Text
	 */
	public void writeText(String text)
	{
		writeText(text, null);
	}

	/**
	 * Writes text to the console.
	 *
	 * @param text Text
	 * @param attributeSet Attributes that should be given to the text or null to use default attributes
	 */
	public void writeText(String text, MutableAttributeSet attributeSet)
	{
		if (attributeSet == null)
		{
			if (standardAttributeSet == null)
			{
				Color color = Color.BLACK;
				Font font = JConsole.this.getFont();

				standardAttributeSet = new SimpleAttributeSet();

				StyleConstants.setForeground(standardAttributeSet, color);
				StyleConstants.setFontFamily(standardAttributeSet, font.getFamily());
				StyleConstants.setFontSize(standardAttributeSet, font.getSize());
			}
			attributeSet = standardAttributeSet;
		}

		try
		{
			Document doc = textPane.getDocument();
			doc.insertString(doc.getLength(), text, attributeSet);

			textPane.setCaretPosition(doc.getLength());
		}
		catch (BadLocationException e)
		{
		}
	}

	/**
	 * Gets the text pane.
	 * @nowarn
	 */
	public JTextPane getTextPane()
	{
		return textPane;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ StreamHandling
	/////////////////////////////////////////////////////////////////////////

	/**
	 * This output stream writes all output data to a text component.
	 */
	protected class GUIMappedOutputStream extends OutputStream
	{
		/** Stream to pass all output data to or null */
		private OutputStream out;

		/** Text color */
		private Color color;

		/** Display font */
		private Font font;

		/** Atribute set for document processing */
		private MutableAttributeSet attributeSet;

		/**
		 * Constructor.
		 *
		 * @param out Stream to pass all output data to or null
		 */
		public GUIMappedOutputStream(OutputStream out)
		{
			this.out = out;

			color = Color.BLACK;
			font = JConsole.this.getFont();

			attributeSet = new SimpleAttributeSet();
		}

		/**
		 * Writes a byte to the text component.
		 * All data will be interpreted as char.
		 *
		 * @param b Byte to write
		 * @exception IOException Never
		 */
		public void write(int b)
			throws IOException
		{
			StyleConstants.setForeground(attributeSet, color);
			StyleConstants.setFontFamily(attributeSet, font.getFamily());
			StyleConstants.setFontSize(attributeSet, font.getSize());

			writeText(new Character((char) b).toString(), attributeSet);

			if (out != null)
			{
				out.write(b);
			}
		}

		/**
		 * Gets the stream to pass all output data to or null.
		 * @nowarn
		 */
		public OutputStream getOut()
		{
			return out;
		}

		/**
		 * Sets the stream to pass all output data to or null.
		 * @nowarn
		 */
		public void setOut(OutputStream out)
		{
			this.out = out;
		}

		/**
		 * Gets the text color.
		 * @nowarn
		 */
		public Color getColor()
		{
			return color;
		}

		/**
		 * Sets the text color.
		 * @nowarn
		 */
		public void setColor(Color color)
		{
			this.color = color;
		}

		/**
		 * Gets the display font.
		 * @nowarn
		 */
		public Font getFont()
		{
			return font;
		}

		/**
		 * Sets the display font.
		 * @nowarn
		 */
		public void setFont(Font font)
		{
			this.font = font;
		}
	}

	/**
	 * This output stream reads all input data from a component.
	 * All KeyEvents will put in a stream and can be used as standard input stream.
	 */
	protected class GUIMappedInputStream extends InputStream
		implements KeyListener
	{
		/** Buffer size */
		private static final int BUFFERSIZE = 2048;

		/** Input buffer */
		private int [] buf = new int [BUFFERSIZE];

		/** Index into buffer of next character to read */
		int inIndex = -1;

		/** Index into buffer of next character to return */
		int outIndex = -1;

		/** Number of available bytes */
		int available = 0;

		/**
		 * Constructor.
		 */
		public GUIMappedInputStream()
		{
			textPane.addKeyListener(this);
		}

		/**
		 * Returns the number of bytes available.
		 * @nowarn
		 */
		public int available()
		{
			return available;
		}

		/**
		 * Always returns false.
		 * @nowarn
		 */
		public boolean markSupported()
		{
			return false;
		}

		/**
		 * Reads a byte from the stream.
		 * This InputStream is a blocking stream - so you have to wait until
		 * the user has pressed a key.
		 *
		 * @return The next byte
		 * @exception IOException Never
		 */
		public int read()
			throws IOException
		{
			while (available == 0)
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
				}
			}

			outIndex++;
			if (outIndex == BUFFERSIZE)
				outIndex = 0;
			available--;
			return buf [outIndex];
		}

		/**
		 * Invoked when a key has been typed.
		 * This event occurs when a key press is followed by a key release.
		 * Implementation of KeyListener.
		 * @nowarn
		 */
		public void keyTyped(KeyEvent e)
		{
		}

		/**
		 * Invoked when a key has been pressed.
		 * Implementation of KeyListener.
		 * Puts the pressed key into the input queue.
		 * @nowarn
		 */
		public void keyPressed(KeyEvent e)
		{
			inIndex++;
			available++;
			if (inIndex == BUFFERSIZE)
				inIndex = 0;
			buf [inIndex] = e.getKeyCode();
		}

		/**
		 * Invoked when a key has been released.
		 * Implementation of KeyListener.
		 * @nowarn
		 */
		public void keyReleased(KeyEvent e)
		{
		}
	}
}
