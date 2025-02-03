package com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl;

import com.io.chenosis.digitalcertificate.entity.Annotation;
import com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl.AbstractFieldHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.IOException;

/**
 * Represents the DateFieldHandler class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation
 * @class DateFieldHandler
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/27/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/27/2025
 */
public class DateFieldHandler extends AbstractFieldHandler {

    @Override
    public void handleField(PDDocument document, PDAcroForm acroForm, Annotation fieldAnnotation) throws IOException {
        PDPage page = getPage(document, fieldAnnotation.getPageNo());
        PDTextField dateField = new PDTextField(acroForm);
        dateField.setPartialName(fieldAnnotation.getFieldName());

        dateField.setValue(fieldAnnotation.getFieldValue()); // Ensure date format is handled before this
        dateField.setDefaultAppearance("/Helv 10 Tf 0 g");

        PDAnnotationWidget widget = dateField.getWidgets().get(0);
        setupWidget(widget, page, fieldAnnotation);

        page.getAnnotations().add(widget);
        acroForm.getFields().add(dateField);
    }
}

