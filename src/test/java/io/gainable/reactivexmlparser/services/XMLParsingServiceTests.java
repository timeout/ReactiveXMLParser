package io.gainable.reactivexmlparser.services;

import io.gainable.reactivexmlparser.models.Document;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class XMLParsingServiceTests {

    private final XMLParsingService xmlParsingService = new XMLParsingService();

    @Test
    void testParseXMLAsStream() throws IOException {
        String filePath = new ClassPathResource("sample_documents.xml").getFile().getAbsolutePath();

        Flux<Document> documentsFlux = xmlParsingService.parseXMLAsStream(filePath);

        Document document1 = new Document(
                "Document 1",
                List.of(
                        "Paragraph 1 for document 1.",
                        "Paragraph 2 for document 1."
                ),
                1);
        Document document2 = new Document(
                "Document 2",
                List.of(
                        "Paragraph 1 for document 2."
                ),
                2
        );
        Document document3 = new Document(
                "Document 3",
                List.of(
                        "Paragraph 1 for document 3.",
                        "Paragraph 2 for document 3.",
                        "Paragraph 3 for document 3."
                ),
                3
        );

        StepVerifier.create(documentsFlux)
                .expectNext(document1)
                .expectNext(document2)
                .expectNext(document3)
                .verifyComplete();
    }
}

