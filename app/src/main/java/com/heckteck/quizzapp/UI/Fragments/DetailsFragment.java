package com.heckteck.quizzapp.UI.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.heckteck.quizzapp.DetailsFragmentArgs;
import com.heckteck.quizzapp.DetailsFragmentDirections;
import com.heckteck.quizzapp.Quiz;
import com.heckteck.quizzapp.QuizViewModel;
import com.heckteck.quizzapp.R;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private ImageView details_image;
    private TextView details_title, details_desc, details_level, details_questions, details_score;
    private Button quizStartBtn;
    private int position;
    private QuizViewModel quizViewModel;
    private NavController navController;
    private String quiz_id;
    private long totalQuestions = 0;
    private String quiz_name;

    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
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

        firebaseFirestore = FirebaseFirestore.getInstance();

        details_image = view.findViewById(R.id.details_image);
        details_title = view.findViewById(R.id.details_title);
        details_desc = view.findViewById(R.id.details_desc);
        details_level = view.findViewById(R.id.details_difficulty_text);
        details_questions = view.findViewById(R.id.details_questions_text);
        quizStartBtn = view.findViewById(R.id.details_start_btn);
        details_score = view.findViewById(R.id.details_score_text);

        navController = Navigation.findNavController(view);

        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        quizStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
                action.setQuizName(quiz_name);
                action.setTotalQuestions(totalQuestions);
                action.setQuizId(quiz_id);
                navController.navigate(action);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizViewModel = new ViewModelProvider(getActivity()).get(QuizViewModel.class);
        quizViewModel.getQuizListData().observe(getViewLifecycleOwner(), new Observer<List<Quiz>>() {
            @Override
            public void onChanged(List<Quiz> quizList) {
                Quiz quiz = quizList.get(position);

                quiz_id = quiz.getQuiz_id();
                totalQuestions = quiz.getQuestions();
                quiz_name = quiz.getName();

                Glide.with(getContext())
                        .load(quiz.getImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(details_image);

                details_title.setText(quiz.getName());
                details_desc.setText(quiz.getDesc());
                details_level.setText(quiz.getLevel());
                details_questions.setText(quiz.getQuestions() + "");

                loadResults();
            }
        });
    }

    private void loadResults() {
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

                        if (correct != null && wrong != null && missed != null){
                            long total = correct + wrong + missed;
                            long percent = (correct * 100) / total;

                            details_score.setText(percent + "%");
                        }else {
                            details_score.setText("NA");
                        }

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
