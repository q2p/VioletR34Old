package q2p.violetr34.engine;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class FileLoader {
	public static void send(final String path, final RequestInfo request, final ResponseInfo response) {
		InputStream is;
		if(path.startsWith("s/")) {
			try {
				is = Assist.getResourceAsStream(path.substring(2));
			} catch(FileNotFoundException e) {
				response.setCode(ResponseCode._404_Not_Found);
				response.setContentType(ContentType.text_plain);
				response.setBody("File " + path + " not found!");
				return;
			}
		} else if(path.startsWith("d/")) {
			final File file = new File(Assist.MAIN_FOLDER + path.substring(2));
			is = trySendThumbedImage(file, request, response);
			if(is == null) {
				try {
					is = new FileInputStream(file);
				} catch(FileNotFoundException e) {
					response.setCode(ResponseCode._404_Not_Found);
					response.setContentType(ContentType.text_plain);
					response.setBody("File " + path + " not found!");
					return;
				}
			}
		} else {
			response.setData(ResponseCode._406_Not_Acceptable, ContentType.text_plain, "Invalid path!");
			return;
		}

		response.setCode(ResponseCode._200_OK);
		response.setContentType(ContentType.getByExtention(Assist.getPathExtention(path)));
		
		try {
			response.flushHeader();
			final int off = Math.min(VioletR34.MAX_BUFF, is.available());
			final byte[] buff = new byte[off];
			is.read(buff);
			is.close();
			response.pushBody(buff);
		} catch (IOException e) {}
	}

	private static InputStream trySendThumbedImage(final File file, final RequestInfo request, final ResponseInfo response) {
		if(request.getArgument("thumb") == null) return null;
		final int sz = Integer.parseInt(request.getArgument("thumb"));
		if(sz > 1000) return null;
		final BufferedImage simg;
		final BufferedImage timg;
		try {
			try { simg = ImageIO.read(file); }
			catch(IOException e) { return null; }
			if(simg.getHeight() >= simg.getWidth()) {
				if(simg.getHeight() <= sz) return null;
				final int smallerSizeX = (int)((float)(sz*simg.getWidth())/(float)simg.getHeight());
				timg = new BufferedImage(smallerSizeX, sz, BufferedImage.TYPE_INT_ARGB);
				timg.getGraphics().drawImage(simg, 0, 0, smallerSizeX, sz, 0, 0, simg.getWidth(), simg.getHeight(), null);
			} else {
				if(simg.getWidth() <= sz) return null;
				final int smallerSizeY = (int)((float)(sz*simg.getHeight())/(float)simg.getWidth());
				timg = new BufferedImage(sz, smallerSizeY, BufferedImage.TYPE_INT_ARGB);
				timg.getGraphics().drawImage(simg, 0, 0, sz, smallerSizeY, 0, 0, simg.getWidth(), simg.getHeight(), null);
			}
			System.gc();
		} catch(OutOfMemoryError e) { System.gc(); return null; }
		
		try {
			final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ImageIO.write(timg, "png", byteStream);
			System.gc();
			return new ByteArrayInputStream(byteStream.toByteArray());
		} catch (IOException | OutOfMemoryError e) { System.gc(); return null; }
	}
}