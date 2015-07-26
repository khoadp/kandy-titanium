package io.kandy.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.*;

public class FileUtils {
	private static String[] mSupportedFiles = new String[] { ".png", ".pdf" };

	public static void copyAssets(Context context, File targetDir) {
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		for (String filename : files) {

			if (isSupported(filename)) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open(filename);

					File outFile = new File(targetDir, filename);

					out = new FileOutputStream(outFile);
					copyFile(in, out);
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							Log.e("tag", "Failed to close inputstream: " + filename, e);
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							Log.e("tag", "Failed to close outputstream: " + filename, e);
						}
					}
				}
			}
		}
	}

	public static File getFilesDirectory(String name) {
		File file = new File(Environment.getExternalStorageDirectory(), name);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private static boolean isSupported(String fileName) {
		for (int i = 0; i < mSupportedFiles.length; i++) {
			if (fileName.endsWith(mSupportedFiles[i])) {
				return true;
			}
		}
		return false;
	}

	public static void clearDirectory(File dir) {
		String[] list = dir.list();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File file = new File(dir, list[i]);
				file.delete();
			}
		}
	}

}
