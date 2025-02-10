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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the TextAreaHandler class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation
 * @class TextAreaHandler
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/27/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/27/2025
 */
public class TextAreaHandler extends AbstractFieldHandler {

    @Override
    public void handleField(PDDocument document, PDAcroForm acroForm, Annotation fieldAnnotation) throws IOException, IOException {

        PDPage page = getPage(document, fieldAnnotation.getPageNo());

        // Calculate Y-coordinate in PDFBox's coordinate system
        float boxHeight = fieldAnnotation.getHeight();
        float boxWidth = fieldAnnotation.getWidth();
        float fontSize = fieldAnnotation.getFontSize();
        float x = fieldAnnotation.getX();
        float y = fieldAnnotation.getY();

        // Define internal margins
        float marginLeft = 5;
        float marginTop = 5;

        // Adjusted positions
        float adjustedX = x + marginLeft;
        float adjustedY = page.getCropBox().getHeight() - (y + boxHeight) + boxHeight - marginTop;

        // Font and color settings
        PDFont font = new PDType1Font(Standard14Fonts.FontName.valueOf(fieldAnnotation.getFont()));
        float maxLineWidth = boxWidth - (2 * marginLeft);

        // Prepare text for multiline rendering
        List<String> lines = new ArrayList<>();
        String text = fieldAnnotation.getFieldValue();
        String[] words = text.replaceAll("\n", " ~~~\n").split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if ("~~~".equals(word)) {
                // Handle explicit line breaks
                lines.add(currentLine.toString().strip());
                currentLine.setLength(0);
            } else {
                float lineWidth = font.getStringWidth(currentLine + " " + word) / 1000 * fontSize;
                if (lineWidth > maxLineWidth) {
                    // Add current line if it exceeds the maximum width
                    lines.add(currentLine.toString().strip());
                    currentLine.setLength(0);
                }
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().strip());
        }

        // Render text content
        try (PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            content.beginText();
            content.setFont(font, fontSize);
            content.setNonStrokingColor(UtilityService.getColor(fieldAnnotation.getColor()));
            content.newLineAtOffset(adjustedX, adjustedY);

            for (String line : lines) {
                content.showText(line);
                adjustedY -= fontSize; // Move to the next line
                if (adjustedY < marginTop) {
                    break; // Stop if text goes beyond the annotation box
                }
            }

            content.endText();
        }

        // Create and configure the annotation
        PDAnnotationText annotationText = new PDAnnotationText();
        annotationText.setAnnotationName(fieldAnnotation.getId() + "-" + fieldAnnotation.getFieldType().name());
        annotationText.setRectangle(new PDRectangle(x, page.getCropBox().getHeight() - (y + boxHeight), boxWidth, boxHeight));
        annotationText.setLocked(true);
        annotationText.setLockedContents(true);
        annotationText.setPrinted(true);
        annotationText.setReadOnly(true);

        // Set appearance stream
        PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
        appearanceStream.setBBox(new PDRectangle(boxWidth, boxHeight));
        appearanceStream.setResources(new PDResources());
        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
        appearanceDictionary.setNormalAppearance(appearanceStream);
        annotationText.setAppearance(appearanceDictionary);

        // Add annotation to the page
        page.getAnnotations().add(annotationText);


//        PDPage page = getPage(document, fieldAnnotation.getPageNo());
//
//        float x = fieldAnnotation.getX();
//        float y = fieldAnnotation.getHeight(); //page.getCropBox().getHeight() - (fieldAnnotation.getY() + fieldAnnotation.getHeight());
//
//        // Create an image annotation
//        PDAnnotationText annotationText = new PDAnnotationText();
//        annotationText.setAnnotationName(fieldAnnotation.getId() + "-" + fieldAnnotation.getFieldType().name());
//        annotationText.setRectangle(new PDRectangle(x, y, fieldAnnotation.getWidth(), fieldAnnotation.getHeight())); // Set position and size
//
//        // Create an appearance stream
//        PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
//        appearanceStream.setBBox(annotationText.getRectangle().createRetranslatedRectangle());
//        appearanceStream.setResources(new PDResources());
//
//        // First add some text, two lines we'll add some annotations to this later
//        PDFont font = new PDType1Font(Standard14Fonts.FontName.valueOf(fieldAnnotation.getFont()));
//
//        PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
//
//        List<String> lines = new ArrayList<>();
//
//        float boxHeight = fieldAnnotation.getHeight();
//        float boxWidth = fieldAnnotation.getWidth();
//
//        float fontSize = fieldAnnotation.getFontSize();
//        String text = fieldAnnotation.getFieldValue();
//
//        float internalMarginLeft = 5; // Adjust based on your specific case
//        float internalMarginTop = 5;
//        float adjustedX = x + internalMarginLeft;
//        float adjustedY = y + boxHeight - internalMarginTop;
//
//        // Precompute the maximum allowed line width
//        float maxLineWidth = boxWidth - internalMarginLeft;
//
//        // Split text into words, handling newlines
//        String[] words = text.replaceAll("[\n]", " ~~~\n").split("[\s\n]+");
//
//        StringBuilder currentLine = new StringBuilder();
//        for (String word : words) {
//            if (word.equalsIgnoreCase("~~~")) {
//                // Add a new line for the placeholder
//                lines.add(currentLine.toString());
//                currentLine.setLength(0); // Reset the current line
//                lines.add(" "); // Add an empty line for the newline character
//            } else {
//                // Calculate the width of the current line + the new word
//                float lineWidth = fontSize * font.getStringWidth(currentLine + " " + word) / 1000;
//
//                if (lineWidth > maxLineWidth) {
//                    // If the line would be too long, add the current line to the list
//                    lines.add(currentLine.toString());
//                    currentLine.setLength(0); // Reset the current line
//                }
//
//                // Add the word to the current line
//                if (currentLine.length() > 0) {
//                    currentLine.append(" ");
//                }
//                currentLine.append(word);
//            }
//        }
//
//        // Add the last line if it exists
//        if (currentLine.length() > 0) {
//            lines.add(currentLine.toString());
//        }
//
//        // Write all lines to the PDF
//        content.beginText();
//        content.setFont(font, fontSize);
//        content.setNonStrokingColor(UtilityService.getColor(fieldAnnotation.getColor()));
//        for (String line : lines) {
//            content.newLineAtOffset(adjustedX, adjustedY);
//            content.showText(line);
//            adjustedY -= fontSize; // Move to the next line
//        }
//        content.endText();
//        content.close();
//
//        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
//        appearanceDictionary.setNormalAppearance(appearanceStream);
//
//        annotationText.setLocked(true);
//        annotationText.setLockedContents(true);
//        annotationText.setPrinted(true);
//        annotationText.setReadOnly(true);
//        annotationText.setAppearance(appearanceDictionary);
//
//
//        page.getAnnotations().add(annotationText);

    }
}

