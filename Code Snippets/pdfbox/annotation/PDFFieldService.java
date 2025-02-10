package com.io.chenosis.digitalcertificate.service.pdfbox.annotation;

import com.io.chenosis.digitalcertificate.entity.Annotation;
import com.io.chenosis.digitalcertificate.enums.FieldTypes;
import com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl.FieldHandlerFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Represents the PDFFieldService class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation
 * @class PDFFieldService
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/24/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/24/2025
 */
public class PDFFieldService {

    public byte[] addFieldsToPDF(Long documentId, List<Annotation> fieldAnnotations) throws IOException {
        byte[] documentBytes = fetchDocumentBytes(documentId);

        try (PDDocument document = Loader.loadPDF(documentBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = initializeAcroForm(document);

            for (Annotation fieldAnnotation : fieldAnnotations) {
                FieldHandler handler = FieldHandlerFactory.getHandler(fieldAnnotation.getFieldType());
                handler.handleField(document, acroForm, fieldAnnotation);
            }

            document.saveIncremental(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] addFieldsToPDF(byte[] documentBytes, List<Annotation> fieldAnnotations) throws IOException {

        try (PDDocument document = Loader.loadPDF(documentBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = initializeAcroForm(document);

            for (Annotation fieldAnnotation : fieldAnnotations) {
                FieldHandler handler = FieldHandlerFactory.getHandler(fieldAnnotation.getFieldType());
                handler.handleField(document, acroForm, fieldAnnotation);
            }

            document.saveIncremental(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void addField(PDAcroForm acroForm, PDPage page, Annotation annotation) throws IOException {
        String fieldName = annotation.getFieldName();
        FieldTypes fieldType = annotation.getFieldType();
        float x = annotation.getX();
        float y = annotation.getY();
        float width = annotation.getWidth();
        float height = annotation.getHeight();

        PDField field;

        if (fieldType == FieldTypes.TEXT || fieldType == FieldTypes.TEXTAREA || fieldType == FieldTypes.DATE) {
            field = new PDTextField(acroForm);
            ((PDTextField) field).setMultiline(fieldType == FieldTypes.TEXTAREA);
            if (fieldType == FieldTypes.DATE) {
                ((PDTextField) field).setValue(annotation.getFieldValue()); // Pre-fill with date value
            } else {
                ((PDTextField) field).setValue(annotation.getFieldValue());
            }
        } else {
            throw new IllegalArgumentException("Unsupported field type for this method: " + fieldType);
        }

        field.setPartialName(fieldName);
        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, width, height));
        widget.setPage(page);

        field.getWidgets().add(widget);
        page.getAnnotations().add(widget);
        acroForm.getFields().add(field);
    }


    private PDAcroForm initializeAcroForm(PDDocument document) {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null) {
            acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
        }
        setupDefaultAppearance(acroForm);
        return acroForm;
    }

    private void setupDefaultAppearance(PDAcroForm acroForm) {
        PDResources resources = new PDResources();
        resources.put(COSName.HELV, new PDType1Font(Standard14Fonts.FontName.HELVETICA));
        acroForm.setDefaultResources(resources);
        acroForm.setDefaultAppearance("/Helv 10 Tf 0 g");
    }

    private byte[] fetchDocumentBytes(Long documentId) {
        // Fetch document bytes from a data source

        return new byte[0]; // Replace with actual implementation
    }
}

