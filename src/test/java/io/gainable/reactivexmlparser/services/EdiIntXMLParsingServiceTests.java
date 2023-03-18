package io.gainable.reactivexmlparser.services;

import io.gainable.reactivexmlparser.models.EdiDocument;
import io.gainable.reactivexmlparser.models.UploadDocument;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;

class EdiIntXMLParsingServiceTests {

    private final EdiIntXMLParsingService ediIntXMLParsingService = new EdiIntXMLParsingService();

    @Test
    void testParseEdiIntXMLAsStream() throws IOException {
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
}
