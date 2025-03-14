package com.spendsages.walletwatch

import android.app.Activity
import android.content.Context
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathFactory

object DataManager {
    /* Purpose: Retrieve a value from an element in the XML data file using an id.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             id represents the numerical id of the target element.
    *
    * Returns: A string representing the value of the target element or
    *          null if the target element id was not found. */
    fun getValueByID(doc: Document, id: String) : String? {
        val element = doc.getElementById(id)

        if (element != null) {
            return element.textContent
        }
        return null
    }

    /* Purpose: Retrieve a value from an element in the XML data file using an XPath expression.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             xpath represents the XPath expression query.
    *
    * Returns: A string representing the value of the target element or
    *          null if the target element id was not found. */
    private fun getValueByXPath(doc: Document, xpath: String) : String {
        return XPathFactory.newInstance().newXPath().evaluate(xpath, doc)
    }

    /* Purpose: Converts a Date object into a LocalDate object.
    *
    * Parameters: dateToConvert represents the Date object.
    *
    * Returns: A LocalDate object. */
    private fun convertToLocalDate(dateToConvert: Date): LocalDate? {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /* Purpose: Retrieves the total amount of expenses from the last seven days
    *           for a given category.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             category represents the specific category to filter for.
    *
    * Returns: total represents the total amount of expenses of a given category
    *          from the last seven days. */
    fun last7DaysTotal(doc: Document, category : String) : Double {
        val cal: Calendar = Calendar.getInstance()

        var amount : String?
        var total = 0.00

        /* Iterate through the last seven calendar days. */
        repeat(7) {
            /* Retrieve the total amount of expenses for each day, if the Day element exists. */
            amount = getValueByID(doc, "$category-" + convertToLocalDate(
                cal.time).toString() + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
            /* Determine the previous day to retrieve next. */
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return total
    }

    /* Purpose: Grabs sub-totals of the last 7 days for one or all categories.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             categoryID represents the specific category to filter for.
    *
    * Returns: Array holding sub-totals for each of the past 7 days. */
    fun last7Days(doc: Document, categoryID : String) : DoubleArray {
        val cal: Calendar = Calendar.getInstance()
        val daysOfWeek = DoubleArray(7) { 0.0 }

        var amount : String?

        if (categoryID == "all") {
            var daySum: Double

            for (i in 0..6) {
                daySum = 0.0

                for (j in 1..3) {
                    /* Retrieve the total amount of expenses for each day,
                    * if the Day element exists. */
                    amount = getValueByID(doc, "c-$j-" + convertToLocalDate(
                        cal.time).toString() + "-t")
                    if (amount != null) {
                        daySum += amount.toDouble()
                    }
                }
                daysOfWeek[i] = daySum
                /* Determine the previous day to retrieve next. */
                cal.add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        else {
            /* Iterate through the last seven calendar days. */
            for (i in 0..6) {
                /* Retrieve the total amount of expenses for each day, if the Day element exists. */
                amount = getValueByID(doc, "$categoryID-" + convertToLocalDate(
                    cal.time).toString() + "-t")
                if (amount != null) {
                    daysOfWeek[i] = amount.toDouble()
                }
                /* Determine the previous day to retrieve next. */
                cal.add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        return daysOfWeek
    }

    /* Purpose: Retrieves the total amount of expenses of the current year to date
    *           for a given category.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             category represents the specific category to filter for.
    *
    * Returns: total represents the total amount of expenses of the year to date. */
    fun yearToDateTotal(doc: Document, category: String) : Double {
        val cal: Calendar = Calendar.getInstance()

        var amount : String?
        var total = 0.00

        /* Iterate through the calendar months that have occurred thus far
        * by starting with the current month (zero-based). */
        repeat(cal.get(Calendar.MONTH) + 1) {
            /* Retrieve the total amount of expenses for each month, if the Month element exists. */
            amount = getValueByID(doc, "$category-" + convertToLocalDate(
                cal.time).toString().substring(0, 7) + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
            /* Determine the previous month to retrieve next. */
            cal.add(Calendar.MONTH, -1)
        }

        return total
    }

    /* Purpose: Grabs sub-totals of the current year totals for one or all categories.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             categoryID represents the specific category to filter for.
    *
    * Returns: Array holding sub-totals for each month of the current year. */
    fun yearToDate(doc: Document, categoryID : String) : DoubleArray {
        val cal: Calendar = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val monthsOfYear = DoubleArray(currentMonth + 1) { 0.0 }

        var amount : String?

        if (categoryID == "all") {
            var monthSum : Double

            for (i in 0..currentMonth) {
                monthSum = 0.0

                for (j in 1..3) {
                    /* Retrieve the total amount of expenses for each Month,
                    * if the Month element exists. */
                    amount = getValueByID(doc, "c-$j-" + convertToLocalDate(
                        cal.time).toString().substringBeforeLast("-") + "-t")
                    if (amount != null) {
                        monthSum += amount.toDouble()
                    }
                }
                monthsOfYear[i] = monthSum
                /* Determine the previous month to retrieve next. */
                cal.add(Calendar.MONTH, -1)
            }
        }
        else {
            /* Iterate through the calendar months of the current year. */
            for (i in 0..currentMonth) {
                /* Retrieve the total amount of expenses for each month,
                * if the month element exists. */
                amount = getValueByID(doc, "$categoryID-" + convertToLocalDate(
                    cal.time).toString().substringBeforeLast("-") + "-t")
                if (amount != null) {
                    monthsOfYear[i] = amount.toDouble()
                }
                /* Determine the previous month to retrieve next. */
                cal.add(Calendar.MONTH, -1)
            }
        }
        return monthsOfYear
    }

    /* Purpose: Retrieves the total amount of expenses from the last twelve months
    *           for a given category.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             category represents the specific category to filter for.
    *
    * Returns: total represents the total amount of expenses of a given category
    *          from the last twelve months. */
    fun last12MonthsTotal(doc: Document, category: String) : Double {
        val cal: Calendar = Calendar.getInstance()

        var amount : String?
        var total = 0.00

        /* Iterate through the last twelve calendar months. */
        repeat(12) {
            /* Retrieve the total amount of expenses for each month, if the Month element exists. */
            amount = getValueByID(doc, "$category-" + convertToLocalDate(
                cal.time).toString().substring(0, 7) + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
            /* Determine the previous month to retrieve next. */
            cal.add(Calendar.MONTH, -1)
        }

        return total
    }

    /* Purpose: Grabs sub-totals of the last 12 months for one or all categories.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             categoryID represents the specific category to filter for.
    *
    * Returns: Array holding sub-totals for each of the past 12 months. */
    fun last12Months(doc: Document, categoryID : String) : DoubleArray {
        val cal: Calendar = Calendar.getInstance()
        val monthsOfYear = DoubleArray(12) { 0.0 }

        var amount : String?

        if (categoryID == "all") {
            var monthSum : Double

            for (i in 0..11) {
                monthSum = 0.0

                for (j in 1..3) {
                    /* Retrieve the total amount of expenses for each Month,
                    * if the Month element exists. */
                    amount = getValueByID(doc, "c-$j-" + convertToLocalDate(
                        cal.time).toString().substringBeforeLast("-") + "-t")
                    if (amount != null) {
                        monthSum += amount.toDouble()
                    }
                }
                monthsOfYear[i] = monthSum
                /* Determine the previous month to retrieve next. */
                cal.add(Calendar.MONTH, -1)
            }
        }
        else {
            /* Iterate through the last 12 calendar months. */
            for (i in 0..11) {
                /* Retrieve the total amount of expenses for each month,
                * if the month element exists. */
                amount = getValueByID(doc, "$categoryID-" + convertToLocalDate(
                    cal.time).toString().substringBeforeLast("-") + "-t")
                if (amount != null) {
                    monthsOfYear[i] = amount.toDouble()
                }
                /* Determine the previous month to retrieve next. */
                cal.add(Calendar.MONTH, -1)
            }
        }
        return monthsOfYear
    }

    /* Purpose: Retrieves the total amount of expenses from the prior year
    *           for a given category.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             category represents the specific category to filter for.
    *
    * Returns: total represents the total amount of expenses of the previous year. */
    fun lastYearTotal(doc: Document, category: String) : Double {
        val cal: Calendar = Calendar.getInstance()
        /* Move cal back to December of the prior year.
        * The day is set to 1 instead of 31, since not
        * all months have a 31, plus the day does not
        * matter in this function, just the month and year.*/
        cal.set(cal.get(Calendar.YEAR) - 1, Calendar.DECEMBER, 1)

        var amount : String?
        var total = 0.00

        /* Iterate through last year's twelve calendar months. */
        repeat(12) {
            /* Retrieve the total amount of expenses for each month, if the Month element exists. */
            amount = getValueByID(doc, "$category-" + convertToLocalDate(
                cal.time).toString().substring(0, 7) + "-t")
            if (amount != null) {
                total += amount.toDouble()
            }
            /* Determine the previous month to retrieve next. */
            cal.add(Calendar.MONTH, -1)
        }

        return total
    }

    /* Purpose: Grabs sub-totals of the the 12 calendar months from the
    *           prior year for one or all categories.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             categoryID represents the specific category to filter for.
    *
    * Returns: Array holding totals for each month of the previous year. */
    fun lastYear(doc: Document, categoryID : String) : DoubleArray {
        val cal: Calendar = Calendar.getInstance()
        /* Move cal back to December of the prior year.
        * The day is set to 1 instead of 31, since not
        * all months have a 31, plus the day does not
        * matter in this function, just the month and year.*/
        cal.set(cal.get(Calendar.YEAR) - 1, Calendar.DECEMBER, 1)

        val monthsOfYear = DoubleArray(12) { 0.0 }

        var amount : String?

        if (categoryID == "all") {
            var monthSum : Double

            for (i in 0..11) {
                monthSum = 0.0

                for (j in 1..3) {
                    /* Retrieve the total amount of expenses for each Month,
                    * if the Month element exists. */
                    amount = getValueByID(doc, "c-$j-" + convertToLocalDate(
                        cal.time).toString().substringBeforeLast("-") + "-t")
                    if (amount != null) {
                        monthSum += amount.toDouble()
                    }
                }
                monthsOfYear[i] = monthSum
                /* Determine the previous month to retrieve next. */
                cal.add(Calendar.MONTH, -1)
            }
        }
        else {
            /* Iterate through the last 12 calendar months. */
            for (i in 0..11) {
                /* Retrieve the total amount of expenses for each month,
                * if the month element exists. */
                amount = getValueByID(doc, "$categoryID-" + convertToLocalDate(
                    cal.time).toString().substringBeforeLast("-") + "-t")
                if (amount != null) {
                    monthsOfYear[i] = amount.toDouble()
                }
                /* Determine the previous month to retrieve next. */
                cal.add(Calendar.MONTH, -1)
            }
        }
        return monthsOfYear
    }

    /* Purpose: Grabs sub-totals of each year for one or all categories.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             categoryID represents the specific category to filter for.
    *
    * Returns: Array holding sub-totals for each recorded year. */
    fun allTime(doc: Document, categoryID : String) : DoubleArray {
        val cal: Calendar = Calendar.getInstance()

        var amount : String?

        /* Determine the first year that data was recorded by grabbing
        * the "year" element "id" attribute of each category. */
        val currentYear = cal.get(Calendar.YEAR)
        var yearOffset = 0
        for (category in 1..3) {
            /* XPath to retrieve the id of the first Year element of the category.
            * New year elements are appended, so the oldest year is first. */
            val xpath = "string(/root/category[@id=\"c-$category\"]/year[1]/@id)"
            val year = getValueByXPath(doc, xpath).substringAfterLast('-').toInt()
            /* Determine if this category's first year is the oldest one so far. */
            val offset = currentYear - year
            if (offset > yearOffset) {
                yearOffset = offset
            }
        }

        /* Initialize the array of year totals. */
        val years = DoubleArray(yearOffset + 1) { 0.0 }

        if (categoryID == "all") {
            var yearSum: Double
            /* Iterate through each calendar year. */
            for (i in 0..yearOffset) {
                yearSum = 0.0

                for (j in 1..3) {
                    /* Retrieve the total amount of expenses for each year,
                    * if the year element exists for the past decade. */
                    amount = getValueByID(doc, "c-$j-" + cal.get(Calendar.YEAR) + "-t")
                    if (amount != null) {
                        yearSum += amount.toDouble()
                    }
                }
                years[i] = yearSum
                /* Determine the previous year to retrieve next. */
                cal.add(Calendar.YEAR, -1)
            }
        }
        else {
            /* Iterate through each calendar year. */
            for (i in 0..yearOffset) {
                /* Retrieve the total amount of expenses for each year,
                * if the year element exists. */
                amount = getValueByID(doc, "$categoryID-" + cal.get(Calendar.YEAR) + "-t")
                if (amount != null) {
                    years[i] = amount.toDouble()
                }
                /* Determine the previous year to retrieve next. */
                cal.add(Calendar.YEAR, -1)
            }
        }
        return years
    }

    /* Purpose: Determine how much of the date XML tree hierarchy already exists.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             category represents the number of the target category.
    *             year represents the numerical year of the target date.
    *             month represents the numerical month of the target date.
    *             day represents the numerical day of the target date.
    *
    * Returns: dateExists represents how much of the date XML tree hierarchy already exists.
    *          A value of 0 means the Year, Month, Day, and Entry elements
    *          need to be added as children to the existing Category element.
    *          A value of 1 means the Month, Day, and Entry elements
    *          need to be added as children to the existing Year element.
    *          A value of 2 means the Day and Entry elements
    *          need to be added as children to the existing Month element.
    *          A value of 3 or more means the Entry element
    *          needs to be added as a child to the existing Day element.
    *          The id of the new entry will be this value minus 1.
    *          For example, 3 means an id of 2 and 4 means an id of 3. */
    private fun findExistingDateTags(doc : Document, category: String,
                                     year : String, month : String, day : String) : Int {
        var id = "c-$category-$year"
        var xpath = "string(/root/category[@id=\"c-$category\"]/year[@id=\"$id\"]/month[@id=\""
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

    /* Purpose: Increment the amount of a total element in the XML data file.
    *
    * Parameters: element represents the total Element to increment.
    *             amount represents the amount to increment element by.
    *
    * Returns: Nothing. */
    private fun incrementTotal(element: Element, amount: String) {
        element.textContent = (element.textContent.toDouble() + amount.toDouble()).toString()
    }

    /* Purpose: Add an entry to the XML data file.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             amountRaw represents the string of the raw dollar amount input.
    *             description represents the string of the optional expense description.
    *             date represents the string of the date of the expense.
    *             category represents the string of the selected category of the expense.
    *
    * Returns: A String representing the new Entry's ID. */
    fun addEntry(doc : Document, amountRaw : String, description : String,
                 date: String, category: String) : String {
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

        return id
    }

    /* Purpose: Check how much of a date tree hierarchy will be "empty" after removing a
    *           particular Entry tag. Any "empty" date tags will be deleted from the XML data file.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             entryID represents the id of the entry that will be deleted.
    *
    * Returns: Nothing. */
    private fun deleteEmptyTags(doc: Document, entryID: String) {
        val entryTag = doc.getElementById(entryID)
        val entryAmount = getValueByID(doc, "$entryID-a")!!

        /* Grab ancestor tags and their totals. */
        val dayTag = entryTag.parentNode
        val dayTotal = dayTag.firstChild as Element

        val monthTag = dayTag.parentNode
        val monthTotal = monthTag.firstChild as Element

        val yearTag = monthTag.parentNode
        val yearTotal = yearTag.firstChild as Element

        val categoryTag = yearTag.parentNode
        val categoryTotal = categoryTag.childNodes.item(1) as Element

        val total = doc.getElementById("t")

        /* Subtract entry amount from the totals of its ancestors. */
        incrementTotal(dayTotal, "-$entryAmount")
        incrementTotal(monthTotal, "-$entryAmount")
        incrementTotal(yearTotal, "-$entryAmount")
        incrementTotal(categoryTotal, "-$entryAmount")
        incrementTotal(total, "-$entryAmount")

        /* Remove the entry from the Day tag. */
        dayTag.removeChild(entryTag)

        /* Check if the Day tag now only has a Total child tag. */
        if (dayTag.childNodes.length == 1) {
            /* Then, remove the Day tag from the Month tag. */
            monthTag.removeChild(dayTag)

            /* Check if the Month tag now only has a Total child tag. */
            if (monthTag.childNodes.length == 1) {
                /* Then, remove the Month tag from the Year tag. */
                yearTag.removeChild(monthTag)

                /* Check if the Year tag now only has a Total child tag. */
                if (yearTag.childNodes.length == 1) {
                    /* Then, remove the Year tag from the Category tag. */
                    categoryTag.removeChild(yearTag)
                }
            }
        }
    }

    /* Purpose: Converts the contents of the archive into a string.
    *
    * Parameters: archive represents the Document of the Archive XML file.
    *
    * Returns: String of the contents of the archive. */
    private fun archiveString(archive : Document?) : String {
        val tf: TransformerFactory = TransformerFactory.newInstance()
        val trans: Transformer = tf.newTransformer()
        val sw = StringWriter()

        trans.transform(DOMSource(archive), StreamResult(sw))

        return sw.toString()
    }

    /* Purpose: Recursively goes through each child node that has an id attribute and updates
    *           the category number. For example, if a restored category was originally "c-1",
    *           but is now being set as "c-2", all the children of the restored category will
    *           need their id attributes to change from "c-1-..." to "c-2-...".
    *
    * Parameters: source represents the node that needs its children to have updated id attributes.
    *             newCategoryID is the new category id section to overwrite for each
    *             child id attribute (e.g. "c-3")
    *
    * Returns: Nothing. */
    private fun recursiveCategoryNumberUpdate(source: Node, newCategoryID: String) {
        /* Iterate through all child nodes of the source. */
        for (i in 0 until source.childNodes.length) {
            val child = source.childNodes.item(i)
            /* Make sure child node exists and has attributes,
            * which should mean it has an id attribute. */
            if (child != null && child.hasAttributes()) {
                /* Ensure the id attribute of the the node exists. */
                try {
                    val id = child.attributes.getNamedItem("id")
                    if (id != null) {
                        /* Replace the category section of the id attribute. */
                        id.textContent = newCategoryID + id.textContent.substring(3)
                        /* Check if the child has child nodes of its own.
                        * And also check to make sure the child is not simply a PCDATA node,
                        * which does not have any children with attributes (e.g. "total" node). */
                        if (child.hasChildNodes() &&
                            !(child.childNodes.length == 1 && !child.firstChild.hasAttributes())
                        ) {
                            /* Recursive call that will update the id attributes of
                            * the children nodes of the child node. */
                            recursiveCategoryNumberUpdate(child, newCategoryID)
                        }
                    }
                }
                /* Simply ignore child since it does not have an id attribute. */
                catch (_ : Exception) {
                    continue
                }
            }
        }
    }

    /* Purpose: Retrieve the Archive XML file.
    *
    * Parameters: activity represents the activity that called this function.
    *
    * Returns: Document of the Archive XML file. */
    fun getArchive(activity: Activity): Document {
        /* Locate Archive XML file in Android Internal Storage */
        var archiveFile = activity.getFileStreamPath(activity.getString(R.string.archiveFilenameString))

        /* Check if Archive XML file does not yet exist since
        * app has likely just now been installed on user's device. */
        if (!archiveFile.exists()) {
            try {
                /* Open skeleton Archive XML file from assets. */
                val outputWriter = OutputStreamWriter(
                    activity.openFileOutput(activity.getString(R.string.archiveFilenameString), Context.MODE_PRIVATE))

                /* Write the contents of the skeleton Archive XML into buffer. */
                outputWriter.write(activity.assets.open(
                    activity.getString(R.string.archiveFilenameString)).bufferedReader().use { it.readText() })

                /* Close the buffer. */
                outputWriter.close()

                /* Open the newly created Archive XML file in Android Internal Storage. */
                archiveFile = activity.getFileStreamPath(activity.getString(R.string.archiveFilenameString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /* Load the contents of the Archive XML file to the value of archive. */
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(archiveFile)
    }

    /* Purpose: Get list of category labels archived in the Archive XML file.
    *
    * Parameters: archive represents the Document of the Archive XML file.
    *
    * Returns: Mutable list of the category label strings. */
    fun getArchivedCategories(archive: Document): MutableList<String> {
        /* Retrieve list of all label nodes in the Archive.xml. */
        val archivedCategories = archive.getElementsByTagName("label")
        val archivedLabels = mutableListOf<String>()

        /* Iterate through nodes and extract labels. */
        var index = 0
        while (index < archivedCategories.length) {
            archivedLabels.add(archivedCategories.item(index).textContent)
            index++
        }

        return archivedLabels
    }

    /* Purpose: Saves each changed category data to Archive.xml and then resets for each new label.
    *           To save, first make a cloned copy of the category data and have the archive adopt
    *           the clone. Then, append that adopted clone to the root element of the archive.
    *
    *           To reset, first subtracts category total from data total.
    *           Second, sets category total to 0.00.
    *           Third, changes category label to new label name.
    *           Finally, deletes all children of category tag, except label tag and total tag.
    *
    * Parameters: activity represents the activity that called this function.
    *             doc represents the Document of the XML data file.
    *             archive represents the Document of the Archive XML file.
    *             labels represent an array of which categories to overwrite.
    *
    * Returns: Boolean of whether or not a category was restored. */
    fun changeCategories(activity : Activity, doc : Document,
                         archive : Document, labels : Array<String?>): Boolean {
        /* Initialize archived category node to restore and set to null. */
        var restoreCategory : Node?
        /* Initialize Boolean result to false. */
        var categoryRestored = false

        /* Iterate through each label in the array. */
        for ((index, label) in labels.withIndex()) {
            /* Check if this label was actually changed. */
            if (!label.isNullOrBlank()) {
                /* Retrieve list of all label nodes in the Archive.xml. */
                val archivedLabels = archive.getElementsByTagName("label")

                /* Iterate through archived label nodes until finding a match. */
                var labelIndex = 0
                /* Reset restored category node to null. */
                restoreCategory = null
                while (restoreCategory == null && labelIndex < archivedLabels.length) {
                    val archivedLabel = archivedLabels.item(labelIndex)
                    /* If the current archived label node matches the new label. */
                    if (archivedLabel.textContent == label) {
                        /* Set the parent category node of this archived label node. */
                        restoreCategory = archivedLabel.parentNode
                    }
                    labelIndex++
                }

                /* Initialize the category id for the id attributes. */
                val categoryID = "c-" + (index + 1).toString()

                /* Access the category element that will be changed. */
                val category = doc.getElementById(categoryID)

                /* Move a cloned copy of the category data over to the Archive.xml. */
                archive.getElementById("r").appendChild(
                    archive.adoptNode(category.cloneNode(true)))

                /* Access the total amount spent in the old category. */
                val categoryTotal = doc.getElementById("$categoryID-t")

                /* Grab the total of all the data in the WalletWatch.xml. */
                val total = doc.getElementById("t")

                /* Decrement the total of all data by the total spent in the old category.
                * Decrementing is done by concatenating a minus sign
                * in front of the amount string. */
                incrementTotal(total, "-" + categoryTotal.textContent)

                /* Access the label of the category. */
                val categoryLabel = doc.getElementById("$categoryID-l")

                /* Change the label of the old category to the new label. */
                categoryLabel.textContent = label

                /* Remove all children of the category element,
                * except the label and total elements. */
                while (category.lastChild != categoryTotal) {
                    category.removeChild(category.lastChild)
                }

                /* Check if a category is to be restored from the Archive.xml. */
                if (restoreCategory != null) {
                    /* Make a deep copy list of the archive category's children,
                    * to avoid IndexOutOfBounds exceptions caused by moving children
                    * to a new parent. */
                    val restoreCategoryList = mutableListOf<Node>()
                    for (i in 0 until restoreCategory.childNodes.length) {
                        val child = restoreCategory.childNodes.item(i)
                        val clone = child.cloneNode(true)
                        restoreCategoryList.add(clone)
                    }
                    /* Iterate through the cloned children inside the archived category list. */
                    for (clone in restoreCategoryList) {
                        /* If the clone node is the "total" node of the archived category list,
                        * grab the total and adjust the category total and data total
                        * in the WalletWatch.xml. */
                        if (clone.nodeName == "total") {
                            /* Restore the total amount of the category. */
                            categoryTotal.textContent = clone.textContent

                            /* Increment the total of all data by the
                            * total spent in the restored category. */
                            incrementTotal(total, clone.textContent)
                        }
                        /* Check if the clone node is one of the
                        * "year" nodes of the archived category list. */
                        else if (clone.nodeName == "year") {
                            /* Grab the id of the clone node, so that we can find
                            * the corresponding non-clone child node. */
                            val cloneYearID = clone.attributes.getNamedItem("id").textContent
                            /* Grab the non-clone child node that matches the year id. */
                            val child: Node? = run {
                                for (i in 0 until restoreCategory.childNodes.length) {
                                    val childNode = restoreCategory.childNodes.item(i)
                                    val childYearID = childNode.attributes.getNamedItem(
                                        "id").textContent
                                    if (childYearID == cloneYearID)
                                    {
                                        return@run childNode
                                    }
                                }
                                return@run null
                            }

                            /* Move the non-clone child category data out of the the archive and
                            * paste it into the category in the WalletWatch.xml. */
                            val year = doc.adoptNode(child)
                            category.appendChild(year)

                            /* Check if the restored category is now in a new position.
                            * If so, then all id attributes of the year node and all of
                            * its recursive children need to be updated.  */
                            val yearID = year.attributes.getNamedItem("id")
                            /* Check if the year id attribute category section does not match the
                            * new category id. */
                            if (yearID.textContent.substring(0, 3) != categoryID) {
                                /* Update the year id attribute. */
                                yearID.textContent =
                                    categoryID + yearID.textContent.substring(3)
                                /* Update the id attributes of all the recursive children
                                * of the year node */
                                recursiveCategoryNumberUpdate(year, categoryID)
                            }
                        }
                    }
                    /* Completely delete the restored category data that is left inside
                    * the Archive.xml, since it has all been restored into the WalletWatch.xml. */
                    archive.getElementById("r").removeChild(restoreCategory)
                    categoryRestored = true
                }
                /* If not restoring an archived category, simply reset the category total. */
                else {
                    /* Reset the total amount of the category to zero. */
                    categoryTotal.textContent = "0.00"
                }
            }
        }

        /* Open skeleton Archive XML file from assets. */
        val outputWriter = OutputStreamWriter(
            activity.openFileOutput(activity.getString(R.string.archiveFilenameString), Context.MODE_PRIVATE))

        /* Convert the value of archive into a string and
        * write it to the now empty Archive XML file */
        outputWriter.write(archiveString(archive))

        /* Close the buffer. */
        outputWriter.close()

        /* Return a Boolean representing if there was no category restored. */
        return !categoryRestored
    }

    /* Purpose: Delete entries from the XML data file.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             selectedEntries represents the list of strings of the ID of each selected entry.
    *
    * Returns: Nothing. */
    fun deleteEntries(doc: Document, selectedEntries: MutableList<String>) {
        /* Iterate through the list of each ID and delete the associated entry. */
        for (id in selectedEntries) {
            deleteEmptyTags(doc, id)
        }
    }

    /* Purpose: Edit an entry in the XML data file.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             amountRaw represents the string of the raw dollar amount input.
    *             description represents the string of the optional expense description.
    *             date represents the string of the date of the expense.
    *             category represents the string of the selected category of the expense.
    *
    * Returns: A String representing the Entry's new ID. */
    fun editEntry(doc: Document, entryID: String, amountRaw : String, description : String,
                  date: String, category: String) : String {
        /* Add a new entry with the updated attributes. */
        val id = addEntry(doc, amountRaw, description, date, category)
        /* Remove the old entry. */
        deleteEmptyTags(doc, entryID)

        return id
    }
}