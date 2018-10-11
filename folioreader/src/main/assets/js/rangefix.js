/*!
 * RangeFix v0.2.6
 * https://github.com/edg2s/rangefix
 *
 * Copyright 2014-17 Ed Sanders.
 * Released under the MIT license
 */
( function ( root, factory ) {
	if ( typeof define === 'function' && define.amd ) {
		// AMD. Register as an anonymous module.
		define( factory );
	} else if ( typeof exports === 'object' && typeof exports.nodeName !== 'string' ) {
		// CommonJS
		module.exports = factory();
	} else {
		// Browser globals
		root.RangeFix = factory();
	}
}( this, function () {

	var broken,
		rangeFix = {};

	/**
	 * Check if bugs are present in the native functions
	 *
	 * For getClientRects, constructs two lines of text and
	 * creates a range between them. Broken browsers will
	 * return three rectangles instead of two.
	 *
	 * For getBoundingClientRect, create a collapsed range
	 * and check if the resulting rect has non-zero offsets.
	 *
	 * getBoundingClientRect is also considered broken if
	 * getClientRects is broken.
	 *
	 * For the IE zoom bug, just check the version number as
	 * we can't detect the bug if the zoom level is currently 100%.
	 *
	 * @private
	 * @return {Object} Object containing boolean properties 'getClientRects',
	 *                  'getBoundingClientRect' and 'ieZoom' indicating bugs are present
	 *                  in these functions/browsers.
	 */
	rangeFix.isBroken = function () {
		var boundingRect, p, span, t1, t2, img, range, jscriptVersion;

		if ( broken === undefined ) {
			p = document.createElement( 'p' );
			span = document.createElement( 'span' );
			t1 = document.createTextNode( 'aa' );
			t2 = document.createTextNode( 'aa' );
			img = document.createElement( 'img' );
			img.setAttribute( 'src', '#null' );
			range = document.createRange();

			broken = {};

			p.appendChild( t1 );
			p.appendChild( span );
			span.appendChild( img );
			span.appendChild( t2 );

			document.body.appendChild( p );

			range.setStart( t1, 1 );
			range.setEnd( span, 0 );

			// A selection ending just inside another element shouldn't select that whole element
			// Broken in Chrome <= 55 and Firefox
			broken.getClientRects = broken.getBoundingClientRect = range.getClientRects().length > 1;

			if ( !broken.getClientRects ) {
				// Regression in Chrome 55:
				// A selection across a wrapped image should give a rect for that image.
				// In Chrome we get two rectangles, one for each text node. In working browsers
				// we get three or more, or in Edge we get one surrounding the text and the image.
				range.setEnd( t2, 1 );
				broken.getClientRects = broken.getBoundingClientRect = range.getClientRects().length === 2;
			}

			if ( !broken.getBoundingClientRect ) {
				// Safari doesn't return a valid bounding rect for collapsed ranges
				// Equivalent to range.collapse( true ) which isn't well supported
				range.setEnd( range.startContainer, range.startOffset );
				boundingRect = range.getBoundingClientRect();
				broken.getBoundingClientRect = boundingRect.top === 0 && boundingRect.left === 0;
			}

			document.body.removeChild( p );

			// Detect IE<=10 where zooming scaling is broken
			// eslint-disable-next-line no-new-func
			jscriptVersion = window.ActiveXObject && new Function( '/*@cc_on return @_jscript_version; @*/' )();
			broken.ieZoom = !!jscriptVersion && jscriptVersion <= 10;
		}
		return broken;
	};

	/**
	 * Compensate for the current zoom level in IE<=10
	 *
	 * getClientRects returns values in real pixels in these browsers,
	 * so using them in your CSS will result in them getting scaled again.
	 *
	 * @private
	 * @param {ClientRectList|ClientRect[]|ClientRect|Object|null} rectOrRects Rect or list of rects to fix
	 * @return {ClientRectList|ClientRect[]|ClientRect|Object|null} Fixed rect or list of rects
	 */
	function zoomFix( rectOrRects ) {
		var zoom;
		if ( !rectOrRects ) {
			return rectOrRects;
		}
		// Optimisation when zoom level is 1: return original object
		if ( screen.deviceXDPI === screen.logicalXDPI ) {
			return rectOrRects;
		}
		// Rect list: map this function to each rect
		if ( 'length' in rectOrRects ) {
			return Array.prototype.map.call( rectOrRects, zoomFix );
		}
		// Single rect: Adjust by zoom factor
		zoom = screen.deviceXDPI / screen.logicalXDPI;
		return {
			top: rectOrRects.top / zoom,
			bottom: rectOrRects.bottom / zoom,
			left: rectOrRects.left / zoom,
			right: rectOrRects.right / zoom,
			width: rectOrRects.width / zoom,
			height: rectOrRects.height / zoom
		};
	}

	/**
	 * Push one array-like object onto another.
	 *
	 * @param {Object} arr Array or array-like object. Will be modified
	 * @param {Object} data Array-like object of items to insert.
	 * @return {number} length of the new array
	 */
	function batchPush( arr, data ) {
		// We need to push insertion in batches, because of parameter list length limits which vary
		// cross-browser - 1024 seems to be a safe batch size on all browsers
		var length,
			index = 0,
			batchSize = 1024;
		if ( batchSize >= data.length ) {
			// Avoid slicing for small lists
			return Array.prototype.push.apply( arr, data );
		}
		while ( index < data.length ) {
			// Call arr.push( i0, i1, i2, ..., i1023 );
			length = Array.prototype.push.apply(
				arr, Array.prototype.slice.call( data, index, index + batchSize )
			);
			index += batchSize;
		}
		return length;
	}

	/**
	 * Get client rectangles from a range
	 *
	 * @param {Range} range Range
	 * @return {ClientRectList|ClientRect[]} ClientRectList or list of ClientRect objects describing range
	 */
	rangeFix.getClientRects = function ( range ) {
		var rects, endContainer, endOffset, partialRange,
			broken = this.isBroken();

		if ( broken.ieZoom ) {
			return zoomFix( range.getClientRects() );
		} else if ( !broken.getClientRects ) {
			return range.getClientRects();
		}

		// Chrome gets the end container rects wrong when spanning
		// nodes so we need to traverse up the tree from the endContainer until
		// we reach the common ancestor, then we can add on from start to where
		// we got up to
		// https://code.google.com/p/chromium/issues/detail?id=324437
		rects = [];
		endContainer = range.endContainer;
		endOffset = range.endOffset;
		partialRange = document.createRange();

		while ( endContainer !== range.commonAncestorContainer ) {
			partialRange.setStart( endContainer, 0 );
			partialRange.setEnd( endContainer, endOffset );

			batchPush( rects, partialRange.getClientRects() );

			endOffset = Array.prototype.indexOf.call( endContainer.parentNode.childNodes, endContainer );
			endContainer = endContainer.parentNode;
		}

		// Once we've reached the common ancestor, add on the range from the
		// original start position to where we ended up.
		partialRange = range.cloneRange();
		partialRange.setEnd( endContainer, endOffset );
		batchPush( rects, partialRange.getClientRects() );
		return rects;
	};

	/**
	 * Get bounding rectangle from a range
	 *
	 * @param {Range} range Range
	 * @return {ClientRect|Object|null} ClientRect or ClientRect-like object describing
	 *                                  bounding rectangle, or null if not computable
	 */
	rangeFix.getBoundingClientRect = function ( range ) {
		var i, l, boundingRect, rect, nativeBoundingRect, broken,
			rects = this.getClientRects( range );

		// If there are no rects return null, otherwise we'll fall through to
		// getBoundingClientRect, which in Chrome and Firefox becomes [0,0,0,0].
		if ( rects.length === 0 ) {
			return null;
		}

		nativeBoundingRect = range.getBoundingClientRect();
		broken = this.isBroken();

		if ( broken.ieZoom ) {
			return zoomFix( nativeBoundingRect );
		} else if ( !broken.getBoundingClientRect ) {
			return nativeBoundingRect;
		}

		// When nativeRange is a collapsed cursor at the end of a line or
		// the start of a line, the bounding rect is [0,0,0,0] in Chrome.
		// getClientRects returns two rects, one correct, and one at the
		// end of the next line / start of the previous line. We can't tell
		// here which one to use so just pick the first. This matches
		// Firefox's behaviour, which tells you the cursor is at the end
		// of the previous line when it is at the start of the line.
		// See https://code.google.com/p/chromium/issues/detail?id=426017
		if ( nativeBoundingRect.width === 0 && nativeBoundingRect.height === 0 ) {
			return rects[ 0 ];
		}

		for ( i = 0, l = rects.length; i < l; i++ ) {
			rect = rects[ i ];
			if ( !boundingRect ) {
				boundingRect = {
					left: rect.left,
					top: rect.top,
					right: rect.right,
					bottom: rect.bottom
				};
			} else {
				boundingRect.left = Math.min( boundingRect.left, rect.left );
				boundingRect.top = Math.min( boundingRect.top, rect.top );
				boundingRect.right = Math.max( boundingRect.right, rect.right );
				boundingRect.bottom = Math.max( boundingRect.bottom, rect.bottom );
			}
		}
		if ( boundingRect ) {
			boundingRect.width = boundingRect.right - boundingRect.left;
			boundingRect.height = boundingRect.bottom - boundingRect.top;
		}
		return boundingRect;
	};

	return rangeFix;
} ) );