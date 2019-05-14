package main;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

class Main {


	public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
		Chuu stan = new Chuu();
		if (System.getProperty("file.encoding").equals("UTF-8"))
			stan.setupBot();
		else
			stan.relaunchInUTF8();

	}


	public static File getThisJarFile() throws UnsupportedEncodingException {
		//Gets the path of the currently running Jar file
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");

		//This is code especially written for running and testing this program in an IDE that doesn't compile to .jar when running.
		if (!decodedPath.endsWith(".jar")) {
			return new File("Chuu.jar");
		}
		return new File(decodedPath);   //We use File so that when we send the path to the ProcessBuilder, we will be using the proper System path formatting.
	}
}

