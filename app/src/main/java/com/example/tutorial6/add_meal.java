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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class add_meal extends AppCompatActivity {

    private EditText editTextIngredient;
    private EditText editTextWeight;
    private TableLayout tableLayoutIngredients;
    private RadioGroup radioGroupMealType;
    private ArrayList<Ingredient> ingredientList;
    private ImageButton menuButton;
    private LinearLayout menuLayout;
    private boolean isMenuVisible = false;
    String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("username");
            // Use the receivedData as needed
        }
        editTextIngredient = findViewById(R.id.editTextIngredient);
        editTextWeight = findViewById(R.id.editTextWeight);
        tableLayoutIngredients = findViewById(R.id.tableLayoutIngredients);
        radioGroupMealType = findViewById(R.id.radioGroupMealType);
        ingredientList = new ArrayList<>();

        ImageButton buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editTextWeight.getText().toString();
                try {
                    // Input is a valid integer
                    int value = Integer.parseInt(input);
                    if(foodCalories.containsKey(editTextIngredient.getText().toString())) {//input is valid ingredient
                        addIngredientToTable();
                    }
                    else{
                        Toast.makeText(add_meal.this,"unfamiliar ingredient",Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    // Input is not a valid integer
                    Toast.makeText(add_meal.this,"weight should be a number",Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToList();
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
                Intent intent = new Intent(add_meal.this, connect.class);
                startActivity(intent);
            }
        });

        ImageButton buttonAddMeal = findViewById(R.id.buttonAddMeal);
        buttonAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddMealActivity
                Intent intent = new Intent(add_meal.this, add_meal.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonDailyAnalysis = findViewById(R.id.buttonDailyAnalysis);
        buttonDailyAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DailyAnalysisActivity
                Intent intent = new Intent(add_meal.this, daily_anlysis.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        ImageButton buttonInsertDetails = findViewById(R.id.buttonInsertDetails);
        buttonInsertDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to InsertDetailsActivity
                Intent intent = new Intent(add_meal.this, details_insert.class);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });
    }

    private void addIngredientToTable() {
        String ingredient = editTextIngredient.getText().toString();
        String weight = editTextWeight.getText().toString();

        if (!ingredient.isEmpty() && !weight.isEmpty()) {
            TableRow row = new TableRow(this);

            TextView textViewIngredient = new TextView(this);
            textViewIngredient.setText(ingredient);
            row.addView(textViewIngredient);

            TextView textViewWeight = new TextView(this);
            textViewWeight.setText(weight);
            row.addView(textViewWeight);

            tableLayoutIngredients.addView(row);

            editTextIngredient.setText("");
            editTextWeight.setText("");
        }
    }

    private void saveDataToList() {
        int rowCount = tableLayoutIngredients.getChildCount();

        for (int i = 1; i < rowCount; i++) {
            TableRow row = (TableRow) tableLayoutIngredients.getChildAt(i);

            TextView textViewIngredient = (TextView) row.getChildAt(0);
            TextView textViewWeight = (TextView) row.getChildAt(1);

            String ingredient = textViewIngredient.getText().toString();
            int weight = Integer.parseInt(textViewWeight.getText().toString());

            Ingredient newIngredient = new Ingredient(ingredient, weight);
            ingredientList.add(newIngredient);
        }

        int selectedRadioButtonId = radioGroupMealType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String mealType = selectedRadioButton.getText().toString();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(currentDate);
        // Retrieve calorie balance from CSV
        float calorieBalance = getCalorieBalanceFromCSV(userName);

        // Call Ben's function and save the output as newCaloriesBalance
        float mealCaloriesBalance = calcMeal(ingredientList);
        updateDailyCalories(userName + "_" +dateString+".csv",mealType,mealCaloriesBalance);
        float newCaloriesBalance = calorieBalance + mealCaloriesBalance;
        updateDailyCalories(userName + "_" +dateString+".csv","calorieBalance",newCaloriesBalance);


        // Display the newCaloriesBalance or perform further operations with it
        Toast.makeText(this, "New Calorie Balance: " + newCaloriesBalance, Toast.LENGTH_SHORT).show();
        tableLayoutIngredients.removeAllViews();
        TableRow row = new TableRow(this);

        TextView textViewIngredient = new TextView(this);
        textViewIngredient.setText("Ingredient");
        row.addView(textViewIngredient);

        TextView textViewWeight = new TextView(this);
        textViewWeight.setText("Weight");
        row.addView(textViewWeight);

        tableLayoutIngredients.addView(row);
    }

    private float getCalorieBalanceFromCSV(String userName) {
        float calorieBalance = 0.0f;
        String directoryPath = "/sdcard/csv_dir/";
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String fileName = userName + "_" + currentDate + ".csv";
        File file = new File(directoryPath, fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the CSV line to retrieve the calorie balance value
                String[] values = line.split(",");
                String key = values[0].trim();
                String value = values[1].trim();

                if (key.equalsIgnoreCase("calorieBalance")) {
                    calorieBalance = Float.parseFloat(value);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return calorieBalance;
    }

    public void updateDailyCalories(String dailyCsvFile, String field, float calorieValue) {
        String csvDirectory = "/sdcard/csv_dir/";
        File csvFile = new File(csvDirectory, dailyCsvFile);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            StringBuilder csvData = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (values.length == 2) {
                    String key = values[0].trim();
                    String value = values[1].trim();

                    if (key.equalsIgnoreCase(field)) {
                        csvData.append(key).append(",").append(calorieValue).append("\n");
                    } else {
                        csvData.append(line).append("\n");
                    }
                } else {
                    csvData.append(line).append("\n");
                }
            }

            reader.close();
            // Write the updated CSV data back to the file
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvData.toString());
            writer.flush();
            writer.close();

            System.out.println("Meal calories updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to update meal calories.");
        }
    }
    private static final Map<String, Float> foodCalories = new HashMap<>();

    static {
        foodCalories.put("tomatoes", (float) 0.2);
        foodCalories.put("apples", (float)0.5);
        foodCalories.put("chicken", (float)1.5);
        foodCalories.put("bananas", (float)0.9);
        foodCalories.put("rice", (float)1.2);
        foodCalories.put("salmon", (float)2.8);
        foodCalories.put("broccoli", (float)0.3);
        foodCalories.put("potatoes", (float)0.8);
        foodCalories.put("carrots", (float)0.4);
        foodCalories.put("oats", (float)3.7);
        foodCalories.put("eggs", (float)1.6);
        foodCalories.put("spinach", (float)0.2);
        foodCalories.put("cheese", (float)4.0);
        foodCalories.put("bread", (float)2.5);
        foodCalories.put("yogurt", (float)1.0);
        foodCalories.put("beef", (float)2.9);
        foodCalories.put("pasta", (float)3.2);
        foodCalories.put("peanuts", (float)5.8);
        foodCalories.put("avocado", (float)1.9);
        foodCalories.put("milk", (float)0.6);
        foodCalories.put("lettuce", (float)0.1);
        foodCalories.put("grapes", (float)0.7);
        foodCalories.put("corn", (float)1.2);
        foodCalories.put("honey", (float)3.2);
        foodCalories.put("sardines", (float)1.9);
        foodCalories.put("almonds", (float)5.9);
        foodCalories.put("blueberries", (float)0.6);
        foodCalories.put("lentils", (float)3.9);
        foodCalories.put("kiwi", (float)0.6);
        foodCalories.put("cabbage", (float)0.27);
        foodCalories.put("Cauliflower", (float)0.27);
        foodCalories.put("garlic", (float)0.3);
        foodCalories.put("chocolate", (float)5.5);
        foodCalories.put("sugar", (float)4.0);
    }

    private float calcMeal(List<Ingredient> ingredientList) {
       //input : list of ingridients - Ingridient object is attached below
        float mealCalorieBalance = 0 ;
        for(Ingredient ingredient:ingredientList) {
            String ingredientName = ingredient.getName();
            int ingredientWeight = ingredient.getWeight();

            // Lookup the calorie value for the ingredient using the foodCalories map
            if (foodCalories.containsKey(ingredientName)) {
                double caloriePerGram = foodCalories.get(ingredientName);
                float calories = (float) (caloriePerGram * ingredientWeight);
                mealCalorieBalance += calories;
            } else {
                Toast.makeText(this, "Warning: Unknown ingredient '" + ingredientName + "'", Toast.LENGTH_SHORT).show();

            }
        }
        return mealCalorieBalance;
    }

    private static class Ingredient {
        private String name;
        private int weight;
        private String calories;

        Ingredient(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        String getName() {
            return name;
        }

        int getWeight() {
            return weight;
        }
    }
}

