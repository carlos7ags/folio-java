// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

/**
 * Digitally signs a PDF with a PAdES B-B signature using a bundled
 * self-signed certificate for demonstration.
 *
 * <p>PAdES levels supported by Folio:
 * <ul>
 *   <li>{@code B_B}: basic signature (this example)</li>
 *   <li>{@code B_T}: + RFC 3161 timestamp (requires a TSA URL)</li>
 *   <li>{@code B_LT}: + embedded revocation data (OCSP/CRL)</li>
 *   <li>{@code B_LTA}: + document timestamp for long-term archival</li>
 * </ul>
 *
 * <p>The bundled {@code demo_key.pem} / {@code demo_cert.pem} in
 * {@code examples/src/main/resources/} is a self-signed RSA-2048 cert
 * intended only for local demos. For production, load a real certificate
 * from a trusted CA, or use {@link PdfSigner#fromPkcs12(byte[], String)}
 * with a PKCS#12 keystore backed by an HSM.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Sign
 * </pre>
 */
public final class Sign {

    public static void main(String[] args) throws Exception {
        // Load the demo key and cert from classpath resources.
        byte[] keyPem = loadResource("/demo_key.pem");
        byte[] certPem = loadResource("/demo_cert.pem");

        // Build an unsigned PDF in memory.
        System.out.println("Creating PDF to sign...");
        byte[] pdfBytes;
        try (var doc = Document.builder()
                .letter()
                .title("Signed Document")
                .author("Folio Demo")
                .margins(72)
                .build()) {

            Font helv = Font.helvetica();
            Font helvBold = Font.helveticaBold();

            doc.add(Heading.of("Signed Document", HeadingLevel.H1));
            doc.add(LineSeparator.of());
            doc.add(Paragraph.of(
                    "This PDF has been digitally signed with a PAdES B-B signature. " +
                    "The signature proves the document has not been modified since " +
                    "signing. Open the file in Adobe Acrobat or a PAdES-aware viewer " +
                    "to inspect the signature panel.",
                    helv, 11).leading(1.5).spaceBefore(12));

            doc.add(Paragraph.of("Signer: Folio Demo Signer", helvBold, 10).spaceBefore(16));
            doc.add(Paragraph.of("Reason: Demonstration", helv, 10));
            doc.add(Paragraph.of("Location: Test Lab", helv, 10));
            doc.add(Paragraph.of("Signed at: " + ZonedDateTime.now(), helv, 10));

            pdfBytes = doc.toBytes();
        }

        System.out.println("  Unsigned PDF: " + pdfBytes.length + " bytes");

        // Sign with PAdES B-B (basic signature, no TSA or revocation data).
        System.out.println("\nSigning with PAdES B-B...");
        try (var signer = PdfSigner.fromPem(keyPem, certPem)) {
            byte[] signed = signer.sign(pdfBytes, PadesLevel.B_B, opts -> opts
                    .name("Folio Demo Signer")
                    .reason("Demonstration")
                    .location("Test Lab")
                    .contactInfo("demo@example.com"));

            Files.write(Path.of("signed.pdf"), signed);
            System.out.println("  Signed PDF:   " + signed.length + " bytes");
            System.out.println("  Wrote signed.pdf");
        }
    }

    private static byte[] loadResource(String path) throws Exception {
        try (InputStream in = Sign.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            return in.readAllBytes();
        }
    }
}
