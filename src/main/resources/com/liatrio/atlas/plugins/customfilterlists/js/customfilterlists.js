AJS.myFilterListPicker = function(gadget, userpref, availableColumns){
    if (!gadget.projectOrFilterName){
        gadget.projectOrFilterName = gadget.getMsg("gadget.common.filterid.none.selected");
    }

    var dataModel = new FilterResultsColumnData1 (availableColumns, gadget.getPref("columnNames"));
    var columnPicker = new ColumnPicker1 ("columnNames", dataModel);

    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.filterid.label"),
        description:gadget.getMsg("gadget.common.filterid.description"),
        id: "proj_filter_picker_" + userpref,
        type: "callbackBuilder",
        callback: function(parentDiv) {
            parentDiv.append(
                AJS.$("<input/>").attr({
                    id: "filter_" + userpref + "_id",
                    type: "hidden",
                    name: userpref
                }).val(gadget.getPref(userpref))
            );

            AJS.$("#filter_projectOrFilterId_id").change(function() {
                dataModel.selectColumn(AJS.$("#filter_" + userpref + "_id").val());
                gadget.resize();
            });

            parentDiv.append(
                AJS.$("<div/>").attr("id", "quickfind-container").append(
                    AJS.$("<label/>").addClass("overlabel").attr({
                        "for":"quickfind",
                        id: "quickfind-label"
                    }).text(gadget.getMsg("gadget.common.quick.find"))
                ).append(
                    AJS.$("<input/>").attr("id", "quickfind").addClass("text")
                ).append(
                    AJS.$("<span/>").addClass("inline-error")
                )
            );
            if (gadget.isLocal()) {
                parentDiv.append(
                    AJS.$("<a href='#'/>").addClass("advanced-search").attr({
                        id: "filter_" + userpref + "_advance",
                        title: gadget.getMsg("gadget.common.filterid.edit")
                    }).text(gadget.getMsg("gadget.common.advanced.search")).click(function(e){
                        var url = jQuery.ajaxSettings.baseUrl + "/secure/FilterPickerPopup.jspa?showProjects=false&field=" + userpref;
                        var windowVal = "filter_" + userpref + "_window";
                        var prefs = "width=800, height=500, resizable, scrollbars=yes";
                        var newWindow = window.open(url, windowVal, prefs, false);
                        newWindow.focus();
                        newWindow.onunload = newWindow.onbeforeunload = function() { dataModel.selectColumn(AJS.$("#filter_" + userpref + "_id").val()); gadget.resize(); };
                        e.preventDefault();
                    })
                );
            }

            AJS.$("<p/>").appendTo(parentDiv);
            columnPicker.initalize(parentDiv);

            AJS.gadget.fields.applyOverLabel("quickfind-label");
            AJS.autoFilters({
                fieldID: "quickfind",
                ajaxData: {},
                dataModel: dataModel,
                baseUrl: jQuery.ajaxSettings.baseUrl,
                relatedId: "filter_" + userpref + "_id",
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
                        AJS.$(errorCollection).each(function() {
                            var parent = AJS.$("#" + this.field).parent();
                            parent.find("span.inline-error").text(options.gadget.getMsg(this.error));
                        });
                    }
                }
            }
        };
    };

    that.completeField = function(value) {
        AJS.$("#" + options.relatedId).val(value.id).trigger("change");
        AJS.$("#" + options.fieldID).val("");
    };

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

    options.minQueryLength = 1;
    options.maxHeight = 200;
    options.queryDelay = 0.25;
    that.init(options);
    return that;
};

function ColumnPicker1 (fieldName, dataModel) {
    this.fieldName = fieldName;
    this.dataModel = dataModel;
    this.initalize = function (parentDiv) {
        var tableElement = AJS.$("<table/>").attr ({
            id: "column-picker-restful-table"
        }).appendTo(parentDiv);
        var tableHelpTextElement = AJS.$("<div/>").attr ({
            id: "column-picker-table-helpText",
            'class': "description"
        }).appendTo(parentDiv);

        var submitElement = AJS.$("<div/>").attr ({
            id: "column-picker-hidden-submit",
            'class': "hidden"
        }).appendTo(parentDiv);

        createRestfulTable(tableElement,
                [{
                    id: "label",
                    header: "Name",
                    allowEdit: false
                }],
                this.dataModel.getSelectedColumns()
        );
        createSubmitElement (submitElement, fieldName, dataModel);
        createHelpText (tableHelpTextElement);
    };

    function createRestfulTable(table, columns, entries) {
        var reorderable = function (rowClass) {
            return rowClass.extend({
                initialize: function (options) {
                    rowClass.prototype.initialize.call(this, AJS.$.extend({}, options, { allowReorder:true }));
                }
            })
        };

        var restyled = function (rowClass) {
            //noinspection JSUnusedGlobalSymbols
            return rowClass.extend({
                renderOperations: function () {
                    var instance = this;
                    return AJS.$('<a id="column-picker-delete-' + AJS.escapeHtml(instance.model.attributes.value) + '" title="' + AJS.I18n.getText("gadget.issuetable.common.column.delete")
                            + '"><span>" + AJS.I18n.getText("gadget.issuetable.common.column.delete") + "</span></a>')
                        .addClass(this.classNames.DELETE)
                        .addClass("icon-delete")
                        .addClass("icon")
                        .click(function () {
                            instance.destroy();
                        });
                    },
                events: {
                    "click .aui-restfultable-editable" : "edit",
                    "mouseover" : 'handleMouseOver',
                    "mouseout" : 'handleMouseOut'
                },
                /* These should ideally be handled in CSS but IE8 and IE9 are not respecting the :hover state */
                handleMouseOver: function (ev) {
                     AJS.$(this.el.children).each( function () { this.bgColor = "#f0f0f0"; } );
                },
                handleMouseOut: function (ev) {
                     AJS.$(this.el.children).each( function () { this.bgColor = ""; } );
                }
            });
        };

        var makeSortable = function (instance) {
            instance.$theadRow.prepend("<th />");
            instance.$tbody.sortable({
                handle: "." + instance.classNames.DRAG_HANDLE,
                helper: function(e, elt) {
                    var helper =  elt.clone(true).addClass(instance.classNames.MOVEABLE);
                    helper.children().each(function (i) {
                        AJS.$(this).width(elt.children().eq(i).width());
                    });
                    return helper;
                },
                start: function (event, ui) {
                    var $this = ui.placeholder.find("td");
                    ui.item
                            .addClass(instance.classNames.MOVEABLE)
                            .children().each(function (i) {
                                AJS.$(this).width($this.eq(i).width());
                            });

                    ui.placeholder
                            .html('<td colspan="' + instance.getColumnCount() + '">&nbsp;</td>')
                            .css("visibility", "visible");
                    instance.getRowFromElement(ui.item[0]).trigger(instance._events.MODAL);
                },
                stop: function (event, ui) {
                    ui.item
                            .removeClass(instance.classNames.MOVEABLE)
                            .children().attr("style", "");
                    ui.placeholder.removeClass(instance.classNames.ROW);
                    instance.getRowFromElement(ui.item[0]).trigger(instance._events.MODELESS);
                },
                update: function (event, ui) {
                    var row = instance.getRowFromElement(ui.item[0]);
                    if (row) {
                        AJS.triggerEvtForInst(instance._events.REORDER_SUCCESS, instance, []);
                    }
                },
                axis: "y",
                delay: 0,
                containment: "document",
                cursor: "move",
                scroll: true,
                zIndex: 8000
            });

            instance.$tbody.bind("selectstart mousedown", function (event) {
                return !AJS.$(event.target).is("." + instance.classNames.DRAG_HANDLE);
            });
        };

        var restfulTable = new AJS.RestfulTable({
            el: table,
            allowCreate: false,
            allowReorder: false, // NOTE: this is overridden at the row level via the "reorderable" decorator
            columns: columns,
            resources: {
                all: function (populate) {
                    populate(entries);
                }
            },
            model: Backbone.Model.extend({
                sync: function (method, model, success) {
                    success(model.toJSON());
                }
            }),
            views:{
                row: restyled(reorderable(AJS.RestfulTable.Row)),
                editRow: reorderable(AJS.RestfulTable.EditRow)
            }
        });
        makeSortable(restfulTable);
        restfulTable.isRefreshingTable = false;

        // Events triggered when new fields are added or removed from the table
        AJS.$(document).bind("column-data-item-selected", refreshTableData);
        AJS.$(document).bind("column-data-item-unselected", refreshTableData);

        function refreshTableData (document, data) {
            restfulTable.isRefreshingTable = true;
            AJS.$(restfulTable.getRows()).each(function () {
                restfulTable.removeRow(this);
            });
            restfulTable.populate(data.dataModel.getSelectedColumns());
            restfulTable.isRefreshingTable = false;
        }

        // Event triggered when items are reordered in the table.
        AJS.$(restfulTable).bind(AJS.RestfulTable.Events.REORDER_SUCCESS, function (event) {
            var selectedColumns = [];
            AJS.$(event.target.getRows()).each(function () {
                selectedColumns.push(
                        {
                            label: this.model.attributes.label,
                            value: this.model.attributes.value
                        });
            });
            dataModel.reorderSelectedColumns(selectedColumns);
        });

        // Event triggered when items are removed from the table.
        AJS.$(restfulTable).bind(AJS.RestfulTable.Events.ROW_REMOVED, function (event, row) {
            // Don't update the data model if we're processing a change triggered by a change in the data model
            if (!restfulTable.isRefreshingTable) {
                dataModel.unselectColumn(row.model.attributes.value);
            }
        });
    }

    // This is a set of hidden fields that provides the value submitted back to the server when "Save" is clicked.
    function createSubmitElement (submitElement, fieldName, dataModel) {
        synchronizeSubmitData (submitElement, fieldName, dataModel);

        // An item has been selected, add it to the end of the list.
        AJS.$(document).bind("column-data-item-selected", function (document, data) {
            submitElement.append(
                    AJS.$("<input>").attr("type", "hidden").attr("name", fieldName).attr("value", data.column.value));
        });

        // An item has been unselected, remove it from the list.
        AJS.$(document).bind("column-data-item-unselected", function (document, data) {
            submitElement.children("[value=" + data.column.value + "]").remove();
        });

        // The items have been reordered, re-generate the list.
        AJS.$(document).bind("column-data-reordered", function (document, data) {
            synchronizeSubmitData (submitElement, fieldName, data.dataModel);
        });
    }

    function synchronizeSubmitData (submitElement, fieldName, dataModel) {
        submitElement.empty();
        AJS.$.each(dataModel.getSelectedColumns(), function () {
           submitElement.append(AJS.$("<input>").attr("type", "hidden").attr("name", fieldName).attr("value", this.value));
        });
    }

    function createHelpText (tableHelpTextElement) {
        tableHelpTextElement.text(AJS.I18n.getText("customfilterlists.configurablefilterlistsgadget.filters.descr"));
    }
}

function FilterResultsColumnData1 (allColumns, currentConfig) {
    this.selectedColumns = [];
    this.allColumns = [];

    // Separate out the columns that are selected from the full list
    this.populateColumns = function (allColumns, currentConfig) {
        this.allColumns = allColumns;
        var configuredColumns = currentConfig.split("|");
        this.populateSelectedColumns(configuredColumns, allColumns);
    };

    this.populateSelectedColumns = function (configuredColumns, allColumns) {
        var selectedColumns = this.selectedColumns;
        var findColumnIndex = this.findColumnIndex;
        AJS.$(configuredColumns).each(function () {
            var configuredColumn = this;
            var i = findColumnIndex(configuredColumn, allColumns);
            if (i != -1) {
                if (findColumnIndex(configuredColumn, selectedColumns) == -1) {
                    selectedColumns.push(allColumns[i]);
                }
            }
        });
    }

    this.getSelectedColumns = function () {
        return this.selectedColumns;
    }

    this.getUnselectedColumns = function () {
        // This array needs to stay in the original order given by the server.
        // If we persist this array, we would lose the order of its items every time
        // an element moves to the other array and back.
        var unselectedColumns = [];
        var findColumnIndex = this.findColumnIndex;
        var selectedColumns = this.selectedColumns;
        AJS.$(allColumns).each(function () {
            if (findColumnIndex(this.value, selectedColumns) == -1) {
                unselectedColumns.push(this);
            }
        });
        return unselectedColumns;
    }

    this.findColumnIndex = function (value, array) {
        for (var i = 0; i < array.length; i++) {
            if (value == array[i].value) {
                return i
            }
        }
        return -1;
    }

    this.selectColumn = function (columnId) {
        // Check it's not already selected (it shouldn't be)
        var selectedIndex = this.findColumnIndex (columnId, this.selectedColumns);
        if (selectedIndex == -1) {
            var allIndex = this.findColumnIndex (columnId, this.allColumns);
            if (allIndex != -1) {
                var column = allColumns[allIndex];
                this.selectedColumns.push(column);
                AJS.$(document).trigger("column-data-item-selected",
                        { dataModel : this, column : allColumns[allIndex]});
            }
        }
    }

    this.unselectColumn = function (columnId) {
        var selectedIndex = this.findColumnIndex (columnId, this.selectedColumns);
        if (selectedIndex != -1) {
            this.selectedColumns.splice(selectedIndex, 1);
            var unselectedColumns = this.getUnselectedColumns();
            var newIndex = this.findColumnIndex (columnId, unselectedColumns);
            AJS.$(document).trigger("column-data-item-unselected",
                    { dataModel : this, column : unselectedColumns[newIndex], index : newIndex});
        }
    }

    this.reorderSelectedColumns = function (newOrder) {
        this.selectedColumns = newOrder;
        AJS.$(document).trigger("column-data-reordered", { dataModel : this });
    }

    this.populateColumns (allColumns, currentConfig);
}
