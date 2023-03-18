package io.gainable.reactivexmlparser.services;

import io.gainable.reactivexmlparser.models.Attachment;
import io.gainable.reactivexmlparser.models.EdiDocument;
import io.gainable.reactivexmlparser.models.UploadDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;

class EdiIntXMLParsingServiceTests {

    private final EdiIntXMLParsingService ediIntXMLParsingService = new EdiIntXMLParsingService();

    @DisplayName("Parse a single EDIArchiveMessage without an attachment")
    @Test
    void testParseEdiIntXMLAsStream1() throws IOException {
        String filePath = new ClassPathResource("edi_int_document.xml").getFile().getAbsolutePath();

        Flux<EdiDocument> ediDocuments = ediIntXMLParsingService.parseEdiIntXMLAsString(filePath);

        EdiDocument ediDocument = new UploadDocument(
                Map.of(
                        "ediReferenceId", "FR210300232060",
                        "ediTrackingId", "0001955097-ERP-3",
                        "ediMessageUid", "f4450e50-c1e5-11eb-8e5c-084b0a030838",
                        "originCreatedOn", "2021.05.31 09:58:20.151 MESZ"
                ),
                Map.of(
                        "ediUid", "1184930",
                        "ediRepository", "EDI_FR",
                        "supplierGln", "3701035100020"
                )
        );

        StepVerifier.create(ediDocuments)
                .expectNext(ediDocument)
                .verifyComplete();
    }

    @DisplayName("Parse a single EDIArchiveMessage with an attachment")
    @Test
    void testParseEdiIntXMLAsStream2() throws IOException {
        String filePath =
                new ClassPathResource("edi_int_attachment.xml").getFile().getAbsolutePath();

        Flux<EdiDocument> ediDocuments = ediIntXMLParsingService.parseEdiIntXMLAsString(filePath);

        final var metadata = Map.of(
                "ediReferenceId", "FR210300232060",
                "ediTrackingId", "0001955097-ERP-3",
                "ediMessageUid", "f4450e50-c1e5-11eb-8e5c-084b0a030838",
                "originCreatedOn", "2021.05.31 09:58:20.151 MESZ"
        );

        final var documentProperties = Map.of(
                "ediUid", "1184930",
                "ediRepository", "EDI_FR",
                "supplierGln", "3701035100020"
        );

        final var contentProperties = Map.of(
                "contentName", "FR210300232060_20210531_EDIFACTSIGNEDORIGINAL",
                "mimeType", "txt",
                "byteContent", "VU5CK1VOT0M6MyszNzAxMDM1MTAwMDIwOjE0KzQzMzM2ODUwMDAwMDA6MTQ6MSsyMTA1MzE6MDk1" +
                        "NisxMTg0OTMwKysrKysrMScNClVOSCs2MDI3NjUrREVTQURWOkQ6MDFCOlVOOkVBTjAwNycNCkJH" +
                        "TSszNTErMDA1OTMzMjArOScNCkRUTSsxMzc6MjAyMTA1MzEwOTUwOjIwMycNCkRUTSsxNzoyMDIx" +
                        "MDUzMTA5NTA6MjAzJw0KRFRNKzI6MjAyMTA2MDEwMDAwOjIwMycNClJGRitEUTowNDMxODE1MScN" +
                        "ClJGRitPTjowNjY4MDEwNjIxMDInDQpOQUQrQlkrNDA1MjkxNTAwMDAwODo6OScNCk5BRCtTVSsz" +
                        "NzAxMDM1MTAwMDIwOjo5Jw0KTkFEK0RQKzQwNTI5MTUwMDAwOTE6OjknDQpDUFMrMScNClBBQysx" +
                        "KysyMDEnDQpNRUErUEQrQUFDK0tHTTo0OC4wMDAnDQpDUFMrMisxJw0KUEFDKzErKzIwMScNCk1F" +
                        "QStQRCtBQUMrS0dNOjQ4LjAwMCcNCk1FQStQRCtUK0tHTToyMCcNCk1FQStQRCtBQUMrS0dNOjQ4" +
                        "LjAwMCcNCk1FQStQRCtUK0tHTToyNScNClBDSSszNEUnDQpHSU4rU1JWKzU0MDAxMTEwMDU2NTIn" +
                        "DQpQQ0krMzZFJw0KR0lOK0JYKzExMTQ5MDI2MycNClBDSSszM0UnDQpHSU4rQkorMzMwNjE0MzEw" +
                        "MDQ3MTA4NTgwJw0KUENJKzM2RScNCkdJTitCWCsxMTE0OTAyNjMnDQpMSU4rMSsrMDQzMzQwMzU4" +
                        "MDU4NDk6U1JWJw0KUElBKzErMTExNDkwMjYzOk5CJw0KUVRZKzEyOjQ4LjAwMCcNClBDSSszOUUn" +
                        "DQpEVE0rMzYxOjIwMjEwNjA3OjEwMicNClVOVCszMys2MDI3NjUnDQpVTlorMSsxMTg0OTMwJw0K"
        );

        StepVerifier.create(ediDocuments)
                .expectNext(new Attachment(metadata, documentProperties, contentProperties))
                .expectNext(new UploadDocument(metadata, documentProperties))
                .verifyComplete();
    }
}