package com.brainvire.aem.brainviredemosite.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MegaMenuModel {

    @ValueMapValue
    private String menuLabel;

    @ValueMapValue
    private String menuLink;

    @ValueMapValue
    private boolean menuLinkNewTab;

    @ValueMapValue
    private boolean showFooterBar;

    @ChildResource(name = "groups")
    private List<Resource> groupResources;

    @ChildResource(name = "cardSection")
    private Resource cardSectionResource;


    public String getMenuLabel() {
        return menuLabel;
    }

    public String getMenuLink() {
        return menuLink;
    }

    public boolean isMenuLinkNewTab() {
        return menuLinkNewTab;
    }

    public boolean isShowFooterBar() {
        return showFooterBar;
    }


    // =========================================================
    // Groups
    // =========================================================

    public List<Group> getGroups() {

        if (groupResources == null || groupResources.isEmpty()) {
            return Collections.emptyList();
        }

        List<Group> groups = new ArrayList<>();

        for (Resource resource : groupResources) {
            groups.add(new Group(resource));
        }

        return groups;
    }


    // =========================================================
    // Card Section
    // =========================================================

    public CardSection getCardSection() {

        if (cardSectionResource == null) {
            return null;
        }

        return new CardSection(cardSectionResource);
    }


    // =========================================================
    // Group
    // =========================================================

    public static class Group {

        private final String groupLabel;
        private final String groupLink;
        private final boolean groupLinkNewTab;
        private final Resource subLinksResource;


        public Group(Resource resource) {

            this.groupLabel = resource.getValueMap().get("groupLabel", String.class);
            this.groupLink  = resource.getValueMap().get("groupLink", String.class);
            this.groupLinkNewTab = Boolean.TRUE.equals(
                    resource.getValueMap().get("groupLinkNewTab", Boolean.class));
            this.subLinksResource = resource.getChild("subLinks");
        }


        public String getGroupLabel() {
            return groupLabel;
        }

        public String getGroupLink() {
            return groupLink;
        }

        public boolean isGroupLinkNewTab() {
            return groupLinkNewTab;
        }

        public List<SubLink> getSubLinks() {

            if (subLinksResource == null) {
                return Collections.emptyList();
            }

            List<SubLink> subLinks = new ArrayList<>();

            for (Resource resource : subLinksResource.getChildren()) {
                subLinks.add(new SubLink(resource));
            }

            return subLinks;
        }
    }


    // =========================================================
    // Sub Link
    // =========================================================

    public static class SubLink {

        private final String subLabel;
        private final String subLink;
        private final String subLinkExternal;
        private final boolean subLinkNewTab;


        public SubLink(Resource resource) {

            this.subLabel        = resource.getValueMap().get("subLabel", String.class);
            this.subLink         = resource.getValueMap().get("subLink", String.class);
            this.subLinkExternal = resource.getValueMap().get("subLinkExternal", String.class);
            this.subLinkNewTab   = Boolean.TRUE.equals(
                    resource.getValueMap().get("subLinkNewTab", Boolean.class));
        }


        public String getSubLabel() {
            return subLabel;
        }

        /** Returns external URL if set, otherwise the internal AEM path. */
        public String getSubLink() {
            return (subLinkExternal != null && !subLinkExternal.isBlank())
                    ? subLinkExternal
                    : subLink;
        }

        public boolean isSubLinkNewTab() {
            return subLinkNewTab;
        }
    }


    // =========================================================
    // Card Section
    // =========================================================

    public static class CardSection {

        private final String cardEyebrow;
        private final String cardTitle;
        private final String cardDescription;
        private final String cardButtonText;
        private final String cardButtonLink;
        private final String cardButtonLinkExternal;
        private final boolean cardButtonNewTab;
        private final String cardImage;


        public CardSection(Resource resource) {

            this.cardEyebrow            = resource.getValueMap().get("cardEyebrow", String.class);
            this.cardTitle              = resource.getValueMap().get("cardTitle", String.class);
            this.cardDescription        = resource.getValueMap().get("cardDescription", String.class);
            this.cardButtonText         = resource.getValueMap().get("cardButtonText", String.class);
            this.cardButtonLink         = resource.getValueMap().get("cardButtonLink", String.class);
            this.cardButtonLinkExternal = resource.getValueMap().get("cardButtonLinkExternal", String.class);
            this.cardButtonNewTab       = Boolean.TRUE.equals(
                    resource.getValueMap().get("cardButtonNewTab", Boolean.class));
            this.cardImage              = resource.getValueMap().get("cardImage", String.class);
        }


        public String getCardEyebrow() {
            return cardEyebrow;
        }

        public String getCardTitle() {
            return cardTitle;
        }

        public String getCardDescription() {
            return cardDescription;
        }

        public String getCardButtonText() {
            return cardButtonText;
        }

        /** Returns external URL if set, otherwise the internal AEM path. */
        public String getCardButtonLink() {
            return (cardButtonLinkExternal != null && !cardButtonLinkExternal.isBlank())
                    ? cardButtonLinkExternal
                    : cardButtonLink;
        }

        public boolean isCardButtonNewTab() {
            return cardButtonNewTab;
        }

        public String getCardImage() {
            return cardImage;
        }
    }
}
