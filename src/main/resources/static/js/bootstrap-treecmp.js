$('#accordion').collapse({
	toggle: true
});
$('.mini-alerts a').popover({
	trigger: 'hover',
	html: true
});
$('#toggleAccordion').on('click', function () {
	$('#accordion').collapse('toggle');
});