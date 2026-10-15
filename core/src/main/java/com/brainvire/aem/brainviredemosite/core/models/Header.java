package com.brainvire.aem.brainviredemosite.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class Header {

    @ValueMapValue
    private String logoImage;

    @ValueMapValue
    private String logoAlt;

    @ValueMapValue
    private String logoLink;

    @ValueMapValue
    private String navigationRoot;

    @ValueMapValue
    private String ctaText;

    @ValueMapValue
    private String ctaLink;

    @ValueMapValue
    private boolean ctaNewTab;

    @SlingObject
    private ResourceResolver resourceResolver;

    private List<NavigationItem> navigationItems;

    @ChildResource(name = "megaMenuItems")
    private List<Resource> megaMenuResources;

    @ValueMapValue
    private String footerBarLabel;

    @ValueMapValue
    private String footerBarStats;

    @ChildResource(name = "footerBarLogos")
    private List<Resource> footerBarLogoResources;

    @PostConstruct
    protected void init() {
        navigationItems = buildNavigationItems();
    }

    private List<NavigationItem> buildNavigationItems() {

        if (navigationRoot == null || navigationRoot.isBlank()) {
            return Collections.emptyList();
        }

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        if (pageManager == null) {
            return Collections.emptyList();
        }

        Page rootPage = pageManager.getPage(navigationRoot);

        if (rootPage == null) {
            return Collections.emptyList();
        }

        List<NavigationItem> items = new ArrayList<>();

        Iterator<Page> children = rootPage.listChildren();

        while (children.hasNext()) {

            Page page = children.next();

            if (page.isHideInNav()) {
                continue;
            }

            items.add(createNavigationItem(page));
        }

        return items;
    }

    private NavigationItem createNavigationItem(Page page) {

        List<NavigationItem> children = new ArrayList<>();

        Iterator<Page> childPages = page.listChildren();

        while (childPages.hasNext()) {

            Page child = childPages.next();

            if (child.isHideInNav()) {
                continue;
            }

            children.add(createNavigationItem(child));
        }

        return new NavigationItem(
                page.getTitle(),
                page.getPath() + ".html",
                !children.isEmpty(),
                children
        );
    }

    public String getLogoImage() {
        return logoImage;
    }

    public String getLogoAlt() {
        return logoAlt;
    }

    public String getLogoLink() {
        return logoLink;
    }

    public String getCtaText() {
        return ctaText;
    }

    public String getCtaLink() {
        return ctaLink;
    }

    public boolean isCtaNewTab() {
        return ctaNewTab;
    }

    public List<NavigationItem> getNavigationItems() {
        return navigationItems;
    }

    public String getFooterBarLabel() {
        return footerBarLabel;
    }

    public String getFooterBarStats() {
        return footerBarStats;
    }

    public List<FooterLogo> getFooterBarLogos() {
        if (footerBarLogoResources == null || footerBarLogoResources.isEmpty()) {
            return Collections.emptyList();
        }
        List<FooterLogo> logos = new ArrayList<>();
        for (Resource r : footerBarLogoResources) {
            logos.add(new FooterLogo(
                r.getValueMap().get("logoName", String.class),
                r.getValueMap().get("logoImage", String.class),
                r.getValueMap().get("logoLink", String.class)
            ));
        }
        return logos;
    }

    public List<MegaMenuModel> getMegaMenuItems() {
        if (megaMenuResources == null || megaMenuResources.isEmpty()) {
            return Collections.emptyList();
        }
        List<MegaMenuModel> items = new ArrayList<>();
        for (Resource r : megaMenuResources) {
            MegaMenuModel m = r.adaptTo(MegaMenuModel.class);
            if (m != null) {
                items.add(m);
            }
        }
        return items;
    }

    public static class FooterLogo {

        private final String logoName;
        private final String logoImage;
        private final String logoLink;

        public FooterLogo(String logoName, String logoImage, String logoLink) {
            this.logoName  = logoName;
            this.logoImage = logoImage;
            this.logoLink  = logoLink;
        }

        public String getLogoName()  { return logoName; }
        public String getLogoImage() { return logoImage; }
        public String getLogoLink()  { return logoLink; }
    }

    public static class NavigationItem {

        private final String title;
        private final String url;
        private final boolean hasChildren;
        private final List<NavigationItem> children;

        public NavigationItem(
                String title,
                String url,
                boolean hasChildren,
                List<NavigationItem> children) {

            this.title = title;
            this.url = url;
            this.hasChildren = hasChildren;
            this.children = children;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public boolean isHasChildren() {
            return hasChildren;
        }

        public List<NavigationItem> getChildren() {
            return children;
        }
    }
}