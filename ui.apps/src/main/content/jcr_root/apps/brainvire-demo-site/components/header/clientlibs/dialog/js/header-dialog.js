(function ($, $(document)) {
    "use strict";

        var CAPITAL_FIELDS_SELECTOR = 'input[name="./groupLabel"], input[name$="/groupLabel"], input[name="./cardSection/cardTitle"], input[name$="/cardTitle"]';

        // Convert Group Label and Card Title inputs to uppercase automatically
        $(document).on("input change blur", CAPITAL_FIELDS_SELECTOR, function () {
            var val = $(this).val();
            if (val && typeof val === "string") {
                var upper = val.toUpperCase();
                if (val !== upper) {
                    $(this).val(upper);
                }
            }
        });

        // When dialog is loaded, ensure any existing values are uppercase
        $(document).on("dialog-ready foundation-contentloaded", function () {
                $(CAPITAL_FIELDS_SELECTOR).each(function () {
                    var val = $(this).val();
                    if (val && typeof val === "string") {
                        var upper = val.toUpperCase();
                        if (val !== upper) {
                            $(this).val(upper);
                        }
                    }
                });
            });

    }(Granite.$, jQuery(document)));
