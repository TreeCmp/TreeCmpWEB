$('#accordion').collapse({
	toggle: true
});
$('.mini-alerts a').popover({
	trigger: 'hover',
	html: true
});
/*$('#accordion').on('shown.bs.collapse', function () {
	alert('Szkoda, ¿e zamkn¹³eœ to okienko...');
	//jakikolwiek Twój kod
});
*/
$('#toggleAccordion').on('click', function () {
	$('#accordion').collapse('toggle');
});