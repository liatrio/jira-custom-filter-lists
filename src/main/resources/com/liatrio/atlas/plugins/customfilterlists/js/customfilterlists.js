AJS.projectOrFilterPicker = function(gadget, userpref){
    if (!gadget.projectOrFilterName){
        gadget.projectOrFilterName = gadget.getMsg("gadget.common.filterid.none.selected");
    }

    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.filterid.label"),
        description:gadget.getMsg("gadget.common.filterid.description"),
        id: "proj_filter_picker_" + userpref,
        type: "callbackBuilder",
        callback: function(parentDiv){
            parentDiv.append(
                AJS.$("<input/>").attr({
                    id: "filter_" + userpref + "_id",
                    type: "hidden",
                    name: userpref
                }).val(gadget.getPref(userpref))
            ).append(
                AJS.$("<span/>").attr({id:"filter_" + userpref + "_name"}).addClass("filterpicker-value-name field-value").text(gadget.projectOrFilterName)
            );
            parentDiv.append(
                AJS.$("<div/>").attr("id", "quickfind-container").append(
                    AJS.$("<label/>").addClass("overlabel").attr({
                        "for":"quickfind",
                        id: "quickfind-label"
                    }).text(gadget.getMsg("gadget.common.quick.find"))
                ).append(
                    AJS.$("<input/>").attr("id", "quickfind")
                ).append(
                    AJS.$("<span/>").addClass("inline-error")
                )
            );
            if (gadget.isLocal()){
                parentDiv.append(
                    AJS.$("<a href='#'/>").addClass("advanced-search").attr({
                        id: "filter_" + userpref + "_advance",
                        title: gadget.getMsg("gadget.common.filterid.edit")
                    }).text(gadget.getMsg("gadget.common.advanced.search")).click(function(e){
                        var url = jQuery.ajaxSettings.baseUrl + "/secure/FilterPickerPopup.jspa?showProjects=false&field=" + userpref;
                        var windowVal = "filter_" + userpref + "_window";
                        var prefs = "width=800, height=500, resizable, scrollbars=yes";

                        var newWindow = window.open(url, windowVal, prefs);
                        newWindow.focus();
                        e.preventDefault();
                    })
                );
            }


            AJS.gadget.fields.applyOverLabel("quickfind-label");
            AJS.autoFilters({
                fieldID: "quickfind",
                ajaxData: {},
                baseUrl: jQuery.ajaxSettings.baseUrl,
                relatedId: "filter_" + userpref + "_id",
                relatedDisplayId: "filter_" + userpref + "_name",
                gadget: gadget,
                filtersLabel: gadget.getMsg("gadget.common.filters"),
                projectsLabel: gadget.getMsg("gadget.common.projects")
            });
        }
    };
};

/**
 * Filter autocomplete picker
 */
AJS.autoFilters = function(options) {
    // prototypial inheritance (http://javascript.crockford.com/prototypal.html)
    var that = begetObject(jira.widget.autocomplete.REST);

    that.getAjaxParams = function(){
        return {
            url: options.baseUrl + "/rest/gadget/1.0/pickers/filters",
            data: {
                fieldName: options.fieldID
            },
            dataType: "json",
            type: "GET",
            global:false,
            error: function(XMLHttpRequest, textStatus, errorThrown){
                if (XMLHttpRequest.data){
                    var errorCollection = XMLHttpRequest.data.errors;
                    if (errorCollection){
                        AJS.$(errorCollection).each(function(){
                            var parent = AJS.$("#" + this.field).parent();
                            parent.find("span.inline-error").text(options.gadget.getMsg(this.error));
                        });
                    }
                }
            }

        };
    };

    that.completeField = function(value) {
        AJS.$("#" + options.relatedId).val(value.id);
        AJS.$("#" + options.relatedDisplayId).addClass("success").text(value.name);
        AJS.$("#" + options.fieldID).val("");
    };

    /**
     * Create html elements from JSON object
     * @method renderSuggestions
     * @param {Object} response - JSON object
     * @returns {Array} Multidimensional array, one column being the html element and the other being its
     * corressponding complete value.
     */
    that.renderSuggestions = function(response) {


        var resultsContainer, suggestionNodes = [];

        this.responseContainer.addClass("aui-list");

        // remove previous results
        this.clearResponseContainer();

        var parent = AJS.$("#" + options.fieldID).parent();
        parent.find("span.inline-error").text("");

        if (response && response.filters && response.filters.length > 0) {


            resultsContainer = AJS.$("<ul class='aui-list-section aui-first aui-last'/>").appendTo(this.responseContainer);

            jQuery(response.filters).each(function() {
                if (!this.isModified){
                    this.isModified = true;
                    this.id = "filter-" + this.id;
                }

                var item = jQuery("<li class='aui-list-item' />").attr("id", this.id +"_" + options.fieldID + "_listitem");
                var link = AJS.$("<a href='#' class='aui-list-item-link' />").append(
                        AJS.$("<span />").addClass("filter-name").html(this.nameHtml)
                    )
                    .click(function (e) {
                        e.preventDefault();
                    })
                    .appendTo(item);

                if (this.descHtml){
                    link.append(
                        AJS.$("<span />").addClass("filter-desc").html(this.descHtml)
                    );
                }

                item.attr("title", link.text());

                // add html element and corresponding complete value  to sugestionNodes Array
                suggestionNodes.push([item.appendTo(resultsContainer), this]);

            });
        }

        if (suggestionNodes.length > 0) {
            this.responseContainer.removeClass("no-results");
            that.addSuggestionControls(suggestionNodes);
        } else {
            this.responseContainer.addClass("no-results");
        }

        return suggestionNodes;

    };

    // Use autocomplete only once the field has atleast 2 characters
    options.minQueryLength = 1;

    options.maxHeight = 200;

    // wait 1/4 of after someone starts typing before going to server
    options.queryDelay = 0.25;

    that.init(options);

    return that;
};
