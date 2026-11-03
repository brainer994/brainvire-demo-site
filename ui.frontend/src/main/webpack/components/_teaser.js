//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Brainvire Demo Site - Teaser JS Enhancements
// 1. Sets dynamic stack indices for Selected Work sticky cards
// 2. Formats capability card bullet items with signature blue slashes
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(function() {
    'use strict';

    function initBrainvireTeasers() {
        var selector = '.teaser.brainvire-teaser:not(.brainvire-teaser--stat-card):not(.brainvire-teaser--capability)';
        var teaserWrappers = document.querySelectorAll(selector);

        if (teaserWrappers && teaserWrappers.length > 0) {
            var containerMap = new Map();

            teaserWrappers.forEach(function(teaser) {
                var parent = teaser.parentElement || document.body;
                if (!containerMap.has(parent)) {
                    containerMap.set(parent, []);
                }
                containerMap.get(parent).push(teaser);
            });

            containerMap.forEach(function(cards) {
                cards.forEach(function(card, idx) {
                    card.style.setProperty('--stack-index', idx);
                    card.style.setProperty('--card-count', cards.length);
                });
            });
        }

        // Capability Cards: Auto-format description lines into slash items
        var capCards = document.querySelectorAll('.brainvire-teaser--capability');
        if (capCards && capCards.length > 0) {
            capCards.forEach(function(card) {
                var desc = card.querySelector('.brainvire-teaser__supporting-description');
                if (desc && !desc.hasAttribute('data-bv-formatted')) {
                    desc.setAttribute('data-bv-formatted', 'true');
                    var rawText = desc.innerText || desc.textContent || '';
                    var lines = rawText.split('\n').map(function(l) {
                        return l.trim();
                    }).filter(function(l) {
                        return l.length > 0;
                    });

                    if (lines.length > 1) {
                        var html = lines.map(function(line) {
                            var cleanLine = line.replace(/^\/\s*/, '');
                            return '<div class="brainvire-capability__item">' +
                                   '<span class="brainvire-capability__slash">/</span>' +
                                   '<span>' + cleanLine + '</span>' +
                                   '</div>';
                        }).join('');
                        desc.innerHTML = html;
                    }
                }
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initBrainvireTeasers);
    } else {
        initBrainvireTeasers();
    }

    if (window.MutationObserver) {
        var observer = new MutationObserver(function() {
            initBrainvireTeasers();
        });
        observer.observe(document.body, { childList: true, subtree: true });
    }
})();
