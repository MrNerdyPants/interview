package com.io.chenosis.digitalcertificate.service.flyingsaucer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents the HtmlToPdfWithPDFBox class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service
 * @class HtmlToPdfWithPDFBox
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/20/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/20/2025
 */
public class HtmlToPdfWithPDFBox {


    private static String htmlToXhtml(String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.html();
    }

    private static void xhtmlToPdf(String xhtml, String outFileName) throws IOException {
        File output = new File(outFileName);
        ITextRenderer iTextRenderer = new ITextRenderer();
        iTextRenderer.setDocumentFromString(xhtml);
        iTextRenderer.layout();
        OutputStream os = new FileOutputStream(output);
        iTextRenderer.createPDF(os);
        os.close();
    }

    public static void convertHtmlToPdf(String htmlContent, String outputFilePath) {
        try (OutputStream os = new FileOutputStream(outputFilePath)) {
            // Clean and parse the HTML content using Jsoup
//            Document jsoupDocument = Jsoup.parse(htmlContent);
//            String cleanedHtml = jsoupDocument.html();
            String cleanedHtml = htmlToXhtml(htmlContent);

            // Create a renderer instance
            ITextRenderer renderer = new ITextRenderer();

            // Set the HTML content
            renderer.setDocumentFromString(cleanedHtml.startsWith("<!DOCTYPE html>") ? cleanedHtml : "<!DOCTYPE html>" + cleanedHtml);

            // Layout and create the PDF
            renderer.layout();
            renderer.createPDF(os);

            System.out.println("PDF created successfully at: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error while generating PDF: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {

        // Load your HTML document
        Path path = Paths.get("index.html");
        String html = Files.readString(path);
//        convertHtmlToPdf(html, "HtmlToPdf.pdf");
        HtmlToPdf htmlToPdf = new HtmlToPdf();
        htmlToPdf.converHtmlToPdf(html, "HtmlToPdf.pdf");



    }
}
