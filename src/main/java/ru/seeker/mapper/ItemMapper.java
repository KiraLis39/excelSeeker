package ru.seeker.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.seeker.dto.ItemDTO;
import ru.seeker.entity.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    public Item toEntity(ItemDTO dto) {
        return Item.builder()
                .uuid(dto.getUuid())
                .category(dto.getCategory())
                .model(dto.getModel())
                .title(dto.getTitle())
                .excerpt(dto.getExcerpt())
                .description(dto.getDescription())
                .link(dto.getLink())
                .price(dto.getPrice())
                .opt(dto.getOpt())
                .sku(dto.getSku())
                .stock(dto.getStock() > 0
                        ? dto.getStock()
                        : dto.getStock_msk() - dto.getReserve_msk() + dto.getStock_spb() - dto.getReserve_spb())
//                .images(dto.getImages())
//                .sheet(sheetMapper.toEntity(dto.getSheet()))
                .build();
    }

    public ItemDTO toDto(Item entity) {
        return ItemDTO.builder()
                .uuid(entity.getUuid())
                .category(entity.getCategory())
                .model(entity.getModel())
                .title(entity.getTitle())
                .excerpt(entity.getExcerpt())
                .description(entity.getDescription())
                .link(entity.getLink())
                .price(entity.getPrice())
                .opt(entity.getOpt())
                .sku(entity.getSku())
                .stock(entity.getStock())
//                .images(entity.getImages())
//                .sheet(sheetMapper.toDto(entity.getSheet()))
                .build();
    }

    public Page<ItemDTO> toDto(Page<Item> entities) {
        return entities == null ? Page.empty() : entities.map(this::toDto);
    }

    public List<ItemDTO> toDto(List<Item> entities) {
        return entities == null
                ? Collections.emptyList()
                : entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<Item> toEntity(Collection<ItemDTO> dtos) {
        return dtos == null
                ? Collections.emptyList()
                : dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
