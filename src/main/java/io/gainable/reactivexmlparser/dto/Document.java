package io.gainable.reactivexmlparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    private String title;
    private List<String> paragraphs;
    private int id;
}

