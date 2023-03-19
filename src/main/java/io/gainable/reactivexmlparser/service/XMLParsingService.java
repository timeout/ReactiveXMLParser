package io.gainable.reactivexmlparser.service;

import io.gainable.reactivexmlparser.dto.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

@Slf4j
@Service
public class XMLParsingService {

    public Flux<Document> parseXMLAsStream(String filePath) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try {
            InputStream inputStream = new FileInputStream(filePath);
            XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(inputStream);

            return Flux.create(sink -> {
                try {
                    Document document = null;
                    String currentElement = null;

                    while (xmlStreamReader.hasNext()) {
                        int eventType = xmlStreamReader.next();

                        switch (eventType) {
                            case XMLStreamReader.START_ELEMENT:
                                currentElement = xmlStreamReader.getLocalName();

                                if ("document".equals(currentElement)) {
                                    document = new Document();
                                    document.setParagraphs(new ArrayList<>());
                                }
                                break;

                            case XMLStreamReader.CHARACTERS:
                                if (document != null && !xmlStreamReader.isWhiteSpace()) {
                                    String text = xmlStreamReader.getText().trim();

                                    if ("title".equals(currentElement)) {
                                        document.setTitle(text);
                                    } else if ("paragraph".equals(currentElement)) {
                                        document.getParagraphs().add(text);
                                    } else if ("id".equals(currentElement)) {
                                        document.setId(Integer.parseInt(text));
                                    }
                                }
                                break;

                            case XMLStreamReader.END_ELEMENT:
                                currentElement = xmlStreamReader.getLocalName();

                                if ("document".equals(currentElement) && document != null) {
                                    sink.next(document);
                                    document = null;
                                }
                                break;
                        }
                    }
                    sink.complete();
                    xmlStreamReader.close();
                } catch (XMLStreamException e) {
                    sink.error(e);
                }
            });
        } catch (FileNotFoundException | XMLStreamException e) {
            return Flux.error(e);
        }
    }


}
