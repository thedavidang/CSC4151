package com.spendsages.walletwatch

import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.w3c.dom.Document
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.OutputStreamWriter
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/* This class provides support for opening and saving the XML data file. */
class DataRepository(private val context: Context) {
    /* file is a handle to the XML data file. */
    private val file: File by lazy {
        create()
    }
    /* doc is a modifiable MutableLiveData object whose value is a Document object. */
    val doc: MutableLiveData<Document> by lazy {
        open()
    }

    /* Purpose: Grabs a handle to the XML data file. If the file does not exist yet,
    * it is created via the skeleton XML from assets and a handle to that new file is returned.
    *
    * Parameters: None.
    *
    * Returns: File that represents a handle to the XML data file. */
    private fun create() : File {
        /* Locate XML data file in Android Internal Storage. */
        var fileHandle = context.getFileStreamPath(context.getString(R.string.docFilenameString))
        /* Check if XML data file does not yet exist since
        * app has likely just now been installed on user's device. */
        if (!fileHandle.exists()) {
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
                fileHandle = context.getFileStreamPath(context.getString(R.string.docFilenameString))
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /* Return handle to the XML data file in Android Internal Storage. */
        return fileHandle
    }

    /* Purpose: A lean algorithm that rapidly opens the XML data file at app launch
    * and extracts just the category labels.
    *
    * Parameters: None.
    *
    * Returns: MutableList<String> that represents the category labels. */
    fun parseCategoryLabels() : Array<String> {
        /* Setup the Simple API for XML Parser object. */
        val parser = SAXParserFactory.newInstance().newSAXParser()
        /* Implement the handler for the SAX parser.
        * This handler implements a purpose-built algorithm
        * tailored and optimized for the retrieval of the
        * "label" XML tags' text content. */
        val handler = object : DefaultHandler() {
            /* Initialize the list of category labels with "All",
            * since that is required to occupy the first zero-based index. */
            val labels = arrayOf("All", "", "", "" )
            var labelIndex = 1
            /* Initialize boolean to false as to indicate that we are not
            * capturing data from the XML yet. */
            private var capturing = false

            override fun startElement(
                uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                /* Start consuming characters from the XML once we come across the
                * START_TAG of a "label" element. */
                if (qName == "label") {
                    capturing = true
                }
            }

            override fun characters(ch: CharArray?, start: Int, length: Int) {
                if (capturing) {
                    /* Add the text content of the "label" element to the category
                    * labels list and stop consuming characters. */
                    labels[labelIndex] = String(ch!!, start, length).trim()
                    labelIndex += 1
                    capturing = false

                    /* Forcibly halt the SAX Parser once all four category labels
                    * have been retrieved, including the pre-initialized "All" label. */
                    if (labelIndex >= labels.size) {
                        /* Throw a friendly SAX exception. */
                        throw SAXException("Parsing Complete")
                    }
                }
            }
        }

        /* Parse the XML for category labels until all are found. */
        try {
            parser.parse(file, handler)
        }
        catch (err: SAXException) {
            /* Log the friendly SAX exception. */
            println(err)
        }

        /* Return the list of category labels, including "All" at index zero */
        return handler.labels
    }

    /* Purpose: Opens the XML data file and pipes the contents into doc.
    *
    * Parameters: None.
    *
    * Returns: MutableLiveData<Document> that represents the data in the XML data file. */
    private fun open() : MutableLiveData<Document> {
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
    fun docString(doc : Document?) : String {
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
            val docValue = doc.value
            outputWriter.write(docString(docValue))

            /* Close the buffer. */
            outputWriter.close()

            /* Reload the contents of the XML data file as a signal to observers. */
            doc.value = docValue
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
}