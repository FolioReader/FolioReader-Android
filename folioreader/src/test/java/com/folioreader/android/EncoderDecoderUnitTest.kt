package com.folioreader.android

import org.junit.Test
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

class EncoderDecoderUnitTest {

    @Test
    fun test1() {

        // Case 1 -
        // Sample EPUB - https://drive.google.com/open?id=1fGO53nVsjOuSaZ-nOuzcLE3WhRSI9dkK
        // For URL asked to r2-streamer "http://127.0.0.1:8080/hindi1/OEBPS/Text/Chapter%201.html",
        // we get "/hindi1/OEBPS/Text/Chapter 1.html" in session.uri of nanohttpd, from which we extract
        // "OEBPS/Text/Chapter 1.html" as file path to search in parsed Publication model.
        // We have "/OEBPS/Text/Chapter%201.html" in spine link of Publication model.

        val case1FilePath = "OEBPS/Text/Chapter 1.html"
        val case1Encoding1 = URI(null, null, case1FilePath, null).toString()    // OEBPS/Text/Chapter%201.html
        val case1Encoding2 = URLEncoder.encode(case1FilePath, "UTF-8")   // OEBPS%2FText%2FChapter+1.html

        // So in Case 1, we need to pick case1Encoding1
        // This logic can is implemented in Publication.isLinkWithHrefURIDecoded()


        // Case 2 -
        // Sample EPUB - https://github.com/FolioReader/FolioReader-Android/files/2403715/sikan.zip
        // For URL asked to r2-streamer "http://127.0.0.1:8080/sikan/OEBPS/Text/%E4%BA%B2%E5%AD%902018%E5%B9%B44%E6%9C%88%E5%88%8A_0001.xhtml",
        // we get "/sikan/OEBPS/Text/亲子2018年4月刊_0001.xhtml" in session.uri of nanohttpd, from which we extract
        // "OEBPS/Text/亲子2018年4月刊_0001.xhtml" as file path to search in parsed Publication model.
        // We have "/OEBPS/Text/%E4%BA%B2%E5%AD%902018%E5%B9%B44%E6%9C%88%E5%88%8A_0001.xhtml" in spine link of Publication model.

        val case2FilePath = "OEBPS/Text/亲子2018年4月刊_0001.xhtml"
        val case2LinkHref = "/OEBPS/Text/%E4%BA%B2%E5%AD%902018%E5%B9%B44%E6%9C%88%E5%88%8A_0001.xhtml"
        val case2Decoding1 = URI(null, null, case2LinkHref, null).toString()    // /OEBPS/Text/%25E4%25BA%25B2%25E5%25AD%25902018%25E5%25B9%25B44%25E6%259C%2588%25E5%2588%258A_0001.xhtml
        val case2Decoding2 = URLDecoder.decode(case2LinkHref, "UTF-8")   // /OEBPS/Text/亲子2018年4月刊_0001.xhtml

        // So in Case 2, we need to pick case2Decoding2
        // This logic can is implemented in Publication.isLinkWithLinkHrefURLDecoded()


        // Case 3 -
        // Sample EPUB - https://github.com/FolioReader/FolioReader-Android/files/2482657/22.zip
        // For URL asked to r2-streamer "http://127.0.0.1:8080/22/OEBPS/Text/%D8%A7%D9%84%D9%85%D8%A7%D8%B6%D9%8A%20%D9%88%D8%A7%D9%84%D9%85%D8%B3%D8%AA%D9%82%D8%A8%D9%84.html",
        // we get "/22/OEBPS/Text/الماضي والمستقبل.html" in session.uri of nanohttpd, from which we extract
        // "OEBPS/Text/الماضي والمستقبل.html" as file path to search in parsed Publication model.
        // We have "/OEBPS/Text/%D8%A7%D9%84%D9%85%D8%A7%D8%B6%D9%8A%20%D9%88%D8%A7%D9%84%D9%85%D8%B3%D8%AA%D9%82%D8%A8%D9%84.html" in spine link of Publication model.

        val case3FilePath = "OEBPS/Text/الماضي والمستقبل.html"
        val case3LinkHref = "/OEBPS/Text/%D8%A7%D9%84%D9%85%D8%A7%D8%B6%D9%8A%20%D9%88%D8%A7%D9%84%D9%85%D8%B3%D8%AA%D9%82%D8%A8%D9%84.html"
        val case3Decoding1 = URI(null, null, case3LinkHref, null).toString()    // /OEBPS/Text/%25D8%25A7%25D9%2584%25D9%2585%25D8%25A7%25D8%25B6%25D9%258A%2520%25D9%2588%25D8%25A7%25D9%2584%25D9%2585%25D8%25B3%25D8%25AA%25D9%2582%25D8%25A8%25D9%2584.html
        val case3Decoding2 = URLDecoder.decode(case3LinkHref, "UTF-8")   // /OEBPS/Text/الماضي والمستقبل.html

        // So in Case 3, we need to pick case3Decoding2
        // This logic can is implemented in Publication.isLinkWithLinkHrefURLDecoded()
    }
}