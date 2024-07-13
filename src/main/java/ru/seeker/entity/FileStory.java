package ru.seeker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "files_story",
        uniqueConstraints = @UniqueConstraint(name = "name_size_uniq_ind", columnNames = {"doc_name", "doc_size"}))
public class FileStory {

    @Id
    @Column(name = "uuid", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(name = "doc_name", nullable = false)
    private String docName;

    @Column(name = "doc_size", nullable = false)
    private long docSize;

    @Column(name = "sheets_count")
    private int sheetsCount;

    @Column(name = "rows_count")
    private int rowsCount;

    @Builder.Default
    @CreatedDate
    @Column(name = "load_date", nullable = false, columnDefinition = "TIMESTAMP default now()") // CURRENT_TIMESTAMP
    private ZonedDateTime loadDate = ZonedDateTime.now();
}
