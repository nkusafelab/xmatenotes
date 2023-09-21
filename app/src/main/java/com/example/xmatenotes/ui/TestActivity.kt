//package com.example.xmatenotes.ui
//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import com.example.xmatenotes.R
//import lecho.lib.hellocharts.model.Axis
//import lecho.lib.hellocharts.model.AxisValue
//import lecho.lib.hellocharts.model.Line
//import lecho.lib.hellocharts.model.LineChartData
//import lecho.lib.hellocharts.model.PointValue
//import lecho.lib.hellocharts.view.LineChartView
//
//
//class TestActivity : AppCompatActivity() {
//
//    private lateinit var chart: LineChartView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_test)
//
//        chart = findViewById<View>(R.id.chart) as LineChartView
//
//        // 创建折线图的数据
//
//        // 创建折线图的数据
//        val values: MutableList<PointValue> = ArrayList()
//        values.add(PointValue(0f, 2f))
//        values.add(PointValue(1f, 4f))
//        values.add(PointValue(2f, 3f))
//        values.add(PointValue(3f, 6f))
//        values.add(PointValue(4f, 8f))
//
//        val line = Line(values).setColor(Color.BLUE).setHasPoints(true).setHasLabels(true)
//
//        val lines: MutableList<Line> = ArrayList()
//        lines.add(line)
//
//        val data = LineChartData()
//        data.lines = lines
//
//        // 设置X轴和Y轴的标签
//
//        // 设置X轴和Y轴的标签
//        val axisValuesX: MutableList<AxisValue> = ArrayList()
//        axisValuesX.add(AxisValue(0f).setLabel("0"))
//        axisValuesX.add(AxisValue(1f).setLabel("1"))
//        axisValuesX.add(AxisValue(2f).setLabel("2"))
//        axisValuesX.add(AxisValue(3f).setLabel("3"))
//        axisValuesX.add(AxisValue(4f).setLabel("4"))
//        val axisX = Axis(axisValuesX).setName("X轴")
//
//        val axisY = Axis().setName("Y轴")
//
//        data.axisXBottom = axisX
//        data.axisYLeft = axisY
//
//        // 将数据设置到折线图中
//
//        // 将数据设置到折线图中
//        chart.lineChartData = data
//
//    }
//}