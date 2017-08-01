var tnt_theme = function() {
    var tree_theme = function (tree_vis, div) {

	tree_vis
	    .label(tnt.tree.label.text()
		   .height(20)
		  )
	    .layout(tnt.tree.layout.vertical()
		    .width(600)
		    .scale(true));

		tree_vis.on("click", function (node) {
			node.toggle();
			tree_vis.update();
		});

	tree_vis(div);
    };

    return tree_theme;
};