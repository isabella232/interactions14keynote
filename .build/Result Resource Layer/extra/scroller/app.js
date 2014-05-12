(function() {
	var page = document.getElementById( "horizontalScroller" ) ||
			document.getElementById( "verticalScroller" ),
		vele = document.getElementById( "vscroller" ),
		hele = document.getElementById( "hscroller" ),
		hscroller, vscroller;

	page.addEventListener( "pageshow", function() {
		// make SectionChanger object
		if ( vele ) {
			vscroller = new tau.Scroller(vele, {
				scrollbar: "tab"
			});
		}

		if ( hele ) {
			hscroller = new tau.Scroller(hele, {
				orientation: "horizontal",
				scrollbar: "tab"
			});
		}
	});

	page.addEventListener( "pagehide", function() {
		// release object
		if ( vscroller ) {
			vscroller.destroy();
			vscroller = null;
		}

		if ( hscroller ) {
			hscroller.destroy();
			hscroller = null;
		}
	});

	page.addEventListener( "click", function(e) {
		console.debug(e.type, e.target);
	});

	page.addEventListener( "scrollstart", function(e) {
		console.debug(e.type, e.target);
	});

	page.addEventListener( "scrollend", function(e) {
		console.debug(e.type, e.target);
	});

	page.addEventListener( "scrollcancel", function(e) {
		console.debug(e.type, e.target);
	});

})();
