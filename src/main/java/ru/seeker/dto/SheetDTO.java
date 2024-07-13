package ru.seeker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SheetDTO {

    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Comment("Имя документа-источника данных")
    @JsonProperty("docName")
    private String docName;

    @Comment("UUID документа-источника данных")
    @JsonProperty("docUuid")
    private UUID docUuid;

    @Comment("Страница документа-источника данных")
    @JsonProperty("sheetName")
    private String sheetName;

    @JsonProperty("parsedDate")
    @Builder.Default
    private ZonedDateTime parsedDate = ZonedDateTime.now();

    @NotNull
    @Builder.Default
    @JsonProperty("items")
    private List<ItemDTO> items = new ArrayList<>();

    @JsonIgnore
    public void addItem(ItemDTO item) {
        this.items.add(item);
    }
}
