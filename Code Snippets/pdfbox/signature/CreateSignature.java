
package com.io.chenosis.digitalcertificate.service.pdfbox.signature;

import com.io.chenosis.digitalcertificate.constants.AppConstants;
import com.io.chenosis.digitalcertificate.enums.SystemErrorCode;
import com.io.chenosis.digitalcertificate.exception.SystemLayerException;
import com.io.chenosis.digitalcertificate.util.CertificateUtility;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.jboss.logging.MDC;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

/**
 * An example for signing a PDF with bouncy castle.
 * A keystore can be created with the java keytool, for example:
 * <p>
 * {@code keytool -genkeypair -storepass 123456 -storetype pkcs12 -alias test -validity 365
 * -v -keyalg RSA -keystore keystore.p12 }
 *
 * @author Thomas Chojecki
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 */
public class CreateSignature extends CreateSignatureBase {

    /**
     * Initialize the signature creator with a keystore and certificate password.
     *
     * @param keystore the pkcs12 keystore containing the signing certificate
     * @param pin      the password for recovering the key
     * @throws KeyStoreException         if the keystore has not been initialized (loaded)
     * @throws NoSuchAlgorithmException  if the algorithm for recovering the key cannot be found
     * @throws UnrecoverableKeyException if the given password is wrong
     * @throws CertificateException      if the certificate is not valid as signing time
     * @throws IOException               if no certificate could be found
     */
    public CreateSignature(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        super(keystore, pin);
    }

    /**
     * Signs the given PDF file. Alters the original file on disk.
     *
     * @param file the PDF file to sign
     * @throws IOException if the file could not be read or written
     */
    public void signDetached(File file) throws IOException {
        signDetached(file, file, null);
    }

    /**
     * Signs the given PDF file.
     *
     * @param inFile  input PDF file
     * @param outFile output PDF file
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile) throws IOException {
        signDetached(inFile, outFile, null);
    }

    /**
     * Signs the given PDF file.
     *
     * @param pdfBytes input PDF file
     * @param tsaUrl   optional TSA url
     * @throws IOException if the input file could not be read
     */
    public byte[] signDetached(byte[] pdfBytes, String tsaUrl) throws IOException {

        setTsaUrl(tsaUrl);

        // sign
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument doc = Loader.loadPDF(pdfBytes)) {
            signDetached(doc, outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Signs the given PDF file.
     *
     * @param inFile  input PDF file
     * @param outFile output PDF file
     * @param tsaUrl  optional TSA url
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile, String tsaUrl) throws IOException {
        if (inFile == null || !inFile.exists()) {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        setTsaUrl(tsaUrl);

        // sign
        try (FileOutputStream fos = new FileOutputStream(outFile);
             PDDocument doc = Loader.loadPDF(inFile)) {
            signDetached(doc, fos);
        }
    }

    public void signDetached(PDDocument document, OutputStream output)
            throws IOException {
        // call SigUtils.checkCrossReferenceTable(document) if Adobe complains
        // and read https://stackoverflow.com/a/71293901/535646
        // and https://issues.apache.org/jira/browse/PDFBOX-5382

        int accessPermissions = SigUtils.getMDPPermission(document);
        if (accessPermissions == 1) {
            throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        // TODO extract the above details from the signing certificate? Reason as a parameter?

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // Optional: certify
        if (accessPermissions == 0) {
            SigUtils.setMDPPermission(document, signature, 2);
        }

        if (isExternalSigning()) {
            document.addSignature(signature);
            ExternalSigningSupport externalSigning =
                    document.saveIncrementalForExternalSigning(output);
            // invoke external signature service
            byte[] cmsSignature = sign(externalSigning.getContent());
            // set signature bytes received from the service
            externalSigning.setSignature(cmsSignature);
        } else {
            SignatureOptions signatureOptions = new SignatureOptions();
            // Size can vary, but should be enough for purpose.
            signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            // register signature dictionary and sign interface
            document.addSignature(signature, this, signatureOptions);

            // write incremental (only for signing purpose)
            document.saveIncremental(output);
        }
    }

    public void signDetached(PDDocument document, ByteArrayOutputStream output)
            throws IOException {
        // call SigUtils.checkCrossReferenceTable(document) if Adobe complains
        // and read https://stackoverflow.com/a/71293901/535646
        // and https://issues.apache.org/jira/browse/PDFBOX-5382

        int accessPermissions = SigUtils.getMDPPermission(document);
        if (accessPermissions == 1) {
            throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }

        Certificate certificate = getCertificateChain()[0];
        X509Certificate x509Certificate = null;
        if (certificate instanceof X509Certificate) {
            x509Certificate = (X509Certificate) certificate;
        }
        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName(String.valueOf(MDC.get(AppConstants.SIGN_USER)));
        signature.setLocation(CertificateUtility.getLocation(x509Certificate));
        signature.setReason(String.valueOf(MDC.get(AppConstants.SIGN_REASON)));
        // TODO extract the above details from the signing certificate? Reason as a parameter?

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // Optional: certify
        if (accessPermissions == 0) {
            SigUtils.setMDPPermission(document, signature, 2);
        }

        if (isExternalSigning()) {
            document.addSignature(signature);
            ExternalSigningSupport externalSigning =
                    document.saveIncrementalForExternalSigning(output);
            // invoke external signature service
            byte[] cmsSignature = sign(externalSigning.getContent());
            // set signature bytes received from the service
            externalSigning.setSignature(cmsSignature);
        } else {
            SignatureOptions signatureOptions = new SignatureOptions();
            // Size can vary, but should be enough for purpose.
            signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            // register signature dictionary and sign interface
            document.addSignature(signature, this, signatureOptions);

            // write incremental (only for signing purpose)
            document.saveIncremental(output);
        }

    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        String trustStore = "D:\\Work\\Projects\\Chenosis\\chenosis-digital-certificate\\src\\main\\resources\\keystore\\ChenosisLocalKeyStore.jks";
        String storePassword = "dev123";

        String fileName = "HtmlToPdf.pdf";

        String tsaUrl = null;
        boolean externalSig = false;


        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        char[] password = storePassword.toCharArray();
        try (InputStream is = new FileInputStream(trustStore)) {
            keystore.load(is, password);
        }
        // TODO alias command line argument

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password);
        signing.setExternalSigning(externalSig);

        File inFile = new File(fileName);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_signed.pdf");
        signing.signDetached(inFile, outFile, tsaUrl);
    }

    public static byte[] signPdf(byte[] pdfBytes) {
        try {
            String trustStore = "D:\\Work\\Projects\\Chenosis\\chenosis-digital-certificate\\src\\main\\resources\\keystore\\ChenosisLocalKeyStore.jks";
            String storePassword = "dev123";

            String tsaUrl = null;
            boolean externalSig = false;


            // load the keystore
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] password = storePassword.toCharArray();
            try (InputStream is = new FileInputStream(trustStore)) {
                keystore.load(is, password);
            }
            // TODO alias command line argument

            // sign PDF
            CreateSignature signing = new CreateSignature(keystore, password);
            signing.setExternalSigning(externalSig);

            return signing.signDetached(pdfBytes, tsaUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SystemLayerException(SystemErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
