package com.zgh.triganglelayoutdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zgh.triganglelayoutdemo.view.TriangleLayout;

public class MainActivity extends AppCompatActivity {
    TriangleLayout triangleLayout;
    int itemSize = 0;
    TextView tv_step;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        triangleLayout = $(R.id.triangleLayout);
        tv_step = $(R.id.tv_step);
        $(R.id.btn_add_view).setOnClickListener(v -> {
            Button button = new Button(this);
            button.setText(itemSize + "");
            triangleLayout.addView(button);
            itemSize++;
        });
        $(R.id.btn_remove_view).setOnClickListener(v -> {
            if (itemSize > 0) {
                triangleLayout.removeView(triangleLayout.getChildAt(itemSize - 1));
                itemSize--;
            }
        });
        $(R.id.btn_change_orientation).setOnClickListener(v -> {
            triangleLayout.setRegularTriangle(!triangleLayout.isRegularTriangle());
        });
        $(R.id.btn_change_to_trapezoid).setOnClickListener(v -> {
            if (triangleLayout.getMaxLineItemSize()== TriangleLayout.AUTO_MAX) {
                int max = (itemSize + triangleLayout.getStep()) / 2;
                triangleLayout.setMaxLineItemSize(max);
            } else {
                triangleLayout.setMaxLineItemSize(TriangleLayout.AUTO_MAX);
            }
        });
        $(R.id.btn_step_increase).setOnClickListener(v -> {
            triangleLayout.setStep(triangleLayout.getStep() + 1);
            tv_step.setText("step=" + triangleLayout.getStep());
        });
        $(R.id.btn_step_reduce).setOnClickListener(v -> {
            triangleLayout.setStep(triangleLayout.getStep() - 1);
            tv_step.setText("step=" + triangleLayout.getStep());
        });
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T $(int id) {
        return (T) findViewById(id);
    }
}
