package com.spendsages.walletwatch

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.time.Instant

/* This class represents an Entry object,
* which is an expense that has a dollar amount, possibly a description of purchase,
* a timestamp of purchase, the category the expense entry falls under, and whether
* or not the expense has been selected for deletion. */
data class Entry(
    var id: String,
    var amount: Double,
    var description: String,
    var timestamp: Instant,
    var category: Int,
    var selected: Boolean)

/* Purpose: Static method that retrieves a list of all entries
* from the XML data file as Entry objects.
*
* Parameters: doc represents the Document of the XML data file.
*
* Returns: entries represent the list of Entry objects. */
fun getEntries(doc : Document) : MutableList<Entry> {
    val entryNodes = doc.getElementsByTagName("entry")
    val entries = mutableListOf<Entry>()

    var node: Node
    var id: String
    var amount: Double
    var description: String
    var timestamp : Instant
    var category: Int

    /* Iterate through the Entry elements in the XML data file  */
    for (index in 0 until entryNodes.length) {
        node = entryNodes.item(index)
        /* Grab the values from the children nodes of the current Entry element. */
        id = node.attributes.getNamedItem("id").textContent
        amount = node.firstChild.textContent.toDouble()
        description = node.childNodes.item(1).textContent
        /* Parse the timestamp string as a timestamp object known as Instant. */
        timestamp = Instant.parse(node.childNodes.item(2).textContent)
        /* Retrieve the category index by extracting the digit from the id "c-x-l". */
        category = id[2].digitToInt()

        /* Create an instance of the Entry class and add it to the list of entries.
        * By default, the entry will not be selected for deletion. */
        entries.add(Entry(id, amount, description, timestamp, category, false))
    }

    /* Return the list of entries, which can safely be empty. */
    return entries
}
