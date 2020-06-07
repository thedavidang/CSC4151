package com.spendsages.walletwatch

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab2Fragment : Fragment() {
    private lateinit var rootView : View
    private lateinit var main : MainActivity
    private lateinit var model : SharedViewModel

    private lateinit var lineChart : LineChartView
    private lateinit var pieChart : PieChartView

    /* Setup metric prefixes for one thousand and above. */
    private val metric = arrayListOf("k", "M", "G", "T", "P", "E", "Z", "Y")

    private lateinit var categories : MutableList<String?>

    private lateinit var category1Text : TextView
    private lateinit var category1Total : TextView

    private lateinit var category2Text : TextView
    private lateinit var category2Total : TextView

    private lateinit var category3Text : TextView
    private lateinit var category3Total : TextView

    private lateinit var allText : TextView
    private lateinit var allTotal : TextView

    private lateinit var spinChartType : Spinner

    private lateinit var spinTimeInterval : Spinner

    private lateinit var spinChartCategory : Spinner

    /* Purpose: Refresh Tab 2 so that the live data is updated on the chart and totals.
    *
    * Parameters: isVisibleToUser represents whether Tab 2 is currently visible to the user.
    *
    * Returns: Nothing. */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(true)

        /* Check if Tab 2 is currently visible to the user. */
        if (isVisibleToUser) {
            /* Try to detach and attach the Tab 2 fragment. */
            val ft = fragmentManager!!.beginTransaction()

            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false)
            }

            ft.detach(this).attach(this).commit()
        }
    }

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
    private fun toggleCategorySelector(isEnabled : Boolean) {
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
    * timeSpan represents whether the user selected 0) Last 7 Days, 1) Last 12 Months,
    * or 2) All Time (All Time will only display the last 10 years).
    * colorPosition represent whether the user selected 0) All categories
    * or 1-3) a specific Category.
    *
    * Returns: Nothing. */
    fun updateLineChart(dataPoints: DoubleArray, timeSpan: Int, colorPosition: Int) {
        /* Initialize a Calender object. */
        val cal: Calendar = Calendar.getInstance()

        /* Initialize counter to based on selected time interval. */
        var counter =
            when (timeSpan) {
                /* Last 12 Months. */
                1 -> {
                    11
                }
                /* All Time (in this case it is actually last 10 years). */
                2 -> {
                    9
                }
                /* Last 7 Days. */
                else -> {
                    6
                }
            }

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
        /* Check which category the user selected. */
        when (colorPosition) {
            /* Set line to red for Category 1. */
            1 -> {
                line.color = this.resources.getColor(R.color.colorCategory1)
            }
            /* Set line to green for Category 2. */
            2 -> {
                line.color = this.resources.getColor(R.color.colorCategory2)
            }
            /* Set line to blue for Category 3. */
            3 -> {
                line.color = this.resources.getColor(R.color.colorCategory3)
            }
            /* Set line to black for All Categories. */
            else -> {
                line.color = this.resources.getColor(R.color.colorAll)
            }
        }

        /* Initialize the lines array such that the line can be added to the LineChartData. */
        val lines = ArrayList<Line>()
        lines.add(line)
        /* Add the line to the LineChartData. */
        val data = LineChartData()
        data.lines = lines

        /* Populate the X-axis labels. */
        val xAxisValues = ArrayList<AxisValue>()
        /* Check which time interval the user selected. */
        when (timeSpan) {
            /* Last 12 Months. */
            1 -> {
                /* Determine the current month. */
                var currentMonth = cal.get(Calendar.MONTH)
                var currentMonthString = ""
                /* Grab the abbreviation for the last 12 months. */
                var j = 11
                for (i in 1..12) {
                    when (currentMonth) {
                        0 -> {
                            currentMonthString = "Jan"
                        }
                        1 -> {
                            currentMonthString = "Feb"
                        }
                        2 -> {
                            currentMonthString = "Mar"
                        }
                        3 -> {
                            currentMonthString = "Apr"
                        }
                        4 -> {
                            currentMonthString = "May"
                        }
                        5 -> {
                            currentMonthString = "Jun"
                        }
                        6 -> {
                            currentMonthString = "Jul"
                        }
                        7 -> {
                            currentMonthString = "Aug"
                        }
                        8 -> {
                            currentMonthString = "Sep"
                        }
                        9 -> {
                            currentMonthString = "Oct"
                        }
                        10 -> {
                            currentMonthString = "Nov"
                        }
                        11 -> {
                            currentMonthString = "Dec"
                        }
                    }
                    /* Add the abbreviation to the X-axis labels array. */
                    xAxisValues.add(AxisValue(j.toFloat(), currentMonthString.toCharArray()))
                    currentMonth -= 1
                    /* Account for rollover into December of the previous year. */
                    if (currentMonth == -1) {
                        currentMonth = 11
                    }
                    j--
                }
            }
            /* All Time (in this case it is actually last 10 years). */
            2 -> {
                /* Determine the current year. */
                var currentYear = cal.get(Calendar.YEAR)
                var j = 9
                /* Grab the last 10 years. */
                for (i in 1..10) {
                    /* Add the year to the X-axis labels array. */
                    xAxisValues.add(AxisValue(j.toFloat(), currentYear.toString().toCharArray()))
                    currentYear -= 1
                    j--
                }
            }
            /* Last 7 Days. */
            else -> {
                /* Determine the current day. */
                var currentDay = cal.get(Calendar.DAY_OF_WEEK)
                var currentDayString = ""
                /* Grab the abbreviation for the last 7 days. */
                var j = 6
                for (i in 1..7) {
                    when (currentDay) {
                        1 -> {
                            currentDayString = "Sun"
                        }
                        2 -> {
                            currentDayString = "Mon"
                        }
                        3 -> {
                            currentDayString = "Tue"
                        }
                        4 -> {
                            currentDayString = "Wed"
                        }
                        5 -> {
                            currentDayString = "Thu"
                        }
                        6 -> {
                            currentDayString = "Fri"
                        }
                        7 -> {
                            currentDayString = "Sat"
                        }
                    }
                    /* Add the abbreviation to the X-axis labels array. */
                    xAxisValues.add(AxisValue(j.toFloat(), currentDayString.toCharArray()))
                    currentDay -= 1
                    /* Account for rollover into Sunday of the previous week. */
                    if (currentDay == 0) {
                        currentDay = 7
                    }
                    j--
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
        /* Initialize the bottom Y-axis value and label.*/
        var yValue = minY
        var yLabel : String
        /* Iterate to create eleven evenly spaced Y-axis values with labels. */
        for (i in 1..11) {
            /* Calculate the scientific notation exponent of the Y-value by
            * taking the floor of the base 10 logarithm of the Y-axis value. */
            val yValExponent = kotlin.math.floor(kotlin.math.log10(yValue))
            /* Check if the Y-axis value is less than 100. */
            if (yValExponent < 2) {
                /* Display cents with two decimal places. */
                yLabel = DecimalFormat("0.00").format(yValue)
            }
            /* Check if the Y-axis value is from 100 to 1,000. */
            else if (yValExponent < 3) {
                /* Display dollars without cents with no decimal places. */
                yLabel = yValue.toString().substringBefore(".")
            }
            /* Check if Y-axis value needs to have at least 2 significant digits. */
            else if ((yValExponent % 3) == 0.0f) {
                /* Add one decimal as to have 2 significant digits. */
                val yString = yValue.toLong().toString()
                yLabel = yString.substring(0, 1) + "." + yString.substring(1, 2) +
                        " " + metric[((yValExponent / 3) - 1).toInt()]
            }
            /* Otherwise, the Y-axis value is 1,000 or more. */
            else {
                /* Set the Y-axis label by consolidating the value to the digits before the
                * thousand separator by getting the substring up to the index of
                * one plus the remainder of the exponent divided by three.
                * Determine the metric prefix by
                * subtracting one from the floor of the exponent divided by three. */
                yLabel = yValue.toLong().toString().substring(0, ((yValExponent % 3) + 1).toInt()) +
                        " " + metric[((yValExponent / 3) - 1).toInt()]
            }

            /* Add the Y-axis value and label to the Y-axis object. */
            yAxisValues.add(AxisValue(yValue, yLabel.toCharArray()))

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
    * Parameters: timeSpan represents whether the user selected 0) Last 7 Days, 1) Last 12 Months,
    * or 2) All Time.
    *
    * Returns: Nothing. */
    fun updatePieChart(timeSpan: Int) {
        /* Initialize value variables used to populate the pie chart. */
        val values = ArrayList<SliceValue>(3)
        val category1Amount : Float
        val category2Amount : Float
        val category3Amount : Float

        /* Check which time interval the user selected. */
        when (timeSpan) {
            /* Last 12 Months. */
            1 -> {
                category1Amount = DataManager.last12MonthsTotal(
                    model.get(), "c-1").toFloat()
                category2Amount = DataManager.last12MonthsTotal(
                    model.get(), "c-2").toFloat()
                category3Amount = DataManager.last12MonthsTotal(
                    model.get(), "c-3").toFloat()
            }
            /* All Time. */
            2 -> {
                category1Amount = DataManager.getValueByID(model.get(), "c-1-t")!!.toFloat()
                category2Amount = DataManager.getValueByID(model.get(), "c-2-t")!!.toFloat()
                category3Amount = DataManager.getValueByID(model.get(), "c-3-t")!!.toFloat()
            }
            /* Last 7 Days. */
            else -> {
                category1Amount = DataManager.last7DaysTotal(model.get(), "c-1").toFloat()
                category2Amount = DataManager.last7DaysTotal(model.get(), "c-2").toFloat()
                category3Amount = DataManager.last7DaysTotal(model.get(), "c-3").toFloat()
            }
        }

        val total = category1Amount + category2Amount + category3Amount

        /* Set the colors and percentage labels for each of the category data set slices. */
        val category1Slice = SliceValue(category1Amount, resources.getColor(R.color.colorCategory1))
        if (category1Amount > 0) {
            category1Slice.setLabel(
                ((category1Amount / total) * 100).roundToInt().toString() + " %")
        }
        else {
            category1Slice.setLabel("")
        }
        values.add(category1Slice)

        val category2Slice = SliceValue(category2Amount, resources.getColor(R.color.colorCategory2))
        if (category2Amount > 0) {
            category2Slice.setLabel(
                ((category2Amount / total) * 100).roundToInt().toString() + " %")
        }
        else {
            category2Slice.setLabel("")
        }
        values.add(category2Slice)

        val category3Slice = SliceValue(category3Amount, resources.getColor(R.color.colorCategory3))
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

    /* Purpose: Private helper method that formats a total to be displayed below the chart.
    *
    * Parameters: total represents the total amount to format.
    *
    * Returns: String of the formatted total. */
    private fun formatTotal(total: Double) : String {
        /* Compute base 10 exponent of total. */
        val exponent = kotlin.math.floor(kotlin.math.log10(total))

        /* Check if total is less than one billion. */
        return if (exponent < 9) {
            /* Format total with dollar sign and thousand separator commas. */
            "$ " + DecimalFormat("#,##0.00").format(total)
        }
        /* Otherwise, total is one billion or more. */
        else {
            var totalString = total.toLong().toString()
            /* Slice total to the first nine digits. */
            if (totalString.length > 9) {
                totalString = totalString.substring(0, 9)
            }
            /* Determine the position of where the first thousand separator would be. */
            val firstThousandSeparatorIndex = ((exponent % 3) + 1).toInt()
            /* Format total with dollar sign and metric prefix. */
            "$ " + totalString.substring(0, firstThousandSeparatorIndex) + "." +
                    totalString.substring(firstThousandSeparatorIndex) + " " +
                    metric[((exponent / 3) - 1).toInt()]
        }
    }

    /* Purpose: Controller method that populates the totals below the chart.
    *
    * Parameters: timeSpan represents whether the user selected 0) Last 7 Days, 1) Last 12 Months,
    * or 2) All Time.
    *
    * Returns: Nothing. */
    fun updateTotals(timeSpan: Int) {
        val category1Amount : Double
        val category2Amount : Double
        val category3Amount : Double

        /* Check which time span the user selected. */
        when (timeSpan) {
            /* Update the totals to use the Last 12 Months totals. */
            1 -> {
                category1Amount = DataManager.last12MonthsTotal(model.get(), "c-1")
                category2Amount = DataManager.last12MonthsTotal(model.get(), "c-2")
                category3Amount = DataManager.last12MonthsTotal(model.get(), "c-3")
            }
            /* Update the totals to use the All Time totals. */
            2 -> {
                category1Amount = DataManager.getValueByID(model.get(), "c-1-t")!!.toDouble()
                category2Amount = DataManager.getValueByID(model.get(), "c-2-t")!!.toDouble()
                category3Amount = DataManager.getValueByID(model.get(), "c-3-t")!!.toDouble()
            }
            /* Update the totals to use the Last 7 Days totals. */
            else -> {
                category1Amount = DataManager.last7DaysTotal(model.get(), "c-1")
                category2Amount = DataManager.last7DaysTotal(model.get(), "c-2")
                category3Amount = DataManager.last7DaysTotal(model.get(), "c-3")
            }
        }

        /* Format totals of each category for display. */
        category1Total.text = formatTotal(category1Amount)
        category2Total.text = formatTotal(category2Amount)
        category3Total.text = formatTotal(category3Amount)
        /* Format total of the sum of all categories. */
        allTotal.text = formatTotal(category1Amount + category2Amount + category3Amount)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tab2, container, false)
        main = activity as MainActivity
        model = main.model

        /* Grab category labels as they currently exist in the local XML repo file. */
        categories = DataManager.getCategories(model.get())

        lineChart = rootView.findViewById(R.id.chartView) as LineChartView
        pieChart = rootView.findViewById(R.id.pieView) as PieChartView

        /* Populate the line chart with Last 7 Days for All Categories. */
        updateLineChart(DataManager.last7Days(model.get(), "all"),
            0, 0)

        /* Populate pie chart with Last 7 Days. */
        updatePieChart(0)

        /* Show the line chart and hide the pie chart. */
        showLineChart()

        /* Set the total and label for Category 1 over the Last 7 Days. */
        category1Text = rootView.findViewById(R.id.category1Text)
        var category1Label = categories[1]!!
        /* Slice label to be at most 10 characters long. */
        if (category1Label.length > 10) {
            category1Label = category1Label.substring(0, 9) + "."
        }
        category1Text.text = category1Label
        category1Total = rootView.findViewById(R.id.category1Total)

        /* Set the total and label for Category 2 over the Last 7 Days. */
        category2Text = rootView.findViewById(R.id.category2Text)
        var category2Label = categories[2]!!
        /* Slice label to be at most 10 characters long. */
        if (category2Label.length > 10) {
            category2Label = category2Label.substring(0, 9) + "."
        }
        category2Text.text = category2Label
        category2Total = rootView.findViewById(R.id.category2Total)

        /* Set the total and label for Category 3 over the Last 7 Days. */
        category3Text = rootView.findViewById(R.id.category3Text)
        var category3Label = categories[3]!!
        /* Slice label to be at most 10 characters long. */
        if (category3Label.length > 10) {
            category3Label = category3Label.substring(0, 9) + "."
        }
        category3Text.text = category3Label
        category3Total = rootView.findViewById(R.id.category3Total)

        /* Set the total for All Categories over the Last 7 Days. */
        allText = rootView.findViewById(R.id.allText)
        allText.text = categories[0]
        allTotal = rootView.findViewById(R.id.allTotal)

        /* Update the totals with the Last 7 Days of entries. */
        updateTotals(0)

        /* Populate the Chart selector with "Line" and "Pie". */
        spinChartType = rootView.findViewById(R.id.chartTypeSpinner)
        spinChartType.adapter = ArrayAdapter(main, R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.chartTypes))
        /* Create a listener that swaps between the line chart and pie chart. */
        spinChartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    /* If "Pie" is selected. */
                    1 -> {
                        /* Show the pie chart, hide the line chart,
                        * and disable and grey-out the category selector. */
                        showPieChart()
                        toggleCategorySelector(false)
                    }
                    /* If "Line" is selected. */
                    else -> {
                        /* Show the line chart, hide the pie chart,
                        * and enable and reveal the category selector. */
                        showLineChart()
                        toggleCategorySelector(true)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        /* Populate Time Interval selector with "Last 7 Days", "Last 12 Months", and "All Time". */
        spinTimeInterval = rootView.findViewById(R.id.lineIntervalSpinner)
        spinTimeInterval.adapter = ArrayAdapter(main,
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.chartIntervals))
        /* Listener to switch the time interval selected. */
        spinTimeInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long
            ) {
                /* Initialize array to store data points for line chart. */
                val data : DoubleArray

                /* Check which time interval the user selected. */
                when (position) {
                    /* Last 12 Months. */
                    1 -> {
                        /* Grab the Last 12 Months of data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last12Months(model.get(), "all")
                            }
                            else {
                                DataManager.last12Months(model.get(), "c-" +
                                        spinChartCategory.selectedItemPosition.toString())
                            }
                    }
                    /* All Time. */
                    2 -> {
                        /* Grab the Last 10 Years of data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last10Years(model.get(), "all")
                            }
                            else {
                                DataManager.last10Years(model.get(), "c-" +
                                        spinChartCategory.selectedItemPosition.toString())
                            }
                    }
                    /* Last 7 days. */
                    else -> {
                        /* Grab the Last 7 Days of data points from
                        * whichever category the user selected. */
                        data =
                            if (spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last7Days(model.get(), "all")
                            }
                            else {
                                DataManager.last7Days(model.get(), "c-" +
                                        spinChartCategory.selectedItemPosition.toString())
                            }
                    }
                }

                /* Update the line chart with the new time interval data set. */
                updateLineChart(data, position, spinChartCategory.selectedItemPosition)

                /* Update the pie chart with the new time interval data set. */
                updatePieChart(position)

                /* Update the totals with the new time interval data set. */
                updateTotals(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        /* Populate the Category selector with "All" and the current category labels. */
        spinChartCategory = rootView.findViewById(R.id.lineCategorySpinner)
        /* Focus on category Spinner as to prevent any of the totals from displaying blank. */
        spinChartCategory.adapter = ArrayAdapter<String?>(main,
            R.layout.support_simple_spinner_dropdown_item, categories)
        /* Enable the category selector drop-down menu. */
        toggleCategorySelector(true)
        /* Listener to update the line chart based on the category selected. */
        spinChartCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long
            ) {
                /* Initialize the array of data points for the line chart. */
                val data : DoubleArray
                /* Grab the selected time interval. */
                val timeSpan = spinTimeInterval.selectedItemPosition

                /* Check if the user selected a specific category. */
                if (position != 0) {
                    /* Grab the data set from whichever time interval
                    * and category the user selected. */
                    data = when (timeSpan) {
                        0 -> {
                            DataManager.last7Days(model.get(), "c-$position")

                        }
                        1 -> {
                            DataManager.last12Months(model.get(), "c-$position")
                        }
                        else -> {
                            DataManager.last10Years(model.get(), "c-$position")
                        }
                    }
                }
                /* Otherwise, the use selected All Categories. */
                else {
                    /* Grab the data set from whichever time interval
                    * the user selected. */
                    data = when (timeSpan) {
                        0 -> {
                            DataManager.last7Days(model.get(), "all")
                        }
                        1 -> {
                            DataManager.last12Months(model.get(), "all")
                        }
                        else -> {
                            DataManager.last10Years(model.get(), "all")
                        }
                    }
                }

                /* Update the line chart with the update category selection. */
                updateLineChart(data, timeSpan, position)
            }


            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab2Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab2Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
