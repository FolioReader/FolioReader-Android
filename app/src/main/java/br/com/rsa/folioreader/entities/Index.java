package br.com.rsa.folioreader.entities;

/**
 * Created by rodrigo.almeida on 30/04/15.
 */
public class Index {
    private int chapter;
    private String pathToLoad;

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public String getPathToLoad() {
        return pathToLoad;
    }

    public void setPathToLoad(String pathToLoad) {
        this.pathToLoad = pathToLoad;
    }
}
