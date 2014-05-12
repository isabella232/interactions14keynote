(function(){

	var page = document.getElementById( "swipelist" ),
	listElement = page.getElementsByClassName("genlist","ul")[0],
	callElement = page.getElementsByClassName("genlist-call","div")[0],
	messageElement = page.getElementsByClassName("genlist-message","div")[0],
	swipeList;
	page.addEventListener( "pageshow", function() {
		// make SwipeList object
		swipeList = new SwipeList( listElement, callElement, messageElement );
	});
	page.addEventListener( "pagehide", function() {
		// release object
		swipeList.destroy();
	});
})();
