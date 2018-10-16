package com.yzmc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;

public class Order_Success extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_success);
        AllActivity.addActivity(this);
        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        TextView textView = findViewById(R.id.text);
        textView.setText(text);

        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Order_Success.this, Main.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Order_Success.this, Main.class);
        startActivity(intent);
    }
}
