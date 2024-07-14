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
@Table(name = "pass",
        uniqueConstraints = @UniqueConstraint(name = "pass_uniq_ind", columnNames = {"login", "pass_hash"}))
public class PassStore {

    @Id
    @Column(name = "uuid", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(name = "login", nullable = false, length = 64)
    private String login;

    @Column(name = "pass_hash", nullable = false)
    private int passHash;

    @Builder.Default
    @CreatedDate
    @Column(name = "create_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private ZonedDateTime createDate = ZonedDateTime.now();
}
