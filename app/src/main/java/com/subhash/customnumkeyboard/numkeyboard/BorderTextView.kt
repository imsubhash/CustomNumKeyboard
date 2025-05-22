package com.subhash.customnumkeyboard.numkeyboard



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat
import com.subhash.customnumkeyboard.R

class BorderTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    private val rect = Rect()
    private var borderTop: Boolean = false
    private var borderLeft: Boolean = false
    private var borderBottom: Boolean = false
    private var borderRight: Boolean = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BorderTextView, 0, 0).apply {
            try {
                borderTop = getBoolean(R.styleable.BorderTextView_borderTop, false)
                borderLeft = getBoolean(R.styleable.BorderTextView_borderLeft, false)
                borderBottom = getBoolean(R.styleable.BorderTextView_borderBottom, false)
                borderRight = getBoolean(R.styleable.BorderTextView_borderRight, false)

                paint.color = getColor(
                    R.styleable.BorderTextView_tvBorderColor,
                    ContextCompat.getColor(context, R.color.black)
                )
                paint.strokeWidth = getDimension(R.styleable.BorderTextView_borderWidth, 4f)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        getDrawingRect(rect)

        if (borderTop) canvas.drawLine(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.top.toFloat(),
            paint
        )
        if (borderLeft) canvas.drawLine(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.left.toFloat(),
            rect.bottom.toFloat(),
            paint
        )
        if (borderBottom) canvas.drawLine(
            rect.left.toFloat(),
            rect.bottom.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            paint
        )
        if (borderRight) canvas.drawLine(
            rect.right.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            paint
        )
    }
}