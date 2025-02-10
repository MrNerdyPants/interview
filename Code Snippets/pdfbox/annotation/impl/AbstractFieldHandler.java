package com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl;

import com.io.chenosis.digitalcertificate.entity.Annotation;
import com.io.chenosis.digitalcertificate.service.pdfbox.annotation.FieldHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

/**
 * Represents the AbstractFieldHandler class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl
 * @class AbstractFieldHandler
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/27/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/27/2025
 */
public abstract class AbstractFieldHandler implements FieldHandler {

    protected void setupWidget(PDAnnotationWidget widget, PDPage page, Annotation fieldAnnotation) {
        float x = fieldAnnotation.getX();
        float y = page.getCropBox().getHeight() - (fieldAnnotation.getY() + fieldAnnotation.getHeight());
        widget.setRectangle(new PDRectangle(x, y, fieldAnnotation.getWidth(), fieldAnnotation.getHeight()));
        widget.setPage(page);
    }

    protected PDPage getPage(PDDocument document, int pageNo) {
        return document.getPage(pageNo - 1);
    }
}

