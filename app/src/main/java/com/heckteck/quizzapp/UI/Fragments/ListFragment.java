package com.heckteck.quizzapp.UI.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.heckteck.quizzapp.Adapters.QuizAdapter;
import com.heckteck.quizzapp.ListFragmentDirections;
import com.heckteck.quizzapp.Quiz;
import com.heckteck.quizzapp.QuizViewModel;
import com.heckteck.quizzapp.R;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements QuizAdapter.OnItemClickedListener {

    private RecyclerView list_view;
    private QuizViewModel quizViewModel;
    private QuizAdapter quizAdapter;
    private ProgressBar list_progress;
    private Animation fade_in, fade_out;
    private NavController navController;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list_view = view.findViewById(R.id.list_view);
        list_progress = view.findViewById(R.id.list_progress);
        quizAdapter = new QuizAdapter(this);

        navController = Navigation.findNavController(view);

        list_view.setLayoutManager(new LinearLayoutManager(getContext()));
        list_view.setHasFixedSize(true);
        list_view.setAdapter(quizAdapter);

        fade_in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizViewModel = new ViewModelProvider(getActivity()).get(QuizViewModel.class);
        quizViewModel.getQuizListData().observe(getViewLifecycleOwner(), new Observer<List<Quiz>>() {
            @Override
            public void onChanged(List<Quiz> quizList) {
                list_view.setAnimation(fade_in);
                list_progress.setAnimation(fade_out);
                quizAdapter.setQuizList(quizList);
                quizAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        ListFragmentDirections.ActionListFragmentToDetailsFragment action = ListFragmentDirections.actionListFragmentToDetailsFragment();
        action.setPosition(position);
        navController.navigate(action);
    }
}
