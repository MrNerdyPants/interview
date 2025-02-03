package com.io.chenosis.digitalcertificate.service.pdfbox.annotation;

import com.io.chenosis.digitalcertificate.entity.Annotation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.IOException;

/**
 * Represents the FieldHandler class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation
 * @class FieldHandler
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/24/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/24/2025
 */
public interface FieldHandler {
    void handleField(PDDocument document, PDAcroForm acroForm, Annotation fieldAnnotation) throws IOException;
}

