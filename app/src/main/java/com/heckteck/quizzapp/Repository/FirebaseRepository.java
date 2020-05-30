package com.heckteck.quizzapp.Repository;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.heckteck.quizzapp.Models.Quiz;

import java.util.List;

import androidx.annotation.NonNull;

public class FirebaseRepository {

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private Query quizReference = firebaseFirestore.collection("QuizList").whereEqualTo("visibility", "public");
    private OnFirestoreTaskComplete onFirestoreTaskComplete;

    public FirebaseRepository(OnFirestoreTaskComplete onFirestoreTaskComplete) {
        this.onFirestoreTaskComplete = onFirestoreTaskComplete;
    }

    public void getQuizData() {
        quizReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    onFirestoreTaskComplete.quizListDataAdded(task.getResult().toObjects(Quiz.class));
                } else {
                    onFirestoreTaskComplete.onError(task.getException());
                }
            }
        });
    }

    public interface OnFirestoreTaskComplete {
        void quizListDataAdded(List<Quiz> quizList);
        void onError(Exception e);
    }

}
