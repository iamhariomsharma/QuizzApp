package com.heckteck.quizzapp.ViewModels;

import com.heckteck.quizzapp.Models.Quiz;
import com.heckteck.quizzapp.Repository.FirebaseRepository;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QuizViewModel extends ViewModel implements FirebaseRepository.OnFirestoreTaskComplete {

    private FirebaseRepository firebaseRepository = new FirebaseRepository(this);
    private MutableLiveData<List<Quiz>> quizListData = new MutableLiveData<>();


    public LiveData<List<Quiz>> getQuizListData() {
        return quizListData;
    }

    public QuizViewModel() {
        firebaseRepository.getQuizData();
    }

    @Override
    public void quizListDataAdded(List<Quiz> quizList) {
        quizListData.setValue(quizList);
    }

    @Override
    public void onError(Exception e) {

    }
}
