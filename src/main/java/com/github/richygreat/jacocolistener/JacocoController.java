package com.github.richygreat.jacocolistener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.tools.ExecFileLoader;

class JacocoController {

	private static final String ERROR = "Unable to access JaCoCo Agent - make sure that you use JaCoCo and version not lower than 0.6.2.";

	private final IAgent agent;

	private boolean testStarted;

	// Visible for testing
	static JacocoController singleton;

	private JacocoController() {
		try {
			this.agent = RT.getAgent();
		} catch (Exception e) {
			throw new JacocoControllerError(ERROR, e);
		} catch (NoClassDefFoundError e) {
			throw new JacocoControllerError(ERROR, e);
		}
	}

	JacocoController(IAgent agent) {
		this.agent = agent;
	}

	public static synchronized JacocoController getInstance() {
		if (singleton == null) {
			singleton = new JacocoController();
		}
		return singleton;
	}

	public synchronized void onTestStart() {
		if (testStarted) {
			throw new JacocoControllerError(
					"Looks like several tests executed in parallel in the same JVM, thus coverage per test can't be recorded correctly.");
		}
		// Dump coverage between tests
		dump("");
		testStarted = true;
	}

	public synchronized void onTestFinish(String name) {
		// Dump coverage for test
		dump(name);
		testStarted = false;
	}

	private void dump(String sessionId) {
		if (sessionId == null || sessionId.trim().length() == 0) {
			return;
		}
		agent.setSessionId(sessionId);
		try {
			File file = File.createTempFile("jacoc", "exec");
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(agent.getExecutionData(true));
				fos.flush();
			}
			ExecFileLoader loader = new ExecFileLoader();
			loader.load(file);

			System.err.println("===================>" + sessionId);
			for (ExecutionData ed : loader.getExecutionDataStore().getContents()) {
				if (ed.getName().contains("com/github/richygreat/springbootsonar/service")) {
					System.err.println(ed.getName() + "=" + ed.hasHits());
				}
			}

			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
			throw new JacocoControllerError(e);
		}
	}

	public static class JacocoControllerError extends Error {
		private static final long serialVersionUID = 4475084778142015394L;

		public JacocoControllerError(String message) {
			super(message);
		}

		public JacocoControllerError(String message, Throwable cause) {
			super(message, cause);
		}

		public JacocoControllerError(Throwable cause) {
			super(cause);
		}
	}
}