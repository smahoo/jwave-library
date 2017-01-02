package de.smahoo.jwave.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public interface JWaveConnection {

	InputStream getInputStream();
	OutputStream getOutputStream();
}
