package com.example.tutorial6;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class daily_anlysis extends AppCompatActivity {

    private TextView tvActivitySummary;
    private TextView tvFoodSummary;
    private TextView tvCaloriesBalance;
    String userName;
    private ImageButton menuButton;
    private LinearLayout menuLayout;
    private String chosenDate;
    private boolean isMenuVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_anlysis);
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("username");
            // Use the receivedData as needed
        }
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        chosenDate = dateFormat.format(currentDate);

        tvActivitySummary = findViewById(R.id.tvActivitySummary);
        tvFoodSummary = findViewById(R.id.tvFoodSummary);
        tvCaloriesBalance = findViewById(R.id.tvCaloriesBalance);

        // Read data from CSV and update the summary sections
        float caloriesOut = updateActivitySummary(userName);
        float caloriesIn = updateFoodSummary(userName);
        boolean IsPosBalance = updateCaloriesBalance(caloriesOut, caloriesIn, userName);

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

        ImageButton buttonChooseDate = findViewById(R.id.buttonChooseDate);
        buttonChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        ImageButton buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ConnectActivity
                Intent intent = new Intent(daily_anlysis.this, connect.class);
                startActivity(intent);
            }
        });

        ImageButton buttonAddMeal = findViewById(R.id.buttonAddMeal);
        buttonAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddMealActivity
                Intent intent = new Intent(daily_anlysis.this, add_meal.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonDailyAnalysis = findViewById(R.id.buttonDailyAnalysis);
        buttonDailyAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DailyAnalysisActivity
                Intent intent = new Intent(daily_anlysis.this, daily_anlysis.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonInsertDetails = findViewById(R.id.buttonInsertDetails);
        buttonInsertDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to InsertDetailsActivity
                Intent intent = new Intent(daily_anlysis.this, details_insert.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

    }

    private void showDatePicker() {
        // Create a DatePickerDialog and set the initial date to the current date
        DatePickerDialog datePickerDialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            datePickerDialog = new DatePickerDialog(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Update the summary based on the selected date
                    if (monthOfYear < 9 && dayOfMonth <= 9) {
                        chosenDate = year + "-0" + (monthOfYear + 1) + "-0" + dayOfMonth;
                    } else if (monthOfYear < 9) {
                        chosenDate = year + "-0" + (monthOfYear + 1) + "-" + dayOfMonth;
                    } else if (dayOfMonth <= 9) {
                        chosenDate = year + "-" + (monthOfYear + 1) + "-0" + dayOfMonth;
                    } else {
                        chosenDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    }
                    float caloriesOut = updateActivitySummary(userName);
                    float caloriesIn = updateFoodSummary(userName);
                    boolean isPosBalance = updateCaloriesBalance(caloriesOut, caloriesIn, userName);
                }
            });
        }

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private float updateActivitySummary(String userName) {
        // Read the data from the CSV file for activity summary
        float stepsWalked = 0;
        float caloriesBurntWalking = 0;
        float stepsRun = 0;
        float caloriesBurntRunning = 0;
        float dailyCalorieBurn = 0;
        float naturldailyCalorieBurn = 0;

        String directoryPath = "/sdcard/csv_dir/";
        String fileName = userName + "_" + chosenDate + ".csv";
        File file = new File(directoryPath, fileName);

        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] line;
            int i = 0;
            while ((line = reader.readNext()) != null) {
                float value = Float.parseFloat(line[1]);
                if (i == 0) {
                    stepsWalked += value;
                    caloriesBurntWalking += caloriesOfWalk(value);
                } else if (i == 1) {
                    stepsRun += value;
                    caloriesBurntRunning += caloriesOfRun(value);
                } else if (i == 2) {
                    naturldailyCalorieBurn += value;
                }
                i++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DecimalFormat df = new DecimalFormat("#.##");
        double roundedwalking = Double.parseDouble(df.format(caloriesBurntWalking));
        double roundedrunning = Double.parseDouble(df.format(caloriesBurntRunning));
        // Update the TextView with the activity summary
        String activitySummary =
                "\nSteps Walked: " + stepsWalked +
                        "\nCalories Burnt Walking: " + roundedwalking +
                        "\nSteps Run: " + stepsRun +
                        "\nCalories Burnt Running: " + roundedrunning +
                        "\nnatural daily Calorie Burn: " + naturldailyCalorieBurn;


        tvActivitySummary.setText(activitySummary);
        dailyCalorieBurn = naturldailyCalorieBurn + (float)roundedrunning + (float)roundedwalking;
        return dailyCalorieBurn;
    }

    private float caloriesOfWalk(float stepsNum) {
        // Assuming 1 step burns 0.05 calories during walking
        return stepsNum * (float)0.05;
    }

    private float caloriesOfRun(float stepsNum) {
        // Assuming 1 step burns 0.1 calories during running
        return stepsNum*(float)0.1;
    }

    private float updateFoodSummary(String userName) {
        // Read the data from the CSV file for food summary
        float breakfastCalories = 0;
        float lunchCalories = 0;
        float dinnerCalories = 0;
        float extrasCalories = 0;
        float totalCalories = 0;

        String directoryPath = "/sdcard/csv_dir/";
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        String formattedDateTime = dateFormat.format(currentDate);
        String fileName = userName + "_" + chosenDate + ".csv";
        File file = new File(directoryPath, fileName);

        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] line;
            int i = 0;
            while ((line = reader.readNext()) != null) {
                if (i == 3) {
                    breakfastCalories += Float.parseFloat(line[1]);
                } else if (i == 4) {
                    lunchCalories += Float.parseFloat(line[1]);
                } else if (i == 5) {
                    dinnerCalories += Float.parseFloat(line[1]);
                } else if (i == 6) {
                    extrasCalories += Float.parseFloat(line[1]);
                }
                i++;
            }
            totalCalories += extrasCalories + dinnerCalories + lunchCalories + breakfastCalories;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update the TextView with the food summary
        String foodSummary =
                "\nBreakfast Calories: " + breakfastCalories +
                        "\nLunch Calories: " + lunchCalories +
                        "\nDinner Calories: " + dinnerCalories +
                        "\nExtras Calories: " + extrasCalories +
                        "\nTotal calories income:" + totalCalories;

        tvFoodSummary.setText(foodSummary);
        return totalCalories;
    }

    private boolean updateCaloriesBalance(float totalOutcomeCalories, float totalIncomeCalories, String userName) {
        // Calculate the total balance
        float totalBalance = totalIncomeCalories - totalOutcomeCalories;
        String directoryPath = "/sdcard/csv_dir/";
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        String formattedDateTime = dateFormat.format(currentDate);
        String fileName = userName + ".csv";
        File file = new File(directoryPath, fileName);

        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] line;
            boolean isConditionMet = false;
            while ((line = reader.readNext()) != null) {
                if (line.length == 3) {
                    String key = line[0].trim();
                    String value = line[1].trim();
                    String dir = line[2].trim();
                    if (dir.equalsIgnoreCase("higher")) {
                        isConditionMet = totalBalance >= Float.parseFloat(value);
                    } else {
                        isConditionMet = totalBalance <= Float.parseFloat(value);
                    }
                }
            }
            reader.close();


            if (!isConditionMet) {
                // Set text color to red
                tvCaloriesBalance.setTextColor(ContextCompat.getColor(this, R.color.red));
            } else {
                // Set text color to green
                tvCaloriesBalance.setTextColor(ContextCompat.getColor(this, R.color.green));
            }
            // Update the TextView with the calories balance
            String caloriesBalance =
                    "\nTotal Income Calories: " + totalIncomeCalories +
                            "\nTotal Outcome Calories: " + totalOutcomeCalories +
                            "\nTotal Balance: " + totalBalance;


            tvCaloriesBalance.setText(caloriesBalance);
            return totalBalance <= 0;
        } catch (
                FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }
}


