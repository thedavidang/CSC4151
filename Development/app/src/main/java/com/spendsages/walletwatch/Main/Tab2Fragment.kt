package com.spendsages.walletwatch

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import java.text.DecimalFormat
import java.util.*

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
    fun displayLineChart(total: DoubleArray, timeSpan: Int, colorPosition: Int) {
        /* Initialize a Calender object. */
        val cal: Calendar = Calendar.getInstance()

        /* Initialize the view of the line chart and set its initial dimensions. */
        var view = Viewport(lineChart.maximumViewport)
        view.top = view.top + view.height() * 0.05f

        /* Populate the X-axis float values. */
        val values = ArrayList<PointValue>()
        /* Initialize counter to six, which acts as a default to 0) Last 7 Days. */
        var h = 6
        /* Check which time interval to use. */
        when (timeSpan) {
            /* Last 12 Months. */
            1 -> {
                /* Populate an X-value for each of the 12 months. */
                h = 11
                for (i in 0..11) {
                    values.add(PointValue(i.toFloat(), total[h].toFloat()))
                    h--
                }
            }
            /* All Time (in this case it is actually last 10 years). */
            2 -> {
                /* Populate an X-value for each of the 10 years. */
                h = 9
                for (i in 0..9) {
                    values.add(PointValue(i.toFloat(), total[h].toFloat()))
                    h--
                }
            }
            /* Last 7 Days. */
            else -> {
                /* Populate an X-value for each of the 7 days. */
                for (i in 0..6) {
                    values.add(PointValue(i.toFloat(), total[h].toFloat()))
                    h--
                }
            }
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
        val axisValues = ArrayList<AxisValue>()
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
                    axisValues.add(AxisValue(j.toFloat(), currentMonthString.toCharArray()))
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
                    axisValues.add(AxisValue(j.toFloat(), currentYear.toString().toCharArray()))
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
                    axisValues.add(AxisValue(j.toFloat(), currentDayString.toCharArray()))
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
        val axisX = Axis(axisValues).setHasLines(true)
        axisX.maxLabelChars = 4
        data.axisXBottom = axisX

        /* Populate the Y-axis with value labels. */
        val axisY = Axis().setHasLines(true)
        /* Format the Y-axis value labels for user readability. */
        val formatter = SimpleAxisValueFormatter()
        /* Determine the maximum Y-axis value. */
        if (view.top < 10f) {
            /* If the max Y-value is less than 10, show 2 decimal places. */
            formatter.decimalDigitsNumber = 2
        } else {
            /* Otherwise, hide decimal places. */
            formatter.decimalDigitsNumber = 0
        }
        axisY.formatter = formatter
        data.axisYLeft = axisY

        /* Populate the line with the data points. */
        lineChart.lineChartData = data

        /* Adjust the dimensions of the line chart to fit the highest Y-value data point. */
        view = Viewport(lineChart.maximumViewport)
        val padding = view.height() * 0.05f
        view.top = view.top + padding
        view.bottom = view.bottom - padding
        lineChart.maximumViewport = view
        lineChart.currentViewport = view
    }

    /* Purpose: Controller method that populates the pie chart with the selected data set.
    *
    * Parameters: timeSpan represents whether the user selected 0) Last 7 Days, 1) Last 12 Months,
    * or 2) All Time.
    *
    * Returns: Nothing. */
    fun displayPieChart(timeSpan: Int) {
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

        /* Set the colors and percentage labels for each of the category data set slices. */
        val category1Slice = SliceValue(category1Amount, resources.getColor(R.color.colorCategory1))
        category1Slice.setLabel("$category1Amount %")
        values.add(category1Slice)

        val category2Slice = SliceValue(category2Amount, resources.getColor(R.color.colorCategory2))
        category2Slice.setLabel("$category2Amount %")
        values.add(category2Slice)

        val category3Slice = SliceValue(category3Amount, resources.getColor(R.color.colorCategory3))
        category3Slice.setLabel("$category3Amount %")
        values.add(category3Slice)

        /* Populate the pie chart with the colored slices. */
        pieChart.pieChartData = PieChartData(values).setHasLabels(true)
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
        displayLineChart(DataManager.last7Days(model.get(), "all"),
            0, 0)

        /* Populate pie chart with Last 7 Days. */
        displayPieChart(0)

        /* Show the line chart and hide the pie chart. */
        showLineChart()

        /* Set the total and label for Category 1 over the Last 7 Days. */
        category1Text = rootView.findViewById(R.id.category1Text)
        category1Text.text = categories[1]
        category1Total = rootView.findViewById(R.id.category1Total)
        var category1Amount = DataManager.last7DaysTotal(model.get(), "c-1")
        var category1TotalString = "$ " +
                DecimalFormat("#,##0.00").format(category1Amount)
        category1Total.text = category1TotalString

        /* Set the total and label for Category 2 over the Last 7 Days. */
        category2Text = rootView.findViewById(R.id.category2Text)
        category2Text.text = categories[2]
        category2Total = rootView.findViewById(R.id.category2Total)
        var category2Amount = DataManager.last7DaysTotal(model.get(), "c-2")
        var category2TotalString = "$ " +
                DecimalFormat("#,##0.00").format(category2Amount)
        category2Total.text = category2TotalString

        /* Set the total and label for Category 3 over the Last 7 Days. */
        category3Text = rootView.findViewById(R.id.category3Text)
        category3Text.text = categories[3]
        category3Total = rootView.findViewById(R.id.category3Total)
        var category3Amount = DataManager.last7DaysTotal(model.get(), "c-3")
        var category3TotalString = "$ " +
                DecimalFormat("#,##0.00").format(category3Amount)
        category3Total.text = category3TotalString

        /* Set the total for All Categories over the Last 7 Days. */
        allText = rootView.findViewById(R.id.allText)
        allText.text = categories[0]
        allTotal = rootView.findViewById(R.id.allTotal)
        var allAmount = category1Amount + category2Amount + category3Amount
        var allTotalString = "$ " +
                DecimalFormat("#,##0.00").format(allAmount)
        allTotal.text = allTotalString

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
                            } else {
                                DataManager.last12Months(model.get(), "c-" +
                                        spinChartCategory.selectedItemPosition.toString())
                            }

                        /* Update the totals to use the Last 12 Months totals. */
                        category1Amount = DataManager.last12MonthsTotal(model.get(), "c-1")
                        category1TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category1Amount)
                        category1Total.text = category1TotalString

                        category2Amount = DataManager.last12MonthsTotal(model.get(), "c-2")
                        category2TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category2Amount)
                        category2Total.text = category2TotalString

                        category3Amount = DataManager.last12MonthsTotal(model.get(), "c-3")
                        category3TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category3Amount)
                        category3Total.text = category3TotalString

                        allAmount = category1Amount + category2Amount + category3Amount
                        allTotalString = "$ " + DecimalFormat("#,##0.00").format(allAmount)
                        allTotal.text = allTotalString
                    }
                    /* All Time. */
                    2 -> {
                        /* Grab the Last 10 Years of data points from
                        * whichever category the user selected. */
                        data =
                            if(spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last10Years(model.get(), "all")
                            } else {
                                DataManager.last10Years(model.get(), "c-" +
                                        spinChartCategory.selectedItemPosition.toString())
                            }

                        /* Update the totals to use the All Time totals. */
                        category1Amount = DataManager.getValueByID(
                            model.get(), "c-1-t")!!.toDouble()
                        category1TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category1Amount)
                        category1Total.text = category1TotalString

                        category2Amount = DataManager.getValueByID(
                            model.get(), "c-2-t")!!.toDouble()
                        category2TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category2Amount)
                        category2Total.text = category2TotalString

                        category3Amount = DataManager.getValueByID(
                            model.get(), "c-3-t")!!.toDouble()
                        category3TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category3Amount)
                        category3Total.text = category3TotalString

                        allAmount = DataManager.getValueByID(
                            model.get(), "t")!!.toDouble()
                        allTotalString = "$ " + DecimalFormat("#,##0.00").format(allAmount)
                        allTotal.text = allTotalString
                    }
                    /* Last 7 days. */
                    else -> {
                        /* Grab the Last 7 Days of data points from
                        * whichever category the user selected. */
                        data =
                            if(spinChartCategory.selectedItemPosition == 0) {
                                DataManager.last7Days(model.get(), "all")
                            } else {
                                DataManager.last7Days(model.get(), "c-" +
                                        spinChartCategory.selectedItemPosition.toString())
                            }

                        /* Update the totals to use the Last 7 Days totals. */
                        category1Amount = DataManager.last7DaysTotal(model.get(), "c-1")
                        category1TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category1Amount)
                        category1Total.text = category1TotalString

                        category2Amount = DataManager.last7DaysTotal(model.get(), "c-2")
                        category2TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category2Amount)
                        category2Total.text = category2TotalString

                        category3Amount = DataManager.last7DaysTotal(model.get(), "c-3")
                        category3TotalString = "$ " +
                                DecimalFormat("#,##0.00").format(category3Amount)
                        category3Total.text = category3TotalString

                        allAmount = category1Amount + category2Amount + category3Amount
                        allTotalString = "$ " + DecimalFormat("#,##0.00").format(allAmount)
                        allTotal.text = allTotalString
                    }
                }

                /* Update the line chart with the new time interval data set. */
                displayLineChart(data, position,
                    spinChartCategory.selectedItemPosition)

                /* Update the pie chart with the new time interval data set. */
                displayPieChart(position)
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
                displayLineChart(data, timeSpan, position)
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
