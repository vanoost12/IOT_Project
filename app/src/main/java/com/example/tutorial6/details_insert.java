package com.example.tutorial6;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class details_insert extends AppCompatActivity {

    private EditText editTextWeight;
    private EditText editTextHeight;
    private EditText editTextAge;
    private EditText editTextUsername;

    private EditText editTextGoal;
    private ImageButton buttonSave;
    private ImageButton menuButton;
    private LinearLayout menuLayout;
    private boolean isMenuVisible = false;
    String userName;
    private int numOfSteps;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_insert);

        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("username");
            // Use the receivedData as needed
        }

        editTextWeight = findViewById(R.id.editTextWeight);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextAge = findViewById(R.id.editTextAge);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextGoal = findViewById(R.id.editTextGoal);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
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
                Intent intent = new Intent(details_insert.this, connect.class);
                startActivity(intent);
            }
        });

        ImageButton buttonAddMeal = findViewById(R.id.buttonAddMeal);
        buttonAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddMealActivity
                Intent intent = new Intent(details_insert.this, add_meal.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonDailyAnalysis = findViewById(R.id.buttonDailyAnalysis);
        buttonDailyAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DailyAnalysisActivity
                Intent intent = new Intent(details_insert.this, daily_anlysis.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonInsertDetails = findViewById(R.id.buttonInsertDetails);
        buttonInsertDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to InsertDetailsActivity
                Intent intent = new Intent(details_insert.this, details_insert.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

    }

    private String getSelectedSex() {
        RadioButton radioButtonMale = findViewById(R.id.radioButtonMale);
        RadioButton radioButtonFemale = findViewById(R.id.radioButtonFemale);

        if (radioButtonMale.isChecked()) {
            return "Male";
        } else if (radioButtonFemale.isChecked()) {
            return "Female";
        } else {
            return null;
        }
    }
    private void saveData() {
        String weight = editTextWeight.getText().toString().trim();
        String height = editTextHeight.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String sex = getSelectedSex();
        String username = editTextUsername.getText().toString().trim();
        String goal = editTextGoal.getText().toString().trim();
        String direction = getSelectedDirection();
        int check;
        try {
            check = Integer.parseInt(weight);
            check = Integer.parseInt(height);
            check = Integer.parseInt(age);
            check = Integer.parseInt(goal);
            if (!weight.isEmpty() && !height.isEmpty() && !age.isEmpty() && !sex.isEmpty() && !username.isEmpty() && !goal.isEmpty() && !direction.isEmpty()) {
                StringBuilder csvData = new StringBuilder();
                csvData.append("Weight").append(",").append(weight).append("\n");
                csvData.append("Height").append(",").append(height).append("\n");
                csvData.append("Age").append(",").append(age).append("\n");
                csvData.append("Sex").append(",").append(sex).append("\n");
                csvData.append("Username").append(",").append(username).append("\n");
                csvData.append("Goal").append(",").append(goal).append(",").append(direction).append("\n");


                try {
                    File csvDir = new File(Environment.getExternalStorageDirectory() + "/csv_dir/");
                    if (!csvDir.exists()) {
                        csvDir.mkdirs();
                    }
                    File csvFile = new File(csvDir, username + ".csv");
                    FileWriter writer = new FileWriter(csvFile, false);
                    writer.write(csvData.toString());
                    writer.flush();
                    writer.close();
                    Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(details_insert.this, connect.class);
            startActivity(intent);
        } catch (NumberFormatException e) {
        Toast.makeText(this,"details are not valid",Toast.LENGTH_SHORT).show();
    }
    }


    private String getSelectedDirection() {
        RadioButton radioButtonLower = findViewById(R.id.radioButtonLower);
        RadioButton radioButtonHigher = findViewById(R.id.radioButtonHigher);

        if (radioButtonLower.isChecked()) {
            return "Lower";
        } else if (radioButtonHigher.isChecked()) {
            return "Higher";
        } else {
            return null;
        }
    }
}
