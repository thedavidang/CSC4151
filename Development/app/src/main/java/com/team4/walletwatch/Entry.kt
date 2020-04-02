package com.team4.walletwatch

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.time.LocalDate

class Entry(var amount: Double, var description: String, var date: LocalDate, var category: String)

fun sortByDateDescending(entries : MutableList<Entry>?) : MutableList<Entry>? {
    val list = entries?.sortedWith(compareByDescending { it.date })

    if (!list.isNullOrEmpty()) {
        return list as MutableList<Entry>?
    }

    return null
}

fun sortByDateAscending(entries : MutableList<Entry>?) : MutableList<Entry>? {
    val list = entries?.sortedWith(compareBy { it.date })

    if (!list.isNullOrEmpty()) {
        return list as MutableList<Entry>?
    }

    return null
}

fun sortByPriceDescending(entries : MutableList<Entry>?) : MutableList<Entry>? {
    val list = entries?.sortedWith(compareByDescending { it.amount })

    if (!list.isNullOrEmpty()) {
        return list as MutableList<Entry>?
    }

    return null
}

fun sortByPriceAscending(entries : MutableList<Entry>?) : MutableList<Entry>? {
    val list = entries?.sortedWith(compareBy { it.amount })

    if (!list.isNullOrEmpty()) {
        return list as MutableList<Entry>?
    }

    return null
}

fun getEntries(doc : Document) : MutableList<Entry>? {
    val entries = mutableListOf<Entry>()

    var year: Int
    var month: Int
    var day: Int

    var amount = 0.00
    var description: String
    var date: LocalDate
    var category = ""

    val rootNodes = doc.documentElement.childNodes
    var dataNodes: NodeList?
    var categoryNodes: NodeList?
    var yearNodes: NodeList?
    var monthNodes: NodeList?
    var dayNodes: NodeList?
    var entryNodes: NodeList?

    var node: Node

    for (a in 0 until rootNodes.length) {
        node = rootNodes.item(a)
        if (node.nodeName == "data") {
            dataNodes = node.childNodes
            for (b in 0 until dataNodes.length) {
                node = dataNodes.item(b)
                if (node.nodeName == "category") {
                    categoryNodes = node.childNodes
                    for (c in 0 until categoryNodes.length) {
                        node = categoryNodes.item(c)
                        if (node.nodeName == "label") {
                            category = node.textContent
                        } else if (node.nodeName == "year") {
                            year = node.attributes.item(
                                0).textContent.substring(4).toInt()
                            yearNodes = node.childNodes
                            for (d in 0 until yearNodes.length) {
                                node = yearNodes.item(d)
                                if (node.nodeName == "month") {
                                    month = node.attributes.item(
                                        0).textContent.substring(9).toInt()
                                    monthNodes = node.childNodes
                                    for (e in 0 until monthNodes.length) {
                                        node = monthNodes.item(e)
                                        if (node.nodeName == "day") {
                                            day = node.attributes.item(
                                                0).textContent.substring(12).toInt()
                                            dayNodes = node.childNodes
                                            for (f in 0 until dayNodes.length) {
                                                node = dayNodes.item(f)
                                                if (node.nodeName == "entry") {
                                                    entryNodes = node.childNodes
                                                    for (g in 0 until entryNodes.length) {
                                                        node = entryNodes.item(g)
                                                        if (node.nodeName == "amount") {
                                                            amount = node.textContent.toDouble()
                                                        } else if (
                                                            node.nodeName == "description") {
                                                            date = LocalDate.of(year, month, day)
                                                            description = node.textContent
                                                            entries.add(
                                                                Entry(amount, description,
                                                                    date, category))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    return entries
}