/*
  * Copyright (c) 2013 Samsung Electronics Co., Ltd
  *
  * Licensed under the Flora License, Version 1.1 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://floralicense.org/license/
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

(function(window, undefined) {
	'use strict';

var eventType = {
		//swipelist provide two event 
		CALL: "swipelist.call",
		MESSAGE: "swipelist.message"
};

var SwipeList = function ( listElement, callElement, messageElement, options ){
	this._create( listElement, callElement, messageElement, options );
	return this;
}

SwipeList.prototype = {
	_create: function( listElement, callElement, messageElement, options ) {
		var page;
		if( !this.listElement || !this.callElement || !this.messageElement ) {
			// If developer don't give parameter, this object set default section
			page = document.getElementsByClassName("ui-page-active");
			this.listElement = page[0].getElementsByClassName("genlist","ul")[0];
			this.callElement = page[0].getElementsByClassName("genlist-call","div")[0];
			this.messageElement = page[0].getElementsByClassName("genlist-message","div")[0];
		}

		this.callElementBG = page[0].getElementsByClassName("genlist-call-background")[0];
		this.messageElementBG = page[0].getElementsByClassName("genlist-message-background")[0];
		this.contentElement = page[0].getElementsByClassName("ui-content")[0];
		this.listElement = listElement;
		this.callElement = callElement;
		this.messageElement = messageElement;
		this.activeElement = callElement;

		this.options = {};

		this._multitouch = false;
		this.startX = 0;
		this.dragging = 0;
		this._interval = 0;
		this._lastScrollTop = 0;
		this._lastElementTop = 0;

		this._callElementStyle = this.callElement.style;
		this._messageElementStyle = this.messageElement.style;


		this._initOptions( options );
		this._bindEvents();
		this._init();
	},

	_init: function() {
		this._callElementStyle["display"] = "none";
		this._messageElementStyle["display"] = "none";
		this._translate( this.callElementBG, this.options.callStartPosition, 0, 0 ) ;
		this._translate( this.messageElementBG, this.options.messageStartPosition, 0, 0 ) ;

	},

	_initOptions: function( options ){
		this.options = {
			threshold: 10,
			animationThreshold: 150,
			animationDuration: 300,
			callStartPosition: -320,
			messageStartPosition: 0,
			callEndPosition: 0,
			messageEndPosition: -320
		}
		this.setOptions( options );
	},
	setOptions: function ( options ) {
		var name;
		for ( name in options ) {
			if ( options.hasOwnProperty(name) && !!options[name] ) {
				this.options[name] = options[name];
			}
		}
	},

	_bindEvents: function( ) {
		if ('ontouchstart' in window) {
			this.listElement.addEventListener( "touchstart", this );
			this.listElement.addEventListener( "touchmove", this );
			this.listElement.addEventListener( "touchend", this );
		} else {
			this.listElement.addEventListener( "mousedown", this );
			document.addEventListener( "mousemove", this );
			document.addEventListener( "mouseup", this );
		}

		this.contentElement.addEventListener( "scroll", this);
		document.addEventListener( "webkitTransitionEnd", this );
		document.addEventListener( "touchcancel", this );
	},

	_unbindEvents: function() {
		if ('ontouchstart' in window) {
			this.listElement.removeEventListener( "touchstart", this);
			this.listElement.removeEventListener( "touchmove", this);
			this.listElement.removeEventListener( "touchend", this);
		} else {
			this.listElement.removeEventListener( "mousedown", this);
			document.removeEventListener( "mousemove", this);
			document.removeEventListener( "mouseup", this);
		}

		this.contentElement.removeEventListener( "scroll", this );
		document.removeEventListener( "webkitTransitionEnd", this );
		document.removeEventListener( "touchcancel", this );
	},

	handleEvent: function( event ) {
		var pos = this._getPointPositionFromEvent( event );

		switch (event.type) {
		case "mousedown":
		case "touchstart":
			this._start( event, pos );
			break;
		case "mousemove":
		case "touchmove":
			this._move( event, pos );
			break;
		case "mouseup":
		case "touchend":
			this._end( event, pos );
			break;
		case "webkitTransitionEnd":
			this._transitionEnd( event );
			break;
		case "touchcancel":
			this._cancel();
			break;
		case "scroll":
			this._scroll = true;
		}
	},

	_translate: function( elem, x, y, duration ) {
		var translate,
			transition,
			elemStyle = elem.style;

		if ( !duration ) {
			transition = "none";
		} else {
			transition = "-webkit-transform " + duration / 1000 + "s ease-out";
		}
		translate = "translate3d(" + x + "px," + y + "px, 0)";

		this.scrollerOffsetX = window.parseInt(x, 10);
		this.scrollerOffsetY = window.parseInt(y, 10);

		elemStyle["-webkit-transform"] = translate;
		elemStyle["-webkit-transition"] = transition;
	},

	_getPointPositionFromEvent: function ( ev ) {
		var multiTouchThreshold = 1;
		if(ev.type === "touchend") {
			multiTouchThreshold = 0;
		}
		if ( ev.touches && ev.touches.length > multiTouchThreshold) {
			this._multitouch = true;
		} else {
			this._multitouch = false;
		}
		return ev.type.search(/^touch/) !== -1 && ev.touches && ev.touches.length ?
				{x: ev.touches[0].clientX, y: ev.touches[0].clientY} :
				{x: ev.clientX, y: ev.clientY};
	},

	_fireEvent: function( eventName, detail ) {
		var evt = new CustomEvent( eventName, {
				"bubbles": true,
				"cancelable": true,
				"detail": detail
			});
		this.listElement.dispatchEvent(evt);
	},

	_detectLiTarget: function( target ) {
		while (target && target.tagName !== 'LI') {
			target = target.parentNode;
		}
		return target;
	},

	_setMovingElementTop: function( element ){

		var diff = this._lastScrollTop - this.contentElement.scrollTop;
		element.style.top = parseInt( this._lastElementTop, 10 ) + diff + "px";
	},

	_start: function( e, pos ) {

		if ( this._multitouch === true || this._scroll ){
			return;
		}

		if ( this._detectLiTarget( e.target ) ) {
			this.startX = pos.x;
			this.dragging = true;
		}

	},

	_move: function( e, pos ) {
		if ( this._multitouch === true || this._scroll ){
			this._setMovingElementTop( this.activeElement );
			return;
		}

		var target = this._detectLiTarget(e.target);

		this._interval = pos.x - this.startX;

		if( this.dragging && target ) {

			if ( Math.abs( this._interval ) > this.options.threshold ) {
				if ( this._interval > 0 ) {
					this.activeElement = this.callElement;
					this.messageElement.style.display = "none";
					this._translate( this.callElementBG, this.options.callStartPosition + this._interval, 0, 0 );
				} else {
					this.activeElement = this.messageElement;
					this.callElement.style.display = "none";
					this._translate( this.messageElementBG, this.options.messageStartPosition + this._interval, 0, 0 );
				}
				this.activeElement.style.top = target.offsetTop - this.contentElement.scrollTop + "px";
				this.activeElement.style.display = "block";
				this._activeFlag = true;
				e.preventDefault();
			} else if ( this._activeFlag ) {
				e.preventDefault();
			}
			this._lastScrollTop = this.contentElement.scrollTop;
			this._lastElementTop = this.activeElement.style.top;
		}
	},

	_end: function( e ) {
		if ( this._multitouch === true ){
			this.dragging = false;
			return;
		}

		this._lastScrollTop = this.contentElement.scrollTop;
		this._lastElementTop = this.activeElement.style.top;

		if( this._interval > this.options.animationThreshold ) {
			this._translate( this.callElementBG, this.options.callEndPosition, 0, this.options.animationDuration );
			this._fireEvent( eventType.CALL );
		} else if( this._interval < -this.options.animationThreshold ) {
			this._translate( this.messageElementBG, this.options.messageEndPosition, 0, this.options.animationDuration );
			this._fireEvent( eventType.MESSAGE );
		} else {
			this._translate( this.callElementBG, this.options.callStartPosition, 0, 0);
			this._translate( this.messageElementBG, this.options.messageStartPosition, 0, 0);
			this.callElement.style.display = "none";
			this.messageElement.style.display = "none";
			this._activeFlag = false;
		}
		this._scroll = false;
		this.dragging = false;
	},

	_transitionEnd: function() {
		if ( this._multitouch === true ){
			return;
		}
		this.callElement.style.display = "none";
		this.messageElement.style.display = "none";
		this._translate( this.callElementBG, this.options.callStartPosition, 0, 0);
		this._translate( this.messageElementBG, this.options.messageStartPosition, 0, 0);
		this._activeFlag = false;
	},

	_cancel: function() {
		// This handler method was called when touchcancel event fired.
		// touchcancel event fired when user click home button.
		this._translate( this.callElementBG, this.options.callStartPosition, 0, 0);
		this._translate( this.messageElementBG, this.options.messageStartPosition, 0, 0);
		this.callElement.style.display = "none";
		this.messageElement.style.display = "none";
		this._activeFlag = false;
	},

	destroy: function() {

		this._unbindEvents();
		this.listElement = null;
		this.callElement = null;
		this.messageElement = null;
	}
}

window.SwipeList = SwipeList;

})(this);
