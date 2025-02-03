package com.io.chenosis.digitalcertificate.service.flyingsaucer;


import com.lowagie.text.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


@Service
public class HtmlToPdf {

    Logger log = LoggerFactory.getLogger(getClass());


    public void convertHtmltoPdfWithImage(String html, String name) throws DocumentException, IOException {
        Document jDoc = Jsoup.parse(html);
        W3CDom w3cDom = new W3CDom();

        org.w3c.dom.Document content = w3cDom.fromJsoup(jDoc);
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setReplacedElementFactory(new MediaReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory(), new File(name)));
        renderer.setDocument(content, null);
        renderer.layout();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        renderer.createPDF(baos);
    }

    public void convertHtmlToPdfUsingBuffer(String html, String name) {
        try {
            StringBuffer buf = new StringBuffer();
            buf.append(html);

            Document jDoc = Jsoup.parse(html);
            W3CDom w3cDom = new W3CDom();

            org.w3c.dom.Document doc = w3cDom.fromJsoup(jDoc); //db.parse(is);


            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(doc, null);

            String outputFile = name;
            OutputStream os = new FileOutputStream(outputFile);
            renderer.layout();
            renderer.createPDF(os);
            os.close();
        } catch (Exception e) {

        }

    }

    public void converHtmlToPdf(String html, String name) {
        try {
            // HTML file - Input
            String inputHTML = html;
            // Converted PDF file - Output
            File outputPdf = new File(name);

            //create well formed HTML
            String xhtml = createWellFormedHtml(inputHTML);
            System.out.println("Starting conversion to PDF...");
            xhtmlToPdf(xhtml, outputPdf);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void converHtmlToPdf() {
        try {
            // HTML file - Input
            File inputHTML = new File(HtmlToPdf.class.getClassLoader().getResource("template/Test.html").getFile());
            // Converted PDF file - Output
            File outputPdf = new File("F:\\NETJS\\Test.pdf");
            HtmlToPdf htmlToPdf = new HtmlToPdf();
            //create well formed HTML
            String xhtml = htmlToPdf.createWellFormedHtml(inputHTML);
            System.out.println("Starting conversion to PDF...");
            htmlToPdf.xhtmlToPdf(xhtml, outputPdf);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String createWellFormedHtml(String inputHTML) throws IOException {
        Document document = Jsoup.parse(inputHTML, "UTF-8");
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        System.out.println("HTML parsing done...");
        return document.html();
    }

    private String createWellFormedHtml(File inputHTML) throws IOException {
        Document document = Jsoup.parse(inputHTML, "UTF-8");
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        System.out.println("HTML parsing done...");
        return document.html();
    }

    private void xhtmlToPdf(String xhtml, File outputPdf) throws IOException {
        OutputStream outputStream = null;
        try {
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            // Register custom ReplacedElementFactory implementation
            sharedContext.setReplacedElementFactory(new ReplacedElementFactoryImpl());
            sharedContext.getTextRenderer().setSmoothingThreshold(0);
            // Register additional font
//            renderer.getFontResolver().addFont(getClass().getClassLoader().getResource("fonts/PRISTINA.ttf").toString(), true);
            // Setting base URL to resolve the relative URLs
//            String baseUrl = FileSystems.getDefault()
//                    .getPath("F:\\", "Anshu\\NetJs\\Programs\\", "src\\main\\resources\\css")
//                    .toUri()
//                    .toURL()
//                    .toString();
            renderer.setDocumentFromString(xhtml, null);
            renderer.layout();
            outputStream = new FileOutputStream(outputPdf);
            renderer.createPDF(outputStream);
            System.out.println("PDF creation completed");
        } catch (com.lowagie.text.DocumentException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
    }
}
