var jpntext = (function() {
    var global = {
        KIND: {
            'mix': 0,
            'ascii': 1,
            'hira': 2,
            'kata': 3,
            'cjk': 4
        },
        kind: function(text) {
            var result;
            if (global.isAscii(text)) {
                result = 'ascii';
            }
            else if (global.isHiragana(text)) {
                result = 'hira';
            }
            else if (global.isKatakana(text)) {
                result = 'kata';
            }
            else if (global.isKanji(text)) {
                result = 'cjk';
            }
            else {
                result = 'mix';
            }
            return global.KIND[result];
        },
        isAscii: function(text) {
            var re = /^[\u0000-\u00ff]+$/;
            return re.test(text);
        },
        isKanji: function(text) {
            var re = /^([\u4e00-\u9fcf]|[\u3400-\u4dbf]|[\u20000-\u2a6df]|[\uf900-\ufadf])+$/;
            return re.test(text);
        },
        isHiragana: function(text) {
            var re = /^[\u3040-\u309f]+$/;
            return re.test(text)
        },
        isKatakana: function(text) {
            var re = /^[\u30a0-\u30ff]+$/;
            return re.test(text);
        }
    };
    return global;
})();
