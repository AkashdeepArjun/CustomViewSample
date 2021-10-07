package com.example.customviewsample

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import java.math.MathContext
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


private enum class FanSpeed(val label:Int){
    OFF(R.string.off),
    LOW(R.string.low),
    MEDIUM(R.string.medium),
    HIGH(R.string.high);

    fun next()= when(this){
        OFF->LOW
        LOW->MEDIUM
        MEDIUM->HIGH
        HIGH->OFF
    }


}

private const val RADIUS_OFFSET_LABEL=30
private const val RADIUS_OFFSET_INDICATOR=-35

class Dialer @JvmOverloads constructor(
    context: Context,
    attrs:AttributeSet?=null,
    defStyleAttr:Int=0
) :View(context,attrs,defStyleAttr){

    private var radius=0.0f
    private var fanspeed=FanSpeed.OFF
    private val pointPosition:PointF=PointF(0.0f,0.0f)

    private var lowSpeedColor=0
    private var mediumSpeedColor=0
    private var highSpeedColor=0

private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    .apply {
        style=Paint.Style.FILL
        textAlign=Paint.Align.CENTER
        textSize=55.0f
        typeface= Typeface.create("",Typeface.BOLD_ITALIC)

    }

    init {
        isClickable=true
        context.withStyledAttributes(attrs,R.styleable.Dialer){
            lowSpeedColor=getColor(R.styleable.Dialer_fan_color1,0)
            mediumSpeedColor=getColor(R.styleable.Dialer_fan_color2,0)
            highSpeedColor=getColor(R.styleable.Dialer_fan_color3,0)
        }
        updateContentDiscription()
        ViewCompat.setAccessibilityDelegate(this,object :AccessibilityDelegateCompat(){
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick=AccessibilityNodeInfoCompat.AccessibilityActionCompat(AccessibilityNodeInfo.ACTION_CLICK,context.getString(if(fanspeed!=FanSpeed.HIGH) R.string.change else R.string.reset))
                info.addAction(customClick)
            }
        })
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
        radius=(min(width,height) /2.0*0.8).toFloat()
    }

    private fun PointF.computeXYForSpeed(speed:FanSpeed,radius:Float){

        val start_angle=Math.PI*(9/8.0)
        val angle=start_angle+speed.ordinal*(Math.PI/4)
        x=((radius* cos(angle)).toFloat())+width/2
        y=((radius* sin(angle)).toFloat())+height/2


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color=when(fanspeed){
            FanSpeed.OFF->Color.GRAY
            FanSpeed.LOW->lowSpeedColor
            FanSpeed.MEDIUM->mediumSpeedColor
            FanSpeed.HIGH->highSpeedColor
        }
        canvas.drawCircle((width/2).toFloat(),(height/2).toFloat(),radius,paint)
        val marker_radius=radius+ RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanspeed,marker_radius)
        paint.color=Color.BLACK
        canvas.drawCircle(pointPosition.x,pointPosition.y,radius/12,paint)

        val label_radius=radius+ RADIUS_OFFSET_LABEL
        for(value in FanSpeed.values()){
            pointPosition.computeXYForSpeed(value,label_radius)
            val label=resources.getString(value.label)
            canvas.drawText(label,pointPosition.x,pointPosition.y,paint)
        }
    }

    override fun performClick(): Boolean {

        if(super.performClick()) return true
        fanspeed=fanspeed.next()
        updateContentDiscription()
        invalidate()
        return true
    }

    fun updateContentDiscription(){
        contentDescription=resources.getString(fanspeed.label)
    }
}