package de.smahoo.jwave.utils.logger;


/**
 * This interface is responsable to wrap any logger. As example
 * the JWave library is developed without additional libraries to have a small component that
 * could easily used by other software. Each software, that uses the JWave library could add
 * their own logger.
 *
 * @author Mathias Runge (mathias.runge@smahoo.de)
 *
 */
public interface Logger {

	void log(LogTag tag, String message);
	void log(LogTag tag, String message, Throwable throwable);
	
}
