package com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl;

import com.io.chenosis.digitalcertificate.enums.FieldTypes;
import com.io.chenosis.digitalcertificate.service.pdfbox.annotation.FieldHandler;

/**
 * Represents the FileHandlerFactory class in the chenosis-digital-certificate project.
 *
 * @author Kashan Asim
 * @version 1.0
 * @project chenosis-digital-certificate
 * @module com.io.chenosis.digitalcertificate.service.pdfbox.annotation.impl
 * @class FileHandlerFactory
 * @lastModifiedBy Kashan.Asim
 * @lastModifiedDate 1/27/2025
 * @license Licensed under the Apache License, Version 2.0
 * @description A brief description of the class functionality.
 * @notes <ul>
 * <li>Provide any additional notes or remarks here.</li>
 * </ul>
 * @since 1/27/2025
 */
public class FieldHandlerFactory {

    public static FieldHandler getHandler(FieldTypes fieldType) {
        return switch (fieldType) {
            case TEXT -> new TextFieldHandler();
            case TEXTAREA -> new TextAreaHandler();
            case IMAGE -> new ImageFieldHandler();
            case DATE -> new DateFieldHandler();
            default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        };
    }
}

