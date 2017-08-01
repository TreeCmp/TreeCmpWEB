function drawTrees(newickIn1, newickIn2, nameTree1, nameTree2) {

    //new ui

    // Ensures that the acknowledgment footer is correctly placed in the sidebar
    (function () {
        var div_footer = $("#footer_div");
        var divdiv = $("#sidebar-wrapper-wrapper");
        div_footer.width(divdiv.width());

    })();

    var spinnerOpts = {
        lines: 13 // The number of lines to draw
        ,
        length: 28 // The length of each line
        ,
        width: 14 // The line thickness
        ,
        radius: 42 // The radius of the inner circle
        ,
        scale: 0.20 // Scales overall size of the spinner
        ,
        corners: 1 // Corner roundness (0..1)
        ,
        color: '#000' // #rgb or #rrggbb or array of colors
        ,
        opacity: 0.25 // Opacity of the lines
        ,
        rotate: 0 // The rotation offset
        ,
        direction: 1 // 1: clockwise, -1: counterclockwise
        ,
        speed: 1 // Rounds per second
        ,
        trail: 60 // Afterglow percentage
        ,
        fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
        ,
        zIndex: 2e9 // The z-index (defaults to 2000000000)
        ,
        className: 'spinner' // The CSS class to assign to the spinner
        ,
        top: '50%' // Top position relative to parent
        ,
        left: '10%' // Left position relative to parent
        ,
        shadow: true // Whether to render a shadow
        ,
        hwaccel: true // Whether to use hardware acceleration
        ,
        position: 'relative' // Element positioning
    };

    var spinner = new Spinner(spinnerOpts);
    //var spinnerTarget = document.getElementById("page-content-wrapper");
    var spinnerTarget = document.getElementById("spinner-container");

    var gistID = $(location).attr('hash').split(/\s*[#-]\s*/); //get gist id from url
    treecomp = TreeCompare().init({
        scaleColor: "black",
        loadingCallback: function () {
            $('#spinner-container').show();
            spinner.spin(spinnerTarget);
        },
        loadedCallback: function () {
            $('#spinner-container').hide();
            spinner.stop();
        },
        enableRerootFixedButtons: $("#rerootFixedButtons").val(),
        enableSwapFixedButtons: $("#swapFixedButtons").val(),
        lineThickness: $("#lineThickness").val(),
        nodeSize: $("#nodeSize").val(),
        fontSize: $("#fontSize").val(),
        useLengths: $("#useLengths").is(":checked"),
        alignTipLabels: $("#alignTipLabels").is(":checked"),
        mirrorRightTree: $("#mirrorRightTree").is(":checked"),
        selectMultipleSearch: $("#selectMultipleSearch").is(":checked"),
        comparisonMetric: $("input[name=comparisonMetric]:checked").val(),
        moveOnClick: $("#moveOnMouseOver").is(":checked"),
        clickAction: $("input[name=clickAction]:checked").val(),
        autoCollapse: $("#collapseAmount").html()
    });

    /* Download name */
    var update_download_fn = function (canvasId, labelId) {
        download_fn = document.getElementById(labelId).value;
        download_fn = (download_fn === '') ? "PhyloIO_Tree" : download_fn;
        document.getElementById('downloadButtons' + canvasId).getElementsByTagName('a')[0].download = download_fn;
    };

    // hide optional settings by default
    $('.opt').parent().hide();

    var mode = $("#mode-buttons .active").attr('id');
    if (mode === "view-btn") {
        $("#compareIn").removeClass("active");
        $("#fileLabel").text("Untitled");

        $(".compareIn").css({
            "display": "none"
        });
        $("#newickInputs").css({
            "padding-bottom": "0px"
        });

        $("#cmp-trees-params").css({
            "display": "none"
        });
        $("#view-tree-params").css({
            "display": "block"
        });
        $("#action").css(
            {"display": "none"}
        );
    }
    /*
     /
     /    LOAD PAGE FROM GIST
     /
     */
    if (gistID.length === 2) { //view mode

        $("#compare-btn").removeClass("active");
        $("#view-btn").addClass("active");
        $(".compareIn").css({
            "display": "none"
        });
        $("#newickInputs").css({
            "padding-bottom": "0px"
        });

        var tree1 = treecomp.addTreeGistURL(gistID[1]);
        $("#page-content-wrapper").empty();
        $("#page-content-wrapper").html('<div class="container-fluid vis-container" id="vis-container1"></div><div class="vis-scale" id="vis-scale1"></div>');
        treecomp.viewTree(tree1.name, "vis-container1", "vis-scale1");
        $("#newickInSingle").val(tree1.nwk);

        $("#colorScale").empty();
        $("#action").css({"display": "none"});

    } else if (gistID.length === 3) { //compare mode

        $("#view-btn").removeClass("active");
        $("#compare-btn").addClass("active");
        $("#newickInputs").css({
            "padding-bottom": "25px"
        });
        $(".compareIn").css({
            "display": "block"
        });
        var tree1 = treecomp.addTreeGistURL(gistID[1]);
        var tree2 = treecomp.addTreeGistURL(gistID[2]);

        $("#page-content-wrapper").empty();

        $("#page-content-wrapper").html('<div class="container-fluid vis-container-2" id="vis-container1"></div><div class="container-fluid vis-container-2" id="vis-container2"></div>');
        treecomp.compareTrees(tree1.name, "vis-container1", tree2.name, "vis-container2", "vis-scale1", "vis-scale2");
        $("#newickIn1").val(tree1.nwk);
        $("#newickIn2").val(tree2.nwk);

        $("#colorScale").empty();
        treecomp.renderColorScale("colorScale");

        $("#action").css({"display": "block"});
    } //end of gist id part

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");

        var div_footer = $("#footer_div");
        var newWidth, sideBarWidth = 420;
        var colorScaleWidth = 75;

        if ($("#collapse-sidebar-span").hasClass("fa fa-2x fa-arrow-circle-left")) {
            $("#collapse-sidebar-span").removeClass("fa fa-2x fa-arrow-circle-left");
            $("#collapse-sidebar-span").addClass("fa fa-2x fa-arrow-circle-right");
            newWidth = 30;
            $('#sidebar-nav').hide();
        }
        else {
            $("#collapse-sidebar-span").removeClass("fa fa-2x fa-arrow-circle-right");
            $("#collapse-sidebar-span").addClass("fa fa-2x fa-arrow-circle-left");
            $('#sidebar-nav').show();
        }
        var position = $("#wrapper").width() / 2 + sideBarWidth / 2 - colorScaleWidth / 3;
        $('#colorScale').css("left", position + "px");

        div_footer.width(newWidth);
    });

    /*
     /
     /    COMPARE MODE: active windows in sidebar
     /    Changed from method to active code
     /
     */
    if ($(window).height() < 750) {
        $("#sidebar-wrapper").css({
            "overflow-y": "scroll"
        });
    }

    treecomp.changeCanvasSettings({
        enableFixedButtons: true
    });


    /*------------
     /
     /    COLLAPSING BY LEVEL BUTTON
     /
     ------------*/
    $("#collapseInc").click(function (e) {
        var amountText = $("#collapseAmount").html();
        var amount = 0;
        var maxAmount = treecomp.getMaxAutoCollapse();
        if (amountText !== "OFF") {
            amount = parseInt(amountText);
            amount += 1;
            if (amount == maxAmount) {
                $("#collapseAmount").html("OFF");
            } else {
                $("#collapseAmount").html(amount.toString());
            }
        } else {
            amount = 0;
            $("#collapseAmount").html("0");
        }
        treecomp.changeAutoCollapseDepth(amount);

    });

    $("#collapseDec").click(function (e) {
        var amountText = $("#collapseAmount").html();
        var amount = 0;
        var maxAmount = treecomp.getMaxAutoCollapse();

        if (amountText !== "0") {
            if (amountText === "OFF") {
                amount = maxAmount;
            } else {
                amount = parseInt(amountText);
            }
            amount -= 1;
            if (amount < 0) {
                amount = null;
                $("#collapseAmount").html("OFF");
            } else {
                $("#collapseAmount").html(amount.toString());
            }
        } else {
            amount = null;
            $("#collapseAmount").html("OFF");
        }

        treecomp.changeAutoCollapseDepth(amount);
    });


    /*------
     /
     /    RENDER BUTTON
     /    Changed from method to active code
     /
     ------*/

    //hideOptionalSettings();
    $("#menu-toggle").click();

    try {
        //$("#page-content-wrapper").empty();
        $("#page-content-wrapper").html('<div class="container-fluid vis-container-2" id="vis-container1"></div><div class="container-fluid vis-container-2" id="vis-container2"></div>');
        var tree1 = treecomp.addTree(newickIn1, nameTree1, "left");
        var tree2 = treecomp.addTree(newickIn2, nameTree2, "right");
        treecomp.changeCanvasSettings({
            autoCollapse: tree1.data.autoCollapseDepth
        })
        var collapseText = tree1.data.autoCollapseDepth === null ? "OFF" : tree1.data.autoCollapseDepth.toString();
        $("#collapseAmount").html(collapseText);
        //TODO: change this

        treecomp.compareTrees(tree1.name, "vis-container1", tree2.name, "vis-container2", "vis-scale1", "vis-scale2");
        treecomp.undo("undoBtn");

        $("#colorScale").empty();
        treecomp.renderColorScale("colorScale");

        $("#action").css({"display": "block"});
    } catch (e) {
        if (e === "TooLittle)") {
            $("#renderErrorMessage").append($('<div class="alert alert-danger" role="alert">Tree file not correct: too little ")"</div>')).hide().slideDown(300);
        } else if (e === "TooLittle(") {
            $("#renderErrorMessage").append($('<div class="alert alert-danger" role="alert">Tree file not correct: too little "("</div>')).hide().slideDown(300);
        } else if (e === "NoTree") {
            $("#renderErrorMessage").append($('<div class="alert alert-danger" role="alert">Please add tree</div>')).hide().slideDown(300);
        } else if (e === "") {
            $("#renderErrorMessage").append($('<div class="alert alert-danger" role="alert">Empty</div>')).hide().slideDown(300);
        }
    }
    /*------
     /
     /    SHARE BUTTON
     /
     ------*/
    $("#export").click(function (e) {
        try {
            var exportURLGist = treecomp.exportTree(true);
            $("#exportURLInSingle").attr('href', exportURLGist);
            $("#exportURLInSingle").html(exportURLGist);
            $('#myModal').modal('show');
        } catch (e) {
            $("#renderErrorMessage").append($('<div class="alert alert-danger" role="alert">Nothing to share</div>')).hide().slideDown(300);
        }
    });

    /*------
     /
     /    SVG export button
     /
     ------*/
    $("#svgExport").click(function (e) {

        // Copy left tree
        var svg1 = document.getElementById('vis-container1').getElementsByTagName('svg')[0].cloneNode(true);
        var svg2 = document.getElementById('vis-container2').getElementsByTagName('svg')[0].cloneNode(true);
        var colorscale = document.getElementById('colorScale').getElementsByTagName('svg')[0].cloneNode(true);

        // Double the width on svg1
        var l_w = parseInt(svg1.getAttribute('width')),
            r_w = parseInt(svg2.getAttribute('width'));
        svg1.setAttribute('width', (l_w + r_w));

        // Add right tree into left tree's svg
        var g = document.createElement('g');
        var lastElementIndex = svg2.childNodes.length - 3;
        var scaleTextIndex = svg2.childNodes.length - 2;

        if (lastElementIndex > -1) {
            g.setAttribute('transform', 'translate(' + l_w + ',0)');
            main = svg2.childNodes[lastElementIndex];
            scale = svg2.childNodes[scaleTextIndex];
            scaleText = svg2.lastElementChild;

            g.appendChild(main);
            g.appendChild(scale);
            g.appendChild(scaleText);
            g.appendChild(colorscale);

            svg1.appendChild(g);
        }
        svgExport.setAttribute('hreflang', 'image/svg+xml');
        svgExport.setAttribute('href', 'data:image/svg+xml;base64,\n' + btoa(svg1.outerHTML));
        svgExport.setAttribute("download", "Phylo.io-cmp.svg");
    });

    /*------
     /
     /    SETTINGS
     /
     ------*/
    function hideOptionalSettings() {
        var v = $("label.opt");
        $(v).each(function (i) {
            ($(this).hide().next().hide())
        });
    }

    $("#settingsPanel").slideDown(300);
/*    $("#sidebar-wrapper").css({
        "overflow-y": "scroll"
    });*/

    $(".sizeAdjust").change(function () {
        treecomp.changeTreeSettings({
            lineThickness: $("#lineThickness").val(),
            nodeSize: $("#nodeSize").val(),
            fontSize: $("#fontSize").val()

        });
    });

    $("#useLengths").on("click", function () {
        treecomp.changeTreeSettings({
            useLengths: $("#useLengths").is(":checked")
        });
    });

    $("#alignTipLabels").on("click", function () {
        treecomp.changeTreeSettings({
            alignTipLabels: $("#alignTipLabels").is(":checked")
        });
    });

    $("#mirrorRightTree").on("click", function () {
        treecomp.changeTreeSettings({
            mirrorRightTree: $("#mirrorRightTree").is(":checked")
        });
    });

    $("input[name=comparisonMetric]:radio").on("change", function () {
        treecomp.changeTreeSettings({
            comparisonMetric: $("input[name=comparisonMetric]:checked").val()
        })
    });

    $("#moveOnMouseOver").on("click", function () {
        treecomp.changeTreeSettings({
            moveOnClick: $("#moveOnMouseOver").is(":checked")
        })
    });

    $("#selectMultipleSearch").on("click", function () {
        treecomp.changeTreeSettings({
            selectMultipleSearch: $("#selectMultipleSearch").is(":checked")
        })
    });


    $("input[name=internalLabels]:radio").on("change", function () {
        treecomp.changeTreeSettings({
            internalLabels: $("input[name=internalLabels]:checked").val()
        });
    });

}

function getTrees(firstTreeId, secondTreeId) {
	$.ajax({
		headers: { 
	        'Accept': 'application/json',
	        'Content-Type': 'application/json' 
	    },
		url: "/TreeCmp/trees",
		type: 'POST',
		dataType: 'json',
		data: "{\"firstTreeId\" : \""+ firstTreeId +"\", \"secondTreeId\" : \""+ secondTreeId +"\"}",
		success: function(data) {
			drawTrees(data.firstTreeNewick, data.secondTreeNewick);
		},
		error: function() {
			alert("something went wrong :(");
		}
	});
}
