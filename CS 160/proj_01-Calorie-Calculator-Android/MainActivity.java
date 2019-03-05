package andrewhuynh.caloriecalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText input1;
    private EditText input2;

    private Spinner exercise1;
    private Spinner exercise2;
    private Spinner exercise3;

    private TextView result1;
    private TextView result2;
    private TextView result3;

    private boolean first = false;

    private double calories;

    private String curr_exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input1 = (EditText) findViewById(R.id.input1);
        input2 = (EditText) findViewById(R.id.input2);

        exercise1 = (Spinner) findViewById(R.id.exercise1);
        exercise2 = (Spinner) findViewById(R.id.exercise2);
        exercise3 = (Spinner) findViewById(R.id.exercise3);

        Button calculate1 = (Button) findViewById(R.id.calculate1);
        Button calculate2 = (Button) findViewById(R.id.calculate2);
        Button calculate3 = (Button) findViewById(R.id.calculate3);

        result1 = (TextView) findViewById(R.id.result1);
        result2 = (TextView) findViewById(R.id.result2);
        result3 = (TextView) findViewById(R.id.result3);

        calculate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCalculations1();
            }
        });

        calculate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCalculations2();
            }
        });

        calculate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCalculations3();
            }
        });
    }

    private void makeCalculations1() {
        String choice = exercise1.getSelectedItem().toString();

        if (choice.equals("")) {
            result1.setText("Please select an exercise fisrt.");
            return;
        }

        String temp = input1.getText().toString();

        if (temp.equals("")) {
            result1.setText("Please input reps/minutes next.");
            return;
        }

        Double amount = Double.valueOf(temp);

        if (amount < 0) {
            result1.setText("Please input a positive value.");
            return;
        }

        if (choice.equals("Pushups")) {
            calories = amount / 350 * 100;
        } else if (choice.equals("Situps")) {
            calories = amount / 200 * 100;
        } else if (choice.equals("Squats")) {
            calories = amount / 225 * 100;
        } else if (choice.equals("Leg-Lifts")) {
            calories = amount / 25 * 100;
        } else if (choice.equals("Planking")) {
            calories = amount / 25 * 100;
        } else if (choice.equals("Jumping Jacks")) {
            calories = amount / 10 * 100;
        } else if (choice.equals("Pullups")) {
            calories = amount / 100 * 100;
        } else if (choice.equals("Cycling")) {
            calories = amount / 12 * 100;
        } else if (choice.equals("Walking")) {
            calories = amount / 20 * 100;
        } else if (choice.equals("Jogging")) {
            calories = amount / 12 * 100;
        } else if (choice.equals("Swimming")) {
            calories = amount / 13 * 100;
        } else if (choice.equals("Stair-Climbing")) {
            calories = amount / 15 * 100;
        }

        calories = Math.round(calories * 10d) / 10d;

        result1.setText("You burned " + calories + " calories.");

        first = true;

        curr_exercise = choice;
    }


    private void makeCalculations2() {
        if (!first) {
            result2.setText("Please do the previous function first.");
            return;
        }

        String choice = exercise2.getSelectedItem().toString();

        if (choice.equals("")) {
            result2.setText("Please select an exercise fisrt.");
            return;
        }

        if (choice.equals(curr_exercise)) {
            result2.setText("Please select a different exercise than the first one.");
            return;
        }

        double amount = 0;

        boolean isReps = false;

        if (choice.equals("Pushups")) {
            amount = calories / 100 * 350;
            isReps = true;
        } else if (choice.equals("Situps")) {
            amount = calories / 100 * 200;
            isReps = true;
        } else if (choice.equals("Squats")) {
            amount = calories / 100 * 225;
            isReps = true;
        } else if (choice.equals("Leg-Lifts")) {
            amount = calories / 100 * 25;
        } else if (choice.equals("Planking")) {
            amount = calories / 100 * 25;
        } else if (choice.equals("Jumping Jacks")) {
            amount = calories / 100 * 10;
        } else if (choice.equals("Pullups")) {
            amount = calories / 100 * 100;
            isReps = true;
        } else if (choice.equals("Cycling")) {
            amount = calories / 100 * 12;
        } else if (choice.equals("Walking")) {
            amount = calories / 100 * 20;
        } else if (choice.equals("Jogging")) {
            amount = calories / 100 * 12;
        } else if (choice.equals("Swimming")) {
            amount = calories / 100 * 13;
        } else if (choice.equals("Stair-Climbing")) {
            amount = calories / 100 * 15;
        }

        amount = Math.round(amount * 10d) / 10d;

        if (isReps) {
            result2.setText("for " + amount + " " + "reps.");
        } else {
            result2.setText("for " + amount + " " + "minutes.");
        }
    }

    private void makeCalculations3() {
        String temp = input2.getText().toString();

        if (temp.equals("")) {
            result3.setText("Please input calories first.");
            return;
        }

        String choice = exercise3.getSelectedItem().toString();

        if (choice.equals("")) {
            result3.setText("Please select an exercise next.");
            return;
        }

        double inputCalories = Double.valueOf(temp);

        if (inputCalories < 0) {
            result3.setText("Please input a positive value.");
            return;
        }

        double amount = 0;

        boolean isReps = false;

        if (choice.equals("Pushups")) {
            amount = inputCalories / 100 * 350;
            isReps = true;
        } else if (choice.equals("Situps")) {
            amount = inputCalories / 100 * 200;
            isReps = true;
        } else if (choice.equals("Squats")) {
            amount = inputCalories / 100 * 225;
            isReps = true;
        } else if (choice.equals("Leg-Lifts")) {
            amount = inputCalories / 100 * 25;
        } else if (choice.equals("Planking")) {
            amount = inputCalories / 100 * 25;
        } else if (choice.equals("Jumping Jacks")) {
            amount = inputCalories / 100 * 10;
        } else if (choice.equals("Pullups")) {
            amount = inputCalories / 100 * 100;
            isReps = true;
        } else if (choice.equals("Cycling")) {
            amount = inputCalories / 100 * 12;
        } else if (choice.equals("Walking")) {
            amount = inputCalories / 100 * 20;
        } else if (choice.equals("Jogging")) {
            amount = inputCalories / 100 * 12;
        } else if (choice.equals("Swimming")) {
            amount = inputCalories / 100 * 13;
        } else if (choice.equals("Stair-Climbing")) {
            amount = inputCalories / 100 * 15;
        }

        amount = Math.round(amount * 10d) / 10d;

        if (isReps) {
            result3.setText("for " + amount + " " + "reps.");
        } else {
            result3.setText("for " + amount + " " + "minutes.");
        }
    }

}
