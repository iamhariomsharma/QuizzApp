package com.heckteck.quizzapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.heckteck.quizzapp.Quiz;
import com.heckteck.quizzapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizHolder> {


    private List<Quiz> quizList;

    private OnItemClickedListener onItemClickedListener;

    public QuizAdapter(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setQuizList(List<Quiz> quizList) {
        this.quizList = quizList;
    }

    @NonNull
    @Override
    public QuizHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        return new QuizHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.list_title.setText(quiz.getName());

        Glide.with(holder.itemView.getContext())
                .load(quiz.getImage())
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.list_image);

        String listDescription = quiz.getDesc();
        if (listDescription.length() > 150) {
            listDescription = listDescription.substring(0, 150);
        }

        holder.list_desc.setText(listDescription + "...");
        holder.list_difficulty.setText(quiz.getLevel());
    }

    @Override
    public int getItemCount() {
        if (quizList == null) {
            return 0;
        } else {
            return quizList.size();
        }
    }

    public class QuizHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView list_image;
        private TextView list_title, list_desc, list_difficulty;
        private Button list_btn;

        QuizHolder(@NonNull View itemView) {
            super(itemView);

            list_image = itemView.findViewById(R.id.list_image);
            list_title = itemView.findViewById(R.id.list_title);
            list_desc = itemView.findViewById(R.id.list_desc);
            list_difficulty = itemView.findViewById(R.id.list_difficulty);
            list_btn = itemView.findViewById(R.id.list_btn);

            list_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickedListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickedListener{
        void onItemClick(int position);
    }
}
