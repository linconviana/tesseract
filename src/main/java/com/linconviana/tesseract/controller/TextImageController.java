package com.linconviana.tesseract.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linconviana.tesseract.service.TextImageService;

import net.sourceforge.tess4j.TesseractException;

@RestController
@RequestMapping(value="/tesseract")
public class TextImageController {

	@Autowired
	private TextImageService service;
	
	//http://localhost:8080/tesseract
	@GetMapping(value = "/image")
	public String getTextImage() throws TesseractException {
		
		return service.getTextImage();
	}
	
	@GetMapping(value = "/pdfimage")
	public String getTextPdfImage() throws TesseractException {
		
		return service.getTextPDFImage();
	}
	
	@GetMapping(value = "/pdf")
	public String getTextPdf() {
		
		return service.getTextPDF();
	}
	
	@GetMapping(value = "/pdfimageAvancado")
	public String getTextPDFImageAvancado() throws TesseractException, IOException {
		
		return service.getTextPDFImageAvancado();
	}
}
