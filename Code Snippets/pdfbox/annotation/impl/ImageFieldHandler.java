package com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl;

import com.io.chenosis.digitalcertificate.entity.Annotation;
import com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl.AbstractFieldHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.IOException;
import java.util.Base64;

/**
 * Represents the ImageFieldHandler class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation
 * @class ImageFieldHandler
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/27/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/27/2025
 */
public class ImageFieldHandler extends AbstractFieldHandler {

    @Override
    public void handleField(PDDocument document, PDAcroForm acroForm, Annotation fieldAnnotation) throws IOException {
        PDPage page = getPage(document, fieldAnnotation.getPageNo());

        PDImageXObject image = PDImageXObject.createFromByteArray(
                document,
                Base64.getDecoder().decode(fieldAnnotation.getFieldValue()),
                null
        );

        try (PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true)) {
            float x = fieldAnnotation.getX();
            float y = page.getCropBox().getHeight() - (fieldAnnotation.getY() + fieldAnnotation.getHeight());
            contentStream.drawImage(image, x, y, fieldAnnotation.getWidth(), fieldAnnotation.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

