package com.spendsages.walletwatch.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.spendsages.walletwatch.DataManager
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.SharedViewModel
import com.spendsages.walletwatch.databinding.FragmentTab2Binding
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

    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart

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

    /* Purpose: Controller method that disables and grays-out category selector or
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

        /* Initialize array of data points. */
        val values = ArrayList<Entry>(counter)

        /* Populate the coordinates for each data point. */
        for (i in 0..counter) {
            val x = i.toFloat()
            val y = dataPoints[counter].toFloat()

            values.add(Entry(x, y))

            /* Decrement "counter". For loop uses "i" as its counter. */
            counter--
        }

        /* Create a line object and color it according to the selected category. */
        val dataSet = LineDataSet(values, "")
        /* Set the line width to 4f (4dp). */
        dataSet.setLineWidth(4f)
        /* Enable datapoint labels. */
        dataSet.setDrawValues(false)
        /* Check which category the user selected. */
        when (category) {
            /* Set line to black for All Categories. */
            0 -> { dataSet.color = resources.getColor(R.color.colorAll, context?.theme) }
            /* Set line to red for Category 1. */
            1 -> { dataSet.color = resources.getColor(R.color.colorCategory1, context?.theme) }
            /* Set line to green for Category 2. */
            2 -> { dataSet.color = resources.getColor(R.color.colorCategory2, context?.theme) }
            /* Set line to blue for Category 3. */
            3 -> { dataSet.color = resources.getColor(R.color.colorCategory3, context?.theme) }
        }

        /* Display datapoint dots on the line. */
        dataSet.setDrawCircles(true)
        dataSet.setDrawCircleHole(false)
        dataSet.setCircleColor(dataSet.color)
        dataSet.circleRadius = 6f

        /* Add the dataSet to the LineData. */
        val data = LineData(dataSet)

        /* Map X-axis labels. */
        val xAxisLabels = ArrayList<String>()
        /* Check which time interval the user selected. */
        when (timeSpan) {
            /* Last 7 Days. */
            0 -> {
                var currentDay = cal.get(Calendar.DAY_OF_WEEK)
                repeat(7) {
                    xAxisLabels.add(when (currentDay) {
                        1 -> "Sun"
                        2 -> "Mon"
                        3 -> "Tue"
                        4 -> "Wed"
                        5 -> "Thu"
                        6 -> "Fri"
                        7 -> "Sat"
                        else -> ""
                    })
                    currentDay -= 1
                    if (currentDay == 0) currentDay = 7
                }
            }
            /* Year To Date (YTD). */
            1 -> {
                var currentMonth = cal.get(Calendar.MONTH)
                repeat(currentMonth + 1) {
                    xAxisLabels.add(monthAbbreviation[currentMonth])
                    currentMonth -= 1
                }
            }
            /* Last 12 Months. */
            2 -> {
                var currentMonth = cal.get(Calendar.MONTH)
                repeat(12) {
                    xAxisLabels.add(monthAbbreviation[currentMonth])
                    currentMonth -= 1
                    if (currentMonth == -1) currentMonth = 11
                }
            }
            /* Last Year. */
            3 -> {
                var currentMonth = 11
                repeat(12) {
                    xAxisLabels.add(monthAbbreviation[currentMonth])
                    currentMonth -= 1
                }
            }
            /* All Time. */
            else -> {
                var currentYear = cal.get(Calendar.YEAR)
                repeat(dataPoints.size) {
                    xAxisLabels.add(currentYear.toString())
                    currentYear -= 1
                }
            }
        }
        xAxisLabels.reverse()

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.textColor = Color.BLACK
        xAxis.setTextSize(14f)
        /* Ensure labels are aligned with data points and not duplicated. */
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setLabelCount(xAxisLabels.size, true)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return xAxisLabels.getOrNull(value.toInt()) ?: ""
            }
        }

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.BLACK
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setTextSize(14f)
        /* Lock the bottom of the Y-axis to 0.0. */
        yAxisLeft.axisMinimum = 0f
        /* Increase padding between Y-axis labels and the Y-axis. */
        yAxisLeft.xOffset = 10f
        yAxisLeft.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val yValExponent = kotlin.math.floor(kotlin.math.log10(value.toDouble()))
                return if (yValExponent < 2.0) {
                    DecimalFormat("0.00").format(value)
                } else if (yValExponent < 3.0) {
                    value.toString().substringBefore(".")
                } else if ((yValExponent % 3.0) == 0.0) {
                    val metricPrefix = main.metric[((yValExponent / 3) - 1).toInt()]
                    val yString = value.toDouble().toLong().toString()
                    yString.substring(0, 1) + "." + yString.substring(1, 2) + " " + metricPrefix
                } else {
                    val metricPrefix = main.metric[((yValExponent / 3) - 1).toInt()]
                    value.toLong().toString().substring(0,
                        ((yValExponent % 3) + 1).toInt()) + " " + metricPrefix
                }
            }
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        /* Add padding to the bottom and right of the chart area. */
        lineChart.setExtraOffsets(0f, 0f, 20f, 20f)
        lineChart.data = data
        lineChart.invalidate()
    }

    /* Purpose: Controller method that populates the pie chart with the selected data set.
    *
    * Parameters: timeSpan represents whether the user selected 0) Last 7 Days,
    * 1) Year To Date (YTD), 2) Last 12 Months,  3) Last Year, or 4) All Time.
    *
    * Returns: Nothing. */
    fun updatePieChart(doc: Document, timeSpan: Int) {
        /* Initialize value variables used to populate the pie chart. */
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

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (category1Amount > 0) {
            entries.add(PieEntry(category1Amount,
                ((category1Amount / total) * 100).roundToInt().toString() + " %"))
            colors.add(resources.getColor(R.color.colorCategory1, context?.theme))
        }

        if (category2Amount > 0) {
            entries.add(PieEntry(category2Amount,
                ((category2Amount / total) * 100).roundToInt().toString() + " %"))
            colors.add(resources.getColor(R.color.colorCategory2, context?.theme))
        }

        if (category3Amount > 0) {
            entries.add(PieEntry(category3Amount,
                ((category3Amount / total) * 100).roundToInt().toString() + " %"))
            colors.add(resources.getColor(R.color.colorCategory3, context?.theme))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(18f)
        pieChart.invalidate()
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
        category1Total.setOnClickListener {
            /* Get a handle to the system clipboard service. */
            val clipboardManager = getSystemService(
                main, ClipboardManager::class.java)
            /* Create a ClipData object to hold the data to be copied. */
            val clipData = ClipData.newPlainText(
                category1Text.text, category1Total.text.removePrefix("$ "))
            /* Set the data to the clipboard. */
            clipboardManager?.setPrimaryClip(clipData)
            /* Let user know that copy succeeded. */
            Toast.makeText(main,
                "Category 1 total copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        category2Text = rootView.findViewById(R.id.category2Text)
        category2Total = rootView.findViewById(R.id.category2Total)
        category2Total.setOnClickListener {
            /* Get a handle to the system clipboard service. */
            val clipboardManager = getSystemService(
                main, ClipboardManager::class.java)
            /* Create a ClipData object to hold the data to be copied. */
            val clipData = ClipData.newPlainText(
                category2Text.text, category2Total.text.removePrefix("$ "))
            /* Set the data to the clipboard. */
            clipboardManager?.setPrimaryClip(clipData)
            /* Let user know that copy succeeded. */
            Toast.makeText(main,
                "Category 2 total copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        category3Text = rootView.findViewById(R.id.category3Text)
        category3Total = rootView.findViewById(R.id.category3Total)
        category3Total.setOnClickListener {
            /* Get a handle to the system clipboard service. */
            val clipboardManager = getSystemService(
                main, ClipboardManager::class.java)
            /* Create a ClipData object to hold the data to be copied. */
            val clipData = ClipData.newPlainText(
                category3Text.text, category3Total.text.removePrefix("$ "))
            /* Set the data to the clipboard. */
            clipboardManager?.setPrimaryClip(clipData)
            /* Let user know that copy succeeded. */
            Toast.makeText(main,
                "Category 3 total copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        allText = rootView.findViewById(R.id.allText)
        allText.text = model.getCategories()[0]
        allTotal = rootView.findViewById(R.id.allTotal)
        allTotal.setOnClickListener {
            /* Get a handle to the system clipboard service. */
            val clipboardManager = getSystemService(
                main, ClipboardManager::class.java)
            /* Create a ClipData object to hold the data to be copied. */
            val clipData = ClipData.newPlainText(
                allText.text, allTotal.text.removePrefix("$ "))
            /* Set the data to the clipboard. */
            clipboardManager?.setPrimaryClip(clipData)
            /* Let user know that copy succeeded. */
            Toast.makeText(main, "Total of all categories copied to clipboard",
                Toast.LENGTH_SHORT).show()
        }

        /* Populate the Chart selector with "Line" and "Pie". */
        spinChartType = rootView.findViewById(R.id.chartTypeSpinner)
        spinChartType.adapter = ArrayAdapter(main,
            android.R.layout.simple_spinner_dropdown_item,
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
                        * and disable and gray-out the category selector. */
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
        spinTimeInterval.adapter = ArrayAdapter(main,
            android.R.layout.simple_spinner_dropdown_item,
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
                                    doc, "c-" +
                                            spinChartCategory.selectedItemPosition.toString()
                                )
                            }
                    }
                }

                /* Update the line chart with the new time interval data set. */
                updateLineChart(data, position,
                    spinChartCategory.selectedItemPosition)

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
        lineChart.maxHighlightDistance = 20f
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    /* Get the dollar amount of the tapped data point. */
                    val output = main.formatDollarAmount(e.y.toDouble())
                    /* Get a handle to the system clipboard service. */
                    val clipboardManager = getSystemService(
                        main, ClipboardManager::class.java)
                    /* Create a ClipData object to hold the data to be copied. */
                    val clipData = ClipData.newPlainText(
                        e.x.toString(), output.removePrefix("$ "))
                    /* Set the data to the clipboard. */
                    clipboardManager?.setPrimaryClip(clipData)
                    /* Let user know that copy succeeded. */
                    Toast.makeText(main, "$output copied to clipboard",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {}
        })

        /* Hide the keyboard. */
        (main.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view?.windowToken, 0)

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
                /* Cache the index of the selected category. */
                val selectedCategory = spinChartCategory.selectedItemPosition

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
                    android.R.layout.simple_spinner_dropdown_item,
                    model.getCategories())

                /* Restore the selected category index. */
                spinChartCategory.setSelection(selectedCategory)

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
            /* Disable view refresh when navigating to Tab2Fragment,
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
