package com.heckteck.quizzapp.UI.Fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.heckteck.quizzapp.Models.Question;
import com.heckteck.quizzapp.QuizFragmentArgs;
import com.heckteck.quizzapp.QuizFragmentDirections;
import com.heckteck.quizzapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends Fragment {

    private TextView quiz_title, questionFeedback, questionText, questionTime, questionNumber;
    private Button optionOneBtn, optionTwoBtn, optionThreeBtn, nextBtn;
    private ImageButton closeBtn;
    private ProgressBar questionProgress;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private String quiz_id;
    private static final String TAG = "QUIZ_FRAGMENT";
    private List<Question> allQuestionsList = new ArrayList<>();
    private List<Question> questionsToAnswerList = new ArrayList<>();
    private long totalQuestionsToAnswer = 0;
    private CountDownTimer countDownTimer;
    private boolean canAnswer = false;
    private int currentQuestion = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;
    private String quizName;
    private NavController navController;

    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else {
            //TODO: Go back to Home Page
        }

        navController = Navigation.findNavController(view);

        quiz_title = view.findViewById(R.id.quiz_title);
        questionFeedback = view.findViewById(R.id.quiz_question_feedback);
        questionText = view.findViewById(R.id.quiz_question);
        questionTime = view.findViewById(R.id.quiz_question_time);
        questionNumber = view.findViewById(R.id.quiz_question_number);
        optionOneBtn = view.findViewById(R.id.quiz_option_one);
        optionTwoBtn = view.findViewById(R.id.quiz_option_two);
        optionThreeBtn = view.findViewById(R.id.quiz_option_three);
        nextBtn = view.findViewById(R.id.quiz_next_btn);
        closeBtn = view.findViewById(R.id.quiz_close_btn);
        questionProgress = view.findViewById(R.id.quiz_question_progress);


        quiz_id = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("QuizList")
                .document(quiz_id)
                .collection("Questions")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allQuestionsList = task.getResult().toObjects(Question.class);
                    pickQuestions();
                    loadUi();
                } else {
                    quiz_title.setText("Error Loading Data");
                }
            }
        });

        optionOneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAnswer(optionOneBtn);
            }
        });

        optionTwoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAnswer(optionTwoBtn);
            }
        });

        optionThreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAnswer(optionThreeBtn);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestion == totalQuestionsToAnswer) {
                    submitResults();
                } else {
                    currentQuestion++;
                    loadQuestion(currentQuestion);
                    resetOptions();
                }
            }
        });

    }

    private void submitResults() {
        HashMap<String, Object> resultsData = new HashMap<>();
        resultsData.put("correct", correctAnswers);
        resultsData.put("wrong", wrongAnswers);
        resultsData.put("unanswered", notAnswered);

        firebaseFirestore.collection("QuizList")
                .document(quiz_id)
                .collection("Results")
                .document(currentUserId)
                .set(resultsData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                            action.setQuizId(quiz_id);
                            navController.navigate(action);
                        }else {
                            quiz_title.setText(task.getException().getMessage());
                        }
                    }
                });
    }

    private void resetOptions() {
        optionOneBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));
        optionTwoBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));
        optionThreeBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));

        optionOneBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionTwoBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionThreeBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void verifyAnswer(Button selectedAnswerButton) {

        if (canAnswer) {
            selectedAnswerButton.setTextColor(getResources().getColor(R.color.colorDark, null));
            if (questionsToAnswerList.get(currentQuestion - 1).getAnswer().equals(selectedAnswerButton.getText().toString())) {
                correctAnswers++;
                selectedAnswerButton.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg, null));
                questionFeedback.setText("Correct Answer");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            } else {
                wrongAnswers++;
                selectedAnswerButton.setBackground(getResources().getDrawable(R.drawable.wrong_answer_btn_bg, null));
                questionFeedback.setText("Wrong Answer \n \n Correct Answer is : " + questionsToAnswerList.get(currentQuestion - 1).getAnswer());
                questionFeedback.setTextColor(getResources().getColor(R.color.colorAccent, null));
            }
            canAnswer = false;
            countDownTimer.cancel();
            showNextBtn();
        }
    }

    private void showNextBtn() {
        if (currentQuestion == totalQuestionsToAnswer) {
            nextBtn.setText("Submit Results");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);
    }

    private void loadUi() {
        quiz_title.setText(quizName);
        questionText.setText("Load first question");
        enableOptions();
        loadQuestion(1);
    }

    private void loadQuestion(int questionNum) {
        questionNumber.setText(questionNum + "");
        questionText.setText(questionsToAnswerList.get(questionNum - 1).getQuestion());
        optionOneBtn.setText(questionsToAnswerList.get(questionNum - 1).getOption_a());
        optionTwoBtn.setText(questionsToAnswerList.get(questionNum - 1).getOption_b());
        optionThreeBtn.setText(questionsToAnswerList.get(questionNum - 1).getOption_c());

        canAnswer = true;
        currentQuestion = questionNum;

        startTimer(questionNum);
    }

    private void startTimer(int questionNum) {
        final long timeToAnswer = questionsToAnswerList.get(questionNum - 1).getTimer();
        questionTime.setText(timeToAnswer + "");

        questionProgress.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeToAnswer * 1000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                questionTime.setText(millisUntilFinished / 1000 + "");

                long percent = millisUntilFinished / (timeToAnswer * 10);
                questionProgress.setProgress((int) percent);
            }

            @Override
            public void onFinish() {
                canAnswer = false;
                questionFeedback.setText("Time Up! No Answer was submitted");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                notAnswered++;
                showNextBtn();
            }
        };
        countDownTimer.start();
    }

    private void enableOptions() {
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        optionOneBtn.setEnabled(true);
        optionTwoBtn.setEnabled(true);
        optionThreeBtn.setEnabled(true);

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void pickQuestions() {
        for (int i = 0; i < totalQuestionsToAnswer; i++) {
            int randomIndex = getRandomInteger(allQuestionsList.size(), 0);
            questionsToAnswerList.add(allQuestionsList.get(randomIndex));
            allQuestionsList.remove(randomIndex);
            Log.d(TAG, "Question " + i + " : " + questionsToAnswerList.get(i).getQuestion());
        }
    }

    public static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

}
