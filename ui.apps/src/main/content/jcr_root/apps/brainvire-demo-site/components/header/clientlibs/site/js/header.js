(function () {

    "use strict";

    document.addEventListener("DOMContentLoaded", function () {

        document.querySelectorAll(".bv-header").forEach(function (header) {

            var activeMenu = null;
            var closeTimer = null;

            // ── Scroll shadow ──────────────────────────────────────
            window.addEventListener("scroll", function () {
                header.classList.toggle("is-scrolled", window.scrollY > 10);
            }, { passive: true });


            // ── Mega menu open/close helpers ───────────────────────
            function openMenu(label) {
                if (activeMenu && activeMenu.dataset.megaMenu === label) return;
                closeAllMenus();
                var menu = header.querySelector(".bv-mega-menu[data-mega-menu=\"" + label + "\"]");
                var navItem = header.querySelector(".bv-header__navigation-item[data-nav-label=\"" + label + "\"]");
                if (menu) {
                    menu.classList.add("is-active");
                    activeMenu = menu;
                }
                if (navItem) {
                    navItem.classList.add("is-active");
                }
            }

            function closeAllMenus() {
                header.querySelectorAll(".bv-mega-menu.is-active").forEach(function (m) {
                    m.classList.remove("is-active");
                });
                header.querySelectorAll(".bv-header__navigation-item.is-active").forEach(function (n) {
                    n.classList.remove("is-active");
                });
                activeMenu = null;
            }

            function scheduleClose() {
                closeTimer = setTimeout(closeAllMenus, 120);
            }

            function cancelClose() {
                clearTimeout(closeTimer);
            }


            // ── Nav item hover ─────────────────────────────────────
            header.querySelectorAll(".bv-header__navigation-item").forEach(function (item) {

                var label = item.dataset.navLabel;

                item.addEventListener("mouseenter", function () {
                    cancelClose();
                    var hasMegaMenu = !!header.querySelector(".bv-mega-menu[data-mega-menu=\"" + label + "\"]");
                    if (hasMegaMenu) {
                        openMenu(label);
                    } else {
                        closeAllMenus();
                    }
                });

                item.addEventListener("mouseleave", scheduleClose);
            });


            // ── Mega menu hover (keep open while inside) ───────────
            header.querySelectorAll(".bv-mega-menu").forEach(function (menu) {
                menu.addEventListener("mouseenter", cancelClose);
                menu.addEventListener("mouseleave", scheduleClose);
            });


            // ── Outside click closes everything ────────────────────
            document.addEventListener("click", function (e) {
                if (!header.contains(e.target)) {
                    closeAllMenus();
                    header.classList.remove("is-menu-open");
                    if (toggle) {
                        toggle.setAttribute("aria-expanded", "false");
                        toggle.setAttribute("aria-label", "Open menu");
                    }
                }
            });


            // ── Mobile hamburger ───────────────────────────────────
            var toggle = header.querySelector(".bv-header__mobile-toggle");

            if (toggle) {
                toggle.addEventListener("click", function () {
                    var open = header.classList.toggle("is-menu-open");
                    toggle.setAttribute("aria-expanded", String(open));
                    toggle.setAttribute("aria-label", open ? "Close menu" : "Open menu");
                });
            }


            // ── Mobile accordion ───────────────────────────────────
            header.querySelectorAll(".js-mobile-accordion").forEach(function (btn) {
                btn.addEventListener("click", function () {
                    var sub = btn.nextElementSibling;
                    var open = btn.classList.toggle("is-open");
                    btn.setAttribute("aria-expanded", String(open));
                    if (sub) sub.classList.toggle("is-open", open);
                });
            });

        });

    });

}());
