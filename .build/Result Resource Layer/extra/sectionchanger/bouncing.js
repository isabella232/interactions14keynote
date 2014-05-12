(function() {

var page = document.getElementById( "bouncingsectionchangerPage" ),
	changer = document.getElementById( "bouncingsectionchanger" ),
	sectionChanger, idx=1;

page.addEventListener( "pageshow", function() {
	// make SectionChanger object
	sectionChanger = new tau.SectionChanger(changer, {
		circular: false,
		orientation: "horizontal",
		scrollbar: "bar",
		useBouncingEffect: true
	});
});

page.addEventListener( "pagehide", function() {
	// release object
	sectionChanger.destroy();
});
})();
