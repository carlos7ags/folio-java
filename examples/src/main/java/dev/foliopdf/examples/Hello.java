// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Minimal one-page PDF with a heading and a paragraph.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Hello
 * </pre>
 */
public final class Hello {
    public static void main(String[] args) {
        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title("Hello World")
                .author("Folio")
                .build()) {

            doc.add(Heading.of("Hello, Folio!", HeadingLevel.H1));
            doc.add(Paragraph.of(
                "This is a simple PDF created with the Folio Java SDK. " +
                "Folio is a library for creating, reading, and signing PDF documents.",
                Font.helvetica(), 12));

            doc.save("hello.pdf");
            System.out.println("Created hello.pdf");
        }
    }
}
