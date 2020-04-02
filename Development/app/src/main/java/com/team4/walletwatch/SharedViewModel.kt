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

class SharedViewModel() : ViewModel() {
    private val doc = MutableLiveData<Document>()

    fun open(activity: Activity) {
        var file = activity.getFileStreamPath(activity.getString(R.string.fileNameString))
        if(!file.exists()) {
            try {
                val outputWriter = OutputStreamWriter(
                    activity.openFileOutput(activity.getString(R.string.fileNameString), Context.MODE_PRIVATE))
                outputWriter.write(activity.assets.open(
                    activity.getString(R.string.fileNameString)).bufferedReader().use{it.readText()})
                outputWriter.close()
                file = activity.getFileStreamPath(activity.getString(R.string.fileNameString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        doc.value = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
    }

    fun get() : Document {
        return doc.value!!
    }

    fun docString(doc : Document?) : String {
        val tf: TransformerFactory = TransformerFactory.newInstance()
        val trans: Transformer = tf.newTransformer()
        val sw = StringWriter()
        trans.transform(DOMSource(doc), StreamResult(sw))
        return sw.toString()
    }

    fun save(activity: Activity) {
        try {
            val outputWriter = OutputStreamWriter(
                activity.openFileOutput(activity.getString(R.string.fileNameString), Context.MODE_PRIVATE))
            outputWriter.write(docString(doc.value))
            outputWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}