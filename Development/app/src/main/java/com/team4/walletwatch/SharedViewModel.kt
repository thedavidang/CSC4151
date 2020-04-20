package com.team4.walletwatch

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.w3c.dom.Document
import java.io.OutputStreamWriter
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/* This class provides support for sharing live data amongst different views. */
class SharedViewModel() : ViewModel() {
    private val doc = MutableLiveData<Document>()

    /* Purpose: Opens the local repo XML file and pipes the contents into doc.
    *
    * Parameters: activity represents the app Activity context that needs the shared view model.
    *
    * Returns: Nothing. */
    fun open(activity: Activity) {
        /* Locate XML file in Android Internal Storage */
        var file = activity.getFileStreamPath(activity.getString(R.string.fileNameString))
        /* Check if XML file does not yet exist since
        * app has likely just now been installed on user's device. */
        if(!file.exists()) {
            try {
                /* Open skeleton XML file from assets and write the contents into buffer. */
                val outputWriter = OutputStreamWriter(
                    activity.openFileOutput(activity.getString(R.string.fileNameString), Context.MODE_PRIVATE))
                outputWriter.write(activity.assets.open(
                    activity.getString(R.string.fileNameString)).bufferedReader().use{it.readText()})
                /* Close the buffer. */
                outputWriter.close()
                /* Open the newly created XML file in Android Internal Storage. */
                file = activity.getFileStreamPath(activity.getString(R.string.fileNameString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /* Load the contents of the XML file to the value of doc. */
        doc.value = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
    }

    /* Purpose: Getter/Accessor that retrieves the contents of the XML file.
    *
    * Parameters: None.
    *
    * Returns: Contents of the XML file as they exist in the value of doc. */
    fun get() : Document {
        return doc.value!!
    }

    /* Purpose: Converts the contents of the value of doc into a string.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    *
    * Returns: String of the contents of the value of doc. */
    fun docString(doc : Document?) : String {
        val tf: TransformerFactory = TransformerFactory.newInstance()
        val trans: Transformer = tf.newTransformer()
        val sw = StringWriter()
        trans.transform(DOMSource(doc), StreamResult(sw))
        return sw.toString()
    }

    /* Purpose: Completely overwrites the contents of the local repo XML file
    * with the string contents of the value of doc.
    *
    * Parameters: activity represents the app Activity context that needs the shared view model.
    *
    * Returns: Nothing. */
    fun save(activity: Activity) {
        try {
            /* Open the local repo XML file in MODE_PRIVATE (Forced Overwrite) mode. */
            val outputWriter = OutputStreamWriter(
                activity.openFileOutput(activity.getString(R.string.fileNameString), Context.MODE_PRIVATE))
            /* Convert the value of doc into a string and
            * write it to the now empty local repo XML file */
            outputWriter.write(docString(doc.value))
            /* Close the buffer. */
            outputWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}