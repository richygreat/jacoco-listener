package com.github.richygreat.jacocolistener;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * JUnit listener that instructs JaCoCo to create one session per test.
 */
public class JUnitListener extends RunListener {

	protected JacocoController jacoco;

	@Override
	public void testStarted(Description description) {
		getJacocoController().onTestStart();
	}

	@Override
	public void testFinished(Description description) {
		jacoco.onTestFinish(getName(description));
	}

	protected JacocoController getJacocoController() {
		if (jacoco == null) {
			jacoco = JacocoController.getInstance();
		}
		return jacoco;
	}

	private static String getName(Description description) {
		return description.getClassName() + " " + description.getMethodName();
	}
}