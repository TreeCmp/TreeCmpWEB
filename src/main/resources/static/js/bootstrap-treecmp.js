$('#accordion').collapse({
	toggle: true
});
$('.mini-alerts a').popover({
	trigger: 'hover',
	html: true
});
/*$('#accordion').on('shown.bs.collapse', function () {
	alert('Szkoda, �e zamkn��e� to okienko...');
	//jakikolwiek Tw�j kod
});
*/
$('#toggleAccordion').on('click', function () {
	$('#accordion').collapse('toggle');
});