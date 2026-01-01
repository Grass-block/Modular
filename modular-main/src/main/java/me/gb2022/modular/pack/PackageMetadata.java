package me.gb2022.modular.pack;

import me.gb2022.modular.FeatureAvailability;

public final class PackageMetadata {
    private final String id;
    private final FeatureAvailability availability;
    private final String version;
    private final boolean internal;
    private final String description;
    private final String author;

    public PackageMetadata(String id, FeatureAvailability availability, String version, boolean internal, String description, String author) {
        this.id = id;
        this.availability = availability;
        this.version = version;
        this.internal = internal;
        this.description = description;
        this.author = author;
    }

    public static PackageMetadata create(ApplicationPackageProvider define) {
        return new PackageMetadata(
                define.id(),
                define.available(),
                define.author(),
                define.internal(),
                define.description(),
                define.author()
        );
    }

    public String id() {
        return id;
    }

    public FeatureAvailability availability() {
        return availability;
    }

    public String version() {
        return version;
    }

    public boolean internal() {
        return internal;
    }

    public String description() {
        return description;
    }

    public String author() {
        return author;
    }
}
