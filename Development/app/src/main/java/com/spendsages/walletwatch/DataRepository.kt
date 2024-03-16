package com.spendsages.walletwatch

import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.w3c.dom.Document
import java.io.OutputStreamWriter
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/* This class provides support for opening and saving the XML data file. */
class DataRepository(private val context: Context) {
    /* doc is a modifiable MutableLiveData object whose value is a Document object. */
    val doc: MutableLiveData<Document> by lazy {
        open()
    }

    /* Purpose: Opens the XML data file and pipes the contents into doc.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    private fun open() : MutableLiveData<Document> {
        /* Locate XML data file in Android Internal Storage. */
        var file = context.getFileStreamPath(context.getString(R.string.docFilenameString))

        /* Check if XML data file does not yet exist since
        * app has likely just now been installed on user's device. */
        if(!file.exists()) {
            try {
                /* Open skeleton XML from assets. */
                val outputWriter = OutputStreamWriter(
                    context.openFileOutput(
                        context.getString(R.string.docFilenameString), Context.MODE_PRIVATE))

                /* Write the contents of the skeleton XML into buffer. */
                outputWriter.write(context.assets.open(
                    context.getString(R.string.docFilenameString)
                ).bufferedReader().use{it.readText()})

                /* Close the buffer. */
                outputWriter.close()

                /* Open the newly created XML data file in Android Internal Storage. */
                file = context.getFileStreamPath(context.getString(R.string.docFilenameString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /* Load the contents of the XML data file and return. */
        val document = MutableLiveData<Document>()
        document.value = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

        return document
    }

    /* Purpose: Converts the contents of the value of doc into a string.
    *
    * Parameters: doc represents the Document of the XML data file.
    *
    * Returns: String of the contents of the value of doc. */
    private fun docString(doc : Document?) : String {
        val tf: TransformerFactory = TransformerFactory.newInstance()
        val trans: Transformer = tf.newTransformer()
        val sw = StringWriter()

        trans.transform(DOMSource(doc), StreamResult(sw))

        return sw.toString()
    }

    /* Purpose: Completely overwrites the contents of the XML data file
    * with the string contents of the value of doc.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    fun save() {
        try {
            /* Open the XML data file in MODE_PRIVATE (Forced Overwrite) mode. */
            val outputWriter = OutputStreamWriter(
                context.openFileOutput(
                    context.getString(R.string.docFilenameString), Context.MODE_PRIVATE))

            /* Convert the value of doc into a string and
            * write it to the now empty XML data file */
            outputWriter.write(docString(doc.value))

            /* Close the buffer. */
            outputWriter.close()

            /* Reload the contents of the XML data file as a signal to observers. */
            doc.value = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                context.getFileStreamPath(context.getString(R.string.docFilenameString)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}