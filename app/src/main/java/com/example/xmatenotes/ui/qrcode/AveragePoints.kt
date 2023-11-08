package com.example.xmatenotes.ui.qrcode

import org.opencv.core.Point

class AveragePoints {

    companion object {
        const val TAG = "AveragePoint"
    }

    private var dotList = ArrayList<ArrayList<Point>>()

    fun addPoints(points: ArrayList<Point>){
        if(dotList.size != points.size){
            clear()
            for (i in 0 until points.size){
                dotList.add(ArrayList<Point>())
            }
        }
        for (i in 0 until points.size){
            dotList[i].add(points[i])
        }
    }

    fun calculateAvgPoint(): ArrayList<Point>{
        var points = ArrayList<Point>()
        for (i in 0 until dotList.size){
            points.add(calculateAvgPoint(dotList[i]))
        }
        return points;
    }

    private fun calculateAvgPoint(points: ArrayList<Point>): Point {
        var point = Point(0.0, 0.0)
        for (i in 0 until points.size){
            point.x += points[i].x
            point.y += points[i].y
        }
        point.x = point.x / points.size
        point.y = point.y / points.size

        return point
    }

    fun clear(){
        dotList.clear()
    }
}