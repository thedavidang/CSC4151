package com.spendsages.walletwatch.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.spendsages.walletwatch.DataManager
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.SharedViewModel
import com.spendsages.walletwatch.databinding.FragmentTab2Binding
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.model.Viewport
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import org.w3c.dom.Document
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass.
 * Use the [Tab2Fragment] constructor method to
 * create an instance of this fragment.
 */
class Tab2Fragment: Fragment() {
    private var _binding: FragmentTab2Binding? = null
    private val binding get() = _binding!!

    private var allowRefresh: Boolean = true

    private lateinit var main: MainActivity
    private lateinit var model: SharedViewModel

    private lateinit var lineChart: LineChartView
    private lateinit var pieChart: PieChartView

    private lateinit var category1Label: String
    private lateinit var category1Text: TextView
    private lateinit var category1Total: TextView

    private lateinit var category2Label: String
    private lateinit var category2Text: TextView
    private lateinit var category2Total: TextView

    private lateinit var category3Label: String
    private lateinit var category3Text: TextView
    private lateinit var category3Total: TextView

    private lateinit var allText: TextView
    private lateinit var allTotal: TextView

    private lateinit var spinChartType: Spinner

    private lateinit var spinTimeInterval: Spinner

    private lateinit var spinChartCategory: Spinner

    private val monthAbbreviation = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    /* Purpose: Controller method that reveals the line chart and hides the pie chart.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    fun showLineChart() {
        lineChart.visibility = View.VISIBLE
        pieChart.visibility = View.INVISIBLE
    }

    /* Purpose: Controller method that reveals the pie chart and hides the line chart.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    fun showPieChart() {
        lineChart.visibility = View.INVISIBLE
        pieChart.visibility = View.VISIBLE
    }

    /* Purpose: Controller method that disables and greys-out category selector or
    * enables and reveals category selector.
    *
    * Parameters: isEnabled represents a Boolean of whether or not to enable the category selector.
    *
    * Returns: Nothing. */
    private fun toggleCategorySelector(isEnabled: Boolean) {
        if (isEnabled) {
            spinChartCategory.isEnabled = true
            spinChartCategory.isClickable = true
            /* Set opacity to 100 % */
            spinChartCategory.alpha = 1.0F
        }
        else {
            spinChartCategory.isEnabled = false
            spinChartCategory.isClickable = false
            /* Set opacity to 50 % */
            spinChartCategory.alpha = 0.5F
        }
    }

    /* Purpose: Controller method that populates the line chart with the selected data set.
    *
    * Parameters: total represents the array of doubles for each data point.
    * timeSpan represents whether the user selected 0) Last 7 Days, 1) Year To Date (YTD),
    * 2) Last 12 Months,  3) Last Year, or 4) All Time.
    * categoryPosition represent whether the user selected 0) All categories
    * or 1-3) a specific Category.
    *
    * Returns: Nothing. */
    fun updateLineChart(dataPoints: DoubleArray, timeSpan: Int, category: Int) {
        /* Initialize a Calender object. */
        val cal: Calendar = Calendar.getInstance()

        /* Initialize 0-based counter for the selected time interval. */
        var counter = dataPoints.size - 1

        /* Initialize minimum and maximum axis values. */
        val minX = 0.0f
        val maxX = counter.toFloat()

        var minY = -1.0f
        var maxY = -1.0f

        /* Initialize array of data points. */
        val values = ArrayList<PointValue>(counter)

        /* Populate the coordinates for each data point. */
        for (i in 0..counter) {
            val x = i.toFloat()
            val y = dataPoints[counter].toFloat()

            values.add(PointValue(x, y))

            /* Update Y-axis minimum and maximum values. */
            if (minY == -1.0f) {
                minY = y
                maxY = y
            }
            else if (y < minY) {
                minY = y
            }
            else if (y > maxY) {
                maxY = y
            }

            /* Decrement "counter". For loop uses "i" as its counter. */
            counter--
        }

        /* Create a line object and color it according to the selected category. */
        val line = Line(values)
        /* Enable datapoint labels. */
        line.setHasLabelsOnlyForSelected(true)
        /* Check which category the user selected. */
        when (category) {
            /* Set line to black for All Categories. */
            0 -> { line.color = resources.getColor(R.color.colorAll, context?.theme) }
            /* Set line to red for Category 1. */
            1 -> { line.color = resources.getColor(R.color.colorCategory1, context?.theme) }
            /* Set line to green for Category 2. */
            2 -> { line.color = resources.getColor(R.color.colorCategory2, context?.theme) }
            /* Set line to blue for Category 3. */
            3 -> { line.color = resources.getColor(R.color.colorCategory3, context?.theme) }
        }

        /* Initialize the lines array such that the line can be added to the LineChartData. */
        val lines = ArrayList<Line>(1)
        lines.add(line)
        /* Add the line to the LineChartData. */
        val data = LineChartData()
        data.lines = lines

        /* Populate the X-axis labels with an initial capacity of 12. */
        val xAxisValues = ArrayList<AxisValue>(12)
        /* Check which time interval the user selected. */
        when (timeSpan) {
            /* Last 7 Days. */
            0 -> {
                /* Determine the current day. */
                var currentDay = cal.get(Calendar.DAY_OF_WEEK)
                var currentDayString = ""
                /* Grab the abbreviation for the last 7 days. */
                var j = 7
                repeat(j) {
                    j -= 1
                    when (currentDay) {
                        1 -> { currentDayString = "Sun" }
                        2 -> { currentDayString = "Mon" }
                        3 -> { currentDayString = "Tue" }
                        4 -> { currentDayString = "Wed" }
                        5 -> { currentDayString = "Thu" }
                        6 -> { currentDayString = "Fri" }
                        7 -> { currentDayString = "Sat" }
                    }
                    /* Add the abbreviation to the X-axis labels array. */
                    val xAxisValue = AxisValue(j.toFloat())
                    xAxisValue.setLabel(currentDayString)
                    xAxisValues.add(xAxisValue)
                    currentDay -= 1
                    /* Account for rollover into Sunday of the previous week. */
                    if (currentDay == 0) {
                        currentDay = 7
                    }
                }
            }
            /* Year To Date (YTD). */
            1 -> {
                /* Determine the current month. */
                var currentMonth = cal.get(Calendar.MONTH)
                /* Grab the abbreviation for each month of the current year. */
                var j = currentMonth + 1
                repeat(j) { i ->
                    j -= 1
                    /* Add the abbreviation to the X-axis labels array. */
                    val xAxisValue = AxisValue(j.toFloat())
                    xAxisValue.setLabel(monthAbbreviation[currentMonth])
                    xAxisValues.add(xAxisValue)
                    currentMonth -= 1
                }
            }
            /* Last 12 Months. */
            2 -> {
                /* Determine the current month. */
                var currentMonth = cal.get(Calendar.MONTH)
                /* Grab the abbreviation for the last 12 months. */
                var j = 12
                repeat(j) {
                    j -= 1
                    /* Add the abbreviation to the X-axis labels array. */
                    val xAxisValue = AxisValue(j.toFloat())
                    xAxisValue.setLabel(monthAbbreviation[currentMonth])
                    xAxisValues.add(xAxisValue)
                    currentMonth -= 1
                    /* Account for rollover into December of the previous year. */
                    if (currentMonth == -1) {
                        currentMonth = 11
                    }
                }
            }
            /* Last Year. */
            3 -> {
                /* The X-axis label on the far right will be "Dec" for December,
                * which is the 12th month or zero-based index 11. */
                var currentMonth = 11
                /* Grab the abbreviation for each of the 12 months. */
                var j = 12
                repeat(j) {
                    j -= 1
                    /* Add the abbreviation to the X-axis labels array. */
                    val xAxisValue = AxisValue(j.toFloat())
                    xAxisValue.setLabel(monthAbbreviation[currentMonth])
                    xAxisValues.add(xAxisValue)
                    currentMonth -= 1
                }
            }
            /* All Time. */
            else -> {
                /* Determine the current year. */
                var currentYear = cal.get(Calendar.YEAR)
                var j = dataPoints.size
                /* Grab all recorded years. */
                repeat(j) {
                    j -= 1
                    /* Add the year to the X-axis labels array. */
                    val xAxisValue = AxisValue(j.toFloat())
                    xAxisValue.setLabel(currentYear.toString())
                    xAxisValues.add(xAxisValue)
                    currentYear -= 1
                }
            }
        }

        /* Populate the X-axis with labels. */
        val axisX = Axis(xAxisValues).setHasLines(true)
        axisX.maxLabelChars = 4
        axisX.textColor = Color.BLACK
        data.axisXBottom = axisX

        /* Adjust the dimensions of the line chart to fit the highest Y-value data point. */
        val view = Viewport(minX, maxY, maxX, minY)
        lineChart.currentViewport = view

        /* Populate the Y-axis with eleven values and labels. */
        val yAxisValues = ArrayList<AxisValue>(11)
        /* Setup step value such that there will be eleven evenly spaced Y-axis labels. */
        val step = (maxY - minY) / 9
        /* Initialize the bottom Y-axis value and label. */
        var yValue = minY
        var yLabel: String
        /* Iterate to create eleven evenly spaced Y-axis values with labels. */
        repeat(11) {
            /* Calculate the scientific notation exponent of the Y-value by
            * taking the floor of the base 10 logarithm of the Y-axis value. */
            val yValExponent = kotlin.math.floor(kotlin.math.log10(yValue.toDouble()))
            /* Check if the Y-axis value is less than 100. */
            if (yValExponent < 2.0) {
                /* Display cents with two decimal places. */
                yLabel = DecimalFormat("0.00").format(yValue)
            }
            /* Check if the Y-axis value is from 100 to 1,000. */
            else if (yValExponent < 3.0) {
                /* Display dollars without cents with no decimal places. */
                yLabel = yValue.toString().substringBefore(".")
            }
            /* Check if Y-axis value needs to have at least 2 significant digits (e.g. 9.0 G). */
            else if ((yValExponent % 3.0) == 0.0) {
                /* Determine the metric prefix by subtracting one from
                * the floor of the exponent divided by three. */
                val metricPrefix = main.metric[((yValExponent / 3) - 1).toInt()]
                val yString = yValue.toDouble().toLong().toString()
                /* Add one decimal as to have 2 significant digits. */
                yLabel = yString.substring(0, 1) + "." + yString.substring(1, 2) +
                        " " + metricPrefix
            }
            /* Otherwise, the Y-axis value is 1,000 or more. */
            else {
                /* Determine the metric prefix by subtracting one from
                * the floor of the exponent divided by three. */
                val metricPrefix = main.metric[((yValExponent / 3) - 1).toInt()]
                /* Set the Y-axis label by consolidating the value to the digits before the
                * thousand separator by getting the substring up to the index of
                * one plus the remainder of the exponent divided by three. */
                yLabel = yValue.toLong().toString().substring(0,
                    ((yValExponent % 3) + 1).toInt()) + " " + metricPrefix
            }

            val yAxisValue = AxisValue(yValue)
            yAxisValue.setLabel(yLabel)
            /* Add the Y-axis value and label to the Y-axis object. */
            yAxisValues.add(yAxisValue)

            /* Increment the yValue by the step. */
            yValue += step
         }

        /* Populate the Y-axis with values and labels. */
        val axisY = Axis(yAxisValues).setHasLines(true)
        /* Limit Y-axis labels to five characters. */
        axisY.maxLabelChars = 5
        axisY.textColor = Color.BLACK
        data.axisYLeft = axisY

        /* Populate the line with the data points. */
        lineChart.lineChartData = data
    }

    /* Purpose: Controller method that populates the pie chart with the selected data set.
    *
    * Parameters: timeSpan represents whether the user selected 0) Last 7 Days,
    * 1) Year To Date (YTD), 2) Last 12 Months,  3) Last Year, or 4) All Time.
    *
    * Returns: Nothing. */
    fun updatePieChart(doc: Document, timeSpan: Int) {
        /* Initialize value variables used to populate the pie chart. */
        val values = ArrayList<SliceValue>(3)
        val category1Amount: Float
        val category2Amount: Float
        val category3Amount: Float

        /* Check which time interval the user selected. */
        when (timeSpan) {
            /* Last 7 Days. */
            0 -> {
                category1Amount = DataManager.last7DaysTotal(doc, "c-1").toFloat()
                category2Amount = DataManager.last7DaysTotal(doc, "c-2").toFloat()
                category3Amount = DataManager.last7DaysTotal(doc, "c-3").toFloat()
            }
            /* Year To Date (YTD). */
            1 -> {
                category1Amount = DataManager.yearToDateTotal(doc, "c-1").toFloat()
                category2Amount = DataManager.yearToDateTotal(doc, "c-2").toFloat()
                category3Amount = DataManager.yearToDateTotal(doc, "c-3").toFloat()
            }
            /* Last 12 Months. */
            2 -> {
                category1Amount = DataManager.last12MonthsTotal(doc, "c-1").toFloat()
                category2Amount = DataManager.last12MonthsTotal(doc, "c-2").toFloat()
                category3Amount = DataManager.last12MonthsTotal(doc, "c-3").toFloat()
            }
            /* Last Year. */
            3 -> {
                category1Amount = DataManager.lastYearTotal(doc, "c-1").toFloat()
                category2Amount = DataManager.lastYearTotal(doc, "c-2").toFloat()
                category3Amount = DataManager.lastYearTotal(doc, "c-3").toFloat()
            }
            /* All Time. */
            else -> {
                category1Amount = DataManager.getValueByID(doc, "c-1-t")!!.toFloat()
                category2Amount = DataManager.getValueByID(doc, "c-2-t")!!.toFloat()
                category3Amount = DataManager.getValueByID(doc, "c-3-t")!!.toFloat()
            }
        }

        val total = category1Amount + category2Amount + category3Amount

        /* Set the colors and percentage labels for each of the category data set slices. */
        val category1Slice = SliceValue(
            category1Amount, resources.getColor(R.color.colorCategory1, context?.theme)
        )
        if (category1Amount > 0) {
            category1Slice.setLabel(
                ((category1Amount / total) * 100).roundToInt().toString() + " %")
        }
        else {
            category1Slice.setLabel("")
        }
        values.add(category1Slice)

        val category2Slice = SliceValue(
            category2Amount, resources.getColor(R.color.colorCategory2, context?.theme)
        )
        if (category2Amount > 0) {
            category2Slice.setLabel(
                ((category2Amount / total) * 100).roundToInt().toString() + " %")
        }
        else {
            category2Slice.setLabel("")
        }
        values.add(category2Slice)

        val category3Slice = SliceValue(
            category3Amount, resources.getColor(R.color.colorCategory3, context?.theme)
        )
        if (category3Amount > 0) {
            category3Slice.setLabel(
                ((category3Amount / total) * 100).roundToInt().toString() + " %")
        }
        else {
            category3Slice.setLabel("")
        }
        values.add(category3Slice)

        /* Populate the pie chart with the colored slices. */
        pieChart.pieChartData = PieChartData(values).setHasLabels(true)
    }

    /* Purpose: Controller method that populates the totals below the chart.
    *
    * Parameters: timeSpan represents whether the user selected 0) Last 7 Days,
    * 1) Year To Date (YTD), 2) Last 12 Months,  3) Last Year, or 4) All Time.
    *
    * Returns: Nothing. */
    fun updateTotals(doc: Document, timeSpan: Int) {
        val category1Amount: Double
        val category2Amount: Double
        val category3Amount: Double

        /* Check which time span the user selected. */
        when (timeSpan) {
            /* Update the totals to use the Last 7 Days totals. */
            0 -> {
                category1Amount = DataManager.last7DaysTotal(doc, "c-1")
                category2Amount = DataManager.last7DaysTotal(doc, "c-2")
                category3Amount = DataManager.last7DaysTotal(doc, "c-3")
            }
            /* Update the totals to use the Year To Date (YTD) totals. */
            1 -> {
                category1Amount = DataManager.yearToDateTotal(doc, "c-1")
                category2Amount = DataManager.yearToDateTotal(doc, "c-2")
                category3Amount = DataManager.yearToDateTotal(doc, "c-3")
            }
            /* Update the totals to use the Last 12 Months totals. */
            2 -> {
                category1Amount = DataManager.last12MonthsTotal(doc, "c-1")
                category2Amount = DataManager.last12MonthsTotal(doc, "c-2")
                category3Amount = DataManager.last12MonthsTotal(doc, "c-3")
            }
            /* Update the totals to use Last Year's totals. */
            3 -> {
                category1Amount = DataManager.lastYearTotal(doc, "c-1")
                category2Amount = DataManager.lastYearTotal(doc, "c-2")
                category3Amount = DataManager.lastYearTotal(doc, "c-3")
            }
            /* Update the totals to use the All Time totals. */
            else -> {
                category1Amount = DataManager.getValueByID(doc, "c-1-t")!!.toDouble()
                category2Amount = DataManager.getValueByID(doc, "c-2-t")!!.toDouble()
                category3Amount = DataManager.getValueByID(doc, "c-3-t")!!.toDouble()
            }
        }

        /* Format totals of each category for display. */
        category1Total.text = main.formatDollarAmount(category1Amount)
        category2Total.text = main.formatDollarAmount(category2Amount)
        category3Total.text = main.formatDollarAmount(category3Amount)
        /* Format total of the sum of all categories. */
        allTotal.text = main.formatDollarAmount(
            category1Amount + category2Amount + category3Amount)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTab2Binding.inflate(inflater, container, false)
        val rootView = binding.root
        main = requireActivity() as MainActivity
        model = main.model

        lineChart = rootView.findViewById(R.id.chartView)!!
        pieChart = rootView.findViewById(R.id.pieView)!!

        /* Show the line chart and hide the pie chart. */
        showLineChart()

        category1Text = rootView.findViewById(R.id.category1Text)
        category1Total = rootView.findViewById(R.id.category1Total)

        category2Text = rootView.findViewById(R.id.category2Text)
        category2Total = rootView.findViewById(R.id.category2Total)

        category3Text = rootView.findViewById(R.id.category3Text)
        category3Total = rootView.findViewById(R.id.category3Total)

        allText = rootView.findViewById(R.id.allText)
        allText.text = model.getCategories()[0]
        allTotal = rootView.findViewById(R.id.allTotal)

        /* Populate the Chart selector with "Line" and "Pie". */
        spinChartType = rootView.findViewById(R.id.chartTypeSpinner)
        spinChartType.adapter = ArrayAdapter(main, android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.chartTypes))
        /* Create a listener that swaps between the line chart and pie chart. */
        spinChartType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    /* If "Line" is selected. */
                    0 -> {
                        /* Show the line chart, hide the pie chart,
                        * and enable and reveal the category selector. */
                        showLineChart()
                        toggleCategorySelector(true)
                    }
                    /* If "Pie" is selected. */
                    else -> {
                        /* Show the pie chart, hide the line chart,
                        * and disable and grey-out the category selector. */
                        showPieChart()
                        toggleCategorySelector(false)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        /* Populate Time Interval selector with "Last 7 Days", "Year To Date", "Last 12 Months",
        * "Last Year", and "All Time". */
        spinTimeInterval = rootView.findViewById(R.id.lineIntervalSpinner)
        spinTimeInterval.adapter = ArrayAdapter(main, android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.chartIntervals))
        /* Listener to switch the time interval selected. */
        spinTimeInterval.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long
            ) {
                val doc = model.get()
                /* Initialize array to store data points for line chart. */
                val data: DoubleArray

                /* Check which time interval the user selected. */
                when (position) {
                    /* Last 7 days. */
                    0 -> {
                        /* Grab the Last 7 Days of data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last7Days(doc, "all")
                            }
                            else {
                                DataManager.last7Days(
                                    doc, "c-" +
                                            spinChartCategory.selectedItemPosition.toString()
                                )
                            }
                    }
                    /* Year To Date (YTD). */
                    1 -> {
                        /* Grab the current year's month-by-month data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.yearToDate(doc, "all")
                            }
                            else {
                                DataManager.yearToDate(
                                    doc, "c-" +
                                            spinChartCategory.selectedItemPosition.toString()
                                )
                            }
                    }
                    /* Last 12 Months. */
                    2 -> {
                        /* Grab the Last 12 Months of data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last12Months(doc, "all")
                            }
                            else {
                                DataManager.last12Months(
                                    doc, "c-" +
                                            spinChartCategory.selectedItemPosition.toString()
                                )
                            }
                    }
                    /* Last Year. */
                    3 -> {
                        /* Grab the prior year's month-by-month data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.lastYear(doc, "all")
                            }
                            else {
                                DataManager.lastYear(
                                    doc, "c-" +
                                            spinChartCategory.selectedItemPosition.toString()
                                )
                            }
                    }
                    /* All Time. */
                    else -> {
                        /* Grab all recorded years of data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.allTime(doc, "all")
                            }
                            else {
                                DataManager.allTime(
                                    doc, "c-" + spinChartCategory.selectedItemPosition.toString()
                                )
                            }
                    }
                }

                /* Update the line chart with the new time interval data set. */
                updateLineChart(data, position, spinChartCategory.selectedItemPosition)

                /* Update the pie chart with the new time interval data set. */
                updatePieChart(doc, position)

                /* Update the totals with the new time interval data set. */
                updateTotals(doc, position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        /* Populate the Category selector with "All" and the current category names. */
        spinChartCategory = rootView.findViewById(R.id.lineCategorySpinner)
        spinChartCategory.adapter = ArrayAdapter(main,
            android.R.layout.simple_spinner_dropdown_item, model.getCategories())
        /* Enable the category selector drop-down menu. */
        toggleCategorySelector(true)
        /* Listener to update the line chart based on the category selected. */
        spinChartCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long
            ) {
                val doc = model.get()
                /* Initialize the array of data points for the line chart. */
                val data: DoubleArray
                /* Grab the selected time interval. */
                val timeSpan = spinTimeInterval.selectedItemPosition

                /* Check which category the user selected. */
                when (position) {
                    /* All Categories. */
                    0 -> {
                        /* Grab the data set from whichever time interval
                        * the user selected. */
                        data = when (timeSpan) {
                            0 -> {
                                DataManager.last7Days(doc, "all")
                            }
                            1 -> {
                                DataManager.yearToDate(doc, "all")
                            }
                            2 -> {
                                DataManager.last12Months(doc, "all")
                            }
                            3 -> {
                                DataManager.lastYear(doc, "all")
                            }
                            else -> {
                                DataManager.allTime(doc, "all")
                            }
                        }
                    }
                    /* A specific category. */
                    else -> {
                        /* Grab the data set from whichever time interval
                        * and category the user selected. */
                        data = when (timeSpan) {
                            0 -> {
                                DataManager.last7Days(doc, "c-$position")
                            }
                            1 -> {
                                DataManager.yearToDate(doc, "c-$position")
                            }
                            2 -> {
                                DataManager.last12Months(doc, "c-$position")
                            }
                            3 -> {
                                DataManager.lastYear(doc, "c-$position")
                            }
                            else -> {
                                DataManager.allTime(doc, "c-$position")
                            }
                        }
                    }
                }

                /* Update the line chart with the update category selection. */
                updateLineChart(data, timeSpan, position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        /* Listener to display value when user taps a datapoint on the line chart. */
        lineChart.onValueTouchListener = object: LineChartOnValueSelectListener {
            override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue?) {
                if (null != value) {
                    val output = main.formatDollarAmount(value.y.toDouble())
                    Toast.makeText(context, output, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onValueDeselected() {}
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Observe the LiveData objects from SharedViewModel. */
        model.getLive().observe(viewLifecycleOwner) { doc ->
            /* Only refresh the category button labels
            * if the user actually changed a category label
            * in the SettingsActivity. */
            if (model.getTabNeedsRefreshState(1)) {

                /* Refresh the label for Category 1. */
                category1Label = model.getCategories()[1]
                /* Slice label to be at most 10 characters long. */
                if (category1Label.length > 10) {
                    category1Label = category1Label.substring(0, 9) + "."
                }
                category1Text.text = category1Label

                /* Refresh the label for Category 2. */
                category2Label = model.getCategories()[2]
                /* Slice label to be at most 10 characters long. */
                if (category2Label.length > 10) {
                    category2Label = category2Label.substring(0, 9) + "."
                }
                category2Text.text = category2Label

                /* Refresh the label for Category 3. */
                category3Label = model.getCategories()[3]
                /* Slice label to be at most 10 characters long. */
                if (category3Label.length > 10) {
                    category3Label = category3Label.substring(0, 9) + "."
                }
                category3Text.text = category3Label

                /* Refresh the category names in the Category selector. */
                spinChartCategory.adapter = ArrayAdapter(main,
                    android.R.layout.simple_spinner_dropdown_item, model.getCategories()
                )

                /* Reset the tab's model boolean. */
                model.setTabNeedsRefreshState(1, false)
            }

            /* Grab the selected time interval. */
            val timeSpan = spinTimeInterval.selectedItemPosition

            /* Refresh the pie chart. */
            updatePieChart(doc, timeSpan)

            /* Grab the selected category. */
            val category = spinChartCategory.selectedItemPosition

            /* Initialize the array of data points for the line chart. */
            val data: DoubleArray = when (category) {
                /* All Categories. */
                0 -> {
                    /* Grab the data set from whichever time interval
                    * the user selected. */
                    when (timeSpan) {
                        0 -> {
                            DataManager.last7Days(doc, "all")
                        }
                        1 -> {
                            DataManager.yearToDate(doc, "all")
                        }
                        2 -> {
                            DataManager.last12Months(doc, "all")
                        }
                        3 -> {
                            DataManager.lastYear(doc, "all")
                        }
                        else -> {
                            DataManager.allTime(doc, "all")
                        }
                    }
                }
                /* A specific category. */
                else -> {
                    /* Grab the data set from whichever time interval
                    * and category the user selected. */
                    when (timeSpan) {
                        0 -> {
                            DataManager.last7Days(doc, "c-$category")
                        }
                        1 -> {
                            DataManager.yearToDate(doc, "c-$category")
                        }
                        2 -> {
                            DataManager.last12Months(doc, "c-$category")
                        }
                        3 -> {
                            DataManager.lastYear(doc, "c-$category")
                        }
                        else -> {
                            DataManager.allTime(doc, "c-$category")
                        }
                    }
                }
            }

            /* Refresh the line chart. */
            updateLineChart(data, timeSpan, category)

            /* Refresh the totals. */
            updateTotals(doc, timeSpan)
        }
    }

    /* Purpose: Refresh Tab 2 so that the live data is updated on the chart and totals.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    override fun onResume() {
        super.onResume()

        if (allowRefresh)
        {
            /* Disable view refresh when navigating away to Tab2Fragment,
            * so that the app does not freeze. */
            allowRefresh = false
            /* Tab 2 is currently visible to the user,
            * so try to detach and re-attach the Tab 2 fragment. */
            @Suppress("DetachAndAttachSameFragment")
            val ftDetach = getParentFragmentManager().beginTransaction()
            ftDetach.setReorderingAllowed(false)
            ftDetach.detach(this).attach(this).commit()
        }
    }

    override fun onPause() {
        super.onPause()
        /* Re-enable view refresh when navigating away from Tab2Fragment,
        * so that Tab2Fragment.onResume can refresh the data in the view. */
        allowRefresh = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        allowRefresh = false
        _binding = null
    }
}
