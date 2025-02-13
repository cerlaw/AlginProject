package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 实现原理：通过StaticLayout来获取一行能展示多少的字符，
 * 然后计算剩余的宽度，进行绘制；
 *
 * 当绘制过程比较完美时，是由于计算的剩余宽度较小，看不出来效果；
 *
 * 缺点：当存在单词较少时，会存在间隙过大的问题，比如第一行只有两个单词时，会出现间距过大的问题
 *
 */
public class AlignTextView extends AppCompatTextView {

    private boolean alignOnlyOneLine;

    public AlignTextView(Context context) {
        this(context, null);
    }

    public AlignTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlignTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private final String dot = "...";
    private final TextPaint textPaint = new TextPaint();
    private float dotWidth;

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlignTextView);
        alignOnlyOneLine = typedArray.getBoolean(R.styleable.AlignTextView_alignOnlyOneLine, false);
        typedArray.recycle();
        setTextColor(getCurrentTextColor());
        textPaint.setTextSize(getTextSize());
        dotWidth = textPaint.measureText(dot);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        getPaint().setColor(color);
    }

    protected void onDraw(Canvas canvas) {
        CharSequence content = getText();
        if (!(content instanceof String)) {
            super.onDraw(canvas);
            return;
        }
        String text = (String) content;
        Layout layout = getLayout();

        for (int i = 0; i < layout.getLineCount(); ++i) {
            int lineBaseline = layout.getLineBaseline(i) + getPaddingTop();
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            if (alignOnlyOneLine && layout.getLineCount() == 1) {
                // 只有一行
                String line = text.substring(lineStart, lineEnd);
                float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
                this.drawScaledText(canvas, line, lineBaseline, width);
            } else if (i == layout.getLineCount() - 1) {
                // 最后一行
                //int ellipsisStart = layout.get(i);
                //String s = text.substring(lineStart, ellipsisStart);
                String display = getLastRowString(text, lineStart);
                canvas.drawText(display, getPaddingLeft(), lineBaseline, getPaint());
                break;
            } else { //中间行
                String line = text.substring(lineStart, lineEnd);
                float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
                this.drawScaledText(canvas, line, lineBaseline, width);
            }
        }

    }

    private String getLastRowString(String text, int lineStart) {
        String display = text.substring(lineStart);
        if (textPaint.measureText(display) > getWidth()) {
            if (getEllipsize() == TextUtils.TruncateAt.END) {
                while (textPaint.measureText(display) >= (getWidth() - dotWidth)) {
                    display = display.substring(0, display.length() - 1);
                }
                display += dot;
            }
        }
        return display;
    }

    private void drawScaledText(Canvas canvas, String line, float baseLineY, float lineWidth) {
        if (line.length() < 1) {
            return;
        }
        float x = getPaddingLeft();
        boolean forceNextLine = line.charAt(line.length() - 1) == 10;
        int length = line.length() - 1;
        if (forceNextLine || length == 0) {
            canvas.drawText(line, x, baseLineY, getPaint());
            return;
        }

        float d = (getMeasuredWidth() - lineWidth - getPaddingLeft() - getPaddingRight()) / length;

        for (int i = 0; i < line.length(); ++i) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, this.getPaint());
            canvas.drawText(c, x, baseLineY, this.getPaint());
            x += cw + d;
        }
    }
}
