package com.paucasesnoves.steamAPI.modules.games.domain;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appId;
    private String title;
    private LocalDate releaseDate;
    private boolean english;
    private Integer minAge;
    private Integer achievements;
    private Integer positiveRatings;
    private Integer negativeRatings;
    private Double avgPlaytime;
    private Double medianPlaytime;
    private Integer ownersLower;
    private Integer ownersUpper;
    private Integer ownersMid;
    private BigDecimal price;

    @ManyToMany
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    // Este es el campo que le falta a Hibernate
    @ManyToOne
    @JoinColumn(name = "category_id") // FK hacia Category
    private Category category;

    @ManyToMany
    private Set<Platform> platforms = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "developer_id")
    private Developer developer;


    @ManyToMany
    @JoinTable(
            name = "game_publisher",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "publisher_id")
    )
    private Set<Publisher> publishers = new HashSet<>();

    public Set<Publisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(Set<Publisher> publishers) {
        this.publishers = publishers;
    }

    public Long getId() {
        return appId;
    }

    public void setId(Long appId) {
        this.appId = appId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isEnglish() {
        return english;
    }

    public void setEnglish(boolean english) {
        this.english = english;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getAchievements() {
        return achievements;
    }

    public void setAchievements(Integer achievements) {
        this.achievements = achievements;
    }

    public Integer getPositiveRatings() {
        return positiveRatings;
    }

    public void setPositiveRatings(Integer positiveRatings) {
        this.positiveRatings = positiveRatings;
    }

    public Integer getNegativeRatings() {
        return negativeRatings;
    }

    public void setNegativeRatings(Integer negativeRatings) {
        this.negativeRatings = negativeRatings;
    }

    public Double getAvgPlaytime() {
        return avgPlaytime;
    }

    public void setAvgPlaytime(Double avgPlaytime) {
        this.avgPlaytime = avgPlaytime;
    }

    public Double getMedianPlaytime() {
        return medianPlaytime;
    }

    public void setMedianPlaytime(Double medianPlaytime) {
        this.medianPlaytime = medianPlaytime;
    }

    public Integer getOwnersLower() {
        return ownersLower;
    }

    public void setOwnersLower(Integer ownersLower) {
        this.ownersLower = ownersLower;
    }

    public Integer getOwnersUpper() {
        return ownersUpper;
    }

    public void setOwnersUpper(Integer ownersUpper) {
        this.ownersUpper = ownersUpper;
    }

    public Integer getOwnersMid() {
        return ownersMid;
    }

    public void setOwnersMid(Integer ownersMid) {
        this.ownersMid = ownersMid;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }


    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<Platform> platforms) {
        this.platforms = platforms;
    }


    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }


}
