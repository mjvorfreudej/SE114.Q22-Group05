package com.example.tourgo.models.request;

public class TourSearchRequest {
    private String query;
    private String region;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private String sortBy;
    private String order;

    public static class Builder {
        private String query;
        private String region;
        private Double minPrice;
        private Double maxPrice;
        private Double minRating;
        private String sortBy;
        private String order;

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder minPrice(Double minPrice, Double maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder minRating(Double minRating) {
            this.minRating = minRating;
            return this;
        }

        public Builder sortBy(String sortBy, String order) {
            this.sortBy = sortBy;
            this.order = order;
            return this;
        }

        public TourSearchRequest build() {
            return new TourSearchRequest(this);
        }
    }

    private TourSearchRequest(Builder builder) {
        this.query = builder.query;
        this.region = builder.region;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.minRating = builder.minRating;
        this.sortBy = builder.sortBy;
        this.order = builder.order;
    }

    public String getQuery() {
        return query;
    }

    public String getRegion() {
        return region;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public Double getMinRating() {
        return minRating;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getOrder() {
        return order;
    }
}
