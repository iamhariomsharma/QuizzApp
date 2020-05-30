package com.heckteck.quizzapp.UI.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.heckteck.quizzapp.R;
import com.heckteck.quizzapp.ResultFragmentArgs;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends Fragment {

    private TextView resultCorrect, resultWrong, resultMissed, resultPercent;
    private ProgressBar resultProgress;
    private Button resultHomeBtn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NavController navController;

    private String quiz_id;
    private String currentUserId;

    public ResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
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
        firebaseFirestore = FirebaseFirestore.getInstance();

        resultCorrect = view.findViewById(R.id.results_correct_text);
        resultWrong = view.findViewById(R.id.results_wrong_text);
        resultMissed = view.findViewById(R.id.results_missed_text);

        resultPercent = view.findViewById(R.id.results_percent);
        resultHomeBtn = view.findViewById(R.id.results_home_btn);
        resultProgress = view.findViewById(R.id.results_progress);

        quiz_id = ResultFragmentArgs.fromBundle(getArguments()).getQuizId();

        resultHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_resultFragment_to_listFragment);
            }
        });

        firebaseFirestore.collection("QuizList")
                .document(quiz_id)
                .collection("Results")
                .document(currentUserId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();
                    if (result != null){
                        Long correct = result.getLong("correct");
                        Long wrong = result.getLong("wrong");
                        Long missed = result.getLong("unanswered");

                        resultCorrect.setText(correct.toString());
                        resultWrong.setText(wrong.toString());
                        resultMissed.setText(missed.toString());

                        long total = correct + wrong + missed;
                        long percent = (correct * 100) / total;

                        resultPercent.setText(percent + "");
                        resultProgress.setProgress((int) percent);

                    }else {
                        Log.d("RESULT_FRAGMENT", task.getException().getMessage());
                    }

                }else {
                    Log.d("RESULT_FRAGMENT", task.getException().getMessage());
                }
            }
        });
    }
}
