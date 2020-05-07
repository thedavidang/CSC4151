package com.team4.walletwatch

import android.app.Activity
import android.content.Context
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathFactory

object DataManager {
    /* Purpose: Retrieve a value from an element in the local repo using an id.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    * id represents the numerical id of the target element.
    *
    * Returns: A string representing the value of the target element or
    * null if the target element id was not found. */
    fun getValueByID(doc: Document, id: String) : String? {
        val element = doc.getElementById(id)

        if (element != null) {
            return element.textContent
        }
        return null
    }

    /* Purpose: Retrieve a value from an element in the local repo using an XPath expression.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    * xpath represents the XPath expression query.
    *
    * Returns: A string representing the value of the target element or
    * null if the target element id was not found. */
    fun getValueByXPath(doc: Document, xpath: String) : String {
        return XPathFactory.newInstance().newXPath().evaluate(xpath, doc)
    }

    /* Purpose: Retrieves the labels of each of the three categories.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    *
    * Returns: categories represents a list of the category labels. */
    fun getCategories(doc: Document) : MutableList<String?> {
        val categories : MutableList<String?> = arrayListOf("All")
        categories.add(getValueByID(doc, "c-1-l"))
        categories.add(getValueByID(doc, "c-2-l"))
        categories.add(getValueByID(doc, "c-3-l"))

        return categories
    }

    /* Purpose: Converts a Date object into a LocalDate object.
    *
    * Parameters: dateToConvert represents the Date object.
    *
    * Returns: A LocalDate object. */
    private fun convertToLocalDate(dateToConvert: Date): LocalDate? {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /* Purpose: Retrieves the total amount of expenses from the last seven days.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    * category represents the specific category to filter for.
    *
    * Returns: total represents the total amount of expenses from the last seven days. */
    fun last7DaysTotal(doc: Document, category : String) : Double {
        val cal: Calendar = Calendar.getInstance()

        var amount : String?
        var total = 0.00

        /* Iterate through the last seven calendar days. */
        for (i in 1..7) {
            /* Retrieve the total amount of expenses for each day, if the Day element exists. */
            amount = getValueByID(doc, category + "-" +
                    convertToLocalDate(cal.time).toString() + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
            /* Determine the day previous to the current day. */
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return total
    }

    /* Purpose: Retrieves the total amount of expenses from the last twelve months.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    * category represents the specific category to filter for.
    *
    * Returns: total represents the total amount of expenses from the last twelve months. */
    fun last12MonthsTotal(doc: Document, category: String) : Double {
        val cal: Calendar = Calendar.getInstance()

        var amount : String?
        var total = 0.00

        /* Iterate through the last twelve calendar months. */
        for (i in 1..12) {
            /* Retrieve the total amount of expenses for each month, if the Month element exists. */
            amount = getValueByID(doc, category + "-" +
                    convertToLocalDate(cal.time).toString().substring(0, 7) + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
            /* Determine the month previous to the current month. */
            cal.add(Calendar.MONTH, -1)
        }

        return total
    }

    /* Purpose: Determine how much of the date XML tree hierarchy already exists.
    *
    * Parameters: doc represents the Document of the local repo XML file.
    * category represents the number of the target category.
    * year represents the numerical year of the target date.
    * month represents the numerical month of the target date.
    * day represents the numerical day of the target date.
    *
    * Returns: dateExists represents how much of the date XML tree hierarchy already exists.
    * A value of 0 means the Year, Month, Day, and Entry elements
    * need to be added as children to the existing Category element.
    * A value of 1 means the Month, Day, and Entry elements
    * * need to be added as children to the existing Year element.
    * A value of 2 means the Day and Entry elements
    * need to be added as children to the existing Month element.
    * A value of 3 or more means the Entry element
    * needs to be added as a child to the existing Day element.
    * The id of the new entry will be this value minus 1.
    * For example, 3 means an id of 2 and 4 means an id of 3. */
    private fun findExistingDateTags(doc : Document, category: String,
                                     year : String, month : String, day : String) : Int {
        var id = "c-$category-$year"
        var xpath = "string(/root/data/category[@id=\"c-$category\"]/year[@id=\"$id\"]/month[@id=\""
        var dateExists = 0

        if (getValueByID(doc, id) != null) {
            dateExists++
            id += "-$month"
            xpath += "$id\"]/day[@id=\""
            if (getValueByID(doc, id) != null) {
                dateExists++
                id += "-$day"
                xpath += "$id\"]/entry[last()]/@id)"
                if (getValueByID(doc, id) != null) {
                    /* XPath to retrieve the id of the last Entry element within the Day element.
                    * New entries are appended, so the most recent entry is last. */
                    dateExists += getValueByXPath(
                        doc, xpath).substringAfterLast('-').toInt()
                }
            }
        }

        return dateExists
    }

    /* Purpose: Increment the amount of a total element in the local repo.
    *
    * Parameters: element represents the total Element to increment.
    * amount represents the amount to increment element by.
    *
    * Returns: Nothing. */
    private fun incrementTotal(element: Element, amount: String) {
        element.textContent = (element.textContent.toDouble() + amount.toDouble()).toString()
    }

    /* Purpose: Increment the amount of a total element in the local repo.
    *
    * Parameters: element represents the total Element to increment.
    * amount represents the amount to increment element by.
    *
    * Returns: Nothing. */
    fun addEntry(doc : Document, amountRaw : String, description : String,
                 date: String, category: String) {
        /* Extract year, month, and day from date string. */
        val year = date.substring(0, 4)
        val month = date.substring(5, 7)
        val day = date.substring(8)
        /* Determine how much of the date XML tree hierarchy already exists. */
        val dateExists = findExistingDateTags(doc, category, year, month, day)
        /* Remove any thousand separator commas. */
        val amount = amountRaw.substring(2).replace(",", "")

        /* Retrieve each of the Total elements for the target date. */
        var id = "c-$category"
        val categoryTotal = doc.getElementById("$id-t")
        id += "-$year"
        var yearTotal = doc.getElementById("$id-t")
        id += "-$month"
        var monthTotal = doc.getElementById("$id-t")
        id += "-$day"
        var dayTotal = doc.getElementById("$id-t")

        /* Retrieve the Category element. */
        val categoryTag = doc.getElementById("c-$category")

        id = "c-$category-$year"
        /* Retrieve the Year element, if it already exists. */
        var yearTag = doc.getElementById(id)

        /* If no part of the date was already in existence,
        * then create the Year element as a child of the Category element. */
        if (dateExists == 0) {
            yearTag = doc.createElement("year")
            yearTag.setAttribute("id", id)

            /* Create the Total element as a child of the Year element. */
            yearTotal = doc.createElement("total")
            yearTotal.setAttribute("id","$id-t")
            yearTotal.textContent = "0"
            yearTag.appendChild(yearTotal)

            categoryTag.appendChild(yearTag)
        }

        id += "-$month"
        /* Retrieve the Month element, if it already exists. */
        var monthTag = doc.getElementById(id)

        /* If only the Year element was already in existence,
        * then create the Month element as a child of the Year element. */
        if (dateExists <= 1) {
            monthTag = doc.createElement("month")
            monthTag.setAttribute("id", id)

            /* Create the Total element as a child of the Month element. */
            monthTotal = doc.createElement("total")
            monthTotal.setAttribute("id","$id-t")
            monthTotal.textContent = "0"
            monthTag.appendChild(monthTotal)

            yearTag.appendChild(monthTag)
        }

        id += "-$day"
        /* Retrieve the Day element, if it already exists. */
        var dayTag = doc.getElementById(id)
        /* Initialize Entry element id to one. */
        var entry = 1

        /* If only the Month element was already in existence,
        * then create the Day element as a child of the Category element. */
        if (dateExists <= 2) {
            dayTag = doc.createElement("day")
            dayTag.setAttribute("id", id)

            /* Create the Total element as a child of the Day element. */
            dayTotal = doc.createElement("total")
            dayTotal.setAttribute("id","$id-t")
            dayTotal.textContent = "0"
            dayTag.appendChild(dayTotal)

            monthTag.appendChild(dayTag)
        }
        /* If the entire date was already in existence,
        * then set the Entry element id to the next sequential number. */
        else {
            entry = dateExists - 1
        }

        id += "-$entry"
        /* Create the Entry element. */
        val entryTag = doc.createElement("entry")
        entryTag.setAttribute("id", id)

        /* Create the Amount element as a child of the Entry element. */
        val amountTag = doc.createElement("amount")
        amountTag.setAttribute("id","$id-a")
        amountTag.textContent = amount
        entryTag.appendChild(amountTag)

        /* Increment all the associated Total elements. */
        incrementTotal(doc.getElementById("t"), amount)
        incrementTotal(categoryTotal, amount)
        incrementTotal(yearTotal, amount)
        incrementTotal(monthTotal, amount)
        incrementTotal(dayTotal, amount)

        /* Create the Description element. */
        val descriptionTag = doc.createElement("description")
        descriptionTag.setAttribute("id","$id-d")
        descriptionTag.textContent = description
        entryTag.appendChild(descriptionTag)

        /* Grab the current timestamp from the device clock in UTC,
        * but replace the date portion with the user's date input. */
        val timestampTag = doc.createElement("timestamp")
        timestampTag.setAttribute("id", "$id-s")
        timestampTag.textContent =
            Instant.now().toString().replaceRange(0, 10, date)
        entryTag.appendChild(timestampTag)

        /* Append the new Entry element right after the last Entry element of the Day element.
        * If there are no Entry elements in this Day yet, it will simply add it right after the
        * Total element for that Day. */
        dayTag.appendChild(entryTag)
    }

    /* Purpose: Converts the contents of the archive into a string.
    *
    * Parameters: archive represents the Document of the archive XML file.
    *
    * Returns: String of the contents of the archive. */
    private fun archiveString(archive : Document?) : String {
        val tf: TransformerFactory = TransformerFactory.newInstance()
        val trans: Transformer = tf.newTransformer()
        val sw = StringWriter()

        trans.transform(DOMSource(archive), StreamResult(sw))

        return sw.toString()
    }

    /* Purpose: Saves each changed category data to Archive.xml and then resets for each new label.
    * To reset, first subtracts category total from data total.
    * Second, sets category total to 0.00.
    * Third, changes category label to new label name.
    * Finally, deletes all children of category tag, except label tag and total tag.
    *
    * Parameters: activity represents the activity that called this function.
    * doc represents the Document of the local repo XML file.
    * labels represent an array of which categories to overwrite
    *
    * Returns: Nothing. */
    fun changeCategories(activity : Activity, doc : Document, labels : Array<String?>) {
        /* Locate archive XML file in Android Internal Storage */
        var archiveFile = activity.getFileStreamPath(activity.getString(R.string.archiveFilenameString))

        /* Check if archive XML file does not yet exist since
        * app has likely just now been installed on user's device. */
        if(!archiveFile.exists()) {
            try {
                /* Open skeleton archive XML file from assets. */
                val outputWriter = OutputStreamWriter(
                    activity.openFileOutput(activity.getString(R.string.archiveFilenameString), Context.MODE_PRIVATE))

                /* Write the contents of the skeleton archive XML into buffer. */
                outputWriter.write(activity.assets.open(
                    activity.getString(R.string.archiveFilenameString)).bufferedReader().use{it.readText()})

                /* Close the buffer. */
                outputWriter.close()

                /* Open the newly created archive XML file in Android Internal Storage. */
                archiveFile = activity.getFileStreamPath(activity.getString(R.string.archiveFilenameString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /* Load the contents of the archive XML file to the value of archive. */
        val archive = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(archiveFile)

        /* Iterate through each label in the array. */
        for ((index, label) in labels.withIndex()) {
            /* Check if this label was actually changed. */
            if (label != null) {
                /* Access the category element that will be changed. */
                val category = doc.getElementById("c-" + (index + 1).toString())
                /* Make a deep copy of the category element. */
                val copy = category.cloneNode(true)
                /* Access the root element of the Archive.xml. */
                val root = archive.getElementById("r")

                // /* Copy the category data over to the Archive.xml. */
                // val newDoc = XDocument(XDocument.Load("input.xml").Descendants("Body").First())

                // /* Access the total amount spent in the old category. */
                // val categoryTotal = doc.getElementById(
                //     "c-" + (index + 1).toString() + "-t")
                // /* Decrement the total of all data by the total amount spent in the old category. */
                // incrementTotal(
                //     doc.getElementById("t"), "-" + categoryTotal.textContent)

                // /* Reset the total amount of the category to zero. */
                // categoryTotal.textContent = "0.00"

                // /* Access the label of the category. */
                // val categoryLabel = doc.getElementById(
                //     "c-" + (index + 1).toString() + "-l")
                // /* Change the label of the old category to the new label. */
                // categoryLabel.textContent = label

                // /* Remove all children of the category element,
                // * except the label and total elements. */
                // while (category.lastChild != categoryTotal) {
                //     category.removeChild(category.lastChild)
                // }
            }
        }

        /* Open skeleton archive XML file from assets. */
        val outputWriter = OutputStreamWriter(
            activity.openFileOutput(activity.getString(R.string.archiveFilenameString), Context.MODE_PRIVATE))

        /* Convert the value of archive into a string and
        * write it to the now empty archive XML file */
        outputWriter.write(archiveString(archive))

        /* Close the buffer. */
        outputWriter.close()
    }
}