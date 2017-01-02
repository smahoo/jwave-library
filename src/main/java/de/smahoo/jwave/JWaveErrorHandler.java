package de.smahoo.jwave;

/**
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public interface JWaveErrorHandler {
	void onError(String message, Throwable throwable);
}
