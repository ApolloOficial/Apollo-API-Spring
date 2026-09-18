package org.apollo.api.dto;

public class SearchResultDTO {

    private Long id;
    private String title;
    private String subtitle;

    public SearchResultDTO() {
    }

    public SearchResultDTO(
            Long id,
            String title,
            String subtitle
    ) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}