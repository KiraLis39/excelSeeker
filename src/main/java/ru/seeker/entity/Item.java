package ru.seeker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "category")
    private String category;

    @Column(name = "model")
    private String model;

    @Column(name = "sku")
    private String sku;

    @Column(name = "title", length = 1023)
    private String title;

    @Column(name = "excerpt", length = 1023)
    private String excerpt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private double price;

    @Column(name = "opt")
    private double opt;

    @Column(name = "stock")
    private int stock;

    @Column(name = "link")
    private String link;

//    @Builder.Default
//    @CollectionTable(name = "web_model_images")
//    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
//    private Set<String> images = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheet", nullable = false)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JsonIgnoreProperties(value = {"items"}, allowSetters = true)
    private Sheet sheet;

    @Override
    public String toString() {
        return "Item{"
                + "uuid=" + uuid
                + ", category='" + category + '\''
                + ", model='" + model + '\''
                + ", sku='" + sku + '\''
                + ", title='" + title + '\''
                + ", excerpt='" + excerpt + '\''
                + ", description='" + description + '\''
                + ", price=" + price
                + ", opt=" + opt
                + ", stock=" + stock
                + ", link='" + link + '\''
                + '}';
    }
}
