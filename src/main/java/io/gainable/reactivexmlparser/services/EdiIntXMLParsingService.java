package io.gainable.reactivexmlparser.services;

import io.gainable.reactivexmlparser.models.Attachment;
import io.gainable.reactivexmlparser.models.EdiDocument;
import io.gainable.reactivexmlparser.models.UploadDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Slf4j
@Service
public class EdiIntXMLParsingService {

    private static final Map<String, String> attachmentTranslations = Map.of(
            "FileName", "contentName",
            "MimeType", "mimeType",
            "ByteContent", "byteContent"
    );

    private static final Map<String, String> fieldTranslations = Map.of(
            "UID", "ediUid",
            "SENDERGLN", "supplierGln"
    );

    private static final Map<String, String> documentTranslations = Map.of(
            "Repository", "ediRepository"
    );

    private static final Map<String, String> metadataTranslations = Map.of(
            "CreationDate", "originCreatedOn",
            "ReferenceId", "ediReferenceId",
            "TrackId", "ediTrackingId",
            "UID", "ediMessageUid"
    );

    public Flux<EdiDocument> parseEdiIntXMLAsString(String filePath) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        Supplier<InputStream> inputStreamSupplier = () -> {
            try {
                return new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to open file input stream", e);
            }
        };

        return Flux.using(
                () -> {
                    try {
                        InputStream inputStream = inputStreamSupplier.get();
                        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(inputStream);
                        return new EdiDocumentIterator(xmlStreamReader, inputStream);
                    } catch (XMLStreamException e) {
                        throw new RuntimeException("Unable to create XMLStreamReader", e);
                    }
                },
                iterator -> Flux.generate(sink -> {
                    if (iterator.hasNext()) {
                        sink.next(iterator.next());
                    } else {
                        sink.complete();
                    }
                }),
                EdiDocumentIterator::close
        );
    }

    private static class EdiDocumentIterator implements Iterator<EdiDocument>, AutoCloseable {
        private final ParsingContext context;
        private final InputStream inputStream;

        public EdiDocumentIterator(XMLStreamReader xmlStreamReader, InputStream inputStream) {
            this.context = new ParsingContext(xmlStreamReader);
            this.inputStream = inputStream;
        }

        private boolean hasNextDocument() {
            if (context.ediDocument != null) {
                return true;
            }

            try {
                while (context.xmlStreamReader.hasNext()) {
                    processEvent(context);

                    if (context.ediDocument != null) {
                        return true;
                    }
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }

            return false;
        }

        private void processEvent(ParsingContext context) throws XMLStreamException {
            int eventType = context.xmlStreamReader.next();
            EventProcessor eventProcessor = new EventProcessor(context);

            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    eventProcessor.handleStartElement();
                    break;
                case XMLStreamReader.CHARACTERS:
                    eventProcessor.handleCharacters();
                    break;
                case XMLStreamReader.END_ELEMENT:
                    eventProcessor.handleEndElement();
                    break;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNextDocument();
        }

        @Override
        public EdiDocument next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            EdiDocument result = context.ediDocument;
            context.ediDocument = null;
            return result;
        }

        @Override
        public void close() {
            closeXmlStreamReader(context.xmlStreamReader);
            closeInputStream(inputStream);
        }

        private void closeXmlStreamReader(XMLStreamReader xmlStreamReader) {
            if (xmlStreamReader != null) {
                try {
                    xmlStreamReader.close();
                } catch (XMLStreamException e) {
                    // Log the exception or handle it according to your application's requirements
                }
            }
        }

        private void closeInputStream(InputStream inputStream) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Log the exception or handle it according to your application's requirements
                }
            }
        }
    }

    private static class EventProcessor {
        private ParsingContext context;

        public EventProcessor(ParsingContext context) {
            this.context = context;
        }

        private void handleStartElement() {
            context.currentElement = context.xmlStreamReader.getLocalName();

            switch (context.currentElement) {
                case "metaData" -> context.metadata = new HashMap<>();
                case "DocumentUploadSummary" -> context.uploadProperties = new HashMap<>();
                case "Field" -> context.fieldName = context.fieldValue = "";
                case "Attachment" -> context.contentProperties = new HashMap<>();
            }
        }


        private void handleCharacters() {
            if (context.metadata != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                if (metadataTranslations.containsKey(context.currentElement)) {
                    context.metadata.put(
                            metadataTranslations.get(context.currentElement),
                            text
                    );
                }
            }

            if (context.uploadProperties != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                if (documentTranslations.containsKey(context.currentElement)) {
                    context.uploadProperties.put(
                            documentTranslations.get(context.currentElement),
                            text
                    );
                }
            }

            if (context.uploadProperties != null && context.fieldName != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                switch (context.currentElement) {
                    case "Name" -> context.fieldName = text;
                    case "Value" -> context.fieldValue = text;
                }
            }

            if (context.uploadProperties != null && context.contentProperties != null && !context.xmlStreamReader.isWhiteSpace()) {
                String text = context.xmlStreamReader.getText().trim();

                if ("ByteContent".equals(context.currentElement) && (context.contentProperties.containsKey(attachmentTranslations.get("ByteContent")))) {
                    text = context.contentProperties.get(attachmentTranslations.get("ByteContent")) + text;
                }

                if (attachmentTranslations.containsKey(context.currentElement)) {
                    context.contentProperties.put(
                            attachmentTranslations.get(context.currentElement),
                            text
                    );
                }
            }
        }

        private void handleEndElement() {
            context.currentElement = context.xmlStreamReader.getLocalName();

            if ("EDIArchiveMessage".equals(context.currentElement)) {
                context.metadata = null;
            }

            if ("Attachment".equals(context.currentElement) && context.contentProperties != null && context.uploadProperties != null) {
                final var withoutWhitespace =
                        context.contentProperties.get("byteContent").replaceAll("\\s", "");
                context.contentProperties.put("byteContent", withoutWhitespace);
                context.ediDocument = new Attachment(
                        context.metadata,
                        context.uploadProperties,
                        context.contentProperties
                );
                context.contentProperties = null;
            }

            if ("Field".equals(context.currentElement) && context.fieldValue != null && context.fieldName != null && context.uploadProperties != null) {
                context.uploadProperties.put(
                        fieldTranslations.get(context.fieldName),
                        context.fieldValue
                );
                context.fieldName = null;
                context.fieldValue = null;
            }

            if ("DocumentUploadSummary".equals(context.currentElement) && context.metadata != null && context.uploadProperties != null) {
                context.ediDocument = new UploadDocument(context.metadata, context.uploadProperties);
                context.uploadProperties = null;
            }
        }
    }

    private static class ParsingContext {
        XMLStreamReader xmlStreamReader;
        Map<String, String> metadata;
        Map<String, String> uploadProperties;
        Map<String, String> contentProperties;
        String fieldName;
        String fieldValue;
        String currentElement;
        EdiDocument ediDocument;

        public ParsingContext(XMLStreamReader xmlStreamReader) {
            this.xmlStreamReader = xmlStreamReader;
        }
    }

}
