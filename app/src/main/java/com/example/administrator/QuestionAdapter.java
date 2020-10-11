package com.example.administrator;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private List<QuestionModel> ques_list;

    public QuestionAdapter(List<QuestionModel> ques_list) {
        this.ques_list = ques_list;
    }

    @NonNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionAdapter.ViewHolder holder, int position) {

        holder.setData(position,this);
    }

    @Override
    public int getItemCount() {
        return ques_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private ImageView deleteB;
        private Dialog loadingDialog;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.setName);
            deleteB = itemView.findViewById(R.id.setDelB);


            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setData(int position, QuestionAdapter questionAdapter) {
            title.setText("QUESTION" + String.valueOf(position +1));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(),QuestionDetailsActivity.class);
                    intent.putExtra("ACTION",position);
                    intent.putExtra("Q_ID",position);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
