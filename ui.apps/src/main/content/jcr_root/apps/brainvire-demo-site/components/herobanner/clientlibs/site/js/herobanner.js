(function (document, $, Coral) {
    "use strict";

    $(document).on("foundation-contentloaded", function (e) {
        $("coral-select.cq-dialog-dropdown-showhide", e.target).each(function (i, element) {
            var target = $(element).data("cq-dialog-dropdown-showhide-target");
            if (target) {
                Coral.commons.ready(element, function (component) {
                    showHide(component, target);
                    component.on("change", function () {
                        showHide(component, target);
                    });
                });
            }
        });
    });

    function showHide(component, target) {
        var value = component.value;
        if (!value && component.selectedItem) {
            value = component.selectedItem.value;
        }

        $(target).each(function () {
            var $field = $(this);
            var expected = $field.data("showhidetargetvalue");
            var $wrapper = $field.closest(".coral-Form-fieldwrapper");
            var $toToggle = $wrapper.length ? $wrapper : $field;

            if (expected === value) {
                $toToggle.removeClass("hide").show();
                $field.removeClass("hide");
            } else {
                $toToggle.addClass("hide").hide();
                $field.addClass("hide");
            }
        });
    }

})(document, Granite.$, Coral);