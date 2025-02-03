package com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl;

import com.io.chenosis.digitalcertificate.entity.Annotation;
import com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl.AbstractFieldHandler;
import com.io.chenosis.digitalcertificate.util.UtilityService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.IOException;

/**
 * Represents the TextFieldHandler class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation
 * @class TextFieldHandler
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/27/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/27/2025
 */
public class TextFieldHandler extends AbstractFieldHandler {

    @Override
    public void handleField(PDDocument document, PDAcroForm acroForm, Annotation fieldAnnotation) throws IOException {

        PDPage page = getPage(document, fieldAnnotation.getPageNo());

        // Calculate the Y-coordinate, adjusting for PDFBox's coordinate system
        float y = (float) (page.getCropBox().getHeight() -
                        (fieldAnnotation.getY() + ((fieldAnnotation.getHeight() + fieldAnnotation.getFontSize()) / 2.0)));

        // Set up the font and calculate text dimensions
        PDFont font = new PDType1Font(Standard14Fonts.FontName.valueOf(fieldAnnotation.getFont()));
        float textWidth = font.getStringWidth(fieldAnnotation.getFieldValue()) / 1000 * fieldAnnotation.getFontSize();
        float textHeight = fieldAnnotation.getFontSize();

        // Calculate dynamic margins
        float marginX = (fieldAnnotation.getWidth() - textWidth) / 2;
        float marginY = (fieldAnnotation.getHeight() - textHeight) / 2;

        float adjustedX = fieldAnnotation.getX() + marginX;
        float adjustedY = y + marginY;

        // Create a content stream to draw the text
        try (PDPageContentStream content = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            content.beginText();
            content.setNonStrokingColor(UtilityService.getColor(fieldAnnotation.getColor()));
            content.setFont(font, fieldAnnotation.getFontSize());
            content.newLineAtOffset(adjustedX, adjustedY);
            content.showText(fieldAnnotation.getFieldValue());
            content.endText();
        }

        // Create and configure the annotation
        PDAnnotationText annotationText = new PDAnnotationText();
        annotationText.setAnnotationName(fieldAnnotation.getId() + "-" + fieldAnnotation.getFieldType().name());
        annotationText.setRectangle(new PDRectangle(fieldAnnotation.getX(), y, fieldAnnotation.getWidth(), fieldAnnotation.getHeight()));
        annotationText.setLocked(true);
        annotationText.setLockedContents(true);
        annotationText.setPrinted(true);
        annotationText.setReadOnly(true);

        // Appearance settings
        PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
        appearanceStream.setBBox(annotationText.getRectangle().createRetranslatedRectangle());
        appearanceStream.setResources(new PDResources());
        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
        appearanceDictionary.setNormalAppearance(appearanceStream);

        annotationText.setAppearance(appearanceDictionary);

        // Add the annotation to the page
        page.getAnnotations().add(annotationText);

    }

    private String buildDefaultAppearance(Annotation fieldAnnotation) {
        String font = fieldAnnotation.getFont() != null ? fieldAnnotation.getFont() : "Helv";
        float fontSize = fieldAnnotation.getFontSize() > 0 ? fieldAnnotation.getFontSize() : 10;
        return String.format("/%s %.2f Tf 0 g", font, fontSize);
    }
}

