package com.bodekjan.tankmarbiya.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by bodekjan on 2016/6/17.
 */
public class MyButton extends Button {
    MyOnClickListener myOnClickListener;
    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public interface MyOnClickListener{
        void onPressed();
        void onNortmal();
    }
    public void setMyOnClickListener(MyOnClickListener myOnClickListener1){
        this.myOnClickListener=myOnClickListener1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                this.myOnClickListener.onPressed();
                break;
            case MotionEvent.ACTION_UP:
                this.myOnClickListener.onNortmal();
                break;
        }
        return super.onTouchEvent(event);
    }
}
