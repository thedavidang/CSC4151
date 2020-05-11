package com.team4.walletwatch

import android.graphics.Color
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
import lecho.lib.hellocharts.view.*
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Month
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun displayLineChart(total: DoubleArray, timeSpan: String) {
        val cal: Calendar = Calendar.getInstance()
        if(timeSpan == "byDay") {
            var view = Viewport(lineChart.maximumViewport)
            view.top = view.top + view.height() * 0.05f

            val values = ArrayList<PointValue>()
            var h = 6
            for (i in 0..6) {
                values.add(PointValue(i.toFloat(), total[h].toFloat()))
                h--
            }

            val line = Line(values).setColor(Color.BLACK)
            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData()
            data.lines = lines
            val axisValues = ArrayList<AxisValue>()

            var currentDay = Calendar.DAY_OF_WEEK
            var currentDayString = "string"
            var j = 6;
            for (i  in 1..7) {
                if (currentDay == 1) {
                    currentDayString = "Mon"
                }
                else if(currentDay == 2) {
                    currentDayString = "Tue"
                }
                else if(currentDay == 3) {
                    currentDayString = "Wed"
                }
                else if(currentDay == 4) {
                    currentDayString = "Thu"
                }
                else if(currentDay == 5) {
                    currentDayString = "Fri"
                }
                else if(currentDay == 6) {
                    currentDayString = "Sat"
                }
                else if(currentDay == 7) {
                    currentDayString = "Sun"
                }
                axisValues.add(AxisValue(j.toFloat(), currentDayString.toCharArray()))
                currentDay = currentDay - 1
                if(currentDay == 0) {
                    currentDay = 7
                }
                j--
            }

            val axisX = Axis(axisValues).setHasLines(true)
            axisX.maxLabelChars = 4
            data.axisXBottom = axisX

            val axisY = Axis().setHasLines(true)
            val formatter = SimpleAxisValueFormatter()
                if (view.top < 10f) {
                formatter.decimalDigitsNumber = 2
            }
            else {
                formatter.decimalDigitsNumber = 0
            }
            axisY.formatter = formatter
            data.axisYLeft = axisY

            lineChart.lineChartData = data

            view = Viewport(lineChart.maximumViewport)
            val padding = view.height() * 0.05f
            view.top = view.top + padding
            view.bottom = view.bottom - padding
            lineChart.maximumViewport = view
            lineChart.currentViewport = view
        }

        else if(timeSpan == "byMonth") {
            var view = Viewport(lineChart.maximumViewport)
            view.top = view.top + view.height() * 0.05f

            val values = ArrayList<PointValue>()
            var h = 11
            for (i in 0..11) {
                values.add(PointValue(i.toFloat(), total[h].toFloat()))
                h--
            }

            val line = Line(values).setColor(Color.BLACK)
            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData()
            data.lines = lines
            val axisValues = ArrayList<AxisValue>()

            var currentMonth = cal.get(Calendar.MONTH)
            var currentMonthString = "string"
            var j = 11
            for (i  in 1..12) {
                if (currentMonth == 0) {
                    currentMonthString = "Jan"
                }
                else if(currentMonth == 1) {
                    currentMonthString = "Feb"
                }
                else if(currentMonth == 2) {
                    currentMonthString = "Mar"
                }
                else if(currentMonth == 3) {
                    currentMonthString = "Apr"
                }
                else if(currentMonth == 4) {
                    currentMonthString = "May"
                }
                else if(currentMonth == 5) {
                    currentMonthString = "Jun"
                }
                else if(currentMonth == 6) {
                    currentMonthString = "Jul"
                }
                else if(currentMonth == 7) {
                    currentMonthString = "Aug"
                }
                else if(currentMonth == 8) {
                    currentMonthString = "Sep"
                }
                else if(currentMonth == 9) {
                    currentMonthString = "Oct"
                }
                else if(currentMonth == 10) {
                    currentMonthString = "Nov"
                }
                else if(currentMonth == 11) {
                    currentMonthString = "Dec"
                }
                axisValues.add(AxisValue(j.toFloat(), currentMonthString.toCharArray()))
                currentMonth = currentMonth - 1
                if(currentMonth == -1) {
                    currentMonth = 11
                }
                j--
            }

            val axisX = Axis(axisValues).setHasLines(true)
            axisX.maxLabelChars = 4
            data.axisXBottom = axisX

            val axisY = Axis().setHasLines(true)
            val formatter = SimpleAxisValueFormatter()
            if (view.top < 10f) {
                formatter.decimalDigitsNumber = 2
            }
            else {
                formatter.decimalDigitsNumber = 0
            }
            axisY.formatter = formatter
            data.axisYLeft = axisY

            lineChart.lineChartData = data

            view = Viewport(lineChart.maximumViewport)
            val padding = view.height() * 0.05f
            view.top = view.top + padding
            view.bottom = view.bottom - padding
            lineChart.maximumViewport = view
            lineChart.currentViewport = view
        }
        else if(timeSpan == "byYear") {
            var view = Viewport(lineChart.maximumViewport)
            view.top = view.top + view.height() * 0.05f

            val values = ArrayList<PointValue>()
            var h = 9
            for (i in 0..9) {
                values.add(PointValue(i.toFloat(), total[h].toFloat()))
                h--
            }

            val line = Line(values).setColor(Color.BLACK)
            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData()
            data.lines = lines
            val axisValues = ArrayList<AxisValue>()

            var currentYear = cal.get(Calendar.YEAR)
            var j = 9;
            for (i  in 1..10) {
                axisValues.add(AxisValue(j.toFloat(), currentYear.toString().toCharArray()))
                currentYear = currentYear - 1
                if(currentYear == 0) {
                    currentYear = 9999
                }
                j--
            }

            val axisX = Axis(axisValues).setHasLines(true)
            axisX.maxLabelChars = 4
            data.axisXBottom = axisX

            val axisY = Axis().setHasLines(true)
            val formatter = SimpleAxisValueFormatter()
            if (view.top < 10f) {
                formatter.decimalDigitsNumber = 2
            }
            else {
                formatter.decimalDigitsNumber = 0
            }
            axisY.formatter = formatter
            data.axisYLeft = axisY

            lineChart.lineChartData = data

            view = Viewport(lineChart.maximumViewport)
            val padding = view.height() * 0.05f
            view.top = view.top + padding
            view.bottom = view.bottom - padding
            lineChart.maximumViewport = view
            lineChart.currentViewport = view
        }


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tab2, container, false)
        main = activity as MainActivity
        model = main.model

        val categories = DataManager.getCategories(model.get())

        lineChart = rootView.findViewById(R.id.chartView) as LineChartView

        category1Text = rootView.findViewById(R.id.category1Text)
        category1Text.text = categories[1]
        category1Total = rootView.findViewById(R.id.category1Total)
        var category1Amount = DataManager.last7DaysTotal(model.get(), "c-1")
        var category1TotalString = "$ " +
                DecimalFormat("0.00").format(category1Amount)
        category1Total.text = category1TotalString

        category2Text = rootView.findViewById(R.id.category2Text)
        category2Text.text = categories[2]
        category2Total = rootView.findViewById(R.id.category2Total)
        var category2Amount = DataManager.last7DaysTotal(model.get(), "c-2")
        var category2TotalString = "$ " +
                DecimalFormat("0.00").format(category2Amount)
        category2Total.text = category2TotalString

        category3Text = rootView.findViewById(R.id.category3Text)
        category3Text.text = categories[3]
        category3Total = rootView.findViewById(R.id.category3Total)
        var category3Amount = DataManager.last7DaysTotal(model.get(), "c-3")
        var category3TotalString = "$ " +
                DecimalFormat("0.00").format(category3Amount)
        category3Total.text = category3TotalString

        allText = rootView.findViewById(R.id.allText)
        allText.text = categories[0]
        allTotal = rootView.findViewById(R.id.allTotal)
        var allAmount = category1Amount + category2Amount + category3Amount
        var allTotalString = "$ " +
                DecimalFormat("0.00").format(allAmount)
        allTotal.text = allTotalString

        spinChartType = rootView.findViewById(R.id.chartTypeSpinner)
        val adapterChartType= ArrayAdapter(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.chartTypes))
        spinChartType.adapter = adapterChartType

        spinTimeInterval = rootView.findViewById(R.id.lineIntervalSpinner)
        val adapterTimeInterval = ArrayAdapter(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.chartIntervals))
        spinTimeInterval.adapter = adapterTimeInterval
        spinTimeInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                when (position) {
                    // last 12 months
                    1 -> {
                        var months:DoubleArray
                        if(spinChartCategory.selectedItemPosition == 0) {
                             months = DataManager.last12Months(model.get(), "all")
                        }
                        else {
                             months = DataManager.last12Months(model.get(), "c-" + spinChartCategory.selectedItemPosition.toString())
                        }
                        displayLineChart(months, "byMonth")
                        category1Amount = DataManager.last12MonthsTotal(model.get(), "c-1")
                        category1TotalString = "$ " +
                                DecimalFormat("0.00").format(category1Amount)
                        category1Total.text = category1TotalString

                        category2Amount = DataManager.last12MonthsTotal(model.get(), "c-2")
                        category2TotalString = "$ " +
                                DecimalFormat("0.00").format(category2Amount)
                        category2Total.text = category2TotalString

                        category3Amount = DataManager.last12MonthsTotal(model.get(), "c-3")
                        category3TotalString = "$ " +
                                DecimalFormat("0.00").format(category3Amount)
                        category3Total.text = category3TotalString

                        allAmount = category1Amount + category2Amount + category3Amount
                        allTotalString = "$ " + DecimalFormat("0.00").format(allAmount)
                        allTotal.text = allTotalString
                    }
                    // all-time
                    2 -> {
                        var years: DoubleArray
                        if(spinChartCategory.selectedItemPosition == 0) {
                            years = DataManager.last10Years(model.get(), "all")

                        }
                        else {
                            years = DataManager.last10Years(model.get(), "c-" + spinChartCategory.selectedItemPosition.toString())
                        }
                        displayLineChart(years, "byYear")
                        category1Amount = DataManager.getValueByID(
                            model.get(), "c-1-t")!!.toDouble()
                        category1TotalString = "$ " +
                                DecimalFormat("0.00").format(category1Amount)
                        category1Total.text = category1TotalString

                        category2Amount = DataManager.getValueByID(
                            model.get(), "c-2-t")!!.toDouble()
                        category2TotalString = "$ " +
                                DecimalFormat("0.00").format(category2Amount)
                        category2Total.text = category2TotalString

                        category3Amount = DataManager.getValueByID(
                            model.get(), "c-3-t")!!.toDouble()
                        category3TotalString = "$ " +
                                DecimalFormat("0.00").format(category3Amount)
                        category3Total.text = category3TotalString

                        allAmount = DataManager.getValueByID(
                            model.get(), "t")!!.toDouble()
                        allTotalString = "$ " + DecimalFormat("0.00").format(allAmount)
                        allTotal.text = allTotalString
                    }
                    // last 7 days
                    else -> {
                        var days: DoubleArray
                        if(spinChartCategory.selectedItemPosition == 0) {
                             days = DataManager.last7Days(model.get(), "all")
                        }
                        else {
                             days = DataManager.last7Days(model.get(), "c-" + spinChartCategory.selectedItemPosition.toString())
                        }
                        displayLineChart(days, "byDay")
                        category1Amount = DataManager.last7DaysTotal(model.get(), "c-1")
                        category1TotalString = "$ " +
                                DecimalFormat("0.00").format(category1Amount)
                        category1Total.text = category1TotalString

                        category2Amount = DataManager.last7DaysTotal(model.get(), "c-2")
                        category2TotalString = "$ " +
                                DecimalFormat("0.00").format(category2Amount)
                        category2Total.text = category2TotalString

                        category3Amount = DataManager.last7DaysTotal(model.get(), "c-3")
                        category3TotalString = "$ " +
                                DecimalFormat("0.00").format(category3Amount)
                        category3Total.text = category3TotalString

                        allAmount = category1Amount + category2Amount + category3Amount
                        allTotalString = "$ " + DecimalFormat("0.00").format(allAmount)
                        allTotal.text = allTotalString
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        spinChartCategory = rootView.findViewById(R.id.lineCategorySpinner)
        val adapterChartCategory = ArrayAdapter<String?>(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            categories)
        spinChartCategory.adapter = adapterChartCategory

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
