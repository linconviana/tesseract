package com.linconviana.tesseract.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmlgraphics.image.rendered.BufferedImageCachableRed;
import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class TextImageService {

	/// :: https://www.youtube.com/watch?app=desktop&v=lgTfKTFvLew
	public String getTextImage() throws TesseractException {

		ITesseract tesseract = new Tesseract();

		tesseract.setDatapath("C:\\tesseract\\tessdata\\pt-br");
		tesseract.setLanguage("por");

		String texto = tesseract.doOCR(new File("C:\\tesseract\\imagem.png"));

		/*
		 * tesseract.setDatapath("C:\\tesseract\\tessdata\\eng");
		 * tesseract.setLanguage("eng");
		 * 
		 * String texto = tesseract.doOCR(new File("C:\\tesseract\\imagem-eng.png"));
		 */

		/*tesseract.setDatapath("C:\\tesseract\\tessdata\\span");
		tesseract.setLanguage("spa");

		String texto = tesseract.doOCR(new File("C:\\tesseract\\imagem-span.png"));*/
		
		return texto;
	}

	public String getTextPDFImage() throws TesseractException {

		//File file = new File("C:\\tesseract\\pdf imagem.pdf"); // ok
		File file = new File("C:\\tesseract\\pdf imagem com texto e imagem.pdf"); // ok mais com caracteres especias nas areas das imagens

		try {

			PDDocument document = PDDocument.load(file);
			PDFRenderer pdfRenderer = new PDFRenderer(document);

			ITesseract tesseract = new Tesseract();

			tesseract.setDatapath("C:\\tesseract\\tessdata\\pt-br");
			tesseract.setLanguage("por");

			StringBuilder extractedText = new StringBuilder();

			for (int page = 0; page < document.getNumberOfPages(); ++page) {
				BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
				String text = tesseract.doOCR(bim);
				extractedText.append(text).append("\n");
			}

			return extractedText.toString();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return null;

	}

	public String getTextPDF() {

		try {
			File file = new File("C:\\tesseract\\texto pdf.pdf"); // ok
			//File file = new File("C:\\tesseract\\pdf com 2 imagens.pdf"); // OK


			PDDocument document = PDDocument.load(file);

			PDFTextStripper pdfTextStripper = new PDFTextStripper();

			String textoPdf = pdfTextStripper.getText(document);
			return !textoPdf.equals("\r\n") ? textoPdf : "vazio";

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return null;
	}
	// https://www.youtube.com/watch?v=xN7bpwukuR4
	// https://www.youtube.com/watch?v=5DqW9KP-aQo
	
	public String getTextPDFImageAvancado() throws TesseractException, IOException {

		//File file = new File("C:\\tesseract\\pdf imagem.pdf"); // ok
		File file = new File("C:\\tesseract\\pdf imagem com texto e imagem.pdf"); // ok
		//File file = new File("C:\\tesseract\\unimed.pdf"); // ok 
		
		byte[] inFileBytes = Files.readAllBytes(file.toPath());
		
		List<byte[]> pdfByteList = extractPagesAsImages(inFileBytes);
		String content = extractTextualContent(pdfByteList);
	
		/// :: https://regex101.com/
		/// :: https://chatgpt.com/
		String dataDocumento = getTextWithRegex(content, "Data (\\d{2}\\/\\d{2}\\/\\d{4})", 1);
		String numeroDocumento = getTextWithRegex(content, "Número doc: (\\d{3}.\\d{3}.\\d{3}-\\d{2})", 1);
		String cnpjDocumento = getTextWithRegex(content, "CNPJ: (\\d{2}.\\d{3}.\\d{3}\\/\\d{4}-\\d{2})", 1);
		String classificacao = getTextWithRegex(content, "classificação\\s+(\\bLivre\\b)", 1);
		String horaDocumento = getTextWithRegex(content, "Data (\\d{2}\\/\\d{2}\\/\\d{4} as (\\d{2}:\\d{2}:\\d{2}))", 2);
		
		StringBuilder sb = new StringBuilder();

        // Adiciona strings ao StringBuilder
        sb.append("Este é o conteudo do texto, " + content);
        sb.append("\n\n\n");
        sb.append("Aqui estão as partes isoladas que recuperei deste testo:");
        sb.append("\n\n");
        sb.append("dataDocumento: " + dataDocumento);
        sb.append("\n");
        sb.append("numeroDocumento: " + numeroDocumento);
        sb.append("\n");
        sb.append("cnpjDocumento: " + cnpjDocumento);
        sb.append("\n");
        sb.append("classificacao: " + classificacao);
        sb.append("\n");
        sb.append("horaDocumento: " + horaDocumento);
        
		return sb.toString();

	}

	private List<byte[]> extractPagesAsImages(byte[] inFileBytes) throws IOException {

		
		PDDocument document = PDDocument.load(inFileBytes);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		List<byte[]> result = new ArrayList<>();	
		int dpi = 300;
		
		for(int i = 0; i < document.getNumberOfPages(); ++i) {
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			BufferedImage bImage = pdfRenderer.renderImageWithDPI(i, dpi, ImageType.RGB);
			ImageIO.write(bImage, "png", bos);
			result.add(bos.toByteArray());
			bos.close();
		}
		
		document.close();
		
		return result;
	}
	
	private String extractTextualContent(List<byte[]> pdfByteList) {

		ITesseract tesseract = new Tesseract();

		tesseract.setDatapath("C:\\tesseract\\tessdata\\pt-br");
		tesseract.setLanguage("por");
		
		return pdfByteList.stream()
				.map(imgBytes -> {
					try {
						
						ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes);
						final BufferedImage img = ImageIO.read(bis);
						return tesseract.doOCR(img);
						
					} catch (IOException | TesseractException e) {
						throw new RuntimeException("Error ao extrair imagem");
					}
					
				}).collect(Collectors.joining("\n"));
	}
	
	private String getTextWithRegex(String content, String regex, int group) {
		
		Pattern pattern = Pattern.compile(regex);
		
		Matcher matcher = pattern.matcher(content);
		
		return matcher.find() ? matcher.group(group) : null;
	}

}
