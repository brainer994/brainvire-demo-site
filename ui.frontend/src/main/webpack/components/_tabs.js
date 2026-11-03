//== Progressive enhancement for Tabs and Footer layout in AEM & local previews
(function() {
    "use strict";

    function initFooterLayout() {
        try {
            var footerRootSelector = 'footer.experiencefragment, .experiencefragment footer, ' +
                '.cmp-experiencefragment--footer, .experiencefragment-footer, .experiencefragment--footer, ' +
                '[class*="experiencefragment-footer"], [class*="experiencefragment--footer"], ' +
                '.xf-web-container';
            var footers = document.querySelectorAll(footerRootSelector);

            // If no explicit footer container found, fallback to document
            var contexts = footers.length > 0 ? footers : [document];

            contexts.forEach(function(ctx) {
                // 1. Normalize tabs in footer
                var tabLists = ctx.querySelectorAll('.cmp-tabs__tablist');
                tabLists.forEach(function(tabList) {
                    var tabs = tabList.querySelectorAll('.cmp-tabs__tab');
                    var container = tabList.closest('.cmp-tabs');
                    if (!container) {
                        return;
                    }

                    var panels = container.querySelectorAll('.cmp-tabs__tabpanel');

                    // Initial state: ensure active panel is visible
                    var activeTab = tabList.querySelector('.cmp-tabs__tab--active, [aria-selected="true"]');
                    var activePanelId = activeTab ? activeTab.getAttribute('aria-controls') : null;

                    panels.forEach(function(panel) {
                        var isActive = (activePanelId && panel.id === activePanelId) ||
                            panel.classList.contains('cmp-tabs__tabpanel--active') ||
                            panel.getAttribute('aria-hidden') === 'false';
                        if (isActive) {
                            panel.style.display = 'block';
                            panel.removeAttribute('aria-hidden');
                        } else {
                            panel.style.display = 'none';
                            panel.setAttribute('aria-hidden', 'true');
                        }
                    });

                    // Click handling
                    tabs.forEach(function(tab) {
                        tab.addEventListener('click', function(e) {
                            e.preventDefault();

                            tabs.forEach(function(t) {
                                t.classList.remove('cmp-tabs__tab--active');
                                t.setAttribute('aria-selected', 'false');
                                t.setAttribute('tabindex', '-1');
                            });

                            tab.classList.add('cmp-tabs__tab--active');
                            tab.setAttribute('aria-selected', 'true');
                            tab.setAttribute('tabindex', '0');

                            var targetPanelId = tab.getAttribute('aria-controls');
                            panels.forEach(function(panel) {
                                if (panel.id === targetPanelId) {
                                    panel.classList.add('cmp-tabs__tabpanel--active');
                                    panel.removeAttribute('aria-hidden');
                                    panel.style.display = 'block';
                                } else {
                                    panel.classList.remove('cmp-tabs__tabpanel--active');
                                    panel.setAttribute('aria-hidden', 'true');
                                    panel.style.display = 'none';
                                }
                            });
                        });
                    });
                });

                // 2. Normalize 5-column navigation grid in footer
                var footerImages = ctx.querySelectorAll('.cmp-image__image, .cmp-image, [data-cmp-hook-image]');
                footerImages.forEach(function(img) {
                    var grid = img.closest('.aem-Grid');
                    if (grid) {
                        grid.classList.add('bv-footer__nav-grid');
                        var childCols = grid.children;
                        for (var i = 0; i < childCols.length; i++) {
                            childCols[i].style.marginLeft = '0px';
                            childCols[i].style.marginRight = '0px';
                            childCols[i].style.float = 'none';
                        }
                    }
                });

                // 3. Normalize bottom copyright bar and region text in footer
                var allParagraphs = ctx.querySelectorAll('.cmp-text p');
                allParagraphs.forEach(function(p) {
                    var text = p.textContent || '';
                    if (text.indexOf('Copyright') !== -1) {
                        var bottomGrid = p.closest('.aem-Grid');
                        if (bottomGrid) {
                            bottomGrid.classList.add('bv-footer__bottom-bar');
                            var bChildren = bottomGrid.children;
                            for (var k = 0; k < bChildren.length; k++) {
                                bChildren[k].style.marginLeft = '0px';
                                bChildren[k].style.marginRight = '0px';
                                bChildren[k].style.float = 'none';
                            }
                        }
                    }

                    // 4. Normalize Region link spacing and classes
                    if (text.indexOf('Region:') !== -1) {
                        p.classList.add('bv-footer__region-text');
                        var rCol = p.closest('.aem-GridColumn, div[class*="aem-GridColumn"]');
                        if (rCol) {
                            rCol.classList.add('bv-footer__region-col');
                        }
                        var link = p.querySelector('a');
                        if (link && p.childNodes.length > 0 && p.childNodes[0].nodeType === Node.TEXT_NODE) {
                            p.childNodes[0].textContent = 'Region: ';
                        }
                    }
                });
            });
        } catch (err) {
            console.error('Error in initFooterLayout:', err);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initFooterLayout);
    } else {
        initFooterLayout();
    }
})();
