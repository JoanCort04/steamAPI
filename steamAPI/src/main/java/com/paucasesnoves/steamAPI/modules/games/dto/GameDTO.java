package com.paucasesnoves.steamAPI.modules.games.dto;

import java.util.Set;

public class GameDTO {

    private Long id;
    private String title;
    private CategoryDTO category;
    private DeveloperDTO developer;
    private PublisherDTO publisher;
    private Set<GenreDTO> genres;
    private Set<PlatformDTO> platforms;
    private Set<TagDTO> tags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(DeveloperDTO developer) {
        this.developer = developer;
    }

    public PublisherDTO getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDTO publisher) {
        this.publisher = publisher;
    }

    public Set<GenreDTO> getGenres() {
        return genres;
    }

    public void setGenres(Set<GenreDTO> genres) {
        this.genres = genres;
    }

    public Set<PlatformDTO> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<PlatformDTO> platforms) {
        this.platforms = platforms;
    }

    public Set<TagDTO> getTags() {
        return tags;
    }

    public void setTags(Set<TagDTO> tags) {
        this.tags = tags;
    }
}
