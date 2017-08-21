timeout = null;

var userAgent = window.navigator.userAgent.toLowerCase(),
    iOS = /iphone|ipod|ipad/.test(userAgent);

$(function(){
  window.ssReader = Class({
    $singleton: true,

    init: function() {
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

    setFontAndada: function(){
      this.setFont("andada");
    },

    setFontLato: function(){
      this.setFont("lato");
    },

    setFontPtSerif: function(){
      this.setFont("pt-serif");
    },

    setFontPtSans: function(){
      this.setFont("pt-sans");
    },

    base64encode: function(str){
      return btoa(unescape(encodeURIComponent(str)));
    },

    base64decode: function(str){
      return decodeURIComponent(escape(atob(str)));
    },

    clearSelection: function(){
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

    setFont: function(fontName){
      $("#ss-wrapper-font").removeClass().addClass("ss-wrapper-"+fontName);
    },

    setSize: function(size){
      $("#ss-wrapper-size").removeClass().addClass("ss-wrapper-"+size);
    },

    setTheme: function(theme){
      $("body, #ss-wrapper-theme").removeClass().addClass("ss-wrapper-"+theme);
    },

    setComment: function(comment, inputId){
      $("#"+inputId).val(ssReader.base64decode(comment));
      $("#"+inputId).trigger("input", ["true"]);
    },

    highlightSelection: function(color){
      try {
        this.highlighter.highlightSelection("highlight_" + color);
        var range = window.getSelection().toString();
        var params = {content: range,rangy: this.getHighlights(),color: color};
        Highlight.onReceiveHighlights(JSON.stringify(params));
      } catch(err){
        console.log("highlightSelection : " + err);
      }
    },

    unHighlightSelection: function(){
      try {
        this.highlighter.unhighlightSelection();
        Highlight.onReceiveHighlights(this.getHighlights());
      } catch(err){}
    },

    getHighlights: function(){
      try {
        return this.highlighter.serialize();
      } catch(err){}
    },

    setHighlights: function(serializedHighlight){
      try {
        this.highlighter.removeAllHighlights();
        this.highlighter.deserialize(serializedHighlight);
      } catch(err){}
    },

    copy: function(){
      SSBridge.onCopy(window.getSelection().toString());
      this.clearSelection();
    },

    share: function(){
      SSBridge.onShare(window.getSelection().toString());
      this.clearSelection();
    },

    search: function(){
      SSBridge.onSearch(window.getSelection().toString());
      this.clearSelection();
    }
  });

  if (iOS){
    window.SSBridge = Class({
      $singleton: true,
      urlBase: "sabbath-school://ss",

      request: function(data){
        window.location = this.urlBase + data;
      },

      onReady: function(){
        this.request("?ready=true");
      },

      onReceiveHighlights: function(highlights){
        this.request("?highlights=" + highlights);
      },

      onVerseClick: function(verse){
        this.request("?verse=" + verse);
      },

      onCommentsClick: function(comments, elementId){
        this.request("?comment=" + comments + "&elementId=" + elementId);
      },

      onCopy: function(text){
        this.request("?copy=" + text);
      },

      onShare: function(text){
        this.request("?share=" + text);
      },

      onSearch: function(text){
        this.request("?search=" + text);
      }
    });
    SSBridge.onReady();
  }

  if(typeof ssReader !== "undefined"){
    ssReader.init();
  }

  $(".verse").click(function(){
    SSBridge.onVerseClick(ssReader.base64encode($(this).attr("verse")));
  });

  $("code").each(function(i){
    var textarea = $("<textarea class='textarea'/>").attr("id", "input-"+i).on("input propertychange", function(event, isInit) {
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
