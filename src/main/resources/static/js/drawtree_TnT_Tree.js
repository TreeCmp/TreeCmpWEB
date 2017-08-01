function drawTree(divId, newick) {
	 var tree_vis = tnt.tree()
     	.data(tnt.tree.parse_newick(newick));
	var theme = tnt_theme();
	
	clear(divId);
	theme(tree_vis, document.getElementById(divId))
}

function drawTrees(newickTree1, newickTree2) {
	drawTree("firstTreeVis", newickTree1);
	if(newickTree2.length !== 0)
		drawTree("secondTreeVis", newickTree2);
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
			extendTrees();
		},
		error: function() {
			alert("something went wrong :(");
		}
	});
}

function extendTrees() {
	var svgs = $("svg");
	$(svgs[0]).width(700);
	$(svgs[1]).width(700);
}

function clear(divId)
{
    document.getElementById(divId).innerHTML = "";
}