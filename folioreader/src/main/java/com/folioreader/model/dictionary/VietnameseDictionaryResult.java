package com.folioreader.model.dictionary;

public class VietnameseDictionaryResult {
    private String word;
    private String define;
    private String example;


    public VietnameseDictionaryResult(String word, String define, String example) {
        this.word = word;
        this.define = define;
        this.example = example;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefine() {
        return define;
    }

    public void setDefine(String define) {
        this.define = define;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
