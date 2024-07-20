package ru.seeker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "sheets")
public class Sheet {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "doc_uuid")
    private UUID docUuid;

    @Column(name = "sheet_name")
    private String sheetName;

    @Builder.Default
    @Column(name = "parsed_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    @CreatedDate
    private ZonedDateTime parsedDate = ZonedDateTime.now();

    @Builder.Default
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "sheet")
    private List<Item> items = new ArrayList<>();

    @Override
    public String toString() {
        return "Sheet{"
                + "uuid=" + uuid
                + ", docName='" + docName + '\''
                + ", sheetName='" + sheetName + '\''
                + ", parsedDate=" + parsedDate
                + '}';
    }
}
