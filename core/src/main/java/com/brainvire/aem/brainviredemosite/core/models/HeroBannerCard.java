package com.brainvire.aem.brainviredemosite.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class HeroBannerCard {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String subtitle;

    @ValueMapValue
    private String link;

    @ValueMapValue
    private Boolean openInNewTab;

    public String getTitle() {
        return title;
    }

    /**
     * Formats the title so that cross marks like " x ", " X ", or " ✕ "
     * are wrapped in a styled span matching Brainvire branding.
     */
    public String getFormattedTitle() {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        if (title.contains("<span")) {
            return title;
        }
        return title.replaceAll("(?i)\\s+(x|✕)\\s+", " <span class=\"hero-banner__card-cross\">✕</span> ");
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getLink() {
        if (link != null && link.startsWith("/content") && !link.contains(".") && !link.contains("#") && !link.contains("?")) {
            return link + ".html";
        }
        return link;
    }

    public boolean isOpenInNewTab() {
        return Boolean.TRUE.equals(openInNewTab);
    }
}

