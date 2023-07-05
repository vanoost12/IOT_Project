package com.example.tutorial6;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class connect extends AppCompatActivity {

    private EditText editTextUsername;
    ImageButton buttonConnect;
    ImageButton buttonSignIn;
    private ImageButton menuButton;
    private LinearLayout menuLayout;
    private boolean isMenuVisible = false;
    private int activity = 0;
    private int numOfSteps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Intent input = getIntent();
        String filePath = new String();
        if (input != null) {
            filePath = input.getStringExtra("file path");
            // Use the receivedData as needed
        }
        if(filePath!=null) {
            if (! Python.isStarted()) {
                Python.start(new AndroidPlatform(this));
            }
            Python py = Python.getInstance();
            PyObject pyobj = py.getModule("main_iot_project");
            PyObject obj = pyobj.callAttr("main", filePath);
            String[] output = obj.toString().split(",");
            numOfSteps = Integer.parseInt(output[0]);
            activity = Integer.parseInt(output[1]);
            String activityName;
            if(activity == 1){
                 activityName = "run";
            }
            else {
                activityName = "walk";
            }
            Toast.makeText(this,"recorded "+ numOfSteps+ "steps of" + activityName ,Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"no new activity was recorded",Toast.LENGTH_SHORT).show();
            numOfSteps=0;
            activity = 0;
        }
        editTextUsername = findViewById(R.id.editTextUsername);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonSignIn = findViewById(R.id.buttonsignin);


        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                if(username.isEmpty()){
                    Toast.makeText(connect.this,"enter user name!",Toast.LENGTH_SHORT).show();
                }
                else {
                    String csvDirectory = "/sdcard/csv_dir/";

                    File csvFile = new File(csvDirectory, username + ".csv");
                    if (csvFile.exists()) {
                        Intent intent = new Intent(connect.this, daily_anlysis.class);
                        createDailyCSV(username);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(connect.this,"user name wasn't found",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(connect.this, details_insert.class);
                intent.putExtra("new steps", numOfSteps);
                startActivity(intent);
            }
        });
    }
    //cabbage - 750
    //beef - 450
    //rice - 550
    //pepper - 150
    //garlic - 20
    public void createDailyCSV(String username) {
        String CSV_DIRECTORY = "/sdcard/csv_dir/";
        // Get the current date
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(currentDate);
        // Create the CSV file name by concatenating username and date
        String csvFileName = username + "_" + dateString + ".csv";
        File csvFile = new File(CSV_DIRECTORY, csvFileName);

        if (csvFile.exists()) {
            // If the file already exists, update the steps and retrieve the updated data
            // Update the CSV file with the updated step counts
            updateSteps(csvFile, numOfSteps, activity,username);
        } else {
            try {
                // Create a new CSV file
                csvFile.createNewFile();

                // Calculate natural calorie burn
                float naturalCalorieBurn = calculateNaturalCalorieBurn(username);

                // Initialize meal calorie values
                int breakfastCalories = 0;
                int lunchCalories = 0;
                int dinnerCalories = 0;
                int extrasCalories = 0;
                
                // Create the CSV data string
                StringBuilder csvData = new StringBuilder();
                csvData.append("walked,").append(predictNumOfStepsWalked()).append("\n");
                csvData.append("ran,").append(numOfSteps*activity).append("\n");
                csvData.append("natural,").append(naturalCalorieBurn).append("\n");
                csvData.append("Breakfast,").append(breakfastCalories).append("\n");
                csvData.append("Lunch,").append(lunchCalories).append("\n");
                csvData.append("Dinner,").append(dinnerCalories).append("\n");
                csvData.append("Extras,").append(extrasCalories).append("\n");
                csvData.append("calorieBalance,").append(-1*naturalCalorieBurn);

                // Write the data to the CSV file
                FileWriter writer = new FileWriter(csvFile);
                writer.write(csvData.toString());
                writer.flush();
                writer.close();

                System.out.println("Daily CSV file created successfully: " + csvFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSteps(File csvFile, int newSteps, int activity,String username) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            StringBuilder csvData = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (values.length == 2) {
                    String key = values[0].trim();
                    String value = values[1].trim();

                    if (key.equalsIgnoreCase("walked") && activity == 0) {
                        int currentStepsWalked = Integer.parseInt(value);
                        int updatedStepsWalked = currentStepsWalked + newSteps;
                        csvData.append("walked,").append(updatedStepsWalked).append("\n");
                    } else if (key.equalsIgnoreCase("ran") && activity==1) {
                        int currentStepsRun = Integer.parseInt(value);
                        int updatedStepsRun = currentStepsRun + newSteps;
                        csvData.append("ran,").append(updatedStepsRun).append("\n");
                    } else if (key.equalsIgnoreCase("natural")) {
                        float updatedBMR = calculateNaturalCalorieBurn(username);
                        csvData.append("natural,").append(updatedBMR).append("\n");
                    }else{
                            csvData.append(line).append("\n");
                        }
                    }
                else {
                    csvData.append(line).append("\n");
                }
            }

            reader.close();

            // Write the updated CSV data back to the file
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvData.toString());
            writer.flush();
            writer.close();

            System.out.println("Steps updated successfully in the CSV file.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to update steps in the CSV file.");
        }
    }

    private int predictNumOfStepsWalked() {
        //receives records of walk and predict the number of steps in python
        return 10;
    }

    private int predictNumOfStepsRan() {
        //receives records of walk and predict the number of steps in python
        return 10;
    }

    public int caloriesOfWalk(int stepsNum) {
        // Assuming 1 step burns 0.05 calories during walking
        int caloriesBurned = (int) (stepsNum * 0.05);
        return caloriesBurned;
    }

    public int caloriesOfRun(int stepsNum) {
        // Assuming 1 step burns 0.1 calories during running
        int caloriesBurned = (int) (stepsNum * 0.1);
        return caloriesBurned;
    }
    private float calculateNaturalCalorieBurn(String userName) {
        //calculate the natural burn of calorie by the data in the file username + ".csv" in java
        String directoryPath = "/sdcard/csv_dir/";
        String fileName = userName + ".csv";
        File file = new File(directoryPath, fileName);
        float weight=0,height=0;
        StringBuilder sex = new StringBuilder();
        int age=0;

        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] line;
            while ((line = reader.readNext()) != null) {
//                String[] values = line.split(",");
                String field = line[0].trim();
                String data = line[1].trim();
                switch (field) {
                    case "Weight":
                        // Process weight data
                        weight = Float.parseFloat(data);
                        break;
                    case "Height":
                        // Process height data
                        height = Float.parseFloat(data);
                        break;
                    case "Sex":
                        // Process sex data
                        sex.append(data);
                        break;
                    case "Age":
                        // Process age data
                        age = Integer.parseInt(data);
                        break;
                    default:
                        break;
                }

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        double natural=20;
        switch(sex.toString()) {
            case "Male":
                natural = calculateBmrMale(age,height,weight);
                break;
            case "Female":
                natural = calculateBmrFemale(age,height,weight);
                break;
            default:
                break;
        }
        return (float)natural;
    }
    private static double calculateBmrMale(int age, double height, double weight) {
        double bmr = 66 + (13.75 * weight) + (5 * height) - (6.75 * age);
        return bmr;
    }
    private static double calculateBmrFemale(int age, double height, double weight) {
        double bmr = 655 + (9.56 * weight) + (1.85 * height) - (4.68 * age);
        return bmr;
    }
}
