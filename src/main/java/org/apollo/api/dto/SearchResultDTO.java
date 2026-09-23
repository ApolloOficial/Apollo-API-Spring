package org.apollo.api.dto;

public class SearchResultDTO {

    private String id;
    private String title;
    private String subtitle;

    public SearchResultDTO() {
    }

    public SearchResultDTO(Object id, String title, String subtitle) {
        this.id = String.valueOf(id);
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
}