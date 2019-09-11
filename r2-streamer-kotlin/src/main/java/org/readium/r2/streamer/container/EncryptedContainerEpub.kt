package org.readium.r2.streamer.container

import org.readium.r2.shared.Link
import org.readium.r2.shared.RootFile
import org.readium.r2.shared.drm.Drm
import org.readium.r2.shared.parser.xml.XmlParser
import org.readium.r2.streamer.parser.lcplFilePath
import org.readium.r2.streamer.parser.mimetype
import org.zeroturnaround.zip.ZipUtil
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import java.util.zip.ZipFile
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptedContainerEpub constructor(
    path: String,
    private val keyString: String
) : EpubContainer, ZipArchiveContainer {

    override var rootFile: RootFile = RootFile(path, mimetype)
    override var zipFile: ZipFile = getDecryptedZipFile(path)
    override var drm: Drm? = null
    override var successCreated: Boolean = File(path).exists()

    override fun xmlDocumentForFile(relativePath: String): XmlParser {
        val containerData = data(relativePath)
        val document = XmlParser()
        document.parseXml(containerData.inputStream())
        return document
    }

    override fun xmlAsByteArray(link: Link?): ByteArray {
        var pathFile = link?.href ?: throw Exception("Missing Link : ${link?.title}")
        if (pathFile.first() == '/')
            pathFile = pathFile.substring(1)

        return data(pathFile)
    }

    override fun xmlDocumentForResource(link: Link?): XmlParser {
        var pathFile = link?.href ?: throw Exception("Missing Link : ${link?.title}")
        if (pathFile.first() == '/')
            pathFile = pathFile.substring(1)
        return xmlDocumentForFile(pathFile)
    }


    override fun scanForDrm(): Drm? {

        if (ZipUtil.containsEntry(File(rootFile.rootPath), lcplFilePath)) {
            return Drm(Drm.Brand.Lcp)
        }
        return null
    }




























    private fun getDecryptedZipFile(relativePath: String): ZipFile {
        Timber.tag("TRAVIS").v("IT'S CALLING INTO THE CODE, IT'S HAPPENING!!!")
        val encrypted = FileInputStream(relativePath)

        val headerInfo = ByteArray(HEADER_INFO_DISPLACEMENT)

        encrypted.read(headerInfo, 0, HEADER_INFO_DISPLACEMENT)

        val key = AesHelper.getAesKey(keyString)
        val skeySpec = SecretKeySpec(key, AesHelper.ENCRYPTION_ALGORITHM)

        val ivSpec = IvParameterSpec(Arrays.copyOfRange(headerInfo, FILE_SIZE_BYTE_DISPLACEMENT, HEADER_INFO_DISPLACEMENT))

        val cipher = Cipher.getInstance(CIPHER_ENCODING)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec)

        val outputFile = File.createTempFile("abcd", ".epub").apply {
            deleteOnExit()
        }

        CipherInputStream(encrypted, cipher).use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
                output.flush()
                Timber.tag("TRAVIS").v("COPIED!!")
            }
        }

        Timber.tag("TRAVIS").v("zipfile built!!")
        return ZipFile(outputFile.path)
    }

    companion object {
        private const val FILE_SIZE_BYTE_DISPLACEMENT = 8
        private const val INITIAL_VALUE_BYTE_DISPLACEMENT = 16
        private const val HEADER_INFO_DISPLACEMENT = FILE_SIZE_BYTE_DISPLACEMENT + INITIAL_VALUE_BYTE_DISPLACEMENT

        private const val CIPHER_ENCODING = "AES/CBC/NoPadding"
    }
}
