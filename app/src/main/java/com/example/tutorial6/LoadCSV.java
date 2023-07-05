package com.example.tutorial6;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;




public class LoadCSV extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_csv);
        Button BackButton =  findViewById(R.id.button_back);
        Button showButton =  findViewById(R.id.show_but);
        LineChart lineChart = findViewById(R.id.line_chart);
        EditText fileNameText = findViewById(R.id.file_Name);
        TextView stepsText = findViewById(R.id.est_steps);
        Context context = LoadCSV.this;


        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = fileNameText.getText().toString().trim();
                File file = new File("/sdcard/csv_dir/" + fileName);
                if (fileName=="") {
                    Toast.makeText(context, "enter required file name!", Toast.LENGTH_SHORT).show();
                }
                if (!file.exists()) {
                    Toast.makeText(context, "The file name doesn't exist!", Toast.LENGTH_SHORT).show();
                }
                else {
                    ArrayList<String[]> csvData;

                    csvData = CsvRead("/sdcard/csv_dir/" + fileName);
                    LineDataSet lineDataSet = new LineDataSet(DataValues(csvData), "N");
                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    String pred = csvData.get(4)[1];
                    stepsText.setText("Estimated number of steps: " +pred);
                    lineDataSet.setColor(Color.GREEN);
                    dataSets.add(lineDataSet);
                    LineData data = new LineData(dataSets);
                    lineChart.setData(data);
                    lineChart.invalidate();
                }
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenLoadCSV();
            }
        });
    }

    private void ClickBack(){
        finish();

    }

    private void OpenLoadCSV(){
        Intent intent = new Intent(context, connect.class);
        startActivity(intent);
    }

    private ArrayList<String[]> CsvRead(String path){
        ArrayList<String[]> CsvData = new ArrayList<>();
        try {
            File file = new File(path);
            CSVReader reader = new CSVReader(new FileReader(file));
            String[]nextline;
            while((nextline = reader.readNext())!= null){
                    CsvData.add(nextline);
            }

        }catch (Exception e){
        }
        return CsvData;
    }

    private ArrayList<Entry> DataValues(ArrayList<String[]> csvData){
        ArrayList<Entry> dataVals = new ArrayList<>();
        for (int i = 8; i < csvData.size(); i++){
            dataVals.add(new Entry(i-8,calc_norm(csvData.get(i))));
        }
        return dataVals;
    }

    private float calc_norm(String[] parts){
//        return 10;
        return (float) Math.sqrt(Math.pow(Float.parseFloat(parts[1]),2)+Math.pow(Float.parseFloat(parts[2]),2)+Math.pow(Float.parseFloat(parts[3]),2));
    }

}