(function() {
	var page = document.getElementById("pageTestVirtualList"),
		vlist;

	page.addEventListener("pageshow", function() {
		var elList = document.getElementById("vlist1");

		vlist = tau.VirtualListview(elList, {
				dataLength: JSON_DATA.length,
				bufferSize: 40
		});

		// Update listitem
		vlist.setListItemUpdater(function(elListItem, newIndex) {
			//TODO: Update listitem here
			var data =  JSON_DATA[newIndex];
			elListItem.innerHTML = '<span class="ui-li-text-main">' + data.NAME+'</span>';
		});
		// Draw child elements
		vlist.draw();
	});
	page.addEventListener("pagehide", function() {
		// Remove all children in the vlist
		vlist.destroy();
	});
}());
