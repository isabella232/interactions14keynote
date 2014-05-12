(function() {
	var page = document.getElementById("pageTestVirtualList"),
		isb, vlist;

	page.addEventListener("pageshow", function() {
		var elisb = document.getElementById("indexscrollbar1"),
			elList = document.getElementById("vlist1"),
			listSize = JSON_DATA.length,
			lastOccuredChar = null,
			sideIndex = {},
			i;

		vlist = tau.VirtualListview(elList, {
				dataLength: listSize,
				bufferSize: 40
		});

		for (i = 0; i < listSize; i++){
			firstChar = JSON_DATA[i].NAME[0];

			if (firstChar !== lastOccuredChar) {
				sideIndex[firstChar] = i;
				lastOccuredChar = firstChar;
			}
		}

		// Update listitem
		vlist.setListItemUpdater(function(elListItem, newIndex) {
			//TODO: Update listitem here
			var data =  JSON_DATA[newIndex];

			elListItem.innerHTML = '<span class="ui-li-text-main">' + data.NAME+'</span>';
		});
		// Draw child elements
		vlist.draw();

		// Update the data-index attribute to the indexscrollbar element, with the index list above
		elisb.setAttribute("data-index", Object.keys(sideIndex).join(","));

		// Create IndexScrollbar
		isb = new tau.IndexScrollbar(elisb);

		// Bind a 'select' callback
		elisb.addEventListener("select", function(ev) {
			var idx = ev.detail.index;

			vlist.scrollToIndex(sideIndex[idx]);
		});
	});
	page.addEventListener("pagehide", function() {
		// Remove all children in the vlist
		vlist.destroy();
		isb.destroy();
	});
}());
