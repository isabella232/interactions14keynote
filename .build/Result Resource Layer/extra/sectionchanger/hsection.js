(function() {

var page = document.getElementById( "hsectionchangerPage" ),
	changer = document.getElementById( "hsectionchanger" ),
	sectionChanger, idx=1;

page.addEventListener( "pageshow", function() {
	// make SectionChanger object
	sectionChanger = new tau.SectionChanger(changer, {
		circular: false,
		orientation: "horizontal"
	});
});

page.addEventListener( "pagehide", function() {
	// release object
	sectionChanger.destroy();
});
})();
