package com.bc.fiduceo.post.plugin.era5;

enum Era5Collection {
    ERA_5,
    ERA_51,
    ERA_5T;

    static Era5Collection fromString(String collectionString) {
        if (collectionString.equalsIgnoreCase("era5") || collectionString.equalsIgnoreCase("era_5")) {
            return ERA_5;
        } else if (collectionString.equalsIgnoreCase("era5t") || collectionString.equalsIgnoreCase("era_5t")) {
            return ERA_5T;
        } else if (collectionString.equalsIgnoreCase("era51") || collectionString.equalsIgnoreCase("era_51")) {
            return ERA_51;
        }

        throw new IllegalArgumentException("Invalid ERA5 collection: " + collectionString);
    }
}
