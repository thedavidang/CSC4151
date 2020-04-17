package com.team4.walletwatch

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.xml.xpath.XPathFactory
import kotlin.collections.ArrayList

object DataManager {
    fun getValueByID(doc: Document, id: String) : String? {
        val element = doc.getElementById(id)

        if (element != null) {
            return element.textContent
        }
        return null
    }

    // More flexible for complicated queries, but slower than getValueByID
    fun getValueByXPath(doc: Document, xpath: String) : String {
        return XPathFactory.newInstance().newXPath().evaluate(xpath, doc)
    }

    fun getCategories(doc: Document) : MutableList<String?> {
        val categories : MutableList<String?> = arrayListOf("All")
        categories.add(getValueByID(doc, "c-1-l"))
        categories.add(getValueByID(doc, "c-2-l"))
        categories.add(getValueByID(doc, "c-3-l"))

        return categories
    }

    private fun convertToLocalDate(dateToConvert: Date): LocalDate? {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun last7DaysTotal(doc: Document, category : String) : Double {
        var total = 0.00
        val cal: Calendar = Calendar.getInstance()
        val days = ArrayList<LocalDate?>()
        days.add(LocalDate.now())

        for (i in 1..6) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
            days.add(convertToLocalDate(cal.time))
        }

        var amount : String?
        for (day in days) {
            amount = getValueByID(doc, category + "-" + day.toString() + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
        }

        return total
    }

    fun last12MonthsTotal(doc: Document, category: String) : Double {
        var total = 0.00
        val cal: Calendar = Calendar.getInstance()
        val months = ArrayList<String?>(12)
        var date = LocalDate.now()
        months.add(date.toString().substring(0, 7))

        for (i in 1..11) {
            cal.add(Calendar.MONTH, -1)
            date = convertToLocalDate(cal.time)
            months.add(date.toString().substring(0, 7))
        }

        var amount : String?
        for (month in months) {
            amount = getValueByID(doc, "$category-$month-t")
            if (amount != null) {
                total += amount.toDouble()
            }
        }

        return total
    }

    private fun findExistingDateTags(doc : Document, category: String,
                                     year : String, month : String, day : String) : Int {
        var id = "c-$category-$year"
        var dateExists = 0

        if (getValueByID(doc, id) != null) {
            dateExists++
            id += "-month"
            if (getValueByID(doc, id) != null) {
                dateExists++
                id += "-day"
                if (getValueByID(doc, id) != null) {
                    val xpath = "number(/root/data/category[@id=\"" + category +
                                "\"]/year[@id=\"" + year +
                                "\"]/month[@id=\"" + month +
                                "\"]/day[@id=\"" + day +
                                "\"]/entry[last()]/@xml:id)"
                    dateExists += getValueByXPath(doc, xpath).toInt()
                }
            }
        }

        // 0 -> Create Year, Month, Day, and Entry tags
        // 1 -> Create Month, Day, and Entry tags within existing Year tag
        // 2 -> Create Day and Entry tags within existing Year and Month tags
        // 3+ -> Create Entry tag within existing Year, Month, and Day tags
        return dateExists
    }

    private fun incrementTotal(element: Element, amount: String) {
        element.textContent = (element.textContent.toDouble() + amount.toDouble()).toString()
    }

    fun addEntry(doc : Document, amountRaw : String, description : String,
                 date: String, category: String) {
        val year = date.substring(0, 4)
        val month = date.substring(5, 7)
        val day = date.substring(8)
        val dateExists = findExistingDateTags(doc, category, year, month, day)
        val amount = amountRaw.substring(2).replace(",", "")

        var entry = 1
        var id = "c-$category"
        val categoryTotal = doc.getElementById("$id-t")
        id += "-$year"
        var yearTotal = doc.getElementById("$id-t")
        id += "-$month"
        var monthTotal = doc.getElementById("$id-t")
        id += "-$day"
        var dayTotal = doc.getElementById("$id-t")
        var dayTag = doc.getElementById(id)

        val categoryTag = doc.getElementById("c-$category")
        id = "c-$category-$year"

        if (dateExists == 0) {
            val yearTag = doc.createElement("year")
            yearTag.setAttribute("id", id)

            yearTotal = doc.createElement("total")
            yearTotal.setAttribute("id","$id-t")
            yearTotal.textContent = "0"
            yearTag.appendChild(yearTotal)

            categoryTag.appendChild(yearTag)
        }

        val yearTag = doc.getElementById(id)
        id += "-$month"

        if (dateExists <= 1) {
            val monthTag = doc.createElement("month")
            monthTag.setAttribute("id", id)

            monthTotal = doc.createElement("total")
            monthTotal.setAttribute("id","$id-t")
            monthTotal.textContent = "0"
            monthTag.appendChild(monthTotal)

            yearTag.appendChild(monthTag)
        }

        val monthTag = doc.getElementById(id)
        id += "-$day"

        if (dateExists <= 2) {
            dayTag = doc.createElement("day")
            dayTag.setAttribute("id", id)

            dayTotal = doc.createElement("total")
            dayTotal.setAttribute("id","$id-t")
            dayTotal.textContent = "0"
            dayTag.appendChild(dayTotal)

            monthTag.appendChild(dayTag)
        }
        else {
            entry = dateExists - 1
        }

        id += "-$entry"

        val entryTag = doc.createElement("entry")
        entryTag.setAttribute("id", id)

        val amountTag = doc.createElement("amount")
        amountTag.setAttribute("id","$id-a")
        amountTag.textContent = amount
        entryTag.appendChild(amountTag)

        incrementTotal(doc.getElementById("t"), amount)
        incrementTotal(categoryTotal, amount)
        incrementTotal(yearTotal, amount)
        incrementTotal(monthTotal, amount)
        incrementTotal(dayTotal, amount)

        val descriptionTag = doc.createElement("description")
        descriptionTag.setAttribute("id","$id-d")
        descriptionTag.textContent = description
        entryTag.appendChild(descriptionTag)

        dayTag.appendChild(entryTag)
    }

    fun overwriteCategories(doc: Document, label : ArrayList<String?>) {

    }
}