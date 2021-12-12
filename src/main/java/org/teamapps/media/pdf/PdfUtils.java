package org.teamapps.media.pdf;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;


public class PdfUtils {

	public static byte[] createPdfThumbnail(File pdfFile) {
		return createPdfThumbnail(pdfFile, 40);
	}

	public static File createPdfThumbnailAsFile(File pdfFile, int resolution) {
		try {
			byte[] bytes = createPdfThumbnail(pdfFile, resolution);
			if (bytes != null) {
				File file = File.createTempFile("preview", ".jpg");
				Files.copy(new ByteArrayInputStream(bytes), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return file;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] createPdfThumbnail(File pdfFile, int resolution) {
		try {
			PDDocument document = PDDocument.load(pdfFile);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			BufferedImage image = pdfRenderer.renderImageWithDPI(0, resolution, ImageType.RGB);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, "JPG", bos);
			document.close();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<File> createPdfBoxPages(File pdfFile, int maxFiles, int resolution, int jpegQuality, int maxPngSize) throws Exception {
		long time = System.currentTimeMillis();
		List<File> pageFiles = new ArrayList<>();
		PDDocument document = PDDocument.load(pdfFile);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		int numberOfPages = document.getNumberOfPages();
		int max = Math.min(maxFiles, numberOfPages);
		for (int page = 0; page < max; page++) {
			BufferedImage image = pdfRenderer.renderImageWithDPI(page, resolution, ImageType.RGB);
			File png = File.createTempFile("temp", ".png");
			ImageIO.write(image, "PNG", png);
			if (png.length() > maxPngSize) {
				File jpg = File.createTempFile("temp", ".jpg");
				ImageIO.write(image, "JPG", jpg);
				if (jpg.length() > 0 && png.length() > 0 && jpg.length() < png.length()) {
					png = jpg;
				}
			}
			pageFiles.add(png);
		}
		document.close();
		System.out.println("TIME:" + (System.currentTimeMillis() - time));
		return pageFiles;
	}


	public static int getPdfPageCount(File pdfFile) {
		try {
			PDDocument document = PDDocument.load(pdfFile);
			//more infos with: PDDocumentInformation info = document.getDocumentInformation();
			int pages = document.getNumberOfPages();
			document.close();
			return pages;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
}