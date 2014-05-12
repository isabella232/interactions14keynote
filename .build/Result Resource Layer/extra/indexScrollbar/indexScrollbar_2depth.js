(function() {
	var page = document.getElementById("pageIndexScrollbar2"),
		isb,
		index = [],
		supIndex = {},
		elIndex = {};
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

		var elisb = document.getElementById("indexscrollbar2"),
			elList = document.getElementById("ibar2_list2"),	// list
			elItems = elList.children,
			elScroller = elList.parentElement,	// the scroller (overflow-y:hidden)
			indexData = getIndexData({
				array: elItems,
				getTextValue: function(array, i) {
					return array[i].innerText;
				}
			});

		// Get index data from array
		// options = {
		//   array: An array of data
		//   getTextValue: function(array, index){
		//       // return the text string of the given index
		//   }
		// }
		function getIndexData(options) {
			var array = options.array,
				getTextValue = options.getTextValue,
				item,
				text,
				firstIndex = [],
				secondIndex = {},
				keyItem = {},
				c1 = null, c2 = null,
				i;

			for(i=0; i < array.length; i++) {
				item = array[i];
				text = getTextValue(array, i);
				if(text.length > 0) {
					if(!c1 || c1 !== text[0]) {	// new c1
						c1 = text[0];
						firstIndex.push(c1);
						keyItem[c1] = item;
						secondIndex[c1] = [];

						c2 = text[1];
						if(c2) {
							secondIndex[c1].push(c2);
						} else {
							c2 = '';
						}
						keyItem[c1+c2] = item;
					} else {	// existing c1
						if(c2 !== text[1]) {
							c2 = text[1];
							secondIndex[c1].push(c2);
							keyItem[c1+c2] = item;
						}
					}
				}
			}
			return {
				firstIndex: firstIndex,
				secondIndex: secondIndex,
				keyItem: keyItem
			};
		}

		// Update the data-index attribute to the indexscrollbar element, with the index list above
		elisb.setAttribute("data-index", indexData.firstIndex);

		// Create IndexScrollbar
		isb = new tau.IndexScrollbar(elisb, {
			index: indexData.firstIndex,
			supplementaryIndex: function(firstIndex) {
				return indexData.secondIndex[firstIndex];
			}
		});

		// Bind 'select' callback
		elisb.addEventListener("select", function(ev) {
			var el,
				index = ev.detail.index;
			el = indexData.keyItem[index];
			if(el) {
				// Scroll to the li-divider element
				elScroller.scrollTop = el.offsetTop - elScroller.offsetTop;
			}
		});
	});

	page.addEventListener("pagehide", function(ev) {
		isb.destroy();
		index.length = 0;
		supIndex = {};
		elIndex = {};
	});
} ());

