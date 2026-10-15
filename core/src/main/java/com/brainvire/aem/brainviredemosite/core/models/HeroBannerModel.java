package com.brainvire.aem.brainviredemosite.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class HeroBannerModel {

    @ValueMapValue private String eyebrow;
    @ValueMapValue private String title;
    @ValueMapValue private String description;
    @ValueMapValue private String firstButtonText;
    @ValueMapValue private String firstButtonLink;
    @ValueMapValue private String secondButtonText;
    @ValueMapValue private String secondButtonLink;
    @ValueMapValue private String backgroundType;
    @ValueMapValue private String backgroundImage;
    @ValueMapValue private String backgroundVideo;
    @ValueMapValue private Integer overlayOpacity;

    @ChildResource(name = "cards")
    private List<Resource> cardResources;

    private List<HeroBannerCard> cards = Collections.emptyList();

    @PostConstruct
    protected void init() {
        List<HeroBannerCard> result = new ArrayList<>();
        if (cardResources != null) {
            for (Resource cardResource : cardResources) {
                HeroBannerCard card = cardResource.adaptTo(HeroBannerCard.class);
                if (card != null) {
                    result.add(card);
                }
            }
        }
        cards = result;
    }

    public String getEyebrow() { return eyebrow; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFirstButtonText() { return firstButtonText; }
    public String getFirstButtonLink() { return formatLink(firstButtonLink); }
    public String getSecondButtonText() { return secondButtonText; }
    public String getSecondButtonLink() { return formatLink(secondButtonLink); }

    private String formatLink(String rawLink) {
        if (rawLink != null && rawLink.startsWith("/content") && !rawLink.contains(".") && !rawLink.contains("#") && !rawLink.contains("?")) {
            return rawLink + ".html";
        }
        return rawLink;
    }
    public String getBackgroundImage() { return backgroundImage; }
    public String getBackgroundVideo() { return backgroundVideo; }

    public String getBackgroundType() {
        return backgroundType == null ? "image" : backgroundType;
    }

    public int getOverlayOpacity() {
        if (overlayOpacity == null) {
            return 50;
        }
        return Math.max(0, Math.min(100, overlayOpacity));
    }

    /** CSS opacity is emitted as a bounded decimal between 0 and 1. */
    public String getOverlayOpacityCss() {
        return String.valueOf(getOverlayOpacity() / 100.0);
    }

    public boolean isVideoBackground() {
        return "video".equals(getBackgroundType())
            && backgroundVideo != null
            && !backgroundVideo.trim().isEmpty();
    }

    public List<HeroBannerCard> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
