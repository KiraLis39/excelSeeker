package ru.seeker.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.seeker.dto.ParsedRowDTO;
import ru.seeker.entity.ParsedRow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParsedRowMapper {
    public ParsedRow toEntity(ParsedRowDTO dto) {
        return ParsedRow.builder()
                .uuid(dto.getUuid())
                .docName(dto.getDocName())
                .sheetName(dto.getSheetName())
                .parsedDate(dto.getParsedDate())
                .rowData(dto.getRowData())
                .build();
    }

    public ParsedRowDTO toDto(ParsedRow entity) {
        return ParsedRowDTO.builder()
                .uuid(entity.getUuid())
                .docName(entity.getDocName())
                .sheetName(entity.getSheetName())
                .parsedDate(entity.getParsedDate())
                .rowData(entity.getRowData())
                .build();
    }

    public Page<ParsedRowDTO> toDto(Page<ParsedRow> entities) {
        return entities == null ? Page.empty() : entities.map(this::toDto);
    }

    public List<ParsedRow> toEntity(Collection<ParsedRowDTO> entities) {
        return entities == null
                ? Collections.emptyList()
                : entities.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
