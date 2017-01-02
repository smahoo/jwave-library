package de.smahoo.jwave.test.utils;


import org.junit.Test;

import de.smahoo.jwave.utils.logger.LogTag;
import de.smahoo.jwave.utils.logger.LoggerConsolePrinter;

public class TestLogger {

	@Test
	public void test_ConsolePrinting() {
		LoggerConsolePrinter printer = new LoggerConsolePrinter("Testing Logger");
		printer.log(LogTag.DEBUG,"Testing Debug Message");
		printer.log(LogTag.INFO,"Testing Info Message");
		printer.log(LogTag.WARN,"Testing Warning Message");
		printer.log(LogTag.ERROR,"Testing Error Message 1-2");
		printer.log(LogTag.DEBUG, "!!!! The following TestException is part of the test and was expected to be thrown!");		
		printer.log(LogTag.ERROR,"Testing Error Message 2-2",new Exception("TestException"));
	}

}
