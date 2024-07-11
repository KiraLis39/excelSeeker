package ru.seeker.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.seeker.dto.SheetDTO;
import ru.seeker.entity.Sheet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SheetMapper {
    private ItemMapper itemMapper;

    @Autowired
    public void init(@Lazy ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public Sheet toEntity(SheetDTO dto) {
        return Sheet.builder()
                .uuid(dto.getUuid())
                .docName(dto.getDocName())
                .sheetName(dto.getSheetName())
                .parsedDate(dto.getParsedDate())
                .items(itemMapper.toEntity(dto.getItems()))
                .build();
    }

    public SheetDTO toDto(Sheet entity) {
        return SheetDTO.builder()
                .uuid(entity.getUuid())
                .docName(entity.getDocName())
                .sheetName(entity.getSheetName())
                .parsedDate(entity.getParsedDate())
                .items(itemMapper.toDto(entity.getItems()))
                .build();
    }

    public Page<SheetDTO> toDto(Page<Sheet> entities) {
        return entities == null ? Page.empty() : entities.map(this::toDto);
    }

    public List<Sheet> toEntity(Collection<SheetDTO> entities) {
        return entities == null
                ? Collections.emptyList()
                : entities.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
