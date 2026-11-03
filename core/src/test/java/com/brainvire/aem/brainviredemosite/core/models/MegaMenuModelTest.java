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
class MegaMenuModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private MegaMenuModel megaMenu;
    private Resource resource;

    @BeforeEach
    public void setup() {
        Page page = context.create().page("/content/test-page");
        resource = context.create().resource(page, "megaMenuItem",
            "menuLabel", "Industries",
            "menuLink", "/content/brainvire-demo-site/industries",
            "menuLinkNewTab", false,
            "showFooterBar", true);

        // Group 1: with lowercase label and 10 items (> 5 items -> multi-column)
        Resource group1 = context.create().resource(resource, "groups/item0",
            "groupLabel", "core expertise",
            "groupLink", "/content/brainvire-demo-site/industries/core-expertise",
            "groupLinkNewTab", false);

        for (int i = 1; i <= 10; i++) {
            context.create().resource(group1, "subLinks/item" + i,
                "subLabel", "Item " + i,
                "subLink", "/content/brainvire-demo-site/item" + i);
        }

        // Group 2: with 4 items (<= 5 items -> single-column)
        Resource group2 = context.create().resource(resource, "groups/item1",
            "groupLabel", "Technology Expertise",
            "groupLink", "/content/brainvire-demo-site/industries/tech-expertise");

        for (int i = 1; i <= 4; i++) {
            context.create().resource(group2, "subLinks/item" + i,
                "subLabel", "Tech " + i,
                "subLink", "https://example.com/tech" + i);
        }

        // Card Section (with mixed-case title to test capitalization)
        context.create().resource(resource, "cardSection",
            "cardTitle", "Executive Briefing",
            "cardDescription", "A prioritized AI roadmap for your enterprise, in one working session.",
            "cardButtonText", "Schedule now",
            "cardButtonLink", "/content/brainvire-demo-site/schedule",
            "cardButtonNewTab", true,
            "additionalText", "Replies within one business day",
            "cardImage", "/content/dam/brainvire-demo-site/card-bg.jpg");

        megaMenu = resource.adaptTo(MegaMenuModel.class);
    }

    @Test
    void testMegaMenuModel() {
        assertNotNull(megaMenu);
        assertEquals("Industries", megaMenu.getMenuLabel());
        assertEquals("/content/brainvire-demo-site/industries", megaMenu.getMenuLink());
        assertFalse(megaMenu.isMenuLinkNewTab());
        assertTrue(megaMenu.isShowFooterBar());

        assertEquals(2, megaMenu.getGroups().size());

        // Group 1 assertions: uppercase conversion & multi-column
        MegaMenuModel.Group g1 = megaMenu.getGroups().get(0);
        assertEquals("CORE EXPERTISE", g1.getGroupLabel());
        assertEquals("CORE EXPERTISE", g1.getTitle());
        assertEquals("CORE EXPERTISE", g1.getGroupTitle());
        assertEquals("/content/brainvire-demo-site/industries/core-expertise.html", g1.getGroupLink());
        assertEquals(10, g1.getSubLinks().size());
        assertTrue(g1.isMultiColumn());
        assertEquals(5, g1.getRowCount());

        // SubLink formatting internal link
        MegaMenuModel.SubLink sl1 = g1.getSubLinks().get(0);
        assertEquals("Item 1", sl1.getSubLabel());
        assertEquals("/content/brainvire-demo-site/item1.html", sl1.getSubLink());

        // Group 2 assertions: <= 5 items -> not multi-column
        MegaMenuModel.Group g2 = megaMenu.getGroups().get(1);
        assertEquals("TECHNOLOGY EXPERTISE", g2.getGroupLabel());
        assertEquals(4, g2.getSubLinks().size());
        assertFalse(g2.isMultiColumn());
        assertEquals(4, g2.getRowCount());

        // SubLink external link
        MegaMenuModel.SubLink sl2 = g2.getSubLinks().get(0);
        assertEquals("https://example.com/tech1", sl2.getSubLink());

        // Card Section assertions (cardTitle capitalized)
        MegaMenuModel.CardSection card = megaMenu.getCardSection();
        assertNotNull(card);
        assertEquals("EXECUTIVE BRIEFING", card.getCardTitle());
        assertEquals("Schedule now", card.getCardButtonText());
        assertEquals("/content/brainvire-demo-site/schedule.html", card.getCardButtonLink());
        assertTrue(card.isCardButtonNewTab());
        assertEquals("Replies within one business day", card.getAdditionalText());
        assertEquals("/content/dam/brainvire-demo-site/card-bg.jpg", card.getCardImage());
    }
}
