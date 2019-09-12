package org.readium.r2.streamer.container

import android.util.Log
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
        return null
    }

    private fun getDecryptedZipFile(relativePath: String): ZipFile {
        val badEncrypted = FileInputStream(relativePath)
        val badHeaderInfo = ByteArray(HEADER_INFO_DISPLACEMENT)
        badEncrypted.read(badHeaderInfo, 0, HEADER_INFO_DISPLACEMENT)
        val badKey = AesHelper.getAesKey(keyString)
        val badSkeySpec = SecretKeySpec(badKey, AesHelper.ENCRYPTION_ALGORITHM)
        val badIvSpec = IvParameterSpec(Arrays.copyOfRange(badHeaderInfo, FILE_SIZE_BYTE_DISPLACEMENT, HEADER_INFO_DISPLACEMENT))
        val badCipher = Cipher.getInstance(CIPHER_ENCODING)
        badCipher.init(Cipher.DECRYPT_MODE, badSkeySpec, badIvSpec)

        val badOutputFile = File.createTempFile("badDecrypted", ".epub")
        CipherInputStream(badEncrypted, badCipher).use { input ->
            FileOutputStream(badOutputFile).use { output ->
                input.copyTo(output)
                output.flush()
                Log.v("TRAVIS", "BAD COPIED!!")
            }
        }


        // THIS will work, the file is the same one that was
        val encrypted = FileInputStream(File("/data/user/0/com.folioreader.android.sample/accel_encrypted.epub"))
        val headerInfo = ByteArray(HEADER_INFO_DISPLACEMENT)
        encrypted.read(headerInfo, 0, HEADER_INFO_DISPLACEMENT)

        val key = AesHelper.getAesKey("abcdefghijklmnop")
        val skeySpec = SecretKeySpec(key, AesHelper.ENCRYPTION_ALGORITHM)

        val ivSpec = IvParameterSpec(Arrays.copyOfRange(headerInfo, FILE_SIZE_BYTE_DISPLACEMENT, HEADER_INFO_DISPLACEMENT))

        val cipher = Cipher.getInstance(CIPHER_ENCODING)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec)

        val outputFile = File("/data/user/0/com.folioreader.android.sample/decrypted.epub")

        CipherInputStream(encrypted, cipher).use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
                output.flush()
                Log.v("TRAVIS", "COPIED!!")
            }
        }

        Log.v("TRAVIS", "${outputFile.path}")
        return ZipFile(outputFile.path)
    }

    companion object {
        private const val FILE_SIZE_BYTE_DISPLACEMENT = 8
        private const val INITIAL_VALUE_BYTE_DISPLACEMENT = 16
        private const val HEADER_INFO_DISPLACEMENT = FILE_SIZE_BYTE_DISPLACEMENT + INITIAL_VALUE_BYTE_DISPLACEMENT

        private const val CIPHER_ENCODING = "AES/CBC/NoPadding"
    }
}
