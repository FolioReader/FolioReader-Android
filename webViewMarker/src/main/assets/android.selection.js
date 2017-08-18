// Namespace
var android = {};
android.selection = {};

android.selection.selectionStartRange = null;
android.selection.selectionEndRange = null;

/** The last point touched by the user. { 'x': xPoint, 'y': yPoint } */
android.selection.lastTouchPoint = null;


/**
 * Starts the touch and saves the given x and y coordinates as last touch point
 */
android.selection.startTouch = function(x, y) {
    android.selection.lastTouchPoint = {'x': x, 'y': y};
};

/**
 *  Checks to see if there is a selection.
 *
 *  @return boolean
 */
android.selection.hasSelection = function() {
    return window.getSelection().toString().length > 0;
};

/**
 *  Clears the current selection.
 */
android.selection.clearSelection = function() {
    try {
        // if current selection clear it.
        var sel = window.getSelection();
        sel.removeAllRanges();
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

/**
 *  Handles the long touch action by selecting the last touched element.
 */
android.selection.longTouch = function() {
    try {
        android.selection.clearSelection();
        var sel = window.getSelection();
        var range = document.caretRangeFromPoint(android.selection.lastTouchPoint.x, android.selection.lastTouchPoint.y);
        range.expand("word");
        var text = range.toString();
        if (text.length == 1) {
            var baseKind = jpntext.kind(text);
            if (baseKind != jpntext.KIND['ascii']) {
                try {
                    do {
                        range.setEnd(range.endContainer, range.endOffset + 1);
                        text = range.toString();
                        var kind = jpntext.kind(text);
                    } while (baseKind == kind);
                    range.setEnd(range.endContainer, range.endOffset - 1);
                }
                catch (e) {
                }
                try {
                    do {
                        range.setStart(range.startContainer, range.startOffset - 1);
                        text = range.toString();
                        var kind = jpntext.kind(text);
                    } while (baseKind == kind);
                    range.setStart(range.startContainer, range.startOffset + 1);
                }
                catch (e) {
                }
            }
        }
        if (text.length > 0) {
            sel.addRange(range);
            android.selection.saveSelectionStart();
            android.selection.saveSelectionEnd();
            android.selection.selectionChanged(true);
        }
     }
     catch (err) {
        window.TextSelection.jsError(err);
     }
};

/**
 * Tells the app to show the context menu.
 */
android.selection.selectionChanged = function(isReallyChanged) {
    try {
        var sel = window.getSelection();
        if (!sel) {
            return;
        }
        var range = sel.getRangeAt(0);

        // Add spans to the selection to get page offsets
        var selectionStart = $("<span id=\"selectionStart\">&#xfeff;</span>");
        var selectionEnd = $("<span id=\"selectionEnd\"></span>");

        var startRange = document.createRange();
        startRange.setStart(range.startContainer, range.startOffset);
        startRange.insertNode(selectionStart[0]);

        var endRange = document.createRange();
        endRange.setStart(range.endContainer, range.endOffset);
        endRange.insertNode(selectionEnd[0]);

        var handleBounds = "{'left': " + (selectionStart.offset().left) + ", ";
        handleBounds += "'top': " + (selectionStart.offset().top + selectionStart.height()) + ", ";
        handleBounds += "'right': " + (selectionEnd.offset().left) + ", ";
        handleBounds += "'bottom': " + (selectionEnd.offset().top + selectionEnd.height()) + "}";

        // Pull the spans
        selectionStart.remove();
        selectionEnd.remove();

        // Reset range
        sel.removeAllRanges();
        sel.addRange(range);

        // Rangy
        var rangyRange = android.selection.getRange();

        // Text to send to the selection
        var text = window.getSelection().toString();

        // Set the content width
        window.TextSelection.setContentWidth(document.body.clientWidth);

        // Tell the interface that the selection changed
        window.TextSelection.selectionChanged(rangyRange, text, handleBounds, isReallyChanged);
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

android.selection.getRange = function() {
    var serializedRangeSelected = rangy.serializeSelection();
    var serializerModule = rangy.modules.Serializer;
    if (serializedRangeSelected != '') {
        if (rangy.supported && serializerModule && serializerModule.supported) {
            var beginingCurly = serializedRangeSelected.indexOf("{");
            serializedRangeSelected = serializedRangeSelected.substring(0, beginingCurly);
            return serializedRangeSelected;
        }
    }
}

/**
 * Returns the last touch point as a readable string.
 */
android.selection.lastTouchPointString = function(){
    if (android.selection.lastTouchPoint == null)
        return "undefined";
    return "{" + android.selection.lastTouchPoint.x + "," + android.selection.lastTouchPoint.y + "}";
};

android.selection.saveSelectionStart = function(){
    try {
        // Save the starting point of the selection
        var sel = window.getSelection();
        var range = sel.getRangeAt(0);
        var saveRange = document.createRange();
        saveRange.setStart(range.startContainer, range.startOffset);
        android.selection.selectionStartRange = saveRange;
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

android.selection.saveSelectionEnd = function(){
    try {
        // Save the end point of the selection
        var sel = window.getSelection();
        var range = sel.getRangeAt(0);
        var saveRange = document.createRange();
        saveRange.setStart(range.endContainer, range.endOffset);
        android.selection.selectionEndRange = saveRange;
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

/**
 * Sets the last caret position for the start handle.
 */
android.selection.setStartPos = function(x, y){
    try {
        android.selection.selectBetweenHandles(document.caretRangeFromPoint(x, y), android.selection.selectionEndRange);
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

/**
 * Sets the last caret position for the end handle.
 */
android.selection.setEndPos = function(x, y){
    try {
        android.selection.selectBetweenHandles(android.selection.selectionStartRange, document.caretRangeFromPoint(x, y));
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

android.selection.restoreStartEndPos = function() {
    try {
        android.selection.selectBetweenHandles(android.selection.selectionEndRange, android.selection.selectionStartRange);
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};

/**
 *  Selects all content between the two handles
 */
android.selection.selectBetweenHandles = function(startCaret, endCaret) {
    try {
        if (startCaret && endCaret) {
            var rightOrder = startCaret.compareBoundaryPoints(Range.START_TO_END, endCaret) <= 0;
            if (rightOrder) {
                android.selection.selectionStartRange = startCaret;
                android.selection.selectionEndRange = endCaret;
            }
            else {
                startCaret = android.selection.selectionStartRange;
                endCaret = android.selection.selectionEndRange;
            }
            var range = document.createRange();
            range.setStart(startCaret.startContainer, startCaret.startOffset);
            range.setEnd(endCaret.startContainer, endCaret.startOffset);
            android.selection.clearSelection();
            var selection = window.getSelection();
            selection.addRange(range);
            android.selection.selectionChanged(rightOrder);
        }
        else {
            android.selection.selectionStartRange = startCaret;
            android.selection.selectionEndRange = endCaret;
        }
    }
    catch (e) {
        window.TextSelection.jsError(e);
    }
};
