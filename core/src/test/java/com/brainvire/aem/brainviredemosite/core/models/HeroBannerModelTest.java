package com.brainvire.aem.brainviredemosite.core.models;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.brainvire.aem.brainviredemosite.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class HeroBannerModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private HeroBannerModel hero;
    private Resource resource;

    @BeforeEach
    public void setup() {
        Page page = context.create().page("/content/test-page");
        resource = context.create().resource(page, "herobanner",
            "sling:resourceType", "brainvire-demo-site/components/herobanner",
            "title", "Enterprise reinvention",
            "eyebrow", "Services",
            "description", "Modern systems",
            "firstButtonText", "Contact Us",
            "firstButtonLink", "/content/contact",
            "secondButtonText", "Explore",
            "secondButtonLink", "https://brainvire.com/work",
            "backgroundType", "video",
            "backgroundVideo", "/content/dam/video.mp4",
            "backgroundImage", "/content/dam/poster.jpg",
            "overlayOpacity", 60);

        context.create().resource(resource, "cards/item0",
            "title", "Odoo X AI",
            "subtitle", "ERP that forecasts",
            "link", "/content/odoo",
            "openInNewTab", true);

        context.create().resource(resource, "cards/item1",
            "title", "AI Solutions",
            "subtitle", "Enterprise automation");

        hero = resource.adaptTo(HeroBannerModel.class);
    }

    @Test
    void testHeroBannerModel() {
        assertNotNull(hero);
        assertEquals("Enterprise reinvention", hero.getTitle());
        assertEquals("Services", hero.getEyebrow());
        assertEquals("Modern systems", hero.getDescription());
        assertEquals("Contact Us", hero.getFirstButtonText());
        assertEquals("/content/contact.html", hero.getFirstButtonLink());
        assertEquals("Explore", hero.getSecondButtonText());
        assertEquals("https://brainvire.com/work", hero.getSecondButtonLink());
        assertTrue(hero.isVideoBackground());
        assertEquals(60, hero.getOverlayOpacity());
        assertEquals("0.6", hero.getOverlayOpacityCss());

        assertEquals(2, hero.getCards().size());

        HeroBannerCard card1 = hero.getCards().get(0);
        assertEquals("Odoo X AI", card1.getTitle());
        assertTrue(card1.getFormattedTitle().contains("<span class=\"hero-banner__card-cross\">✕</span>"));
        assertEquals("ERP that forecasts", card1.getSubtitle());
        assertEquals("/content/odoo.html", card1.getLink());
        assertTrue(card1.isOpenInNewTab());

        HeroBannerCard card2 = hero.getCards().get(1);
        assertEquals("AI Solutions", card2.getTitle());
        assertEquals("AI Solutions", card2.getFormattedTitle());
        assertNull(card2.getLink());
        assertFalse(card2.isOpenInNewTab());
    }
}
