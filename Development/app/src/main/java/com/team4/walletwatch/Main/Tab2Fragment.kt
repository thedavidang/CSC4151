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

    fun displayLineChart(days: DoubleArray) {
        var view = Viewport(lineChart.maximumViewport)
        view.top = view.top + view.height() * 0.05f

        val values = ArrayList<PointValue>()

        values.add(PointValue(0f, days[0].toFloat()))
        values.add(PointValue(1f, days[1].toFloat()))
        values.add(PointValue(2f, days[2].toFloat()))
        values.add(PointValue(3f, days[3].toFloat()))
        values.add(PointValue(4f, days[4].toFloat()))
        values.add(PointValue(5f, days[5].toFloat()))
        values.add(PointValue(6f, days[6].toFloat()))

        val line = Line(values).setColor(Color.BLACK)
        val lines = ArrayList<Line>()
        lines.add(line)

        val data = LineChartData()
        data.lines = lines

        val axisValues = ArrayList<AxisValue>()
        axisValues.add(AxisValue(0f, "Sun".toCharArray()))
        axisValues.add(AxisValue(1f, "Mon".toCharArray()))
        axisValues.add(AxisValue(2f, "Tue".toCharArray()))
        axisValues.add(AxisValue(3f, "Wed".toCharArray()))
        axisValues.add(AxisValue(4f, "Thu".toCharArray()))
        axisValues.add(AxisValue(5f, "Fri".toCharArray()))
        axisValues.add(AxisValue(6f, "Sat".toCharArray()))

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
                        if(spinChartCategory.selectedItemPosition == 0) {
                            var months = DataManager.last12Months(model.get(), "all")
                        }
                        else {
                            var months = DataManager.last12Months(model.get(), "c-" + spinChartCategory.selectedItemPosition.toString())
                        }
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
                        displayLineChart(days)
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
