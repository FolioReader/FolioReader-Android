package br.com.rsa.folioreader.customviews;

import nl.siegmann.epublib.domain.TOCReference;

/**
 * Created by rodrigo.almeida on 29/04/15.
 */
public class FolioReaderTOCReference extends TOCReference {
    private TOCReference parent;

    public TOCReference getParent() {
        return parent;
    }

    public void setParent(TOCReference parent) {
        this.parent = parent;
    }
}
