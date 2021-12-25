package com.example.capturethedots

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import kotlin.math.floor

class OthelloGrid (context: Context, attrs: AttributeSet?) : View(context, attrs), GestureDetector.OnGestureListener {

    private var lineColor = Color.DKGRAY
    private var fillColor = Color.rgb(114, 166, 120)
    private var darkDotColor = Color.rgb(33, 36, 33)
    private var lightDotColor = Color.rgb(216, 230, 216)

    private val paint = Paint()
    private val dotPaint = Paint()
    private var shell = Rect()

    private var turn = 1
    private var invalidMove = 0
    private var darkScore = 2
    private var lightScore = 2

    private val startingTiles = arrayOf(arrayOf(0,0,0,0,0,0,0,0),
        arrayOf(0,0,0,0,0,0,0,0),
        arrayOf(0,0,0,0,0,0,0,0),
        arrayOf(0,0,0,2,1,0,0,0),
        arrayOf(0,0,0,1,2,0,0,0),
        arrayOf(0,0,0,0,0,0,0,0),
        arrayOf(0,0,0,0,0,0,0,0),
        arrayOf(0,0,0,0,0,0,0,0))
    private var tilePlacement = startingTiles.copy()

    private var width = 0.0f
    private var height = 0.0f
    private var verticalBuff = 0.0f
    private var horizontalBuff = 0.0f
    private var shapeWidth = 0.0f
    private var cellWidth = 0.0f
    private var halfCell = 0.0f

    private var mDetector = GestureDetectorCompat(this.context, this)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        width = w.toFloat()
        height = h.toFloat()

        if(width > height){
            verticalBuff = 0f
            horizontalBuff = (width - height) / 2
            cellWidth = height / 8
            shapeWidth = height
        }else{
            verticalBuff = (height - width) / 2
            horizontalBuff = 0f
            cellWidth = width / 8
            shapeWidth = width
        }

        halfCell = cellWidth / 2

        shell = Rect(horizontalBuff.toInt(),
            verticalBuff.toInt(),
            (width - horizontalBuff).toInt(),
            (height - verticalBuff).toInt())

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(mDetector.onTouchEvent(event)){
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = fillColor
        canvas?.drawRect(shell, paint)

        paint.color = lineColor
        paint.strokeWidth = 6f
        for(i in 0..8){
            canvas?.drawLine((cellWidth*i) + horizontalBuff, verticalBuff, (cellWidth*i) + horizontalBuff, height-verticalBuff, paint)
            canvas?.drawLine(horizontalBuff, (cellWidth*i) + verticalBuff, width-horizontalBuff, (cellWidth*i) + verticalBuff, paint)
        }

        dotPaint.textAlign = Paint.Align.CENTER
        dotPaint.color = darkDotColor
        dotPaint.textSize = halfCell
        if(turn > 0) {
            canvas?.drawText("Player $turn's turn       Score P1:$darkScore P2:$lightScore", width / 2, verticalBuff - (halfCell / 2), dotPaint)
        }else if(darkScore > lightScore){
            canvas?.drawText("Player 1 wins         Score P1:$darkScore P2:$lightScore", width / 2, verticalBuff - (halfCell / 2), dotPaint)
        }else{
            canvas?.drawText("Player 2 wins         Score P1:$darkScore P2:$lightScore", width / 2, verticalBuff - (halfCell / 2), dotPaint)
        }

        if(invalidMove == 1){
            canvas?.drawText("Invalid Move", width/2, verticalBuff - (halfCell * 1.7f), dotPaint)
        }

        canvas?.drawRoundRect(RectF((width/2)-(cellWidth*2.05f), verticalBuff + shapeWidth + halfCell - (cellWidth*.05f), (width/2)+(cellWidth*2.05f), verticalBuff + shapeWidth + (cellWidth * 1.55f)), halfCell*1.05f, halfCell*1.05f, paint)
        paint.color = fillColor
        canvas?.drawRoundRect(RectF((width/2)-(cellWidth*2), verticalBuff + shapeWidth + halfCell, (width/2)+(cellWidth*2), verticalBuff + shapeWidth + (cellWidth * 1.5f)), halfCell, halfCell, paint)
        canvas?.drawText("Restart", width/2, verticalBuff + shapeWidth + (cellWidth*1.15f), dotPaint)

        for(y in 0..7){
            for(x in 0..7){
                if(tilePlacement[x][y] == 1) {
                    dotPaint.color = darkDotColor
                    canvas?.drawCircle((x * cellWidth) + halfCell + horizontalBuff, (y * cellWidth) + halfCell + verticalBuff, halfCell * .8f, dotPaint)
                }else if(tilePlacement[x][y] == 2){
                    dotPaint.color = lightDotColor
                    canvas?.drawCircle((x * cellWidth) + halfCell + horizontalBuff, (y * cellWidth) + halfCell + verticalBuff, halfCell * .8f, dotPaint)
                }

            }
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
        //TODO("Not yet implemented")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        if(e != null){

            if(e.x > (width/2)-(cellWidth*2.05f) && e.x < (width/2)+(cellWidth*2.05f)
                    && e.y > verticalBuff + shapeWidth + halfCell - (cellWidth*.05f) && e.y < verticalBuff + shapeWidth + (cellWidth * 1.55f)){
                restartGame()
                invalidate()
                return true
            }

            if(turn == 0){return true}

            val col = floor((e.x - horizontalBuff)/cellWidth).toInt()
            val row = floor((e.y - verticalBuff)/cellWidth).toInt()
            if(validateMove(col, row, turn)) {
                invalidMove = 0
                if (turn == 1) {//place appropriate tile
                    tilePlacement[col][row] = 1
                } else {
                    tilePlacement[col][row] = 2
                }

                turn = (turn%2) + 1
                if(!checkForMoves(turn)){
                    turn = 0
                }
            }else{
                invalidMove = 1
            }
        }
        invalidate()
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        //TODO("Not yet implemented")
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    private fun validateMove(x: Int, y: Int, turn: Int): Boolean{
        val otherTurn = (turn%2) + 1
        var flag = 0

        if(tilePlacement[x][y] != 0){
            return false
        }

        if(x > 1){
            if(tilePlacement[x - 1][y] == otherTurn && tilePlacement[x - 2][y] == turn){
                flag++
                tilePlacement[x - 1][y] = turn
            }
        }
        if(x < 6){
            if(tilePlacement[x + 1][y] == otherTurn && tilePlacement[x + 2][y] == turn){
                flag++
                tilePlacement[x + 1][y] = turn
            }
        }
        if(y > 1){
            if(tilePlacement[x][y - 1] == otherTurn && tilePlacement[x][y - 2] == turn){
                flag++
                tilePlacement[x][y - 1] = turn
            }
        }
        if(y < 6){
            if(tilePlacement[x][y + 1] == otherTurn && tilePlacement[x][y + 2] == turn){
                flag++
                tilePlacement[x][y + 1] = turn
            }
        }

        if(turn == 1 && flag > 0){
            darkScore += (1 + flag)
            lightScore -= flag
        }else if(flag > 0){
            lightScore += (1 + flag)
            darkScore -= flag
        }

        if(flag > 0){ return true }
        return false
    }

    fun Array<Array<Int>>.copy() = Array(size) { get(it).clone() }

    private fun restartGame(){
        turn = 1
        lightScore = 2
        darkScore = 2
        tilePlacement = startingTiles.copy()
        invalidMove = 0
    }

    private fun checkForMoves(player: Int): Boolean{

        for(y in 0..7) {
            for (x in 0..7) {
                if(tilePlacement[x][y] == player){
                    if(hasMove(x, y, player)){
                       return true
                    }
                }
            }
        }

        return false
    }

    private fun hasMove(x: Int, y: Int, player: Int): Boolean{
        val otherTurn = (player%2) + 1

        if(x > 1){
            if(tilePlacement[x - 1][y] == otherTurn && tilePlacement[x - 2][y] == 0){
                return true
            }
        }
        if(x < 6){
            if(tilePlacement[x + 1][y] == otherTurn && tilePlacement[x + 2][y] == 0){
                return true
            }
        }
        if(y > 1){
            if(tilePlacement[x][y - 1] == otherTurn && tilePlacement[x][y - 2] == 0){
                return true
            }
        }
        if(y < 6){
            if(tilePlacement[x][y + 1] == otherTurn && tilePlacement[x][y + 2] == 0){
                return true
            }
        }

        return false
    }
}
