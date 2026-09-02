package dev.mori.patchiom.log;

import dev.mori.patchiom.cli.Colors;

public interface OutputAdapter {

	void success(String message);
	void info(String message);
	void error(String message);
	void progress(String message);
	void empty();

	class Raw implements OutputAdapter {

		@Override
		public void success(String message) {
			System.out.println(message);
		}

		@Override
		public void info(String message) {
			System.out.println(message);
		}

		@Override
		public void error(String message) {
			System.err.println(message);
		}

		@Override
		public void progress(String message) {
			System.out.println(message);
		}

		@Override
		public void empty() {
			System.out.println();
		}

	}

	class Formatted implements OutputAdapter {

		@Override
		public void success(String message) {
			System.out.println(Colors.GREEN + message + Colors.RESET);
		}

		@Override
		public void info(String message) {
			System.out.println(Colors.CYAN + message + Colors.RESET);
		}

		@Override
		public void error(String message) {
			System.out.println(Colors.RED + message + Colors.RESET);
		}

		@Override
		public void progress(String message) {
			System.out.println(Colors.GRAY + message + Colors.RESET);
		}

		@Override
		public void empty() {
			System.out.println();
		}
	}


}
