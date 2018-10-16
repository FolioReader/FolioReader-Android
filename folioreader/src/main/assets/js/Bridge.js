//
//  Bridge.js
//  FolioReader-Android
//
//  Created by Heberti Almeida on 06/05/15.
//  Copyright (c) 2015 Folio Reader. All rights reserved.
//

var thisHighlight;
var audioMarkClass;
var wordsPerMinute = 180;

var Direction = Object.freeze({
    VERTICAL: "VERTICAL",
    HORIZONTAL: "HORIZONTAL"
});

var DisplayUnit = Object.freeze({
    PX: "PX",
    DP: "DP",
    CSS_PX: "CSS_PX"
});

var scrollWidth;
var horizontalInterval;
var horizontalIntervalPeriod = 1000;
var horizontalIntervalCounter = 0;
var horizontalIntervalLimit = 3000;

var searchResults = [];
var lastSearchQuery = null;
var testCounter = 0;
var searchResultsInvisible = true;

var viewportRect;

// Class manipulation
function hasClass(ele, cls) {
    return !!ele.className.match(new RegExp('(\\s|^)' + cls + '(\\s|$)'));
}

function addClass(ele, cls) {
    if (!hasClass(ele, cls)) ele.className += " " + cls;
}

function removeClass(ele, cls) {
    if (hasClass(ele, cls)) {
        var reg = new RegExp('(\\s|^)' + cls + '(\\s|$)');
        ele.className = ele.className.replace(reg, ' ');
    }
}

// Menu colors
function setHighlightStyle(style) {
    Highlight.getUpdatedHighlightId(thisHighlight.id, style);
}

function removeThisHighlight() {
    return thisHighlight.id;
}

function removeHighlightById(elmId) {
    var elm = document.getElementById(elmId);
    elm.outerHTML = elm.innerHTML;
    return elm.id;
}

function getHighlightContent() {
    return thisHighlight.textContent
}

function getBodyText() {
    return document.body.innerText;
}

// Method that gets the Rect of current selected text
// and returns in a JSON format
var getRectForSelectedText = function (elm) {
    if (typeof elm === "undefined") elm = window.getSelection().getRangeAt(0);

    var rect = elm.getBoundingClientRect();
    return "{{" + rect.left + "," + rect.top + "}, {" + rect.width + "," + rect.height + "}}";
};

// Reading time
function getReadingTime() {
    var text = document.body.innerText;
    var totalWords = text.trim().split(/\s+/g).length;
    var wordsPerSecond = wordsPerMinute / 60; //define words per second based on words per minute
    var totalReadingTimeSeconds = totalWords / wordsPerSecond; //define total reading time in seconds
    var readingTimeMinutes = Math.round(totalReadingTimeSeconds / 60);

    return readingTimeMinutes;
}

function scrollAnchor(id) {
    window.location.hash = id;
}

/**
 Remove All Classes - removes the given class from all elements in the DOM
 */
function removeAllClasses(className) {
    var els = document.body.getElementsByClassName(className)
    if (els.length > 0)
        for (i = 0; i <= els.length; i++) {
            els[i].classList.remove(className);
        }
}

/**
 Audio Mark ID - marks an element with an ID with the given class and scrolls to it
 */
function audioMarkID(className, id) {
    if (audioMarkClass)
        removeAllClasses(audioMarkClass);

    audioMarkClass = className
    var el = document.getElementById(id);

    scrollToElement(el);
    el.classList.add(className)
}

function setMediaOverlayStyle(style) {
    document.documentElement.classList.remove("mediaOverlayStyle0", "mediaOverlayStyle1", "mediaOverlayStyle2")
    document.documentElement.classList.add(style)
}

function setMediaOverlayStyleColors(color, colorHighlight) {
    var stylesheet = document.styleSheets[document.styleSheets.length - 1];
//    stylesheet.insertRule(".mediaOverlayStyle0 span.epub-media-overlay-playing { background: "+colorHighlight+" !important }")
//    stylesheet.insertRule(".mediaOverlayStyle1 span.epub-media-overlay-playing { border-color: "+color+" !important }")
//    stylesheet.insertRule(".mediaOverlayStyle2 span.epub-media-overlay-playing { color: "+color+" !important }")
}

var currentIndex = -1;


function findSentenceWithIDInView(els) {
    // @NOTE: is `span` too limiting?
    for (indx in els) {
        var element = els[indx];

        // Horizontal scroll
        if (document.body.scrollTop == 0) {
            var elLeft = document.body.clientWidth * Math.floor(element.offsetTop / window.innerHeight);
            // document.body.scrollLeft = elLeft;

            if (elLeft == document.body.scrollLeft) {
                currentIndex = indx;
                return element;
            }

            // Vertical
        } else if (element.offsetTop > document.body.scrollTop) {
            currentIndex = indx;
            return element;
        }
    }

    return null
}

function findNextSentenceInArray(els) {
    if (currentIndex >= 0) {
        currentIndex++;
        return els[currentIndex];
    }

    return null
}

function resetCurrentSentenceIndex() {
    currentIndex = -1;
}

function rewindCurrentIndex() {
    currentIndex = currentIndex - 1;
}

function getSentenceWithIndex(className) {
    var sentence;
    var sel = getSelection();
    var node = null;
    var elements = document.querySelectorAll("span.sentence");

    // Check for a selected text, if found start reading from it
    if (sel.toString() != "") {
        console.log(sel.anchorNode.parentNode);
        node = sel.anchorNode.parentNode;

        if (node.className == "sentence") {
            sentence = node

            for (var i = 0, len = elements.length; i < len; i++) {
                if (elements[i] === sentence) {
                    currentIndex = i;
                    break;
                }
            }
        } else {
            sentence = findSentenceWithIDInView(elements);
        }
    } else if (currentIndex < 0) {
        sentence = findSentenceWithIDInView(elements);
    } else {
        sentence = findNextSentenceInArray(elements);
    }

    var text = sentence.innerText || sentence.textContent;

    scrollToElement(sentence);

    if (audioMarkClass) {
        removeAllClasses(audioMarkClass);
    }

    audioMarkClass = className;
    sentence.classList.add(className)
    return text;
}

$(function () {
    window.ssReader = Class({
        $singleton: true,

        init: function () {
            rangy.init();

            this.highlighter = rangy.createHighlighter();

            this.highlighter.addClassApplier(rangy.createClassApplier("highlight_yellow", {
                ignoreWhiteSpace: true,
                tagNames: ["span", "a"]
            }));

            this.highlighter.addClassApplier(rangy.createClassApplier("highlight_green", {
                ignoreWhiteSpace: true,
                tagNames: ["span", "a"]
            }));

            this.highlighter.addClassApplier(rangy.createClassApplier("highlight_blue", {
                ignoreWhiteSpace: true,
                tagNames: ["span", "a"]
            }));

            this.highlighter.addClassApplier(rangy.createClassApplier("highlight_pink", {
                ignoreWhiteSpace: true,
                tagNames: ["span", "a"]
            }));

            this.highlighter.addClassApplier(rangy.createClassApplier("highlight_underline", {
                ignoreWhiteSpace: true,
                tagNames: ["span", "a"]
            }));

        },

        setFontAndada: function () {
            this.setFont("andada");
        },

        setFontLato: function () {
            this.setFont("lato");
        },

        setFontPtSerif: function () {
            this.setFont("pt-serif");
        },

        setFontPtSans: function () {
            this.setFont("pt-sans");
        },

        base64encode: function (str) {
            return btoa(unescape(encodeURIComponent(str)));
        },

        base64decode: function (str) {
            return decodeURIComponent(escape(atob(str)));
        },

        clearSelection: function () {
            if (window.getSelection) {
                if (window.getSelection().empty) {  // Chrome
                    window.getSelection().empty();
                } else if (window.getSelection().removeAllRanges) {  // Firefox
                    window.getSelection().removeAllRanges();
                }
            } else if (document.selection) {  // IE?
                document.selection.empty();
            }
        },

        // Public methods

        setFont: function (fontName) {
            $("#ss-wrapper-font").removeClass().addClass("ss-wrapper-" + fontName);
        },

        setSize: function (size) {
            $("#ss-wrapper-size").removeClass().addClass("ss-wrapper-" + size);
        },

        setTheme: function (theme) {
            $("body, #ss-wrapper-theme").removeClass().addClass("ss-wrapper-" + theme);
        },

        setComment: function (comment, inputId) {
            $("#" + inputId).val(ssReader.base64decode(comment));
            $("#" + inputId).trigger("input", ["true"]);
        },

        highlightSelection: function (color) {
            try {

                this.highlighter.highlightSelection(color, null);
                var range = window.getSelection().toString();
                var params = {content: range, rangy: this.getHighlights(), color: color};
                this.clearSelection();
                Highlight.onReceiveHighlights(JSON.stringify(params));
            } catch (err) {
                console.log("highlightSelection : " + err);
            }
        },

        unHighlightSelection: function () {
            try {
                this.highlighter.unhighlightSelection();
                Highlight.onReceiveHighlights(this.getHighlights());
            } catch (err) {
            }
        },

        getHighlights: function () {
            try {
                return this.highlighter.serialize();
            } catch (err) {
            }
        },

        setHighlights: function (serializedHighlight) {
            try {
                this.highlighter.removeAllHighlights();
                this.highlighter.deserialize(serializedHighlight);
            } catch (err) {
            }
        },

        removeAll: function () {
            try {
                this.highlighter.removeAllHighlights();
            } catch (err) {
            }
        },

        copy: function () {
            SSBridge.onCopy(window.getSelection().toString());
            this.clearSelection();
        },

        share: function () {
            SSBridge.onShare(window.getSelection().toString());
            this.clearSelection();
        },

        search: function () {
            SSBridge.onSearch(window.getSelection().toString());
            this.clearSelection();
        }
    });

    if (typeof ssReader !== "undefined") {
        ssReader.init();
    }

    $(".verse").click(function () {
        SSBridge.onVerseClick(ssReader.base64encode($(this).attr("verse")));
    });

    $("code").each(function (i) {
        var textarea = $("<textarea class='textarea'/>").attr("id", "input-" + i).on("input propertychange", function (event, isInit) {
            $(this).css({'height': 'auto', 'overflow-y': 'hidden'}).height(this.scrollHeight);
            $(this).next().css({'height': 'auto', 'overflow-y': 'hidden'}).height(this.scrollHeight);

            if (!isInit) {
                var that = this;
                if (timeout !== null) {
                    clearTimeout(timeout);
                }
                timeout = setTimeout(function () {
                    SSBridge.onCommentsClick(
                        ssReader.base64encode($(that).val()),
                        $(that).attr("id")
                    );
                }, 1000);
            }
        });
        var border = $("<div class='textarea-border' />");
        var container = $("<div class='textarea-container' />");

        $(textarea).appendTo(container);
        $(border).appendTo(container);

        $(this).after(container);
    });
});

function array_diff(array1, array2) {
    var difference = $.grep(array1, function (el) {
        return $.inArray(el, array2) < 0
    });
    return difference.concat($.grep(array2, function (el) {
        return $.inArray(el, array1) < 0
    }));
    ;
}

//For testing purpose only
function sleep(seconds) {
    var e = new Date().getTime() + (seconds * 1000);
    while (new Date().getTime() <= e) {
    }
}

// Mock objects for testing purpose
/*var FolioPageFragment = {

    setHorizontalPageCount : function(pageCount) {
        console.warn("-> Mock call to FolioPageFragment.setHorizontalPageCount(" + pageCount + ")");
    },

    storeFirstVisibleSpan : function(usingId, value) {
        console.warn("-> Mock call to FolioPageFragment.storeFirstVisibleSpan(" + usingId + ", " + value + ")");
    },

    getDirection : function() {
        //var direction = Direction.VERTICAL;
        var direction = Direction.HORIZONTAL;
        console.warn("-> Mock call to FolioPageFragment.getDirection(), return " + direction);
        return direction;
    },

    getTopDistraction : function() {
        console.warn("-> Mock call to FolioPageFragment.getTopDistraction(), return " + 0);
        return 0;
    },

    getBottomDistraction : function() {
        console.warn("-> Mock call to FolioPageFragment.getBottomDistraction(), return " + 0);
        return 0;
    }
};

var FolioWebView = {

    setCompatMode : function(compatMode) {
        console.warn("-> Mock call to FolioWebView.setCompatMode(" + compatMode + ")");
    }
};

var WebViewPager = {

    setCurrentPage : function(pageIndex) {
        console.warn("-> Mock call to WebViewPager.setCurrentPage(" + pageIndex + ")");
    },

    setPageToLast : function() {
        console.warn("-> Mock call to WebViewPager.setPageToLast()");
    },

    setPageToFirst : function() {
        console.warn("-> Mock call to WebViewPager.setPageToFirst()");
    }
};

var LoadingView = {

    show : function() {
        console.warn("-> Mock call to LoadingView.show()");
    },

    hide : function() {
        console.warn("-> Mock call to LoadingView.hide()");
    },

    visible : function() {
        console.warn("-> Mock call to LoadingView.visible()");
    },

    invisible : function() {
        console.warn("-> Mock call to LoadingView.invisible()");
    }
};*/

function isElementVisible(element, isHorizontal) {

    var rect = element.getBoundingClientRect();

    if (isHorizontal)
        return rect.left > 0;
    else
        return rect.top > 0;
}

/**
 Gets the first visible span from the displayed chapter and if it has id then usingId is true with
 value as span id else usingId is false with value as span index. usingId and value is forwarded to
 FolioPageFragment#storeFirstVisibleSpan(boolean, String) JavascriptInterface.

 @param {boolean} isHorizontal - scrolling type of DirectionalViewpager#mDirection
 */
function getFirstVisibleSpan(isHorizontal) {

    var spanCollection = document.querySelectorAll("span.sentence");

    if (spanCollection.length == 0) {
        FolioPageFragment.storeFirstVisibleSpan(false, 0);
        return;
    }

    var spanIndex = 0;
    var spanElement;

    for (var i = 0; i < spanCollection.length; i++) {
        if (isElementVisible(spanCollection[i], isHorizontal)) {
            spanIndex = i;
            spanElement = spanCollection[i];
            break;
        }
    }

    var usingId = spanElement.id ? true : false;
    var value = usingId ? spanElement.id : spanIndex;
    FolioPageFragment.storeFirstVisibleSpan(usingId, value);
}

/**
 Scrolls the web page to particular span using id or index

 @param {boolean} usingId - if span tag has id then true or else false
 @param {number} value - if usingId true then span id else span index
 */
function scrollToSpan(usingId, value) {

    if (usingId) {
        var spanElement = document.getElementById(value);
        if (spanElement)
            scrollToElement(spanElement);
    } else {
        var spanCollection = document.querySelectorAll("span.sentence");
        if (spanCollection.length == 0 || value < 0 || value >= spanCollection.length
            || value == null) {
            LoadingView.hide();
            return;
        }
        scrollToElement(spanCollection[value]);
    }

    LoadingView.hide();
}

function goToHighlight(highlightId) {
    var element = document.getElementById(highlightId.toString());
    if (element)
        scrollToElement(element);

    LoadingView.hide();
}

function goToAnchor(anchorId) {
    var element = document.getElementById(anchorId);
    if (element)
        scrollToElement(element);

    LoadingView.hide();
}

function scrollToLast() {
    console.log("-> scrollToLast");

    var direction = FolioWebView.getDirection();
    var scrollingElement = bodyOrHtml();

    switch (direction) {
        case Direction.VERTICAL:
            scrollingElement.scrollTop =
                scrollingElement.scrollHeight - document.documentElement.clientHeight;
            break;
        case Direction.HORIZONTAL:
            scrollingElement.scrollLeft =
                scrollingElement.scrollWidth - document.documentElement.clientWidth;
            WebViewPager.setPageToLast();
            break;
    }

    LoadingView.hide();
}

function scrollToFirst() {
    console.log("-> scrollToFirst");

    var direction = FolioWebView.getDirection();
    var scrollingElement = bodyOrHtml();

    switch (direction) {
        case Direction.VERTICAL:
            scrollingElement.scrollTop = 0;
            break;
        case Direction.HORIZONTAL:
            scrollingElement.scrollLeft = 0;
            WebViewPager.setPageToFirst();
            break;
    }

    LoadingView.hide();
}

function getCompatMode() {
    FolioWebView.setCompatMode(document.compatMode);
}

function horizontalRecheck() {

    horizontalIntervalCounter += horizontalIntervalPeriod;

    if (window.scrollWidth != document.documentElement.scrollWidth) {
        // Rare condition
        // This might happen when document.documentElement.scrollWidth gives incorrect value
        // when the webview is busy re-drawing contents.
        //console.log("-> horizontalIntervalCounter = " + horizontalIntervalCounter);
        console.warn("-> scrollWidth changed from " + window.scrollWidth + " to " +
            document.documentElement.scrollWidth);
        postInitHorizontalDirection();
    }

    if (horizontalIntervalCounter >= horizontalIntervalLimit)
        clearInterval(horizontalInterval);
}

function initHorizontalDirection() {

    preInitHorizontalDirection();
    postInitHorizontalDirection();

    horizontalInterval = setInterval(horizontalRecheck, horizontalIntervalPeriod);
}

function preInitHorizontalDirection() {

    //console.log(window);
    //console.log("-> " + document.getElementsByTagName('title')[0].innerText);
    var htmlElement = document.getElementsByTagName('html')[0];
    var bodyElement = document.getElementsByTagName('body')[0];

    // Required when initHorizontalDirection() is called multiple times.
    // Currently it is called only once per page.
    htmlElement.style.width = null;
    bodyElement.style.width = null;
    htmlElement.style.height = null;
    bodyElement.style.height = null;

    var bodyStyle = bodyElement.currentStyle || window.getComputedStyle(bodyElement);
    var paddingTop = parseInt(bodyStyle.paddingTop, 10);
    var paddingRight = parseInt(bodyStyle.paddingRight, 10);
    var paddingBottom = parseInt(bodyStyle.paddingBottom, 10);
    var paddingLeft = parseInt(bodyStyle.paddingLeft, 10);
    //console.log("-> padding = " + paddingTop + ", " + paddingRight + ", " + paddingBottom + ", " + paddingLeft);

    //document.documentElement.clientWidth is window.innerWidth excluding x scrollbar width
    var pageWidth = document.documentElement.clientWidth - (paddingLeft + paddingRight);
    //document.documentElement.clientHeight is window.innerHeight excluding y scrollbar height
    var pageHeight = document.documentElement.clientHeight - (paddingTop + paddingBottom);

    bodyElement.style.webkitColumnGap = (paddingLeft + paddingRight) + 'px';
    bodyElement.style.webkitColumnWidth = pageWidth + 'px';

    //console.log("-> window.innerWidth = " + window.innerWidth);
    //console.log("-> window.innerHeight = " + window.innerHeight);
    //console.log("-> clientWidth = " + document.documentElement.clientWidth);
    //console.log("-> clientHeight = " + document.documentElement.clientHeight);
    //console.log("-> bodyElement.offsetWidth = " + bodyElement.offsetWidth);
    //console.log("-> bodyElement.offsetHeight = " + bodyElement.offsetHeight);
    //console.log("-> pageWidth = " + pageWidth);
    //console.log("-> pageHeight = " + pageHeight);

    htmlElement.style.height = (pageHeight + (paddingTop + paddingBottom)) + 'px';
    bodyElement.style.height = pageHeight + 'px';
}

function postInitHorizontalDirection() {

    var htmlElement = document.getElementsByTagName('html')[0];
    var bodyElement = document.getElementsByTagName('body')[0];
    var bodyStyle = bodyElement.currentStyle || window.getComputedStyle(bodyElement);
    var paddingTop = parseInt(bodyStyle.paddingTop, 10);
    var paddingRight = parseInt(bodyStyle.paddingRight, 10);
    var paddingBottom = parseInt(bodyStyle.paddingBottom, 10);
    var paddingLeft = parseInt(bodyStyle.paddingLeft, 10);
    var clientWidth = document.documentElement.clientWidth;

    var scrollWidth = document.documentElement.scrollWidth;
    //console.log("-> document.documentElement.offsetWidth = " + document.documentElement.offsetWidth);
    if (scrollWidth > clientWidth
        && scrollWidth > document.documentElement.offsetWidth) {
        scrollWidth += paddingRight;
    }
    var newBodyWidth = scrollWidth - (paddingLeft + paddingRight);
    window.scrollWidth = scrollWidth;

    htmlElement.style.width = scrollWidth + 'px';
    bodyElement.style.width = newBodyWidth + 'px';

    // pageCount deliberately rounded instead of ceiling to avoid any unexpected error
    var pageCount = Math.round(scrollWidth / clientWidth);
    var pageCountFloat = scrollWidth / clientWidth;

    if (pageCount != pageCountFloat) {
        console.warn("-> pageCount = " + pageCount + ", pageCountFloat = " + pageCountFloat
            + ", Something wrong in pageCount calculation");
    }

    //console.log("-> scrollWidth = " + scrollWidth);
    //console.log("-> newBodyWidth = " + newBodyWidth);
    //console.log("-> pageCount = " + pageCount);

    FolioPageFragment.setHorizontalPageCount(pageCount);
}

function bodyOrHtml() {
    if ('scrollingElement' in document) {
        return document.scrollingElement;
    }
    // Fallback for legacy browsers
    if (navigator.userAgent.indexOf('WebKit') != -1) {
        return document.body;
    }
    return document.documentElement;
}

function scrollToElement(element) {

    var scrollingElement = bodyOrHtml();
    var direction = FolioWebView.getDirection();

    switch (direction) {

        case Direction.VERTICAL:
            var topDistraction = FolioWebView.getTopDistraction(DisplayUnit.DP);
            var pageTop = scrollingElement.scrollTop + topDistraction;
            var pageBottom = scrollingElement.scrollTop + document.documentElement.clientHeight
                - FolioWebView.getBottomDistraction(DisplayUnit.DP);

            var elementTop = element.offsetTop - 20;
            elementTop = elementTop < 0 ? 0 : elementTop;
            var elementBottom = element.offsetTop + element.offsetHeight + 20;
            var needToScroll = (elementTop < pageTop || elementBottom > pageBottom);

            //console.log("-> topDistraction = " + topDistraction);
            //console.log("-> pageTop = " + pageTop);
            //console.log("-> elementTop = " + elementTop);
            //console.log("-> pageBottom = " + pageBottom);
            //console.log("-> elementBottom = " + elementBottom);

            if (needToScroll) {
                var newScrollTop = elementTop - topDistraction;
                newScrollTop = newScrollTop < 0 ? 0 : newScrollTop;
                //console.log("-> Scrolled to = " + newScrollTop);
                scrollingElement.scrollTop = newScrollTop;
            }
            break;

        case Direction.HORIZONTAL:
            var clientWidth = document.documentElement.clientWidth;
            var pageIndex = Math.floor(element.offsetLeft / clientWidth);
            var newScrollLeft = clientWidth * pageIndex;
            //console.log("-> newScrollLeft = " + newScrollLeft);
            scrollingElement.scrollLeft = newScrollLeft;
            WebViewPager.setCurrentPage(pageIndex);
            break;
    }

    return element;
}

// Testing purpose calls
function test() {

    ++testCounter;
    console.log("-> testCounter = " + testCounter);

    var searchQuery = "look";

    if (testCounter == 1) {

        getCompatMode();

        if (FolioWebView.getDirection() == Direction.HORIZONTAL)
            initHorizontalDirection();

        highlightSearchResult(searchQuery, 1);

    } else if (testCounter == 2) {

        makeSearchResultsInvisible();

    } else if (testCounter == 3) {

        highlightSearchResult(searchQuery, 2);

    } else if (testCounter == 4) {

    }
}

function highlightSearchResult(searchQuery, occurrenceInChapter) {

    if (searchQuery == lastSearchQuery) {
        makeSearchResultsInvisible();
    } else {
        resetSearchResults();
        searchResults = applySearchResultClass(searchQuery);
        console.debug("-> Search Query Found = " + searchResults.length);
    }

    applySearchResultVisibleClass(occurrenceInChapter);
    LoadingView.hide();
}

function applySearchResultClass(searchQuery) {

    var searchQueryRegExp = new RegExp(escapeRegExp(searchQuery), "i");

    var searchResults = [];
    var searchChildNodesArray = [];
    var elementArray = [];
    var textNodeArray = [];

    var bodyElement = document.getElementsByTagName('body')[0];
    var elementsInBody = bodyElement.getElementsByTagName('*');

    for (var i = 0; i < elementsInBody.length; i++) {

        var childNodes = elementsInBody[i].childNodes;

        for (var j = 0; j < childNodes.length; j++) {

            if (childNodes[j].nodeType == Node.TEXT_NODE &&
                childNodes[j].nodeValue.trim().length) {
                //console.log("-> " + childNodes[j].nodeValue);

                if (childNodes[j].nodeValue.match(searchQueryRegExp)) {
                    //console.log("-> Found -> " + childNodes[j].nodeValue);

                    searchChildNodesArray.push(
                        getSearchChildNodes(childNodes[j].nodeValue, searchQuery));

                    elementArray.push(elementsInBody[i]);
                    textNodeArray.push(childNodes[j]);
                }
            }
        }
    }

    for (var i = 0; i < searchChildNodesArray.length; i++) {

        var searchChildNodes = searchChildNodesArray[i];

        for (var j = 0; j < searchChildNodes.length; j++) {

            if (searchChildNodes[j].className == "search-result")
                searchResults.push(searchChildNodes[j]);
            elementArray[i].insertBefore(searchChildNodes[j], textNodeArray[i]);
        }

        elementArray[i].removeChild(textNodeArray[i]);
    }

    lastSearchQuery = searchQuery;
    return searchResults;
}

function getSearchChildNodes(text, searchQuery) {

    var arrayIndex = [];
    var matchIndexStart = -1;
    var textChunk = "";
    var searchChildNodes = [];

    for (var i = 0, j = 0; i < text.length; i++) {

        textChunk += text[i];

        if (text[i].match(new RegExp(escapeRegExp(searchQuery[j]), "i"))) {

            if (matchIndexStart == -1)
                matchIndexStart = i;

            if (searchQuery.length == j + 1) {

                var textNode = document.createTextNode(
                    textChunk.substring(0, textChunk.length - searchQuery.length));

                var searchNode = document.createElement("span");
                searchNode.className = "search-result";
                var queryTextNode = document.createTextNode(
                    text.substring(matchIndexStart, matchIndexStart + searchQuery.length));
                searchNode.appendChild(queryTextNode);

                searchChildNodes.push(textNode);
                searchChildNodes.push(searchNode);

                arrayIndex.push(matchIndexStart);
                matchIndexStart = -1;
                j = 0;
                textChunk = "";

            } else {
                j++;
            }

        } else {
            matchIndexStart = -1;
            j = 0;
        }
    }

    if (textChunk !== "") {
        var textNode = document.createTextNode(textChunk);
        searchChildNodes.push(textNode);
    }

    return searchChildNodes;
}

function makeSearchResultsVisible() {

    for (var i = 0; i < searchResults.length; i++) {
        searchResults[i].className = "search-result-visible";
    }
    searchResultsInvisible = false;
}

function makeSearchResultsInvisible() {

    if (searchResultsInvisible)
        return;
    for (var i = 0; i < searchResults.length; i++) {
        if (searchResults[i].className == "search-result-visible")
            searchResults[i].className = "search-result-invisible";
    }
    searchResultsInvisible = true;
}

function applySearchResultVisibleClass(occurrenceInChapter) {

    var searchResult = searchResults[occurrenceInChapter - 1];
    if (searchResult === undefined)
        return;
    searchResult.className = "search-result-visible";
    searchResultsInvisible = false;

    scrollToElement(searchResult);
}

function resetSearchResults() {

    for (var i = 0; i < searchResults.length; i++) {
        searchResults[i].outerHTML = searchResults[i].innerHTML;
    }

    searchResults = [];
    lastSearchQuery = null;
    searchResultsInvisible = true;
}

function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
}

function getSelectionRect(element) {
    console.log("-> getSelectionRect");

    var range;
    if (element !== undefined) {
        range = document.createRange();
        range.selectNodeContents(element);
    } else {
        range = window.getSelection().getRangeAt(0);
    }

    //var rect = range.getBoundingClientRect();
    var rect = RangeFix.getBoundingClientRect(range);
    FolioWebView.setSelectionRect(rect.left, rect.top, rect.right, rect.bottom);
}

function clearSelection() {
    console.log("-> clearSelection");
    window.getSelection().removeAllRanges();
}

// onClick method set for highlights
function onClickHighlight(element) {
    console.log("-> onClickHighlight");
    event.stopPropagation();
    thisHighlight = element;
    getSelectionRect(element);
}

function deleteThisHighlight() {
    if (thisHighlight !== undefined)
        FolioWebView.deleteThisHighlight(thisHighlight.id);
}

function onTextSelectionItemClicked(id) {
    var selectionType = window.getSelection().type;
    var selectedText = "";
    if (selectionType == "Range") {
        selectedText = window.getSelection().toString();
    } else {
        selectedText = thisHighlight.textContent;
    }
    FolioWebView.onTextSelectionItemClicked(id, selectedText);
}

function onClickHtml() {
    console.debug("-> onClickHtml");
    if (FolioWebView.isPopupShowing()) {
        FolioWebView.dismissPopupWindow();
    } else {
        FolioWebView.toggleSystemUI();
    }

    setTimeout(getLastReadPosition, 1000);
}

function getLastReadPosition() {

    viewportRect = constructDOMRect(FolioWebView.getViewportRect(DisplayUnit.CSS_PX));
    var node = getFirstVisibleNode(document.body) || document.body;

    var cfi;
    if (node.nodeType === Node.TEXT_NODE) {
        cfi = EPUBcfi.Generator.generateCharacterOffsetCFIComponent(node, 0);
    } else {
        cfi = EPUBcfi.Generator.generateElementCFIComponent(node);
    }

    console.debug("-> " + cfi);
    viewportRect = null;
}

function constructDOMRect(rectJsonString) {
    var rectJson = JSON.parse(rectJsonString);
    return new DOMRect(rectJson.x, rectJson.y, rectJson.width, rectJson.height);
}

/**
 * Gets the first partially or completely visible node in viewportRect
 * @param node
 * @returns ELEMENT_NODE / TEXT_NODE / null
 */
function getFirstVisibleNode(node) {

    var range = document.createRange();
    range.selectNodeContents(node);
    var rect = RangeFix.getBoundingClientRect(range);
    if (rect == null)
        return null;

    var intersects = rectIntersects(viewportRect, rect);
    var contains = rectContains(viewportRect, rect);

    if (contains) {
        // node's rect is completely inside viewportRect.
        return node;

    } else if (intersects) {

        var childNodes = node.childNodes;
        for (var i = 0; i < childNodes.length; i++) {

            // EPUB CFI ignores nodes other than ELEMENT_NODE and TEXT_NODE
            // http://www.idpf.org/epub/linking/cfi/epub-cfi.html#sec-path-child-ref

            if (childNodes[i].nodeType === Node.ELEMENT_NODE || childNodes[i].nodeType === Node.TEXT_NODE) {
                var childNode = getFirstVisibleNode(childNodes[i]);
                if (childNode) {
                    return childNode;
                }
            }
        }

        // No children found or no child's rect completely inside viewportRect,
        // so returning this node as it's rect intersected with viewportRect.
        return node;
    }
    return null;
}

/**
 * Returns true iff the two specified rectangles intersect. In no event are
 * either of the rectangles modified.
 *
 * @param a The first rectangle being tested for intersection
 * @param b The second rectangle being tested for intersection
 * @return true iff the two specified rectangles intersect.
 */
function rectIntersects(a, b) {
    return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
}

/**
 * Returns true iff the specified rectangle b is inside or equal to
 * rectangle b. An empty rectangle never contains another rectangle.
 *
 * @param a The rectangle being tested whether rectangle b is inside this or not.
 * @param b The rectangle being tested for containment.
 * @return true iff the specified rectangle r is inside or equal to this
 *              rectangle
 */
function rectContains(a, b) {
    // check for empty first
    return a.left < a.right && a.top < a.bottom
        // now check for containment
        && a.left <= b.left && a.top <= b.top && a.right >= b.right && a.bottom >= b.bottom;
}
