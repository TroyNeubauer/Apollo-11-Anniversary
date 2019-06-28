package com.troy.apollo11anni;

import javax.swing.SwingUtilities;

public class Main {
	
	static Window window = null;

	public static void main(String[] args) throws InterruptedException {
		final Apollo11 apollo = new Apollo11();
		SwingUtilities.invokeLater(() -> {
			window = new Window(apollo);
		});
		Thread.sleep(1000);
		while(window == null) {
			Thread.sleep(10);
		}
		while (true) {
			window.update();
			apollo.update();
			Thread.sleep(100);
		}
	}

}
