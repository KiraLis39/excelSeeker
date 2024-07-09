package ru.seeker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "parsed_document_row", indexes = @Index(name = "row_data_tind", columnList = "row_data"))
public class ParsedRow {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Comment("Имя документа-источника данных")
    @Column(name = "doc_name", nullable = false)
    private String docName;

    @Comment("Страница документа-источника данных")
    @Column(name = "sheet_name", nullable = false)
    private String sheetName;

    @Column(name = "parsed_date", nullable = false)
    @CreatedDate
    private ZonedDateTime parsedDate = ZonedDateTime.now();

    @Column(name = "row_data", length = 1023)
    private String rowData;
}
