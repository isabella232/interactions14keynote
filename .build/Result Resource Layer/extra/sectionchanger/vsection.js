(function() {

var page = document.getElementById( "vsectionchangerPage" ),
	changer = document.getElementById( "vsectionchanger" ),
	sectionChanger, idx=1;

page.addEventListener( "pageshow", function() {
	// make SectionChanger object
	sectionChanger = new tau.SectionChanger(changer, {
		circular: false,
		orientation: "vertical"
	});
});

page.addEventListener( "pagehide", function() {
	// release object
	sectionChanger.destroy();
});
})();
