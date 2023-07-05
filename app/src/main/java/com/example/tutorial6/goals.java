package com.example.tutorial6;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class goals extends AppCompatActivity {

    private TextView tvCurrentGoal;
    private EditText etThreshold;
    private RadioGroup rgGoalDirection;
    private ImageButton btnUpdateGoal;
    private TextView tvGoalSummary;
    private ImageButton menuButton;
    private LinearLayout menuLayout;
    private boolean isMenuVisible = false;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        // Get the username from the intent
        userName = getIntent().getStringExtra("username");
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        etThreshold = findViewById(R.id.etThreshold);
        rgGoalDirection = findViewById(R.id.rgGoalDirection);
        btnUpdateGoal = findViewById(R.id.btnUpdateGoal);
        tvGoalSummary = findViewById(R.id.tvGoalSummary);

        // Set the current goal based on the CSV file
        String[] currentGoal = getCurrentGoalFromCSV();
//        /Toast.makeText(this, userName, Toast.LENGTH_SHORT).show();
        if (currentGoal != null) {
            tvCurrentGoal.setText("Current Goal:\t " + currentGoal[2] + " then " + currentGoal[1] + "  calories.");
        }

        btnUpdateGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGoal();
                calculateGoalSummary();
            }
        });
        menuButton = findViewById(R.id.imageButtonMenu);
        menuLayout = findViewById(R.id.menuLayout);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuVisible) {
                    menuLayout.setVisibility(View.GONE);
                    isMenuVisible = false;
                } else {
                    menuLayout.setVisibility(View.VISIBLE);
                    isMenuVisible = true;
                }
            }
        });

        ImageButton buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ConnectActivity
                Intent intent = new Intent(goals.this, connect.class);
                startActivity(intent);
            }
        });

        ImageButton buttonAddMeal = findViewById(R.id.buttonAddMeal);
        buttonAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddMealActivity
                Intent intent = new Intent(goals.this, add_meal.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonDailyAnalysis = findViewById(R.id.buttonDailyAnalysis);
        buttonDailyAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DailyAnalysisActivity
                Intent intent = new Intent(goals.this, daily_anlysis.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonInsertDetails = findViewById(R.id.buttonInsertDetails);
        buttonInsertDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to InsertDetailsActivity
                Intent intent = new Intent(goals.this, details_insert.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });
        calculateGoalSummary();
    }

    private String[] getCurrentGoalFromCSV() {
        String filePath = getFilesDir().getPath() + "/" + userName + ".csv";
        File csvFile = new File(filePath);
        if (csvFile.exists()) {
            try {
                CSVReader reader = new CSVReader(new FileReader(csvFile));
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length >= 2 && nextLine[0].equals("Goal")) {
                        return nextLine;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void updateGoal() {
        String newGoal = etThreshold.getText().toString();
        int selectedDirection = rgGoalDirection.getCheckedRadioButtonId();
        String direction = "";

        if (selectedDirection == R.id.rbLower) {
            direction = "Lower";
        } else if (selectedDirection == R.id.rbHigher) {
            direction = "Higher";
        }

        String filePath = "/sdcard/csv_dir/" + userName + ".csv";
        File csvFile = new File(filePath);

        if (csvFile.exists()) {
            try {
                CSVReader reader = new CSVReader(new FileReader(csvFile));
                String[] lines;
                StringBuilder csvData = new StringBuilder();


                while((lines = reader.readNext()) != null) {
                            if (lines[0] == "Goal") {
                                csvData.append("Goal,").append(newGoal).append(",").append(direction).append("\n");
                            }
                            else{
                                csvData.append(lines).append("\n");
                            }
                }
                FileWriter writer = new FileWriter(csvFile);
                writer.write(csvData.toString());
                writer.flush();
                writer.close();
                reader.close();
                tvCurrentGoal.setText("Current Goal:" + direction + " then " + newGoal + "calories");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateGoalSummary() {
        String filePath = "/sdcard/csv_dir/";
        File directory = new File(filePath);
        File[] files = directory.listFiles();

        int goalCount = 0;
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith(userName) && fileName.endsWith(".csv")) {
                    try {
                        CSVReader reader = new CSVReader(new FileReader(file));
                        String[] nextLine;
                        while ((nextLine = reader.readNext()) != null) {
                            if (nextLine.length == 2 && nextLine[0].equals("calorieBalance")) {
                                float balance = Float.parseFloat(nextLine[1]);
                                String[] goal = getCurrentGoalFromCSV();
                                if (goal != null && balance!=0) {
                                    float threshold = Float.parseFloat(goal[1]);
                                    String direction = goal[2];
                                    if ((direction.equals("Lower") && balance <= threshold) ||
                                            (direction.equals("Higher") && balance >= threshold)) {
                                        goalCount++;
                                    }
                                }
                            }
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        tvGoalSummary.setText("Number of days within goal: " + goalCount);
    }
}
