(function() {
	var page = document.getElementById("pageIndexScrollbar1"),
		isb;
	page.addEventListener("pageshow", function(ev) {

/*****************************************************************
	IndexScrollbar example

indexScrollbar.js and indexScrollbar.css are required
to be included.

* Usage
------------------------------------------------------------------
<div id="foo" class="ui-indexScrollbar" data-index="1,2,3"></div>

// Create an IndexScrollbar
var el = document.getElementById("foo"),
    indexScrollBar = tau.IndexScrollbar(el);

// Bind select event callback
el.addEventListener("select", function( ev ) {
	// the index string is stored in the ev.detail.index.
	var index = ev.detail.index;

	// Do anything you want with this index.
	console.log(index);
});
------------------------------------------------------------------

* HTML property

  * data-index : A string, having index strings concatenated by ','. For example,
                 "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".

* Function

  * IndexScrollbar( element ) : Extract the sub-elements into the given element.

******************************************************************/

		var elisb = document.getElementById("indexscrollbar1"),
			elList = document.getElementById("list1"),	// list
			elDividers = elList.getElementsByClassName("li-divider"),	// list dividers
			elScroller = elList.parentElement,	// the scroller (overflow-y:hidden)
			dividers = {},	// collection of list dividers
			indices = [],	// index list
			elDivider,
			i, idx;

		// For all list dividers,
		for(i=0; i < elDividers.length; i++) {
			// Add the list divider elements to the collection
			elDivider = elDividers[i];
			idx = elDivider.innerText;
			dividers[idx] = elDivider;

			// Add the index to the index list
			indices.push(idx);
		}

		// Update the data-index attribute to the indexscrollbar element, with the index list above
		elisb.setAttribute("data-index", indices.join(","));

		// Create IndexScrollbar
		isb = new tau.IndexScrollbar(elisb);

		// Bind a 'select' callback
		elisb.addEventListener("select", function(ev) {
			var elDivider,
				idx = ev.detail.index;
			elDivider = dividers[idx];
			if(elDivider) {
				// Scroll to the li-divider element
				elScroller.scrollTop = elDivider.offsetTop - elScroller.offsetTop;
			}
		});
	});

	page.addEventListener("pagehide", function(ev) {
		isb.destroy();
	});
} ());

